package cn.softeng.basicsim;

import cn.softeng.events.ProcessTarget;

/**
 * @date: 12/23/2020 12:18 PM
 */
public class InitModelTarget extends ProcessTarget {
    @Override
    public void process() {
        // 初始化每一个实体
        for (Entity each : Entity.getClonesOfIterable(Entity.class)) {
            each.earlyInit();
        }

        // 再次初始化某个实体
        for (Entity each : Entity.getClonesOfIterable(Entity.class)) {
            each.lateInit();
        }

        // 启动每一个实体
//        double

        // 调度初始化时间


        // 调度模型运行的结束


        // 开始检查暂停条件


    }

    @Override
    public String getDescription() {
        return "SimulationInit";
    }
}
