package cn.softeng.events;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


/**
 * EventManager测试类，主要测试事件调度机制是否正常
 * @date: 12/14/2020 4:08 PM
 */
@Slf4j
public class EventManagerTest {

    private static class TestTarget extends ProcessTarget {
        final int num;

        TestTarget(int num) {
            this.num = num;
        }

        @Override
        public void process() {
            log.debug("testTarget: " + num + " start run");
            int currentTick = (int) Process.current().evt().getTicks();
            log.debug("currentTick is : " + Process.current().evt().getTicks());
        }

        @Override
        public String getDescription() {
            return "Test" + num;
        }
    }

    @Test
    public void test_schedule_void() throws InterruptedException {
        EventManager evt = new EventManager("DefaultEventManager");
        log.debug("----start----");
        for (int i = 0; i < 1000; i++) {
            evt.scheduleProcessExternal(i, 0, false, new TestTarget(i), null);
        }
        evt.resume(1000);
        Thread.sleep(1000000);
    }

}
