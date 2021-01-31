package cn.softeng.basicsim;

/**
 * 迭代与指定类型entityClass相同的对象
 * @date: 12/17/2020 2:08 PM
 */
public class InstanceIterable<T extends Entity> extends EntityIterator<T> {

    public InstanceIterable(Class<T> tClass) {
        super(tClass);
    }

    /**
     * 用于迭代器指针向后遍历时，匹配到需要的实体
     * 只有当entityClass与参数中的类型相同时，才匹配成功
     * @param tClass 当前遍历的实体类型
     * @return 若匹配条件为真，返回true
     */
    @Override
    boolean matches(Class<?> tClass) {
        return this.entityClass == tClass;
    }
}
