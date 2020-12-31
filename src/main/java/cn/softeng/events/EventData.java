package cn.softeng.events;

/**
 * @date: 11/20/2020 10:40 AM
 */
public class EventData {

    public final long ticks;
    public final int priority;
    public final String description;

    public EventData(long tk, int pri, String desc) {
        ticks = tk;
        priority = pri;
        description = desc;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof EventData)) {
            return false;
        }
        EventData data = (EventData) obj;
        return (ticks == data.ticks) && (priority == data.priority)
                && description.equals(data.description);
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s]", ticks, priority, description);
    }

}
