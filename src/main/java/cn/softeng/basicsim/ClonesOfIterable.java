package cn.softeng.basicsim;

/**
 * @date: 12/17/2020 2:53 PM
 */
public class ClonesOfIterable<T extends Entity> extends EntityIterator<T> {
    public ClonesOfIterable(Class<T> tClass) {
        super(tClass);
    }

    @Override
    boolean matches(Class<?> tClass) {
        return entityClass.isAssignableFrom(tClass);
    }
}
