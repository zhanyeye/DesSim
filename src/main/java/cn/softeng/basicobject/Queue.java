package cn.softeng.basicobject;

import cn.softeng.basicsim.Entity;
import cn.softeng.basicsim.EntityTarget;
import cn.softeng.events.EventHandle;
import cn.softeng.events.EventManager;
import cn.softeng.events.ProcessTarget;
import cn.softeng.input.BooleanInput;
import cn.softeng.input.EntityInput;
import cn.softeng.input.ValueInput;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * 队列组件,用于实体排队
 * @date: 12/17/2020 10:01 AM
 */
@Slf4j
public class Queue extends LinkedComponent {

    /**
     * 队列中放置接收到的实体的优先级，例如 priority 1 > priority 2
     */
    private final ValueInput priority;

    /**
     * 设定实体进入Queue的方式:(FIFO or LIFO)
     */
    private final BooleanInput fifo;

    /**
     * 违约时间，实体在队列中最长等待时间，（超时则视作违约，离开实体）
     */
    private final ValueInput renegeTime;

    /**
     * 决定该队列组件是否考虑违约情况
     */
    private final BooleanInput renegeCondition;

    /**
     * 当实体等待超时后，它将去哪一个组件
     */
    private final EntityInput<LinkedComponent> renegeDestination;

    /**
     * 队列中所有实体的集合，每个实体连同其的入队时间、优先级等信息被封装成一个QueueEntry
     * 集合中的entry按照指定的优先级排序
     */
    private final TreeSet<QueueEntry> itemSet;

    /**
     * 该Queue组件的使用者
     */
    private final ArrayList<QueueUser> userList;

    // ***********************************************************
    // 数据统计相关变量
    // ***********************************************************

    protected double timeOfLastUpdate;             // 上次更新统计的时间
    protected double startOfStatisticsCollection;  // 收集统计信息的开始时间
    protected int minElements;                     // 队列中观察到的最小实体数
    protected int maxElements;                     // 队列中观察到的最大实体数数
    protected double elementSeconds;               // 所有实体在队列中花费的总时间
    protected long numberReneged;                  // 等待超时的实体数目

    private final DoQueueChanged userUpdate = new DoQueueChanged(this);
    private final EventHandle userUpdateHandle = new EventHandle();

    {
        // 初始化默认优先级
        priority = new ValueInput("Priority", Long.valueOf(0));
        this.addInput(priority);

        // 初始化队列进程方式
        fifo = new BooleanInput("FIFO", true);
        this.addInput(fifo);

        // 初始化实体等待时间
        renegeTime = new ValueInput("RenegeCondition", null);
        this.addInput(renegeTime);

        // 初始化是否考虑超时情况
        renegeCondition = new BooleanInput("RenegeCondition", false);
        this.addInput(renegeCondition);

        // 初始化超时实体的去向
        renegeDestination = new EntityInput<>(LinkedComponent.class, "RenegeDestination", null);
        this.addInput(renegeDestination);
    }

    public Queue() {
        itemSet = new TreeSet<>();
        userList = new ArrayList<>();
    }

    @Override
    public void earlyInit() {
        super.earlyInit();

        // 清空队列中的实体
        itemSet.clear();

        // 清空统计数据
        timeOfLastUpdate = 0.0;
        startOfStatisticsCollection = 0.0;
        minElements = 0;
        maxElements = 0;
        elementSeconds = 0.0;
        numberReneged = 0;

        // 识别使用该Queue的对象
        userList.clear();
        for (Entity each : Entity.getClonesOfIterable(Entity.class)) {
            if (each instanceof QueueUser) {
                QueueUser u = (QueueUser) each;
                if (u.getQueues().contains(this)) {
                    userList.add(u);
                }
            }
        }
    }

    /**
     * 对进入队列中的实体又进行了一次封装，
     * 添加优先级，入队时间等属性，以便重新排序
     */
    private static class QueueEntry implements Comparable<QueueEntry> {
        final Entity entity;
        final long entryNum;
        final int priority;
        final double timeAdded;

        public QueueEntry(Entity ent, long num, int pri, double time) {
            entity = ent;
            entryNum = num;
            priority = pri;
            timeAdded = time;
        }

        @Override
        public int compareTo(QueueEntry entry) {
            if (this.priority > entry.priority) {
                return 1;
            } else if (this.priority < entry.priority) {
                return -1;
            } else if (this.entryNum > entry.entryNum) {
                return 1;
            } else if (this.entryNum < entry.entryNum) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * 从Queue等待集合中取出指定的实体
     * @param ent
     * @return
     */
    private QueueEntry getQueueEntry(Entity ent) {
        Iterator<QueueEntry> itr = itemSet.iterator();
        while (itr.hasNext()) {
            QueueEntry entry = itr.next();
            if (entry.entity == ent) {
                return entry;
            }
        }
        return null;
    }

    /**
     * 获取指定实体在队列中排队的位置
     * @param entity
     * @return
     */
    public int getPosition(Entity entity) {
        int ret = 0;
        Iterator<QueueEntry> itr = itemSet.iterator();
        while (itr.hasNext()) {
            if (itr.next().entity == entity) {
                return ret;
            }
            ret++;
        }
        return -1;
    }

    /**
     * 队列变化通知Target, 用于通知Queue的使用者,Queue发生了改变
     */
    private static class DoQueueChanged extends ProcessTarget {
        private final Queue queue;

        public DoQueueChanged(Queue q) {
            queue = q;
        }

        @Override
        public void process() {
            // 告诉每一个队列的使用者，队列发生改变
            for (QueueUser each : queue.userList) {
                each.queueChanged();
            }
        }

        @Override
        public String getDescription() {
            return queue.getName() + ".UpdateAllQueueUsers";
        }
    }

    // ***************************************************************************
    // 队列处理方法
    // ***************************************************************************

    /**
     * 队列组件接受到新的实体
     * @param entity
     */
    @Override
    public void addEntity(Entity entity) {
        super.addEntity(entity);

        // todo 更新队列统计相关操作

        // 建立一个实体项目
        long entryNum = this.getTotalNumberAdded();
        if (!fifo.getValue()) {
            // 如果是先进后出,则排队的实体号为负值
            entryNum *= -1;
        }
        int pri = (int) priority.getValue().intValue();
        QueueEntry entry = new QueueEntry(entity, entryNum, pri, getSimTicks());

        // 将实体添加到集合中
        boolean bool = itemSet.add(entry);
        if (!bool) {
            error("Entity %s is already present in the queue.", entity);
        }

        // 通知该队列的所有用户
        if (!userUpdateHandle.isScheduled()) {
            // 当前时刻调度 userUpdate target 通知所有使用该队列的组件
            EventManager.scheduleTicks(0, 2, false, userUpdate, userUpdateHandle);
        }

        // 调度指定时间去检查放弃（违约）条件
        if (renegeTime.getValue() != null) {
            long dur =  renegeTime.getValue();
            // 以FIFO的顺序调度违约测试，所以若有多个实体被同时添加到队列中
            // 则队列中越靠近前目的实体会先被测试
            EventManager.scheduleTicks(dur, 5, true, new RenegeActionTarget(this, entity), null);
        }
        log.debug("time: {} - {} > addQueue : {}  > inQueue : {}", getSimTicks(), this.getName(), this.getTotalNumberAdded(), this.getNumberInProgress());
    }

    /**
     * 从队列中移除指定的实体
     * @param entry
     * @return
     */
    public Entity remove(QueueEntry entry) {
        int queueSize = itemSet.size();

        // 将指定实体从队列的所有实体 TreeSet 集合中删除
        boolean found = itemSet.remove(entry);
        if (!found) {
            error("Cannot find the entry in itemSet");
        }
        this.incrementNumberProcessed();
        log.debug("time: {} - {} > addQueue : {}  > inQueue : {}", getSimTicks(), this.getName(), this.getTotalNumberAdded(), this.getNumberInProgress());
        return entry.entity;
    }

    /**
     * 当队列中有实体等待超时，需要进行的操作
     * @param entity 等待超时的实体
     */
    public void renegeAction(Entity entity) {
        // 从等待集合中查找实体对应的条目，
        QueueEntry entry = this.getQueueEntry(entity);
        if (entry == null) {
            // 如果实体已经离开队列，则什么也不做
            return;
        }

        // 临时将接受到实体设置为超时的实体
        Entity oldEntity = this.getReceivedEntity();
        this.setReceivedEntity(entity);

        // 检查是否考虑超时情况的条件
        if (renegeCondition.getValue() == false) {
            // receivedEntity 恢复原状
            this.setReceivedEntity(oldEntity);
            return;
        }

        // 重置实体
        this.setReceivedEntity(oldEntity);

        // 将超时实体从队列中移除，并传送到超时处理组件
        this.remove(entry);
        numberReneged++;
        renegeDestination.getValue().addEntity(entity);
    }

    /**
     * 当队列中有实体等待超时后，对实体进行的相关操作的 target
     */
    private static class RenegeActionTarget extends EntityTarget<Queue> {
        private final Entity queuedEntity;

        RenegeActionTarget(Queue q, Entity e) {
            super(q, "renegeAction");
            queuedEntity = e;
        }

        @Override
        public void process() {
            entity.renegeAction(queuedEntity);
        }
    }

    /**
     * 从队列中移除第一个实体
     * @return
     */
    public Entity removeFirst() {
        if (itemSet.isEmpty()) {
            error("Cannot remove an entity frome an empty queue");
        }
        return this.remove(itemSet.first());
    }

    /**
     * 返回队列中的第一个实体
     * @return
     */
    public Entity getFirst() {
        return itemSet.first().entity;
    }

    /**
     * 返回队列中实体的数目
     * @return
     */
    public int getCount() {
        return itemSet.size();
    }

    /**
     * 队列是否为空
     * @return
     */
    public boolean isEmpty() {
        return itemSet.isEmpty();
    }

    /**
     * 返回队列中第一个实体花费的时间刻度
     * @return
     */
    public double getQueueTime() {
        return this.getSimTicks() - itemSet.first().timeAdded;
    }

    /**
     * 返回队列中第一个对象的优先级值
     * @return
     */
    public int getFirstPriority() {
        return itemSet.first().priority;
    }

    private void updateStatistics(int oldValue, int newValue) {
        minElements = Math.min(newValue, minElements);
        maxElements = Math.max(newValue, maxElements);

        // todo 其他统计相关操作
    }

}
