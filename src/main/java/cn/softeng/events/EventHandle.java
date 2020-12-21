package cn.softeng.events;

/**
 * @date: 11/3/2020 8:53 PM
 * An EventHandle provides a means to remember a future scheduled event in order
 * to manage it's execution.  Examples of this control would be killing the event
 * or executing it earlier than otherwise scheduled.
 * 事件句柄：仿真运行时模型层会产生对应的event，加入到事件队列中。为了更好的调度这些event（比如要删除
 * 或修改调度时间），所以需要持有一个该事件的引用，方便在事件队列中找到对应的事件
 * 一般用法：比如 LinkedService 组件会有一个endActionHandle, 用于持有endAction的event
 * EventHandle具体的event装配工作是在 EventManager.scheduleTicks()中进行的
 */
public class EventHandle {
    BaseEvent event = null;

    public EventHandle() {}

    /**
     * 如果该handle已开始跟踪某个事件，返回true
     * 表明该事件已经被加入到事件队列中
     * @return boolean
     */
    public final boolean isScheduled(){
        return event != null;
    }

}