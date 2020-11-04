package cn.softeng.events;

/**
 * @author: zhanyeye
 * @date: 11/4/2020 9:13 AM
 * @Description:
 * 事件数据的持有类，用于事件调度器调度未来事件
 * 它是一个链表结构，同时链表头也对应一个红黑树结点
 * 当多个事件的发生时刻和优先级都相同时，这些事件会构成一个链表
 */
final class Event extends BaseEvent {
    /**
     * 该事件对应红黑树节点，可能为null?
     */
    EventNode node;

    /**
     * 链表元素的下一个事件，这个链表中的事件发生时间和优先级都相同
     */
    Event next;

    Event() {}
}
