package cn.softeng.processflow;

import cn.softeng.basicsim.Entity;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * EntityExternalGenerator以动态间隔创建Entities，这些序列放置在目标Queue中
 * @date: 12/25/2020 10:08 AM
 */
@Slf4j
public class EntityExternalGeneration extends LinkedService {
    /**
     * 实体第一次产生时间
     */
    @Setter
    private long firstArrivalTime;
    /**
     * 每次产生的实体数目
     */
    @Setter
    private int entityPerArrival;
    /**
     * 所产生的实体对象
     */
    @Setter
    private Entity prototypeEntity;

    /**
     * 累计产生的实体数目
     */
    private int numberGenerated = 0;
    /**
     * 是否继续产生实体
     */
    private boolean continueRun = true;

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

    /**
     * 开始处理前的必要操作，（类似设计模式中的钩子函数）
     * @param simTime 当前的仿真时间
     * @return 返回 true 则允许处理
     */
    @Override
    protected boolean startProcessing(long simTime) {
        if (continueRun) {
            continueRun = false;
            return true;
        } else {
           return false;
        }
    }

    /**
     * 该组件对应的事件，执行完时对应的操作
     * @param simTime 当前的仿真时间
     */
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
//            log.debug("time: {} - EntityGenerator > numberGenerater : {}", simTime, numberGenerated);
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
