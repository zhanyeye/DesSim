package cn.softeng.input;

import cn.softeng.basicsim.Entity;

/**
 * @date: 12/16/2020 3:39 PM
 */
public class EntityInput<T extends Entity> extends Input<T> {

    private Class<T> entityClass;
    private Class<? extends T> entitySubClass;
    private boolean includeSubClasses;

    public EntityInput(Class<T> tClass, String keyword, T defValue) {
        super(keyword, defValue);
        entityClass = tClass;
        entitySubClass = tClass;
        includeSubClasses = true;
    }

    @Override
    public void updateValue(Object newValue) {
        value = (T) newValue;
    }
}
