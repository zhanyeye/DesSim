package cn.softeng.basicsim;

import cn.softeng.events.EventManager;
import cn.softeng.events.ProcessTarget;
import lombok.extern.slf4j.Slf4j;

/**
 * @date: 12/23/2020 12:18 PM
 */
@Slf4j
public class InitModelTarget extends ProcessTarget {

    public static boolean vertical;

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

    }

    @Override
    public String getDescription() {
        return "SimulationInit";
    }
}
