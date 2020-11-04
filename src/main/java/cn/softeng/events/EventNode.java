package cn.softeng.events;

/**
 * @author: zhanyeye
 * @date: 11/4/2020 9:15 AM
 * @Description:
 * 一个实现事件优先队列的红黑树结点, 结点指向一个事件链表,链表可能只有一个元素
 *
 */
class EventNode {

    interface Runner {
        void runOnNode(EventNode node);
    }

    /**
     * 事件发生的时间
     */
    long schedTick;

    /**
     * 调度事件的优先级
     */
    int priority;

    /**
     * 链表表头
     */
    Event head;

    /**
     * 链表表尾
     */
    Event tail;

    /**
     * 该结点的颜色
     */
    boolean red;

    /**
     * 左子结点
     */
    EventNode left;

    /**
     * 右子结点
     */
    EventNode right;

    EventNode(long tick, int prio) {
        schedTick = tick;
        priority = prio;
        left = nilNode;
        right = nilNode;
    }

    /**
     * 向该节点的事件链表种添加一个事件
     * @param e 被添加的事件
     * @param fifo 元素插入的方式
     */
    final void addEvent(Event e, boolean fifo) {
        if (head == null) {
            // 若链表为空
            head = e;
            tail = e;
            e.next = null;
            return;
        }

        if (fifo) {
            // 尾插法
            tail.next = e;
            tail = tail.next;
            e.next = null;
        } else {
            // 头插发
            e.next = head;
            head = e;
        }
    }

    /**
     * 从结点链表中移除指定事件
     * @param evt 被移除的事件
     */
    final void removeEvent(Event evt) {
        // quick case where we are the head event
        if (this.head == evt) {
            this.head = evt.next;
            if (evt.next == null) {
                this.tail = null;
            }
        }
        else {
            Event prev = this.head;
            while (prev.next != evt) {
                prev = prev.next;
            }

            prev.next = evt.next;
            if (evt.next == null) {
                this.tail = prev;
            }
        }
    }

    /**
     * 和其他的EventNode 比较大小 (发生时间，优先级)
     * @param other
     * @return
     */
    final int compareToNode(EventNode other) {
        return compare(other.schedTick, other.priority);
    }

    /**
     * 根据事件发生的时间刻度和事件优先级比较大小
     * @param schedTick 时间发生的时间刻度
     * @param priority
     * @return -1 表示小于； 1 表示大于； 0表示等于
     */
    final int compare(long schedTick, int priority) {
        if (this.schedTick < schedTick) {
            return -1;
        } else if (this.schedTick > schedTick) {
            return  1;
        } else if (this.priority < priority) {
            return -1;
        } else if (this.priority > priority) {
            return  1;
        } else {
            return 0;
        }
    }

    /**
     * 红黑树右旋
     * @param parent
     */
    final void rotateRight(EventNode parent) {
        if (parent != null) {
            if (parent.left == this) {
                parent.left = left;
            } else {
                parent.right = left;
            }
        }

        EventNode oldMid = left.right;
        left.right = this;

        this.left = oldMid;
    }


    /**
     * 红黑树左旋
     * @param parent
     */
    final void rotateLeft(EventNode parent) {
        if (parent != null) {
            if (parent.left == this) {
                parent.left = right;
            } else {
                parent.right = right;
            }
        }

        EventNode oldMid = right.left;
        right.left = this;

        this.right = oldMid;
    }


    /**
     * 克隆一个指定结点
     * @param source 被克隆的结点
     */
    final void cloneFrom(EventNode source) {
        this.head = source.head;
        this.tail = source.tail;
        this.schedTick = source.schedTick;
        this.priority = source.priority;
        Event next = this.head;
        while (next != null) {
            next.node = this;
            next = next.next;
        }
    }

    /**
     * 红黑树的空结点
     */
    static final EventNode nilNode;

    static {
        nilNode = new EventNode(0, 0);
        nilNode.left = null;
        nilNode.right = null;
        nilNode.red = false;
    }
}