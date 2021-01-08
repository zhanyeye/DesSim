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
    private double entitiesPerArrival;

    /**
     * EntityLauncher触发时间
     */
    @Setter
    private double scheduleTime;

    /**
     * 生成的实体原型
     */
    @Setter
    private Entity prototypeEntity;

    public EntityLauncher() {}

    public EntityLauncher(String name) {
        setName(name);
    }

    public EntityLauncher(Integer id) {
        setName(String.valueOf(id));
    }

    /**
     * 到目前为止所生成的实体数
     */
    private long numberGenerated = 0;

    private final ProcessTarget doActionTarget = new DoActionTarget(this);

    private final EventHandle doActionHandle = new EventHandle();

    {
        scheduleTime = Double.MAX_VALUE;
        entitiesPerArrival = 1;
        prototypeEntity = new SimEntity("prototypeEntity");
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
     * 调度生成实体，加入事件队列后，立马暂停调度器，用于实体被触发产生
     * @param eventManager
     * @param scheduleTime
     * @param entitiesPerArrival
     */
    public void scheduleAction(EventManager eventManager, double scheduleTime, int entitiesPerArrival, ProcessTarget clearTarget) {

        double simTime = eventManager.getCurrentTime();
        if (scheduleTime < simTime) {
            error("schedule time is less than current time ????, no! no! no!");
            return;
        }

        this.scheduleTime = scheduleTime;
        this.entitiesPerArrival = entitiesPerArrival;

        double dur = scheduleTime - simTime;

        if (clearTarget != null) {
            // 若clearTarget不为空,则清空组件数据
            long waitTicks =  eventManager.secondsToNearestTick(dur);
            eventManager.scheduleProcessExternal(waitTicks, 6, true, clearTarget, null);
        }

        // 将该事件优先级设置为最低
        eventManager.scheduleProcessExternalAndPause(dur, 6, true, doActionTarget, null);
        // 更新 eventManager 的 nextTick
        eventManager.updateNextTick();
    }

    /**
     * 生成实体的操作
     */
    public void doAction() {
        int num = (int) entitiesPerArrival;
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

    /**
     * 正在进行中的实体数量
     * @return
     */
    @Override
    public long getNumberInProgress() {
        return 0;
    }

    /**
     * 更新统计数据
     */
    @Override
    public void updateStatistics() {
//        log.debug("Launcher: {} -> NumAdd: {}, NumberProcessed: {}, NumInProcess: {}", this.getName(), this.getNumberAdded(), this.getNumberProcessed(), this.getNumberInProgress());
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
