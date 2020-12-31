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
    private long serviceTime;

    private Entity servedEntity;

    {
        serviceTime = 0;
    }

    public Server() {}

    public Server(String name) {
        setName(name);
    }

    @Override
    public void earlyInit() {
        super.earlyInit();
        servedEntity = null;
    }

    @Override
    protected boolean startProcessing(long simTime) {
        if (waitQueue.isEmpty()) {
            return false;
        }
        // 从队列中删除第一个实体
        this.servedEntity = this.getNextEntityFromQueue();
        return true;
    }

    @Override
    protected void endProcessing(long simTime) {
        // 将实体发送到链中的下一个组件
        this.sendToNextComponent(servedEntity);
        servedEntity = null;
    }

    @Override
    protected long getProcessingTime(long simTime) {
        return serviceTime;
    }


    @Override
    public void updateStatistics() {
        log.debug("Server : {} -> NumAdd: {}, NumberProcessed: {}, NumInProcess: {}",this.getName(), this.getTotalNumberAdded(), this.getTotalNumberProcessed(), this.getNumberInProgress());
        numAddMap.put(getSimTicks(), getTotalNumberAdded());
        numInProgressMap.put(getSimTicks(), getNumberInProgress());
        numProcessedMap.put(getSimTicks(), getTotalNumberProcessed());
    }

}
