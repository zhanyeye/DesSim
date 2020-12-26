package cn.softeng.basicobject;

import cn.softeng.basicsim.Entity;
import lombok.extern.slf4j.Slf4j;

/**
 * EntityExternalGenerator以动态间隔创建Entities，这些序列放置在目标Queue中
 * @date: 12/25/2020 10:08 AM
 */
@Slf4j
public class EntityExternalGeneration extends LinkedService {
    private long firstArrivalTime;
    private int entityPerArrival;
    private Entity prototypeEntity;
    private int numberGenerated = 0;

    {
        firstArrivalTime = 0;
        entityPerArrival = 1;
    }

    public EntityExternalGeneration(long firstArrivalTime, int entityPerArrival, Entity entity) {
        this.firstArrivalTime = firstArrivalTime;
        this.entityPerArrival = entityPerArrival;
        this.prototypeEntity = entity;
    }

    public EntityExternalGeneration() {}

    @Override
    public void startUp() {
        super.startUp();
        this.startAction();
    }

    @Override
    protected boolean startProcessing(long simTime) {
        return true;
    }

    @Override
    protected void endProcessing(long simTime) {
        // 创建一个新的实体
        int num = entityPerArrival;
        for (int i = 0; i < num; i++) {
            numberGenerated++;
            Entity proto = prototypeEntity;
            StringBuilder sb = new StringBuilder();
            sb.append(this.getName()).append("_").append(numberGenerated);
            Entity entity = Entity.fastCopy(proto, sb.toString());
            entity.earlyInit();
            log.debug("time: {} - EntityGenerator > numberGenerater : {}", simTime, numberGenerated);
            // 将实体传送给链中的下一个元素
            this.sendToNextComponent(entity);

        }
    }

    @Override
    protected long getProcessingTime(long simTime) {
        return firstArrivalTime;
    }

    @Override
    public long getNumberInProgress() {
        return 0;
    }



}
