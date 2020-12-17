package cn.softeng.events;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @date: 12/14/2020 4:08 PM
 */
public class EventManagerTest {
    @Test
    public void test_func_null() {
        EventManager evt = new EventManager("DefaultEventManager");


    }


    @Test
    public void testScheduleTime() {
        EventManager evt = new EventManager("TestScheduleTimeEVT");
        evt.clear();

        ArrayList<String> log = new ArrayList<>();
        evt.scheduleProcessExternal(0, 0, false, new LogTarget(0, log), null);
        evt.scheduleProcessExternal(1, 0, false, new LogTarget(1, log), null);
        evt.scheduleProcessExternal(2, 0, false, new LogTarget(2, log), null);
        evt.scheduleProcessExternal(3, 0, false, new LogTarget(3, log), null);
        evt.scheduleProcessExternal(4, 0, false, new LogTarget(4, log), null);

        TestFrameworkHelpers.runEventsToTick(evt, 100, 1000);

        ArrayList<String> expected = new ArrayList<>();
        expected.add("Target:0");
        expected.add("Target:1");
        expected.add("Target:2");
        expected.add("Target:3");
        expected.add("Target:4");

        assertTrue(expected.size() == log.size());
        for (int i = 0; i < expected.size(); i++) {
            assertTrue(expected.get(i).equals(log.get(i)));
        }
    }

    private static class LogTarget extends ProcessTarget {
        final ArrayList<String> log;
        final int num;
        LogTarget(int i, ArrayList<String> l) {
            log = l;
            num = i;
        }

        @Override
        public String getDescription() {
            return "Target:" + num;
        }

        @Override
        public void process() {
            log.add("Target:" + num);
        }
    }

}
