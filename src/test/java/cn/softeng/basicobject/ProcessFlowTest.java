package cn.softeng.basicobject;

import cn.softeng.basicsim.Entity;
import cn.softeng.basicsim.InitModelTarget;
import cn.softeng.basicsim.StartUpTarget;
import cn.softeng.events.EventManager;
import cn.softeng.input.Input;
import cn.softeng.input.ValueInput;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @date: 12/23/2020 10:48 AM
 */
@Slf4j
public class ProcessFlowTest {
    @Test
    public  void test_EntityGeneratorFunc_noError() throws InterruptedException {
        // 创建事件管理器
        EventManager evt = new EventManager("DefaultEventManager");

        // 创建实体，并完成用户输入
        EntityGenerator entityGenerator = new EntityGenerator();
        entityGenerator.setName("EntityGenerator1");

        SimEntity simEntity = new SimEntity();
        simEntity.setName("defaultEntity");

        entityGenerator.getInput("FirstArrivalTime").updateValue(0L);
        entityGenerator.getInput("InterArrivalTime").updateValue(5L);
        entityGenerator.getInput("EntitiesPerArrival").updateValue(2L);
        entityGenerator.getInput("PrototypeEntity").updateValue(simEntity);

        evt.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
        evt.resume(100);

        // Junit本身是不支持普通的多线程测试的，这是因为Junit的底层实现上，是用System.exit退出用例执行的。
        // JVM终止了，在测试线程启动的其他线程自然也无法执行。所以手动睡眠主线程。
        while (true) {
            Thread.sleep(1);
        }
    }

    @Test
    public void test_Queue_noError() throws InterruptedException {
        // 创建事件管理器
        EventManager evt = new EventManager("DefaultEventManager");

        // 创建实体，并完成用户输入
        EntityGenerator entityGenerator = new EntityGenerator();
        entityGenerator.setName("EntityGenerator");

        SimEntity simEntity = new SimEntity();
        simEntity.setName("DefaultEntity");

        Queue queue = new Queue();
        queue.setName("Queue");

        Server server = new Server();
        server.setName("Server");

        EntitySink entitySink = new EntitySink();
        entitySink.setName("EntitySink");

        entityGenerator.getInput("FirstArrivalTime").updateValue(0L);
        entityGenerator.getInput("InterArrivalTime").updateValue(1L);
        entityGenerator.getInput("EntitiesPerArrival").updateValue(1L);
        entityGenerator.getInput("PrototypeEntity").updateValue(simEntity);
        entityGenerator.getInput("NextComponent").updateValue(queue);

        server.getInput("NextComponent").updateValue(entitySink);
        server.getInput("ServiceTime").updateValue(2L);
        server.getInput("WaitQueue").updateValue(queue);

        evt.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
        evt.resume(50);

        // Junit本身是不支持普通的多线程测试的，这是因为Junit的底层实现上，是用System.exit退出用例执行的。
        // JVM终止了，在测试线程启动的其他线程自然也无法执行。所以手动睡眠主线程。
        while (true) {
            Thread.sleep(1);
        }
    }

}
