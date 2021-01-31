package cn.softeng.basicsim;

/**
 * 迭代指定类型entityClass的子类对象
 * @date: 12/17/2020 2:53 PM
 */
public class ClonesOfIterable<T extends Entity> extends EntityIterator<T> {

    public ClonesOfIterable(Class<T> tClass) {
        super(tClass);
    }

    /**
     * 用于迭代器指针向后遍历时，匹配到需要的实体
     * 只有当entityClass与参数中的类型相同或为其父类，才匹配成功
     * @param tClass 当前遍历的实体类型
     * @return 若匹配条件为真，返回true
     */
    @Override
    boolean matches(Class<?> tClass) {
        // 若参数中的类型能够被赋值到指定类型entityClass则符合要求
        return this.entityClass.isAssignableFrom(tClass);
    }
}
