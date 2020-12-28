package cn.softeng.processflow;

import cn.softeng.basicsim.Entity;
import cn.softeng.basicsim.EntityTarget;
import cn.softeng.events.EventHandle;
import cn.softeng.events.EventManager;
import cn.softeng.events.ProcessTarget;
import lombok.Setter;

/**
 * 实体启动器，用于运行时触发生成实体
 */
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

    /**
     * 到目前为止所生成的实体数
     */
    private long numberGenerated = 0;

    private final ProcessTarget doActionTarget = new DoActionTarget(this);

    private final EventHandle doActionHandle = new EventHandle();

    private EventManager eventManager;

    {
        scheduleTime = Long.MAX_VALUE;
        entitiesPerArrival = 1;
    }

    @Override
    public void earlyInit() {
        super.earlyInit();
        numberGenerated = 0;
    }

    @Override
    public void startUp() {
        super.startUp();
        eventManager = EventManager.current();
        scheduleAction();
    }

    public void scheduleAction() {
       this.scheduleAction(scheduleTime, entitiesPerArrival);
    }

    public void scheduleAction(long scheduleTime, int entitiesPerArrival) {
        long simTime = getSimTicks();
        if (scheduleTime < simTime) {
            error("schedule time is less than current time ????, are you ok ????");
            return;
        }

        this.scheduleTime = scheduleTime;
        this.entitiesPerArrival = entitiesPerArrival;

        long waitlength = scheduleTime - simTime;
        eventManager.scheduleProcessExternal(waitlength, 0, false, doActionTarget, doActionHandle);
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
//            log.debug("time: {} - EntityGenerator > numberGenerater : {}", simTime, numberGenerated);
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

}
