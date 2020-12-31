package cn.softeng.events;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


/**
 * EventManager测试类，主要测试事件调度机制是否正常
 * @date: 12/14/2020 4:08 PM
 */
@Slf4j
public class EventManagerTest {

    /**
     * 测试仅从外部添加事件（即执行事件时不会触发新的事件），调度运行是否正常
     * @throws InterruptedException
     */
    @Test
    public void test_externalSchedule_noError() throws InterruptedException {
        EventManager evt = new EventManager("DefaultEventManager");
        log.debug("[----start----]");
        for (int i = 0; i < 100; i++) {
            evt.scheduleProcessExternal(i, 0, false, new ExternalTestTarget(i), null);
        }
        evt.resume(1000);

        // Junit本身是不支持普通的多线程测试的，这是因为Junit的底层实现上，是用System.exit退出用例执行的。
        // JVM终止了，在测试线程启动的其他线程自然也无法执行。所以手动睡眠主线程。
//        while (true) {
//            Thread.sleep(1);
//        }
    }

    /**
     * 用于测试外部调度任务的target (事件队列中的事件仅由外部添加)
     */
    private static class ExternalTestTarget extends ProcessTarget {
        final int num;
        ExternalTestTarget(int num) {
            this.num = num;
        }

        @Override
        public void process() {
            log.debug("externalTestTarget: " + num + " start run");
            log.debug("currentTick is : " + Process.current().evt().getTicks());
        }

        @Override
        public String getDescription() {
            return "externalTest" + num;
        }
    }

    /**
     * 测试事件自身会产生新的事件时，运行是否正常
     * @throws InterruptedException
     */
    @Test
    public void test_internalSchedule_noError() throws InterruptedException {
        EventManager evt = new EventManager("DefalutEventManager");
        evt.scheduleProcessExternal(0, 0, false, new InternalTestTarget("External"), null);
        evt.resume(1000);

        // Junit本身是不支持普通的多线程测试的，这是因为Junit的底层实现上，是用System.exit退出用例执行的。
        // JVM终止了，在测试线程启动的其他线程自然也无法执行。所以手动睡眠主线程。
//        while (true) {
//            Thread.sleep(1);
//        }
    }

    /**
     * 用于测试内部调度的target (事件自身在执行过程中可能会产生新的事件)
     */
    private static class InternalTestTarget extends ProcessTarget {
        final String name;
        InternalTestTarget(String name) {
            this.name = name;
        }

        @Override
        public void process() {
           log.info("internalTestTarget" + name + "start run");
           log.info("currentTick is " + Process.current().evt().getTicks());
           EventManager.scheduleTicks(2, 0, false, new InternalTestTarget("createbyinternal"), null);
        }

        @Override
        public String getDescription() {
            return "internalTest " + name;
        }
    }


}
