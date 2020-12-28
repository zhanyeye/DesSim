package cn.softeng.events;

/**
 * @date: 11/3/2020 9:19 PM
 * @Description: 事件抽象类
 */
public class BaseEvent {
    /**
     * 事件处理内容
     */
    ProcessTarget target;
    /**
     * 持有一个该事件的引用，方便在事件队列中找到对应的事件
     */
    EventHandle handle;
}
