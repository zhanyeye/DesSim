package cn.softeng.events;

/**
 * @date: 11/3/2020 8:53 PM
 * @Description: 用于记录一个即将调度的未来事件
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