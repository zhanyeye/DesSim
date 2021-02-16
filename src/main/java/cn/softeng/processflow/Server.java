package cn.softeng.processflow;

import cn.softeng.basicsim.Entity;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务从队列中一个接一个地处理实体。完成一个实体后，它将其传递到链中的下一个LinkedComponent。
 * @date: 12/22/2020 9:39 AM
 */
@Slf4j
public class Server extends LinkedService {
    @Setter
    private double serviceTime;

    private Entity servedEntity;

    {
        serviceTime = 0;
    }

    public Server() {}

    public Server(String name) {
        setName(name);
    }

    public Server(Integer id) {
        setName(String.valueOf(id));
    }

    @Override
    public void earlyInit() {
        super.earlyInit();
        servedEntity = null;
    }

    /**
     * 当实体被处理时，调用的钩子函数，server组件会从队列中取出一个临时实体来加工
     * @param simTime 当前的仿真时间
     * @return
     */
    @Override
    protected boolean startProcessing(double simTime) {
        if (waitQueue.isEmpty()) {
            return false;
        }
        // 从队列中删除第一个实体
        this.servedEntity = this.getNextEntityFromQueue();
        return true;
    }

    /**
     * 当时实体处理结束时，会调用的钩子函数，server会将实体传递给下一个组件
     * @param simTime 当前的仿真时间
     */
    @Override
    protected void endProcessing(double simTime) {
        // 将实体发送到链中的下一个组件
        this.sendToNextComponent(servedEntity);
        servedEntity = null;
    }

    /**
     * 获取实体加工的时间
     * @param simTime 当前的仿真时间
     * @return
     */
    @Override
    protected double getProcessingTime(double simTime) {
        return serviceTime;
    }


    @Override
    public void updateStatistics() {
//        log.debug("Server : {} -> NumAdd: {}, NumberProcessed: {}, NumInProcess: {}",this.getName(), this.getNumberAdded(), this.getNumberProcessed(), this.getNumberInProgress());
        numAddMap.put(getSimTime(), getNumberAdded());
        numInProgressMap.put(getSimTime(), getNumberInProgress());
        numProcessedMap.put(getSimTime(), getNumberProcessed());
    }

    @Override
    public void clearStatistics() {
        numAddMap.clear();
        numInProgressMap.clear();
        numProcessedMap.clear();
    }

}
