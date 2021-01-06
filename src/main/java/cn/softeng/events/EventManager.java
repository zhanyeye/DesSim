package cn.softeng.events;

import cn.softeng.basicsim.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The EventManager is responsible for scheduling future events, controlling
 * conditional event evaluation, and advancing the simulation time. Events are
 * scheduled in based on:
 * <ul>
 * <li>1 - The execution time scheduled for the event
 * <li>2 - The priority of the event (if scheduled to occur at the same time)
 * <li>3 - If both 1) and 2) are equal, the user specified FIFO or LIFO order
 * </ul>
 * <p>
 * The event time is scheduled using a backing long value. Double valued time is
 * taken in by the scheduleWait function and scaled to the nearest long value
 * using the simTimeFactor.
 * <p>
 * Most EventManager functionality is available through static methods that rely
 * on being in a running model context which will access the eventmanager that is
 * currently running, think of it like a thread-local variable for all model threads.
 */
@Slf4j
public final class EventManager {

    public final String name;

    /**
     * 全局同步锁
     */
    private final Object lockObject;

    /**
     * 事件优先队列：一个红黑树+链表数据结构的实现
     */
    private final EventTree eventTree;

    /**
     * 用于控制调度器是否执行的 flag
     */
    private volatile boolean executeEvents;

    /**
     * 调度器当前是否运行
     */
    private final AtomicBoolean isRunning;

    /**
     * 确保EventManager同一时间只有一个processTarget被执行
     */
    private boolean processRunning;

    /**
     * 停止调度 flag, 有些操作前需要暂停调度
     */
    private boolean disableSchedule;

    /**
     * 条件事件列表，包含内容如: 用户暂停事件(条件事件包含 PauseModelTarget)
     * 请注意：条件放在一个数组中，而不是事件队列中
     */
    private final ArrayList<ConditionalEvent> condEvents;

    /**
     * 仿真时钟的当前刻度
     */
    @Getter
    private final AtomicLong currentTick;

    /**
     * 执行下一个事件的时间点 （时间刻度tick）
     */
    private long nextTick;

    /**
     * 仿真运行的的截止时间点
     */
    private long targetTick;

    /**
     * 若为true,执行事件队列中最近一个事件后停止
     */
    private boolean oneEvent;

    /**
     * 若为true,执行事件列表中下一时刻所有事件后停止
     */
    private boolean oneSimTime;

    /**
     * 每秒中所模拟的离散刻度数
     */
    private double ticksPerSecond;

    /**
     * 每一个刻度所代表的时间长度（以秒为单位）
     */
    private double secsPerTick;

    // **************************
    // Real time execution state
    // **************************

    /**
     * the simulation tick corresponding to the wall-clock millis value
     * 于真实时钟毫秒值对应的仿真刻度
     */
    private long realTimeTick;

    /**
     * the wall-clock time in millis
     * 真实时钟的毫秒值
     */
    private long realTimeMillis;

    /**
     * TRUE if the simulation is to be executed in Real Time mode
     */
    private volatile boolean executeRealTime;
    /**
     * TRUE if the time keeping for Real Time model needs re-basing
     */
    private volatile boolean rebaseRealTime;
    /**
     * target ratio of elapsed simulation time to elapsed wall clock time
     */
    private volatile double realTimeFactor;

    private EventTimeListener timelistener;

    private EventTraceListener trcListener;

    /**
     * 收集发生的时间点
     */
    @Getter
    private Set<Double> timePointSet;


    public EventManager(String name) {
        // Basic initialization
        this.name = name;
        lockObject = new Object();

        // Initialize and event lists and timekeeping variables
        currentTick = new AtomicLong(0);
        nextTick = 0;
        oneEvent = false;
        oneSimTime = false;

        // set tick length to 0.000001s = 0.001ms (1000000 tick for 1s = 1000 tick for 1ms)
        setTickLength(1e-6d);

        eventTree = new EventTree();
        condEvents = new ArrayList<>();
        timePointSet = new LinkedHashSet<>();

        isRunning = new AtomicBoolean(false);
        executeEvents = false;
        processRunning = false;
        disableSchedule = false;
        executeRealTime = false;
        realTimeFactor = 1;
        rebaseRealTime = true;
        setTimeListener(null);
    }

    /**
     * 设置时间监听器，通知gui系统仿真时间变化
     * @param l
     */
    public final void setTimeListener(EventTimeListener l) {
        synchronized (lockObject) {
            if (l != null) {
                timelistener = l;
            } else {
                timelistener = new NoopListener();
            }
        }
    }

    /**
     * 设置事件跟踪监听器
     * @param l
     */
    public final void setTraceListener(EventTraceListener l) {
        synchronized (lockObject) {
            trcListener = l;
        }
    }

    /**
     * 清空事件管理器的状态
     */
    public void clear() {
        synchronized (lockObject) {
            currentTick.set(0);
            nextTick = 0;
            targetTick = Long.MAX_VALUE;
            rebaseRealTime = true;

            eventTree.runOnAllNodes(new KillAllEvents());
            eventTree.reset();
            clearFreeList();

            for (int i = 0; i < condEvents.size(); i++) {
                condEvents.get(i).target.kill();
                if (condEvents.get(i).handle != null) {
                    condEvents.get(i).handle.event = null;
                }
            }
            condEvents.clear();
        }
    }

    /**
     * 实现了 EventNode.Runner 接口，关闭所有事件
     */
    private static class KillAllEvents implements EventNode.Runner {
        @Override
        public void runOnNode(EventNode node) {
            Event each = node.head;
            while (each != null) {
                if (each.handle != null) {
                    each.handle.event = null;
                    each.handle = null;
                }

                each.target.kill();
                each = each.next;
            }
        }
    }

    /**
     * 指定线程去执行
     * the return from execute target informs whether or not this thread should grab an new Event, or return to the pool
     * cur执行完毕后，若没有等待cur的nextProcess，则返回true,cur继续获取ProcessTarget执行；若有等待cur执行完毕的nextProcess,唤醒
     * nextProcess,并放回false,将cur返回线程池。
     * cur执行过程中被kill或者出现异常，都会被返回false
     *
     * @param cur
     * @param t
     * @return boolean 返回true: 让该线程继续获取事件； 返回false:将该线程返回线程池
     */
    private boolean executeTarget(Process cur, ProcessTarget t) {
        try {
            // If the event has a captured process, pass control to it
            // 如果该事件已经捕获了线程，则这是一个waitTarget，还是让原来的线程执行它
            // 只有 waitTarget的getProcess才不为空
            if (t.getProcess() != null) {
                Process p = t.getProcess();
                p.setNextProcess(cur);
                p.wake();
                // the cur process waits until process p finishes executing
                // it will be wake by cur.wakeNextProcess in executeTarget(p, t)
                threadWait(cur);
                return true;
            }

            // Execute the method
            t.process();

            // Notify the event manager that the process has been completed
            if (trcListener != null) {
                disableSchedule();
                trcListener.traceProcessEnd();
                enableSchedule();
            }
            // 如果cur有等待它的nextProcess,则唤醒nextProcess
            if (cur.hasNext()) {
                cur.wakeNextProcess();
                return false;
            } else {
                return true;
            }
        } catch (Throwable e) {
            // This is how kill() is implemented for sleeping processes.
            if (e instanceof ThreadKilledException) {
                return false;
            }

            // Tear down any threads waiting for this to finish
            // 说明执行t.process()时出现了异常,删除任何等待此操作完成的线程
            Process next = cur.forceKillNext();
            while (next != null) {
                next = next.forceKillNext();
            }
            executeEvents = false;
            processRunning = false;
            isRunning.set(false);
            timelistener.handleError(e);
            return false;
        }
    }


    /**
     * Main event execution method the eventManager, this is the only entrypoint for Process objects taken out of the pool.
     * 主事件的执行方法，这是从线程池取出 Process 对象的唯一入口点
     * @param cur
     * @param t
     */
    final void execute(Process cur, ProcessTarget t) {
        synchronized (lockObject) {
            // This occurs in the startProcess or interrupt case where we start
            // a process with a target already assigned
            if (t != null) {
                executeTarget(cur, t);
                return;
            }

            if (processRunning) {
                return;
            }

            processRunning = true;
            enableSchedule();
            isRunning.set(true);
            timelistener.timeRunning();

            // Loop continuously
            while (true) {
                // 获取优先队列的队首元素
                EventNode nextNode = eventTree.getNextNode();
                if (nextNode == null || currentTick.get() > targetTick) {
                    // 事件队列中所有事件执行完毕，或执行到目标时间
                    executeEvents = false;
                    // 更新统计数据
                    updateStatitics();
                }

                if (!executeEvents) {
                    // 暂停调度
                    if (currentTick.get() == targetTick) {
                        // 更新统计数据
                        updateStatitics();
                    }
                    processRunning = false;
                    isRunning.set(false);
                    timelistener.timeRunning();
                    return;
                }

                // If the next event is at the current tick, execute it
                // 如果下一个事件发生刻度等于系统当前刻度，则执行它
                if (nextNode.schedTick == currentTick.get()) {
                    // Remove the event from the future events
                    Event nextEvent = nextNode.head;
                    ProcessTarget nextTarget = nextEvent.target;

                    if (trcListener != null) {
                        disableSchedule();
                        trcListener.traceEvent(nextNode.schedTick, nextNode.priority, nextTarget);
                        enableSchedule();
                    }

                    removeEvent(nextEvent);

                    // the return from execute target informs whether or not this
                    // thread should grab an new Event, or return to the pool
                    boolean bool = executeTarget(cur, nextTarget);
                    if (oneEvent) {
                        // 若调度器只执行一个事件，则将oneEvent归位，并停止调度
                        oneEvent = false;
                        executeEvents = false;
                    }
                    if (bool) {
                        continue;
                    } else {
                        return;
                    }
                }

                // If the next event would require us to advance the time, check the conditonal events
                // 如果下一个事件时刻大于系统当前时刻，需要推进仿真时间，则检查条件事件
                if (eventTree.getNextNode().schedTick > nextTick) {
                    if (condEvents.size() > 0) {
                        evaluateConditions();
                        if (!executeEvents) {
                            continue;
                        }
                    }

                    // If a conditional event was satisfied, we will have a new event at the
                    // beginning of the eventStack for the current tick, go back to the
                    // beginning, otherwise fall through to the time-advance
                    nextTick = eventTree.getNextNode().schedTick;
                    if (nextTick == currentTick.get()) {
                        continue;
                    }
                }

                // 当仿真时钟到达目标时间时，设置executeEvents为false, 同时不再推进时钟
                if (currentTick.get() == targetTick) {
                    executeEvents = false;
                    continue;
                }

                // 时钟推进前，更新统计数据
                updateStatitics();

                // Advance to the next event time
                // 实时模式推进时间: 通过wait20ms（推进时间）, 然后continue，再来判断时间，
                if (executeRealTime) {
                    // Loop until the next event time is reached
                    long realTick = this.calcRealTimeTick();
                    if (realTick < nextTick && realTick < targetTick) {
                        // Update the displayed simulation time
                        currentTick.set(realTick);
                        timelistener.tickUpdate(currentTick.get());
                        //Halt the thread for 20ms and then reevaluate the loop
                        try { lockObject.wait(20); } catch( InterruptedException e ) {}
                        continue;
                    }
                }

                // advance time
                // 非实时模式推进时间
                if (targetTick < nextTick) {
                    currentTick.set(targetTick);
                } else {
                    currentTick.set(nextTick);
                }

                log.debug("time: {} - [time advance]", ticksToSeconds(currentTick.get()));

                timelistener.tickUpdate(currentTick.get());

                // 若一次执行下一时刻上的所有事件，则将oneSimTime归位，并停止调度
                if (oneSimTime) {
                    executeEvents = false;
                    oneSimTime = false;
                }
            }
        }
    }

    /**
     * EventViewer NextEvent button to Execute a single event from the event
     * @param simTime
     */
    public void nextOneEvent(double simTime) {
        oneEvent = true;
        resume(this.secondsToNearestTick(simTime));
    }

    /**
     * Event Viewer NextTime button to Execute all the events from the future
     * event list that are scheduled for the next event time. the conditional events
     * are then executed along with any new events that have been scheduled for this time
     * @param simTime
     */
    public void nextEventTime(double simTime) {
        oneSimTime = true;
        resume(this.secondsToNearestTick(simTime));
    }

    public final long getTicks() {
        return currentTick.get();
    }

    public final boolean isRunning() {
        return isRunning.get();
    }

    /**
     * 检查EventManager的所有条件事件列表是否满足
     */
    private void evaluateConditions() {
        // Protecting the conditional evaluate() callbacks and the traceWaitUntilEnded callback
        disableSchedule();
        try {
            for (int i = 0; i < condEvents.size();) {
                ConditionalEvent conditionalEvent = condEvents.get(i);
                if (trcListener != null) {
                    trcListener.traceConditionalEval(conditionalEvent.target);
                }
                boolean bool = conditionalEvent.c.evaluate();
                if (trcListener != null) {
                    trcListener.traceConditionalEvalEnded(bool, conditionalEvent.target);
                }
                if (bool) {
                    condEvents.remove(i);
                    EventNode node = getEventNode(currentTick.get(), 0);
                    Event evt = getEvent(node, conditionalEvent.target, conditionalEvent.handle);

                    if (evt.handle != null) {
                        // no need to check the handle.isScheduled as we just unscheduled it above
                        // and we immediately switch it to this event
                        evt.handle.event = evt;
                    }
                    node.addEvent(evt, true);
                    continue;
                }
                i++;
            }
        } catch (Throwable e) {
            executeEvents = false;
            processRunning = false;
            isRunning.set(false);
            timelistener.handleError(e);
        }

        enableSchedule();
    }

    /**
     * Return the simulation time corresponding the given wall clock time
     * 通过计算 上一次realTimeTick + 本次事件执行所花费的tick, 得到这一次应该更新的 realTimeTick
     * @return simulation time in seconds
     */
    private long calcRealTimeTick() {
        long curMS = System.currentTimeMillis();
        if (rebaseRealTime) {
            // 只会在第一次运行时执行
            realTimeTick = currentTick.get();
            realTimeMillis = curMS;
            rebaseRealTime = false;
        }

        double simElapsedsec = ((curMS - realTimeMillis) * realTimeFactor) / 1000.0d;
        long simElapsedTicks = secondsToNearestTick(simElapsedsec);
        return realTimeTick + simElapsedTicks;
    }

    /**
     * Pause the current active thread and restart the next thread on the
     * active thread list. For this case, a future event or conditional event
     * has been created for the current thread.  Called by
     * eventManager.scheduleWait() and related methods, and by
     * eventManager.waitUntil().
     * restorePreviousActiveThread()
     * Must hold the lockObject when calling this method.
     * 让当前线程等待，执行其后面的线程
     */
    private void captureProcess(Process cur) {
        // if we don't wake a new process, take one from the pool
        Process next = cur.preCapture();
        if (next == null) {
            // 若cur没有nextProcess,从线程池中拉取一个新线程执行事件管理器
            processRunning = false;
            Process.processEvents(this);
        }
        else {
            next.wake();
        }
        // 当前线程等待
        threadWait(cur);
        // 当前线程被唤醒后的一些操作
        cur.postCapture();
    }

    /**
     * 将系统的当前时刻 + 等待时刻，得出即将插入队列的事件的发生时间
     * Calculate the time for an event taking into account numeric overflow.
     * Must hold the lockObject when calling this method
     * @param waitLength 系统当前时刻等待多久后会发生下一个事件
     * @return 即将插入队列的发生时间
     */
    private long calculateEventTime(long waitLength) {
        // Test for negative duration schedule wait length
        if(waitLength < 0) {
            throw new ProcessError("Negative duration wait is invalid, waitLength = " + waitLength);
        }

        // Check for numeric overflow of internal time
        long nextEventTime = currentTick.get() + waitLength;
        if (nextEventTime < 0) {
            nextEventTime = Long.MAX_VALUE;
        }

        return nextEventTime;
    }

    /**
     * Pause the execution of the current Process and schedule it to wake up at a future
     * time in the controlling EventManager,
     * @param ticks the number of ticks in the future to wake at
     * @param priority the priority of the scheduled wakeup event
     * @param fifo break ties with previously scheduled events using FIFO/LIFO ordering
     * @param handle an optional handle to hold onto the scheduled event
     * @throws ProcessError if called outside of a Process context
     */
    public static final void waitTicks(long ticks, int priority, boolean fifo, EventHandle handle) {
        Process cur = Process.current();
        cur.evt().waitTicks(cur, ticks, priority, fifo, handle);
    }

    /**
     * Pause the execution of the current Process and schedule it to wake up at a future
     * time in the controlling EventManager,
     * @param secs the number of seconds in the future to wake at
     * @param priority the priority of the scheduled wakeup event
     * @param fifo break ties with previously scheduled events using FIFO/LIFO ordering
     * @param handle an optional handle to hold onto the scheduled event
     * @throws ProcessError if called outside of a Process context
     */
    public static final void waitSeconds(double secs, int priority, boolean fifo, EventHandle handle) {
        Process cur = Process.current();
        long ticks = cur.evt().secondsToNearestTick(secs);
        cur.evt().waitTicks(cur, ticks, priority, fifo, handle);
    }

    /**
     * Schedules a future event to occur with a given priority.  Lower priority
     * events will be executed preferentially over higher priority.  This is
     * by lower priority events being placed higher on the event stack.
     * @param ticks the number of discrete ticks from now to schedule the event.
     * @param priority the priority of the scheduled event: 1 is the highest priority (default is priority 5)
     */
    private void waitTicks(Process cur, long ticks, int priority, boolean fifo, EventHandle handle) {
        assertCanSchedule();
        long nextEventTime = calculateEventTime(ticks);
        // 创建一个等待目标，封装当前线程
        WaitTarget t = new WaitTarget(cur);
        EventNode node = getEventNode(nextEventTime, priority);
        // 获取一个新事件
        Event evt = getEvent(node, t, handle);

        if (handle != null) {
            if (handle.isScheduled()) {
                throw new ProcessError("Tried to schedule using an EventHandle already in use");
            }
            handle.event = evt;
        }

        if (trcListener != null) {
            disableSchedule();
            trcListener.traceWait(nextEventTime, priority, t);
            enableSchedule();
        }
        node.addEvent(evt, fifo);
        captureProcess(cur);
    }

    /**
     * Find an eventNode in the list, if a node is not found, create one and
     * insert it.
     */
    private EventNode getEventNode(long tick, int prio) {
        return eventTree.createOrFindNode(tick, prio);
    }

    /**
     * 空闲Event链表，保存空闲Event的引用，用于复用空闲事件
     */
    private Event freeEvents = null;

    /**
     * 返回一个设置了指定属性的事件实例，并该event的属性赋值（包括加入红黑树node），若 freeEvent 链表不为空，则复用空闲事件对象，否则new一个实例
     * @param node 事件插入事件队列后（红黑树实现），对应的红黑树节点
     * @param target 事件执行目标
     * @param handle
     * @return
     */
    private Event getEvent(EventNode node, ProcessTarget target, EventHandle handle) {
        Event ret = null;
        if (freeEvents != null) {
            // freeEvent列表不为空，复用空闲事件
            Event evt = freeEvents;
            freeEvents = evt.next;
            ret = evt;
        } else {
            // freeEvent列表为空，new一个新的实例
            ret = new Event();
        }
        ret.node = node;
        ret.target = target;
        ret.handle = handle;
        return ret;
    }

    private void clearFreeList() {
        freeEvents = null;
    }

    public static final void waitUntil(Conditional cond, EventHandle handle) {
        Process cur = Process.current();
        cur.evt().waitUntil(cur, cond, handle);
    }

    /**
     * Used to achieve conditional waits in the simulation.  Adds the calling
     * thread to the conditional stack, then wakes the next waiting thread on
     * the thread stack.
     */
    private void waitUntil(Process cur, Conditional cond, EventHandle handle) {
        assertCanSchedule();
        WaitTarget t = new WaitTarget(cur);
        ConditionalEvent evt = new ConditionalEvent(cond, t, handle);
        if (handle != null) {
            if (handle.isScheduled()) {
                throw new ProcessError("Tried to waitUntil using a handle already in use");
            }
            handle.event = evt;
        }
        condEvents.add(evt);
        if (trcListener != null) {
            disableSchedule();
            trcListener.traceWaitUntil();
            enableSchedule();
        }
        captureProcess(cur);
    }

    public static final void scheduleUntil(ProcessTarget t, Conditional cond, EventHandle handle) {
        Process cur = Process.current();
        cur.evt().schedUntil(cur, t, cond, handle);
    }

    private void schedUntil(Process cur, ProcessTarget t, Conditional cond, EventHandle handle) {
        assertCanSchedule();
        ConditionalEvent evt = new ConditionalEvent(cond, t, handle);
        if (handle != null) {
            if (handle.isScheduled()) {
                throw new ProcessError("Tried to scheduleUntil using a handle already in use");
            }
            handle.event = evt;
        }
        condEvents.add(evt);
        if (trcListener != null) {
            disableSchedule();
            trcListener.traceSchedUntil(t);
            enableSchedule();
        }
    }

    public static final void startProcess(ProcessTarget t) {
        Process cur = Process.current();
        cur.evt().start(cur, t);
    }

    private void start(Process cur, ProcessTarget t) {
        Process newProcess = Process.allocate(this, cur, t);
        // Notify the eventManager that a new process has been started
        assertCanSchedule();
        if (trcListener != null) {
            disableSchedule();
            trcListener.traceProcessStart(t);
            enableSchedule();
        }
        // Transfer control to the new process
        newProcess.wake();
        threadWait(cur);
    }

    /**
     * Remove an event from the eventList, must hold the lockObject.
     * @param evt
     */
    private void removeEvent(Event evt) {
        EventNode node = evt.node;
        node.removeEvent(evt);
        if (node.head == null) {
            if (!eventTree.removeNode(node.schedTick, node.priority)) {
                throw new ProcessError("Tried to remove an eventnode that could not be found");
            }
        }

        // Clear the event to reuse it
        evt.node = null;
        evt.target = null;
        if (evt.handle != null) {
            evt.handle.event = null;
            evt.handle = null;
        }

        evt.next = freeEvents;
        freeEvents = evt;
    }

    private ProcessTarget rem(EventHandle handle) {
        BaseEvent base = handle.event;
        ProcessTarget t = base.target;
        handle.event = null;
        base.handle = null;
        if (base instanceof Event) {
            removeEvent((Event)base);
        } else {
            condEvents.remove(base);
        }
        return t;
    }

    /**
     * Removes the event held in the EventHandle and disposes of it, the ProcessTarget is not run.
     * If the handle does not currently hold a scheduled event, this method simply returns.
     * @throws ProcessError if called outside of a Process context
     */
    public static final void killEvent(EventHandle handle) {
        Process cur = Process.current();
        cur.evt().killEvent(cur, handle);
    }

    /**
     * Removes an event from the pending list without executing it.
     * 从事件等待列表中删除事件
     */
    private void killEvent(Process cur, EventHandle handle) {
        assertCanSchedule();

        // no handle given, or Handle was not scheduled, nothing to do
        if (handle == null || handle.event == null) {
            return;
        }

        if (trcListener != null) {
            disableSchedule();
            trcKill(handle.event);
            enableSchedule();
        }
        ProcessTarget t = rem(handle);

        t.kill();
    }

    private void trcKill(BaseEvent event) {
        if (event instanceof Event) {
            EventNode node = ((Event)event).node;
            trcListener.traceKill(node.schedTick, node.priority, event.target);
        }
        else {
            trcListener.traceKill(-1, -1, event.target);
        }
    }

    /**
     * Interrupts the event held in the EventHandle and immediately runs the ProcessTarget.
     * If the handle does not currently hold a scheduled event, this method simply returns.
     * @throws ProcessError if called outside of a Process context
     */
    public static final void interruptEvent(EventHandle handle) {
        Process cur = Process.current();
        cur.evt().interruptEvent(cur, handle);
    }

    /**
     *	Removes an event from the pending list and executes it.
     */
    private void interruptEvent(Process cur, EventHandle handle) {
        assertCanSchedule();

        // no handle given, or Handle was not scheduled, nothing to do
        if (handle == null || handle.event == null) {
            return;
        }

        if (trcListener != null) {
            disableSchedule();
            trcInterrupt(handle.event);
            enableSchedule();
        }
        ProcessTarget t = rem(handle);

        Process proc = t.getProcess();
        if (proc == null) {
            proc = Process.allocate(this, cur, t);
        }
        proc.setNextProcess(cur);
        proc.wake();
        threadWait(cur);
    }

    private void trcInterrupt(BaseEvent event) {
        if (event instanceof Event) {
            EventNode node = ((Event)event).node;
            trcListener.traceInterrupt(node.schedTick, node.priority, event.target);
        }
        else {
            trcListener.traceInterrupt(-1, -1, event.target);
        }
    }

    public void setExecuteRealTime(boolean useRealTime, double factor) {
        if (useRealTime == executeRealTime && factor == realTimeFactor) {
            return;
        }
        executeRealTime = useRealTime;
        realTimeFactor = factor;
        if (useRealTime) {
            rebaseRealTime = true;
        }
    }

    /**
     * Locks the calling thread in an inactive state to the global lock.
     * When a new thread is created, and the current thread has been pushed
     * onto the inactive thread stack it must be put to sleep to preserve
     * program ordering.
     * <p>
     * The function takes no parameters, it puts the calling thread to sleep.
     * This method is NOT static as it requires the use of wait() which cannot
     * be called from a static context
     * <p>
     * There is a synchronized block of code that will acquire the global lock
     * and then wait() the current thread.
     */
    private void threadWait(Process cur) {
        // Ensure that the thread owns the global thread lock
        try {
            /*
             * Halt the thread and only wake up by being interrupted.
             *
             * The infinite loop is _absolutely_ necessary to prevent
             * spurious wakeups from waking us early....which causes the
             * model to get into an inconsistent state causing crashes.
             */
            while (true) { lockObject.wait(); }
        }
        // Catch the exception when the thread is interrupted
        catch( InterruptedException e ) {}
        if (cur.shouldDie()) {
            throw new ThreadKilledException("Thread killed");
        }
    }

    /**
     * 有外部调度事件的执行，例如启动仿真
     * @param waitLength
     * @param eventPriority
     * @param fifo
     * @param t
     * @param handle
     */
    public void scheduleProcessExternal(long waitLength, int eventPriority, boolean fifo, ProcessTarget t, EventHandle handle) {
        synchronized (lockObject) {
            long schedTick = calculateEventTime(waitLength);
            EventNode node = getEventNode(schedTick, eventPriority);
            Event evt = getEvent(node, t, handle);

            if (handle != null) {
                if (handle.isScheduled()) {
                    throw new ProcessError("Tried to schedule using an EventHandle already in use");
                }
                handle.event = evt;
            }
            // FIXME: this is the only callback that does not occur in Process context, disable for now
            //if (trcListener != null)
            //	trcListener.traceSchedProcess(this, currentTick.get(), schedTick, eventPriority, t);
            node.addEvent(evt, fifo);

            // During real-time waits an event can be inserted becoming the next event to execute
            // If nextTick is not updated, we can fall through the entire time update code and not
            // execute this event, leading to the state machine becoming broken
            if (nextTick > eventTree.getNextNode().schedTick) {
                nextTick = eventTree.getNextNode().schedTick;
            }
        }
    }

    /**
     * 外部命令向时间队列中添加新事件，推进到时间发生时间，然后立马暂停
     * @param waitLength
     * @param eventPriority
     * @param fifo
     * @param t
     * @param handle
     */
    public void scheduleProcessExternalAndPause(long waitLength, int eventPriority, boolean fifo, ProcessTarget t, EventHandle handle) {
        synchronized (lockObject) {
            long schedTick = calculateEventTime(waitLength);
            EventNode node = getEventNode(schedTick, eventPriority);
            Event evt = getEvent(node, t, handle);

            if (handle != null) {
                if (handle.isScheduled()) {
                    throw new ProcessError("Tried to schedule using an EventHandle already in use");
                }
                handle.event = evt;
            }
            // FIXME: this is the only callback that does not occur in Process context, disable for now
            //if (trcListener != null)
            //	trcListener.traceSchedProcess(this, currentTick.get(), schedTick, eventPriority, t);
            node.addEvent(evt, fifo);

            // During real-time waits an event can be inserted becoming the next event to execute
            // If nextTick is not updated, we can fall through the entire time update code and not
            // execute this event, leading to the state machine becoming broken
            if (nextTick > eventTree.getNextNode().schedTick) {
                nextTick = eventTree.getNextNode().schedTick;
            }
            pause();
        }
    }

    /**
     * Schedule a future event in the controlling EventManager for the current Process.
     *
     * @param waitLength the number of ticks in the future to schedule this event
     * @param eventPriority the priority of the scheduled event
     * @param fifo break ties with previously scheduled events using FIFO/LIFO ordering
     * @param t the process target to run when the event is executed
     * @param handle an optional handle to hold onto the scheduled event
     * @throws ProcessError if called outside of a Process context
     */
    public static final void scheduleTicks(long waitLength, int eventPriority, boolean fifo, ProcessTarget t, EventHandle handle) {
        Process cur = Process.current();
        cur.evt().scheduleTicks(cur, waitLength, eventPriority, fifo, t, handle);
    }

    /**
     * Schedule a future event in the controlling EventManager for the current Process.
     *
     * @param secs the number of seconds in the future to schedule this event
     * @param eventPriority the priority of the scheduled event
     * @param fifo break ties with previously scheduled events using FIFO/LIFO ordering
     * @param t the process target to run when the event is executed
     * @param handle an optional handle to hold onto the scheduled event
     *
     * @throws ProcessError if called outside of a Process context
     */
    public static final void scheduleSeconds(double secs, int eventPriority, boolean fifo, ProcessTarget t, EventHandle handle) {
        Process cur = Process.current();
        long ticks = cur.evt().secondsToNearestTick(secs);
        cur.evt().scheduleTicks(cur, ticks, eventPriority, fifo, t, handle);
    }

    private void scheduleTicks(Process cur, long waitLength, int eventPriority, boolean fifo, ProcessTarget t, EventHandle handle) {
        assertCanSchedule();
        long schedTick = calculateEventTime(waitLength);
        EventNode node = getEventNode(schedTick, eventPriority);
        Event evt = getEvent(node, t, handle);

        if (handle != null) {
            if (handle.isScheduled()) {
                throw new ProcessError("Tried to schedule using an EventHandle already in use");
            }
            handle.event = evt;
        }
        if (trcListener != null) {
            disableSchedule();
            trcListener.traceSchedProcess(schedTick, eventPriority, t);
            enableSchedule();
        }
        node.addEvent(evt, fifo);
    }

    /**
     * Sets the value that is tested in the doProcess loop to determine if the
     * next event should be executed.  If set to false, the eventManager will
     * execute a threadWait() and wait until an interrupt is generated.  It is
     * guaranteed in this state that there is an empty thread stack and the
     * thread referenced in activeThread is the eventManager thread.
     */
    public void pause() {
        executeEvents = false;
    }

    /**
     * Sets the value that is tested in the doProcess loop to determine if the
     * next event should be executed.  Generates an interrupt of activeThread
     * in case the eventManager thread has already been paused and needs to
     * resume the event execution loop.  This prevents the model being resumed
     * from an inconsistent state.
     * @param targetTicks - clock ticks at which to pause
     */
    public void resume(long targetTicks) {
        synchronized (lockObject) {

            // Ignore the pause time if it has already been reached
            if (currentTick.get() <= targetTicks) {
                targetTick = targetTicks;
            } else {
                targetTick = Long.MAX_VALUE;
            }

            rebaseRealTime = true;
            if (executeEvents) {
                // 仿真重复启动
                return;
            }

            executeEvents = true;
            isRunning.set(true);
            Process.processEvents(this);
        }
    }

    public void resume(double simTime) {
        resume(secondsToNearestTick(simTime));
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns whether or not we are currently running in a Process context
     * that has a controlling EventManager.
     * @return true if we are in a Process context, false otherwise
     */
    public static final boolean hasCurrent() {
        return (Thread.currentThread() instanceof Process);
    }

    /**
     * Returns whether or not a future event can be scheduled from the present thread.
     * @return true if a future event can be scheduled
     */
    public static final boolean canSchedule() {
        return hasCurrent() && EventManager.current().scheduleEnabled();
    }

    /**
     * Returns the controlling EventManager for the current Process.
     * @throws ProcessError if called outside of a Process context
     */
    public static final EventManager current() {
        return Process.current().evt();
    }

    /**
     * Returns the current simulation tick for the current Process.
     * @throws ProcessError if called outside of a Process context
     */
    public static final long simTicks() {
        return Process.current().evt().currentTick.get();
    }

    /**
     * Returns the current simulation time in seconds for the current Process.
     * @throws ProcessError if called outside of a Process context
     */
    @Deprecated
    public static final double simSeconds() {
        return Process.current().evt().getSeconds();
    }

    @Deprecated
    public final double getSeconds() {
        return currentTick.get() * secsPerTick;
    }

    public final void setTickLength(double tickLength) {
        secsPerTick = tickLength;
        ticksPerSecond = Math.round(1e9d / secsPerTick) / 1e9d;
    }

    /**
     * Convert the number of seconds rounded to the nearest tick.
     * 将经过的秒数转化为经过的tick
     */
    public final long secondsToNearestTick(double seconds) {
        return Math.round(seconds * ticksPerSecond);
    }

    /**
     * Convert the number of ticks into a value in seconds.
     */
    public final double ticksToSeconds(long ticks) {
        return ticks * secsPerTick;
    }

    /**
     * Apppend EventData objects to the provided list for all pending events.
     * @param events List to append EventData objects to
     */
    public final void getEventDataList(ArrayList<EventData> events) {
        // Unsynchronized for use by the Event Viewer
        EventDataBuilder lb = new EventDataBuilder(events);
        eventTree.runOnAllNodes(lb);
    }

    private static class EventDataBuilder implements EventNode.Runner {
        final ArrayList<EventData> eventDataList;

        EventDataBuilder(ArrayList<EventData> events) {
            eventDataList = events;
        }

        @Override
        public void runOnNode(EventNode node) {
            Event evt = node.head;
            while (evt != null) {
                long ticks = evt.node.schedTick;
                int pri = evt.node.priority;
                String desc = evt.target.getDescription();
                eventDataList.add(new EventData(ticks, pri, desc));
                evt = evt.next;
            }
        }
    }

    public final void getConditionalDataList(ArrayList<String> events) {
        for (ConditionalEvent cond : condEvents) {
            events.add(cond.target.getDescription());
        }
    }

    private void disableSchedule() {
        disableSchedule = true;
    }

    private void enableSchedule() {
        disableSchedule = false;
    }

    private void assertCanSchedule() {
        if (disableSchedule) {
            throw new ProcessError("Event Control attempted from inside a user callback");
        }
    }

    private boolean scheduleEnabled() {
        return !disableSchedule;
    }

    /**
     * 当前事件队列是否为空
     * @return
     */
    public boolean hasEvent() {
        return eventTree.getNextNode() != null;
    }

    /**
     * 更新下一个事件发生时间
     */
    public void updateNextTick() {
        if (eventTree.getNextNode() != null) {
            nextTick = eventTree.getNextNode().schedTick;
        }
    }

    /**
     * 更新统计数据，当时间推进&
     */
    public void updateStatitics() {
        for (Entity entity : Entity.getClonesOfIterator(Entity.class)) {
            entity.updateStatistics();
        }
        timePointSet.add(ticksToSeconds(currentTick.get()));
    }

    public double getNextEventTime() {
        return ticksToSeconds(nextTick);
    }
}
