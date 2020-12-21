package cn.softeng.basicobject;

import java.util.ArrayList;

/**
 * @date: 12/17/2020 10:00 AM
 */
public class LinkedService extends LinkedComponent implements QueueUser {

    @Override
    public ArrayList<Queue> getQueues() {
        return null;
    }

    @Override
    public void queueChanged() {

    }
}
