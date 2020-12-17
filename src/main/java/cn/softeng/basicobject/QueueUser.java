package cn.softeng.basicobject;

import java.util.ArrayList;

/**
 * @date: 12/17/2020 10:17 AM
 */
public interface QueueUser {
    /**
     * 返回一个Queue列表，列表中的Queue为该对象所使用
     */
    public abstract ArrayList<Queue> getQueues();

    /**
     * 当该对象所使用的Queue中，有新实体加入，调用该方法
     */
    public abstract void queueChanged();
}
