package cn.softeng;

import static org.junit.Assert.assertTrue;

import cn.softeng.processflow.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * Unit test for simple DesSim.
 */
@Slf4j
public class DesSimTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws InterruptedException {
        EntityLauncher launcher = new EntityLauncher();
        launcher.setName("launcher");
        Queue queue1 = new Queue();
        queue1.setName("queue1");
        Queue queue2 = new Queue();
        queue2.setName("queue2");
        Server server1 = new Server();
        server1.setName("server1");
        Server server2 = new Server();
        server2.setName("server2");
        EntitySink sink = new EntitySink();
        sink.setName("sink");

        launcher.setNextComponent(queue1);
        server1.setWaitQueue(queue1);
        server1.setServiceTime(2);
        server1.setNextComponent(queue2);
        server2.setWaitQueue(queue2);
        server2.setServiceTime(3);
        server2.setNextComponent(sink);

        DesSim.initModel(DesSim.Type.HORIZONTAL);
        DesSim.serialScheduling(0, 1);


        while (true) {
            Thread.sleep(1);
        }
    }

    @Test
    public void test2() throws InterruptedException {
        EntityLauncher launcher = new EntityLauncher();
        launcher.setName("launcher");
        Queue queue1 = new Queue();
        queue1.setName("queue1");
        Queue queue2 = new Queue();
        queue2.setName("queue2");
        Server server1 = new Server();
        server1.setName("server1");
        Server server2 = new Server();
        server2.setName("server2");
        EntitySink sink = new EntitySink();
        sink.setName("sink");

        launcher.setNextComponent(queue1);
        server1.setWaitQueue(queue1);
        server1.setServiceTime(2);
        server1.setNextComponent(queue2);
        server2.setWaitQueue(queue2);
        server2.setServiceTime(3);
        server2.setNextComponent(sink);

        DesSim.initModel(DesSim.Type.VERTICAL);



        DesSim.parallelScheduling(5, 1);
        DesSim.resume(5);
        Thread.sleep(1000);
        DesSim.resume(7);
        Thread.sleep(1000);
        DesSim.parallelScheduling(7, 1);
        Thread.sleep(1000);
        DesSim.resume(10);
        Thread.sleep(1000);
        DesSim.resume(100);





        while (true) {
            Thread.sleep(1);
        }
    }

}
