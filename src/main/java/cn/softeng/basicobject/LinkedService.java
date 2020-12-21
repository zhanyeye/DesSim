package cn.softeng.basicobject;

import cn.softeng.basicsim.Entity;
import cn.softeng.basicsim.EntityTarget;
import cn.softeng.events.EventHandle;
import cn.softeng.events.EventManager;
import cn.softeng.events.ProcessTarget;
import cn.softeng.input.EntityInput;

import java.util.ArrayList;

/**
 *
 * @date: 12/17/2020 10:00 AM
 */
public class LinkedService extends LinkedComponent implements QueueUser {

    /**
     * 等待进入该组件的实体，被放置的队列
     */
    protected final EntityInput<Queue> waitQueue;

    private boolean busy;
    private long startTime;
    private long duration;
    private boolean processKilled;
    private long stopWorkTime;

    private final ProcessTarget endActionTarget = new EndActionTarget(this);
    /**
     * 用于持有endActions事件的引用
     */
    private final EventHandle endActionHandle = new EventHandle();

    {
        waitQueue = new EntityInput<>(Queue.class, "WaitQueue", null);
        this.addInput(waitQueue);
    }

    @Override
    public void earlyInit() {
        super.earlyInit();
        this.setBusy(false);
        startTime = 0;
        duration = 0;
        processKilled = false;
        stopWorkTime = 0;
    }

    @Override
    public String getInitialState() {
        return "Idle";
    }

    @Override
    public void addEntity(Entity entity) {
        // 若该组件的入口没有队列，则直接处理实体
        if (waitQueue.getValue() == null) {
            super.addEntity(entity);
            return;
        }

        // 向该组件的入口的Queue中添加实体
        waitQueue.getValue().addEntity(entity);
    }

    protected final boolean isBusy() {
        return busy;
    }

    private void setBusy(boolean bool) {
        if (bool == busy) {
            return;
        }

        if (!bool) {
            stopWorkTime = this.getSimTicks();
        }
        busy = bool;
    }

    protected double getStopWorkTime() {
        return stopWorkTime;
    }

    /**
     * 获取该组件用到的队列
     * @return
     */
    @Override
    public ArrayList<Queue> getQueues() {
        ArrayList<Queue> ret = new ArrayList<>();
        if (waitQueue.getValue() != null) {
            ret.add(waitQueue.getValue());
        }
        return ret;
    }

    /**
     * 当该组件用到的队列改变时，触发
     */
    @Override
    public void queueChanged() {
        this.restartAction();
    }

    // *****************************
    // 处理实体
    // *****************************

    /**
     * 执行此LinkedService子类所需的任何特殊处理
     * @param simTime 当前的仿真时间
     * @return 如果可以继续处理，则为true
     */
    protected boolean startProcessing(long simTime) {
        return true;
    }

    /**
     * 放回处理完实体所需要的时间
     * @param simTime 当前的仿真时间
     * @return 所需的处理时间
     */
    protected long getProcessingTime(long simTime) {
        return 0;
    }

    /**
     * 执行此LinkedService子类所需的任何特殊处理
     * @param simTime 当前的仿真时间
     */
    protected void endProcessing(long simTime) {}

    /**
     * 组件开始处理实体
     */
    protected final void startAction() {
        // 执行LinkedService子类的特别处理操作
        long simTime = this.getSimTicks();
        boolean bool = this.startProcessing(simTime);
        if (!bool) {
            this.stopAction();
            return;
        }

        // 设置状态
        if (!isBusy()) {
            this.setBusy(true);
            this.setPresentState();
        }

        // 调度服务完成
        startTime = simTime;
        duration = this.getProcessingTime(simTime);
        this.scheduleProcessTicks(duration, 5, endActionTarget, endActionHandle);
    }

    /**
     * 完成一个实体的处理
     */
    final void endAction() {
        // 执行此LinkedService子类所需的任何特殊处理
        this.endProcessing(this.getSimTicks());
        // 处理下一个实体
        this.startAction();
    }

    /**
     * 中断实体的处理
     */
    private void stopAction() {
        // 如果正在进行则中断操作
        if (endActionHandle.isScheduled()) {
            EventManager.killEvent(endActionHandle);
            processKilled = true;
        }
        // 更新统计数据
        this.setBusy(false);
        this.setPresentState();
    }

    private void restartAction() {
        if (this.isIdle()) {
            if (processKilled) {
                processKilled = false;
//                boolean bool = this.up
            }
        }
    }


    private static class EndActionTarget extends EntityTarget<LinkedService> {
        EndActionTarget(LinkedService ent) {
            super(ent, "endAction");
        }

        @Override
        public void process() {
//
        }
    }

    /**
     * 测试 LinkedService 是否可以工作
     * @return
     */
    public boolean isIdle() {
        return !isBusy();
    }


}
