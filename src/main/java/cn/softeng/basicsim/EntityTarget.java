package cn.softeng.basicsim;

import cn.softeng.events.ProcessTarget;

/**
 * 命令模式中的 ConcreteCommand, 但还是抽象了一层
 * 用于声明 Entity 子类中包含的 ConcreteCommand 所执行的操作
 * @date: 12/19/2020 7:17 PM
 */
public abstract class EntityTarget<T extends Entity> extends ProcessTarget {
    /**
     * 持有相应的接收者对象: 被执行的实体
     */
    protected final T entity;
    /**
     * 被执行的方法名，用于更新 getDescription()
     */
    private final String desc;

    public EntityTarget(T entity, String method) {
        this.entity = entity;
        this.desc = method;
    }

    @Override
    public String getDescription() {
        return entity.getName() + "." + desc;
    }
}
