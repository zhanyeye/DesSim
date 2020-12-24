package cn.softeng.basicobject;

import cn.softeng.basicsim.Entity;
import cn.softeng.input.EntityInput;
import cn.softeng.input.ValueInput;
import lombok.extern.slf4j.Slf4j;

/**
 * @date: 12/22/2020 9:38 AM
 */
@Slf4j
public class EntityGenerator extends LinkedService {
    /**
     * 第一个产生的实体到达时间
     */
    private final ValueInput firstArrivalTime;
    /**
     * 生成实体之间的到达间隔时间
     */
    private final ValueInput interArrivalTime;

    /**
     * 每次到达要生成的实体数
     */
    private final ValueInput entitiesPerArrival;

    /**
     * 用于生成实体的原型
     */
    private final EntityInput<Entity> prototypeEntity;

    /**
     * 要生成的最大实体数
     */
    private final ValueInput maxNumber;

    /**
     * 到目前为止所生成的实体数
     */
    private int numberGenerated = 0;

    {
        firstArrivalTime = new ValueInput("FirstArrivalTime", 0L);
        this.addInput(firstArrivalTime);

        interArrivalTime = new ValueInput("InterArrivalTime", 1L);
        this.addInput(interArrivalTime);

        entitiesPerArrival = new ValueInput("EntitiesPerArrival", 1L);
        this.addInput(entitiesPerArrival);

        prototypeEntity = new EntityInput<>(Entity.class, "PrototypeEntity", null);
        this.addInput(prototypeEntity);

        maxNumber = new ValueInput("MaxNumber", Long.MAX_VALUE);
        this.addInput(maxNumber);
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
        return (maxNumber.getValue() == null) || numberGenerated < maxNumber.getValue();
    }

    @Override
    protected void endProcessing(long simTime) {
        // 创建一个新的实体
        int num = entitiesPerArrival.getValue().intValue();
        for (int i = 0; i < num; i++) {
            numberGenerated++;
            Entity proto = prototypeEntity.getValue();
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
        if (numberGenerated == 0) {
            return firstArrivalTime.getValue();
        }
        return interArrivalTime.getValue();
    }

    @Override
    public long getNumberInProgress() {
        return 0;
    }





}
