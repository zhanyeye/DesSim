package cn.softeng.basicsim;

import cn.softeng.events.ProcessTarget;

/**
 * 启动实体的Target（命令模式中的 ConcreteCommand）
 * EntityGenerator 中实现了startUp()方法
 * @date: 12/23/2020 12:18 PM
 */
public class StartUpTarget extends ProcessTarget {

    final Entity entity;

    public StartUpTarget(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void process() {
        entity.startUp();
    }

    @Override
    public String getDescription() {
        return entity.getName() + ". startUp";
    }
}
