package cn.softeng.basicsim;

/**
 * 迭代指定类型entityClass的子类对象
 * @date: 12/17/2020 2:53 PM
 */
public class ClonesOfIterable<T extends Entity> extends EntityIterator<T> {
    public ClonesOfIterable(Class<T> tClass) {
        super(tClass);
    }

    @Override
    boolean matches(Class<?> tClass) {
        // 若参数中的类型能够被赋值到指定类型entityClass则符合要求
        return entityClass.isAssignableFrom(tClass);
    }
}
