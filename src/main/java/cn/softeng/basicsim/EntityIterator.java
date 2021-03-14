package cn.softeng.basicsim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 实体迭代器的抽象类
 * @date: 12/17/2020 2:09 PM
 */
public abstract class EntityIterator<T extends Entity> implements Iterable<T>, Iterator<T> {
    /**
     * Entity的所有实例，即将被遍历的实例集合
     */
    private final ArrayList<? extends Entity> allInstances = Entity.getAll();
    /**
     * 用于指定需要遍历目标实体的类型
     */
    protected final Class<T> entityClass;
    /**
     * 游标当前位置指针
     */
    private int curPos;
    /**
     * 游标下一个遍历位置的指针
     */
    private int nextPos;

    public EntityIterator(Class<T> tClass) {
        entityClass = tClass;
        curPos = -1;
        nextPos = -1;
    }

    /**
     * 用于迭代器指针向后遍历时，匹配到需要的实体
     * @param tClass 当前遍历的实体类型
     * @return 若匹配条件为真，返回true
     */
    abstract boolean matches(Class<?> tClass);

    /**
     * 更新游标下一个要遍历的位置
     */
    private void updatePos() {
        // 若 nextPos 已到达边界则return
        if (nextPos >= allInstances.size()) {
            return;
        }
        // 向后移动 nextPos 直到遇到类型匹配的实体
        while (++nextPos < allInstances.size()) {
            if (matches(allInstances.get(nextPos).getClass())) {
                break;
            }
        }
    }

    /**
     * 返回迭代器, 实现Iterable<T>接口，使该对象支持 for-each loop
     * @return Iterator<T>
     */
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        if (curPos == nextPos) {
            updatePos();
        }
        return nextPos < allInstances.size();
    }

    @Override
    public T next() {
        if (curPos == nextPos) {
            updatePos();
        }

        if (nextPos < allInstances.size()) {
            curPos = nextPos;
            return entityClass.cast(allInstances.get(curPos));
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
