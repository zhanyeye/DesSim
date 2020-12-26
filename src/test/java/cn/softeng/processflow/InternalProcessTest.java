package cn.softeng.processflow;

import cn.softeng.basicsim.InitModelTarget;
import cn.softeng.events.EventManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @date: 12/23/2020 10:48 AM
 */
@Slf4j
public class InternalProcessTest {
    /**
     * 测试EntityGengrator产生试题是否符合预期
     * @throws InterruptedException
     */
    @Test
    public  void test_EntityGeneratorFunc_noError() throws InterruptedException {
        // 创建事件管理器
        EventManager evt = new EventManager("DefaultEventManager");

        // 创建实体，并完成用户输入
        EntityGenerator entityGenerator = new EntityGenerator();
        entityGenerator.setName("EntityGenerator1");

        SimEntity simEntity = new SimEntity();
        simEntity.setName("defaultEntity");
//
//        entityGenerator.getInput("FirstArrivalTime").updateValue(0L);
//        entityGenerator.getInput("InterArrivalTime").updateValue(5L);
//        entityGenerator.getInput("EntitiesPerArrival").updateValue(2L);
//        entityGenerator.getInput("PrototypeEntity").updateValue(simEntity);

        evt.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
        evt.resume(50);
//        evt.resume(100);

        // Junit本身是不支持普通的多线程测试的，这是因为Junit的底层实现上，是用System.exit退出用例执行的。
        // JVM终止了，在测试线程启动的其他线程自然也无法执行。所以手动睡眠主线程。
        while (true) {
            Thread.sleep(1);
        }
    }

    /**
     * 测试单个Queue和Service时，运行是否正常
     * 即： EntityGenerator(实体产生间隔1s，每次产生一个实体) -> Queue1 -> Server1(服务时间2s) -> EntitySink
     * 仿真运行的截止时间是25，waitInQueue: 12, inServer: 1, inSink: 12
     * @throws InterruptedException
     */
    @Test
    public void test_singleQueueAndService_noError() throws InterruptedException {
        // 创建事件管理器
        EventManager evt = new EventManager("DefaultEventManager");


        // 创建实体，并完成用户输入
        EntityGenerator entityGenerator = new EntityGenerator();
        entityGenerator.setName("EntityGenerator");

        SimEntity simEntity = new SimEntity();
        simEntity.setName("DefaultEntity");

        Queue queue = new Queue();
        queue.setName("Queue1");

        Server server = new Server();
        server.setName("Server1");

        EntitySink entitySink = new EntitySink();
        entitySink.setName("EntitySink");

//        entityGenerator.getInput("FirstArrivalTime").updateValue(0L);
//        entityGenerator.getInput("InterArrivalTime").updateValue(1L);
//        entityGenerator.getInput("EntitiesPerArrival").updateValue(1L);
//        entityGenerator.getInput("PrototypeEntity").updateValue(simEntity);
//        entityGenerator.getInput("NextComponent").updateValue(queue);
//
//        server.getInput("NextComponent").updateValue(entitySink);
//        server.getInput("ServiceTime").updateValue(2L);
//        server.getInput("WaitQueue").updateValue(queue);

        // 初始化实体
        evt.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
        // 运行到25时刻停止
        evt.resume(25);

        // Junit本身是不支持普通的多线程测试的，这是因为Junit的底层实现上，是用System.exit退出用例执行的。
        // JVM终止了，在测试线程启动的其他线程自然也无法执行。所以手动睡眠主线程。
        while (true) {
            Thread.sleep(1);
        }
    }

    /**
     * 测试2个Queue和Service时，运行是否正常
     * 即： EntityGenerator(实体产生间隔1s，每次产生一个实体) -> Queue1 -> Server1(服务时间2s) -> Queue2 -> Server2(服务时间4s) -> EntitySink
     * 仿真运行的截止时间是25，waitInQueue1: 12, inServer1: 1, waitInQueue2: 6, inServer2: 1, inSink: 5
     * @throws InterruptedException
     */
    @Test
    public void test_twoQueueAndServer_noError() throws InterruptedException {
        // 创建事件管理器
        EventManager evt = new EventManager("DefaultEventManager");

        // 创建实体，并完成用户输入
        EntityGenerator entityGenerator = new EntityGenerator();
        entityGenerator.setName("EntityGenerator");

        SimEntity simEntity = new SimEntity();
        simEntity.setName("DefaultEntity");

        Queue queue1 = new Queue();
        queue1.setName("Queue1");
        Queue queue2 = new Queue();
        queue2.setName("Queue2");

        Server server1 = new Server();
        server1.setName("Server1");
        Server server2 = new Server();
        server2.setName("Server2");

        EntitySink entitySink = new EntitySink();
        entitySink.setName("EntitySink");

//        entityGenerator.getInput("FirstArrivalTime").updateValue(0L);
//        entityGenerator.getInput("InterArrivalTime").updateValue(1L);
//        entityGenerator.getInput("EntitiesPerArrival").updateValue(1L);
//        entityGenerator.getInput("PrototypeEntity").updateValue(simEntity);
//        entityGenerator.getInput("NextComponent").updateValue(queue1);
//
//        server1.getInput("WaitQueue").updateValue(queue1);
//        server1.getInput("ServiceTime").updateValue(2L);
//        server1.getInput("NextComponent").updateValue(queue2);
//
//        server2.getInput("WaitQueue").updateValue(queue2);
//        server2.getInput("ServiceTime").updateValue(4L);
//        server2.getInput("NextComponent").updateValue(entitySink);

        // 初始化实体
        evt.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
        // 运行到25时刻停止
        evt.resume(25);

        // Junit本身是不支持普通的多线程测试的，这是因为Junit的底层实现上，是用System.exit退出用例执行的。
        // JVM终止了，在测试线程启动的其他线程自然也无法执行。所以手动睡眠主线程。
        while (true) {
            Thread.sleep(1);
        }

    }

}
