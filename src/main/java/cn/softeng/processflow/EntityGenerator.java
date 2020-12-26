package cn.softeng.processflow;

import cn.softeng.basicsim.Entity;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * EntityGenerator以随机间隔创建Entities序列，这些序列放置在目标Queue中
 * @date: 12/22/2020 9:38 AM
 */
@Slf4j
public class EntityGenerator extends LinkedService {
    /**
     * 第一个产生的实体到达时间
     */
    @Setter
    private long firstArrivalTime;
    /**
     * 生成实体之间的到达间隔时间
     */
    @Setter
    private long interArrivalTime;

    /**
     * 每次到达要生成的实体数
     */
    @Setter
    private long entitiesPerArrival;

    /**
     * 用于生成实体的原型
     */
    @Setter
    private Entity prototypeEntity;

    /**
     * 要生成的最大实体数
     */
    @Setter
    private long maxNumber;

    /**
     * 到目前为止所生成的实体数
     */
    private long numberGenerated = 0;

    {
        this.firstArrivalTime = 0;
        this.interArrivalTime = 1;
        this.entitiesPerArrival = 1;
        this.prototypeEntity = null;
        this.maxNumber = Long.MAX_VALUE;
    }

    @Override
    public void startUp() {
        super.startUp();
        // Start generating entities
        this.startAction();
    }

    @Override
    public void addEntity(Entity ent ) {
        error("An entity cannot be sent to an EntityGenerator.");
    }

    @Override
    public void earlyInit() {
        super.earlyInit();
        numberGenerated = 0;
    }

    @Override
    protected boolean startProcessing(long simTime) {
        return numberGenerated < maxNumber;
    }

    @Override
    protected void endProcessing(long simTime) {
        // 创建一个新的实体
        long num = entitiesPerArrival;
        for (int i = 0; i < num; i++) {
            numberGenerated++;
            Entity proto = prototypeEntity;
            StringBuilder sb = new StringBuilder();
            sb.append(this.getName()).append("_").append(numberGenerated);
            Entity entity = Entity.fastCopy(proto, sb.toString());
            entity.earlyInit();
//            log.debug("time: {} - EntityGenerator > numberGenerater : {}", simTime, numberGenerated);
            // 将实体传送给链中的下一个元素
            this.sendToNextComponent(entity);

        }
    }

    @Override
    protected long getProcessingTime(long simTime) {
        if (numberGenerated == 0) {
            return firstArrivalTime;
        }
        return interArrivalTime;
    }

    @Override
    public long getNumberInProgress() {
        return 0;
    }

    @Override
    public void updateStatistics() {
        log.debug("Generator -> NumAdd: {}, NumberProcessed: {}, NumInProcess: {}", this.getTotalNumberAdded(), this.getTotalNumberProcessed(), this.getNumberInProgress());
    }

}
