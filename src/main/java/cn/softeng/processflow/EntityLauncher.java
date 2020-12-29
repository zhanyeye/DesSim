package cn.softeng.processflow;

import cn.softeng.basicsim.Entity;
import cn.softeng.basicsim.EntityTarget;
import cn.softeng.events.EventHandle;
import cn.softeng.events.EventManager;
import cn.softeng.events.ProcessTarget;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 实体启动器，用于运行时触发生成实体
 */
@Slf4j
public class EntityLauncher extends LinkedService{

    /**
     * 组件触发后生成的实体数
     */
    @Setter
    private int entitiesPerArrival;

    /**
     * 生成的实体原型
     */
    @Setter
    private Entity prototypeEntity;

    /**
     * EntityLauncher触发时间
     */
    @Setter
    private long scheduleTime;

    public EntityLauncher() {}

    public EntityLauncher(String name) {
        setName(name);
    }

    /**
     * 到目前为止所生成的实体数
     */
    private long numberGenerated = 0;

    private final ProcessTarget doActionTarget = new DoActionTarget(this);

    private final EventHandle doActionHandle = new EventHandle();

    {
        scheduleTime = Long.MAX_VALUE;
        entitiesPerArrival = 1;
        prototypeEntity = new SimEntity();
    }

    @Override
    public void earlyInit() {
        super.earlyInit();
        numberGenerated = 0;
    }

    @Override
    public void startUp() {
        super.startUp();
    }

    /**
     * 调度生成实体（只调度一次），用于DES被串行调度
     * @param eventManager
     * @param scheduleTime
     * @param entitiesPerArrival
     */
    public void scheduleOneAction(EventManager eventManager, long scheduleTime, int entitiesPerArrival) {
        this.scheduleTime = scheduleTime;
        this.entitiesPerArrival = entitiesPerArrival;
        // 优先级须大于5，当0时刻调度该事件时，initModelTarget.process()中的初始化操作优先级是5
        // 必须先初始化再调度该事件，所以优先级需要大于5
        eventManager.scheduleProcessExternal(scheduleTime, 6, false, doActionTarget, doActionHandle);
    }

    /**
     * 调度生成实体，加入事件队列后，立马暂停调度器，用于DES被并行调度
     * @param eventManager
     * @param scheduleTime
     * @param entitiesPerArrival
     */
    public void scheduleAction(EventManager eventManager, long scheduleTime, int entitiesPerArrival) {
        long simTime = eventManager.getTicks();
        if (scheduleTime < simTime) {
            error("schedule time is less than current time ????, no! no! no!");
            return;
        }

        this.scheduleTime = scheduleTime;
        this.entitiesPerArrival = entitiesPerArrival;

        long waitlength = scheduleTime - simTime;
        eventManager.scheduleProcessExternalAndPause(waitlength, 0, false, doActionTarget, null);
    }

    public void doAction() {
        int num = entitiesPerArrival;
        for (int i = 0; i < num; i++) {
            numberGenerated++;
            Entity proto = prototypeEntity;
            StringBuilder sb = new StringBuilder();
            sb.append(this.getName()).append("_").append(numberGenerated);
            Entity entity = Entity.fastCopy(proto, sb.toString());
            entity.earlyInit();
            // 将实体传送给链中的下一个元素
            this.sendToNextComponent(entity);
        }
    }

    /**
     * 调度EntityLauncher去产生实体的代码
     */
    private static class DoActionTarget extends EntityTarget<EntityLauncher> {

        public DoActionTarget(EntityLauncher entity) {
            super(entity, "doAction");
        }

        @Override
        public void process() {
            entity.doAction();
        }
    }

    @Override
    public void updateStatistics() {
        log.debug("Launcher -> NumAdd: {}, NumberProcessed: {}, NumInProcess: {}", this.getTotalNumberAdded(), this.getTotalNumberProcessed(), this.getNumberInProgress());
        numAddMap.put(getSimTicks(), getTotalNumberAdded());
        numInProgress.put(getSimTicks(), getNumberInProgress());
        numProcessedMap.put(getSimTicks(), getTotalNumberProcessed());
    }

}
