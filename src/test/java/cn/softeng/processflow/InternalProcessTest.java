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

        evt.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
        evt.resume(50);

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

        // 初始化实体
        evt.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
        // 运行到25时刻停止
        evt.resume(25);

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

        // 初始化实体
        evt.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
        // 运行到25时刻停止
        evt.resume(25);

    }

}
