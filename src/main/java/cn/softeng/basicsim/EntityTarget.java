package cn.softeng.basicsim;

import cn.softeng.events.ProcessTarget;

/**
 * 将实体模型相应的操作转化成对应的事件
 * @date: 12/19/2020 7:17 PM
 */
public abstract class EntityTarget<T extends Entity> extends ProcessTarget {
    protected final T entity;
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
