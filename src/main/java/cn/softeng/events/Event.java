package cn.softeng.events;

/**
 * @date: 11/4/2020 9:13 AM
 * Holder class for event data used by the event monitor to schedule future events.
 * Event是一个链表结构，当Event发生的tick和priority相等时，他们位于同一个链表中
 * Event中node字段对应一个红黑树结点，只有链表的头元素需要持有一个红黑树结点的引用
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
