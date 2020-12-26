package cn.softeng.states;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;

import cn.softeng.basicsim.Entity;
import cn.softeng.events.EventManager;

/**
 * @date: 12/16/2020 6:05 PM
 */
public class StateEntity extends Entity {
    /**
     * The present state of the entity
     */
    private StateRecord presentState;
    private final HashMap<String, StateRecord> states;
    private long lastStateCollectionTick;
    private long workingTicks;

    public StateEntity() {
        states = new HashMap<>();
    }

    @Override
    public void earlyInit() {
        // 为实体属性设置默认值
        super.earlyInit();
        // 初始化状态数据
        this.initStateData();
    }

    @Override
    public void lateInit() {
        super.lateInit();
    }

    /**
     * 初始化状态实体，
     */
    private void initStateData() {
        lastStateCollectionTick = 0;
        if (EventManager.hasCurrent()) {
            // 获取仿真当前时钟刻度
            lastStateCollectionTick = getSimTicks();
        }
        // 累计工作时间刻度
        workingTicks = 0;
        states.clear();

        // 给状态实体添加一个初始状态
        String initState = getInitialState().intern();
        StateRecord init = new StateRecord(initState, isValidWorkingState(initState));
        init.startTick = lastStateCollectionTick;
        presentState = init;
        states.put(init.name, init);
    }



    /**
     * Get the name of the initial state this Entity will be initialized with.
     * @return
     */
    public String getInitialState() {
        return "Idle";
    }

    /**
     * Tests the given state name to see if it is valid for this Entity.
     * @param state
     * @return
     */
    public boolean isValidState(String state) {
        return "Idle".equals(state) || "Working".equals(state);
    }

    /**
     * ~
     * Tests the given state name to see if it is counted as working hours when in
     * that state..
     * @param state
     * @return
     */
    public boolean isValidWorkingState(String state) {
        return "Working".equals(state);
    }

    /**
     * Sets the state of this Entity to the given state.
     */
    public final void setPresentState( String state ) {
        if (presentState == null) {
            this.initStateData();
        }

        if (presentState.name.equals(state)) {
            return;
        }

        StateRecord nextState = states.get(state);
        if (nextState == null) {
            if (!isValidState(state))
                error("Specified state: %s is not valid", state);

            String intState = state.intern();
            nextState = new StateRecord(intState, isValidWorkingState(intState));
            states.put(nextState.name, nextState);
        }

//		this.setGraphicsForState(state);

        updateStateStats();
        nextState.startTick = lastStateCollectionTick;

        StateRecord prev = presentState;
        presentState = nextState;
        stateChanged(prev, presentState);
    }


    /**
     * A callback subclasses can override that is called on each state transition.
     *
     * The state has already been updated when this is called so presentState == next
     * @param prev the state this Entity was in previously
     * @param next the state this Entity is currently in
     */
    public void stateChanged(StateRecord prev, StateRecord next) {

//        if (traceState.getValue()) {
//            long curTick = EventManager.simTicks();
//            EventManager evt = EventManager.current();
//            double duration = evt.ticksToSeconds(curTick - prev.getStartTick());
//            double timeOfPrevStart = evt.ticksToSeconds(prev.getStartTick());
////			stateReportFile.format("%.5f  %s.setState( \"%s\" ) dt = %g\n",
////			                       timeOfPrevStart, this.getName(),
////			                       prev.name, duration);
////			stateReportFile.flush();
//        }
//
//        for (StateEntityListener each : stateListeners) {
//            each.updateForStateChange(this, prev, next);
//        }
    }

    /**
     * Update the statistics kept for ticks in the presentState
     */
    private void updateStateStats() {
        long curTick = getSimTicks();
        if (curTick == lastStateCollectionTick) {
            return;
        }

        long durTicks = curTick - lastStateCollectionTick;
        lastStateCollectionTick = curTick;

        presentState.totalTicks += durTicks;
        presentState.currentCycleTicks += durTicks;
        if (presentState.working)
            workingTicks += durTicks;
    }

    /**
     * Runs after initialization period
     */
    public void collectInitializationStats() {
        updateStateStats();

        for (StateRecord each : states.values()) {
            each.initTicks = each.totalTicks;
            each.totalTicks = 0;
            each.completedCycleTicks = 0;
        }
    }

    /**
     * Runs after each report interval
     */
    public void clearReportStats() {
        updateStateStats();

        // clear totalHours for each state record
        for (StateRecord each : states.values()) {
            each.totalTicks = 0;
            each.completedCycleTicks = 0;
        }
    }

    /**
     * Clear the current cycle hours, also reset the start of cycle time
     */
    public void clearCurrentCycleStats() {
        updateStateStats();

        // clear current cycle hours for each state record
        for (StateRecord each : states.values()) {
            each.currentCycleTicks = 0;
        }
    }

    /**
     * Runs when cycle is finished
     */
    public void collectCycleStats() {
        updateStateStats();

        // finalize cycle for each state record
        for (StateRecord each : states.values()) {
            each.completedCycleTicks += each.currentCycleTicks;
            each.currentCycleTicks = 0;
        }
    }

    public void addState(String str) {
        if (states.get(str) != null)
            return;
        if (!isValidState(str))
            error("Specified state: %s is not valid", str);

        String state = str.intern();
        StateRecord stateRec = new StateRecord(state, isValidWorkingState(state));
        states.put(stateRec.name, stateRec);
    }

    public StateRecord getState(String state) {
        return states.get(state);
    }

    public StateRecord getState() {
        return presentState;
    }

    private static class StateRecSort implements Comparator<StateRecord> {
        @Override
        public int compare(StateRecord sr1, StateRecord sr2) {
            return sr1.name.compareTo(sr2.name);
        }
    }

    public ArrayList<StateRecord> getStateRecs() {
        ArrayList<StateRecord> recs = new ArrayList<>(states.size());
        for (StateRecord rec : states.values())
            recs.add(rec);
        Collections.sort(recs, new StateRecSort());
        return recs;
    }

    /**
     * Return true if the entity is working
     */
    public boolean isWorking() {
        return presentState.working;
    }

    /**
     * A helper used to implement some of the state-based outputs, likely not
     * useful for model code.
     * @param simTicks
     * @param state
     * @return
     */
    public long getTicksInState(long simTicks, StateRecord state) {
        if (state == null)
            return 0;

        long ticks = state.totalTicks;
        if (getState() == state)
            ticks += (simTicks - lastStateCollectionTick);
        return ticks;
    }

    public long getTicksInState(StateRecord state) {
        if (state == null)
            return 0;

        long ticks = state.totalTicks;
        if (getState() == state)
            ticks += (getSimTicks() - lastStateCollectionTick);
        return ticks;
    }

    public long getCurrentCycleTicks(StateRecord state) {
        if (state == null)
            return 0;

        long ticks = state.currentCycleTicks;
        if (getState() == state)
            ticks += (getSimTicks() - lastStateCollectionTick);
        return ticks;
    }

    public long getCompletedCycleTicks(StateRecord state) {
        if (state == null)
            return 0;

        return state.completedCycleTicks;
    }

    private long getWorkingTicks(long simTicks) {
        long ticks = workingTicks;
        if (presentState.working)
            ticks += (simTicks - lastStateCollectionTick);

        return ticks;
    }

    /**
     * Returns the number of seconds that the entity has been in use.
     */
    public double getWorkingTime() {
        long ticks = getWorkingTicks(getSimTicks());
        return EventManager.current().ticksToSeconds(ticks);
    }

    public void setPresentState() {}


//	public String getPresentState(double simTime) {
//		if (presentState == null) {
//			return this.getInitialState();
//		}
//		return presentState.name;
//	}


    public double getWorkingTime(double simTime) {
        if (presentState == null) {
            return 0.0;
        }
        long simTicks = EventManager.current().secondsToNearestTick(simTime);
        long ticks = getWorkingTicks(simTicks);
        return EventManager.current().ticksToSeconds(ticks);
    }


    public LinkedHashMap<String, Double> getStateTimes(double simTime) {
        long simTicks = EventManager.current().secondsToNearestTick(simTime);
        LinkedHashMap<String, Double> ret = new LinkedHashMap<>(states.size());
        for (StateRecord stateRec : this.getStateRecs()) {
            long ticks = getTicksInState(simTicks, stateRec);
            Double t = EventManager.current().ticksToSeconds(ticks);
            ret.put(stateRec.name, t);
        }
        return ret;
    }
}
