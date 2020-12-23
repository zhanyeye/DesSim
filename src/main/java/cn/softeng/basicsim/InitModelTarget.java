package cn.softeng.basicsim;

import cn.softeng.events.EventManager;
import cn.softeng.events.ProcessTarget;
import lombok.extern.slf4j.Slf4j;

/**
 * @date: 12/23/2020 12:18 PM
 */
@Slf4j
public class InitModelTarget extends ProcessTarget {
    @Override
    public void process() {
        log.info("init");
        // 初始化每一个实体
        for (Entity each : Entity.getClonesOfIterable(Entity.class)) {
            each.earlyInit();
        }

        // 再次初始化某个实体
        for (Entity each : Entity.getClonesOfIterable(Entity.class)) {
            each.lateInit();
        }

        // 启动每一个实体
        long startTime = 0;
        for (Entity each : Entity.getClonesOfIterator(Entity.class)) {
            EventManager.scheduleTicks(startTime, 0, true, new StartUpTarget(each), null);
        }

//        // 调度初始化时间
//        if (Simulation.getInitializationTime() > 0.0) {
//            double clearTime = startTime + Simulation.getInitializationTime();
//            EventManager.scheduleTicks(clearTime, 5, false, new ClearStatisticsTarget(), null);
//        }

        // 调度模型运行的结束
//        long endTime = 100;
//        EventManager.scheduleTicks(endTime, 5, false, new EndModelTarget(), null);

        // 开始检查暂停条件
//        Simulation.getInstance().doPauseCondition();


    }

    @Override
    public String getDescription() {
        return "SimulationInit";
    }
}
