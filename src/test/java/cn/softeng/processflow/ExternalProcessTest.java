package cn.softeng.processflow;

import cn.softeng.DesSim;
import cn.softeng.basicsim.InitModelTarget;
import cn.softeng.events.EventManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @date: 12/25/2020 10:50 AM
 */
@Slf4j
public class ExternalProcessTest {

    /**
     * 测试EntityExternalGengrator产生试题是否符合预期
     * @throws InterruptedException
     */
    @Test
    public void test_EntityExternalGeneratorFunc_noError() throws InterruptedException {
        // 创建事件管理器
        EventManager evt = new EventManager("DefaultEventManager");


        SimEntity simEntity = new SimEntity();
        simEntity.setName("defaultEntity");


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


        EntityLauncher launcher = new EntityLauncher();
        launcher.setName("launcher");
        launcher.setNextComponent(queue1);

        server1.setWaitQueue(queue1);
        server1.setServiceTime(2);
        server1.setNextComponent(queue2);

        server2.setWaitQueue(queue2);
        server2.setServiceTime(3);
        server2.setNextComponent(entitySink);


        evt.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
        launcher.setEntitiesPerArrival(100);
        launcher.setScheduleTime(0);
        launcher.setPrototypeEntity(simEntity);

        evt.resume(100);

        evt.resume(1000);

    }
}
