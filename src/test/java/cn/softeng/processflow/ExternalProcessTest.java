package cn.softeng.processflow;

import cn.softeng.DesSim;
import cn.softeng.basicsim.InitModelTarget;
import cn.softeng.events.EventManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import sun.security.krb5.internal.crypto.Des;

/**
 * @date: 12/25/2020 10:50 AM
 */
@Slf4j
public class ExternalProcessTest {


    public void test() {

    }

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

        // 创建实体，并完成用户输入
//        EntityExternalGeneration externalGeneration = new EntityExternalGeneration(0, 100, simEntity);
//        externalGeneration.setName("EntityExternalGenerator");



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
//        launcher.scheduleAction(0, 100);

//
//        externalGeneration.setNextComponent(queue1);
//        externalGeneration.setEntityPerArrival(100);

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
//        launcher.scheduleAction(0, 100);
        evt.resume(100);


        evt.resume(1000);


//        DesSim.initModel();
//        DesSim.singleScheduling();

        // Junit本身是不支持普通的多线程测试的，这是因为Junit的底层实现上，是用System.exit退出用例执行的。
        // JVM终止了，在测试线程启动的其他线程自然也无法执行。所以手动睡眠主线程。
        while (true) {
            Thread.sleep(1);
        }
    }
}
