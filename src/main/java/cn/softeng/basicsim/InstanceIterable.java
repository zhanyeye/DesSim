package cn.softeng.basicsim;

/**
 * @date: 12/17/2020 2:08 PM
 */
public class InstanceIterable<T extends Entity> extends EntityIterator<T> {
    public InstanceIterable(Class<T> tClass) {
        super(tClass);
    }

    @Override
    boolean matches(Class<?> tClass) {
        return entityClass == tClass;
    }
}
