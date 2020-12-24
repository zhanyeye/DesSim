package cn.softeng.basicobject;

import cn.softeng.basicsim.Entity;
import cn.softeng.input.ValueInput;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务从队列中一个接一个地处理实体。完成一个实体后，它将其传递到链中的下一个LinkedComponent。
 * @date: 12/22/2020 9:39 AM
 */
@Slf4j
public class Server extends LinkedService {
    private final ValueInput serviceTime;
    private Entity servedEntity;

    {
        serviceTime = new ValueInput("ServiceTime", 0L);
        this.addInput(serviceTime);
    }

    @Override
    public void earlyInit() {
        super.earlyInit();
        servedEntity = null;
    }

    @Override
    protected boolean startProcessing(long simTime) {
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
        return serviceTime.getValue();
    }

}
