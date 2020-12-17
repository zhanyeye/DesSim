package cn.softeng.basicsim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 实体迭代器的抽象类
 * @date: 12/17/2020 2:09 PM
 */
public abstract class EntityIterator<T extends Entity> implements Iterable<T>, Iterator<T> {
    private final ArrayList<? extends Entity> allInstances = Entity.getAll();
    protected final Class<T> entityClass;
    private int curPos;
    private int nextPos;

    public EntityIterator(Class<T> tClass) {
        entityClass = tClass;
        curPos = -1;
        nextPos = -1;
    }

    /**
     * 用于迭代器指针向后遍历时，匹配到需要的实体
     * @param tClass
     * @return
     */
    abstract boolean matches(Class<?> tClass);

    private void updatePos() {
        if (nextPos >= allInstances.size()) {
            return;
        }
        while (++nextPos < allInstances.size()) {
            if (matches(allInstances.get(nextPos).getClass())) {
                break;
            }
        }
    }

    /**
     * 返回迭代器
     * @return
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
