package cn.softeng.events;

/**
 * @date: 11/3/2020 8:53 PM
 * An EventHandle provides a means to remember a future scheduled event in order
 * to manage it's execution.  Examples of this control would be killing the event
 * or executing it earlier than otherwise scheduled.
 */
public class EventHandle {
    BaseEvent event = null;

    public EventHandle() {}

    /**
     * 如果handle追踪了一个未来事件，返回true
     * @return boolean
     */
    public final boolean isScheduled(){
        return event != null;
    }

}