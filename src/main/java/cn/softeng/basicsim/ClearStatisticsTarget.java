package cn.softeng.basicsim;

import cn.softeng.events.EventManager;
import cn.softeng.events.ProcessTarget;

/**
 * @date: 1/8/2021 9:09 AM
 * 清空组件统计数据的target(命令)
 * 命令模式中的 ConcreteCommand，执行 EventManager 的 clearStatitics() 操作
 */
public class ClearStatisticsTarget extends ProcessTarget {
    @Override
    public void process() {
        // 清空所有模型组件的统计数据
        EventManager.current().clearStatitics();
    }

    @Override
    public String getDescription() {
        return "Cleaning up statistics";
    }
}
