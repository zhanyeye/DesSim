package cn.softeng.basicobject;

import cn.softeng.basicsim.Entity;
import cn.softeng.events.ProcessTarget;
import cn.softeng.input.BooleanInput;
import cn.softeng.input.EntityInput;
import cn.softeng.input.ValueInput;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * 队列组件，用于排队
 * @date: 12/17/2020 10:01 AM
 */
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
     * 实体决定放弃之前，要在队列中等待的时间
     */
    private final ValueInput renegeTime;
    /**
     * 用于确定实体在等待其renegeTime后是否会放弃
     */
    private final BooleanInput renegeCondition;
    /**
     * 当实体放弃等待后，它将被送给哪一个组件
     */
    private final EntityInput<LinkedComponent> renegeDestination;
    /**
     * 按队列顺序的所有实体的集合
     */
    private final TreeSet<QueueEntry> itemSet;
    /**
     * 使用该队列的组件
     */
    private final ArrayList<QueueUser> userList;

    // ***********************************************************
    // 数据统计相关变量
    // ***********************************************************

    protected double timeOfLastUpdate;
    protected double startOfStatisticsCollection;
    protected int minElements;
    protected int maxElements;
    protected double elementSeconds;
    protected long numberReneged;

    {
        priority = new ValueInput("Priority", Double.valueOf(0));
        this.addInput(priority);

        fifo = new BooleanInput("FIFO", true);
        this.addInput(fifo);

        renegeTime = new ValueInput("RenegeCondition", Double.valueOf(0));
        this.addInput(renegeTime);

        renegeCondition = new BooleanInput("RenegeCondition", false);
        this.addInput(renegeCondition);

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
     * 队列中的实体条目
     */
    private static class QueueEntry implements Comparable<QueueEntry> {
        final Entity entity;
        final long entityNum;
        final int priority;
        final double timeAdded;

        public QueueEntry(Entity ent, long n, int pri, double time) {
            entity = ent;
            entityNum = n;
            priority = pri;
            timeAdded = time;
        }

        @Override
        public int compareTo(QueueEntry entry) {
            if (this.priority > entry.priority) {
                return 1;
            } else if (this.priority < entry.priority) {
                return -1;
            } else if (this.entityNum > entry.entityNum) {
                return 1;
            } else if (this.entityNum < entry.entityNum) {
                return -1;
            } else {
                return 0;
            }
        }
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

    @Override
    public void addEntity(Entity entity) {
        super.addEntity(entity);
        // 建立一个实体项目
        long entityNum = this.getTotalNumberAdded();
        if (!fifo.getValue()) {
            entityNum *= -1;
        }
        int pri = (int) priority.getValue().intValue();
//        QueueEntry entry = new QueueEntry(entity, entityNum, pri, getS)



    }






}
