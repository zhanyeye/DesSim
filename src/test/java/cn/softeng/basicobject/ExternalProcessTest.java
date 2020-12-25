package cn.softeng.basicobject;

import cn.softeng.basicsim.InitModelTarget;
import cn.softeng.events.EventManager;
import org.junit.Test;

/**
 * @date: 12/25/2020 10:50 AM
 */
public class ExternalProcessTest {
    /**
     * 测试EntityExternalGengrator产生试题是否符合预期
     * @throws InterruptedException
     */
    @Test
    public  void test_EntityExternalGeneratorFunc_noError() throws InterruptedException {
        // 创建事件管理器
        EventManager evt = new EventManager("DefaultEventManager");

        SimEntity simEntity = new SimEntity();
        simEntity.setName("defaultEntity");

        // 创建实体，并完成用户输入
        EntityExternalGeneration externalGeneration = new EntityExternalGeneration(100, 1, simEntity);
        externalGeneration.setName("EntityExternalGenerator");

        Queue queue = new Queue();
        queue.setName("Queue1");

        Server server = new Server();
        server.setName("Server1");

        EntitySink entitySink = new EntitySink();
        entitySink.setName("EntitySink");


        externalGeneration.getInput("NextComponent").updateValue(queue);

        server.getInput("NextComponent").updateValue(entitySink);
        server.getInput("ServiceTime").updateValue(50L);
        server.getInput("WaitQueue").updateValue(queue);

        evt.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
        evt.resume(200);

        // Junit本身是不支持普通的多线程测试的，这是因为Junit的底层实现上，是用System.exit退出用例执行的。
        // JVM终止了，在测试线程启动的其他线程自然也无法执行。所以手动睡眠主线程。
        while (true) {
            Thread.sleep(1);
        }
    }
}
