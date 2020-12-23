package cn.softeng.basicsim;

/**
 * 迭代与指定类型entityClass相同的对象
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
