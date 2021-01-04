package cn.softeng;

import cn.softeng.basicsim.InitModelTarget;
import cn.softeng.processflow.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import sun.security.krb5.internal.crypto.Des;


import java.util.Map;

/**
 * Unit test for simple DesSim.
 */
@Slf4j
public class DesSimTest {
    /**
     * 水平调度测试
     */
    @Test
    public void testSerialScheduling() throws InstantiationException, IllegalAccessException {

        // ************************************************
        // 定义模型, 同时设置标识符，(先定义出所有组件，在给组件赋值)
        // ************************************************
        EntityLauncher launcher = new EntityLauncher(1);
        Queue queue = new Queue(2);
        Server server1 = new Server(3);
        Server server2 = new Server(4);
        EntitySink sink = new EntitySink(5);

        // ******************************
        // 为模型属性赋值
        // ******************************

        // 设置实体启动器的后继
        launcher.setNextComponent(DesSim.getEntity(2));

        // 设置服务的等待队列，服务时间，服务的后继
        server1.setWaitQueue((Queue) DesSim.getEntity(2));
        server1.setServiceTime(2);
        server1.setNextComponent(DesSim.getEntity(5));

        // 设置服务的等待队列，服务时间，服务的后继
        server2.setWaitQueue((Queue) DesSim.getEntity(2));
        server2.setServiceTime(2);
        server2.setNextComponent(DesSim.getEntity(5));

        // ********************************
        // 运行模型
        // ********************************

        // 初始化模型
        DesSim.initModel(DesSim.Type.HORIZONTAL);
        // 开始水平调度
        DesSim.inject(0, 100);

        // *******************************
        // 获取数据
        // *******************************

        // 输出时钟序列
        log.debug("Server:");
        log.debug("{}", DesSim.getTimePointList().toString());
        log.debug("{}",server1.getNumAddList().toString());
        log.debug("{}",server1.getNumProcessList().toString());
        log.debug("{}", server1.getNumInProgressList().toString());

    }

    @Test
    public void testParallelScheduling() throws IllegalAccessException, InstantiationException {

        // ***********************************************
        // 定义模型, 同时设置标识符，(先定义出所有组件，在给组件赋值)
        // ***********************************************
        EntityLauncher launcher = new EntityLauncher("launcher");
        Queue queue1 = new Queue("queue1");
        Queue queue2 = new Queue("queue2");
        Server server1 = new Server("server1");
        Server server2 = new Server("server2");
        EntitySink sink = new EntitySink("sink");

        // ******************************
        // 为模型属性赋值
        // ******************************
        launcher.setNextComponent(queue1);
        server1.setWaitQueue(queue1);
        server1.setServiceTime(5);
        server1.setNextComponent(queue2);
        server2.setWaitQueue(queue2);
        server2.setServiceTime(5);
        server2.setNextComponent(sink);


        // ********************************
        // 运行模型
        // ********************************

        DesSim.initModel(DesSim.Type.VERTICAL);

        DesSim.inject(0, 1);

        DesSim.resume(5);
        DesSim.resume(7);

        log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");

        DesSim.inject(7,1);

        DesSim.resume(10);
        DesSim.resume(15);

        DesSim.inject(15, 1);

        DesSim.resume(100);

        log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");

        // *******************************
        // 获取数据
        // *******************************

        // 输出时钟序列
        log.debug("Server:");
        log.debug("{}", DesSim.getTimePointList().toString());
        log.debug("{}",server1.getNumAddList().toString());
        log.debug("{}",server1.getNumProcessList().toString());
        log.debug("{}", server1.getNumInProgressList().toString());

    }

    @Test
    public void testStandaloneSxheduling() {

        // ************************************************
        // 定义模型, 同时设置标识符，(先定义出所有组件，在给组件赋值)
        // ************************************************
        EntityGenerator generator = new EntityGenerator("EntityGenerator");
        SimEntity simEntity = new SimEntity("DefaultEntity");
        Queue queue1 = new Queue("Queue1");
        Queue queue2 = new Queue("Queue2");
        Server server1 = new Server("Server1");
        Server server2 = new Server("Server2");
        EntitySink sink = new EntitySink("EntitySink");

        // ******************************
        // 为模型属性赋值
        // ******************************
        generator.setNextComponent(queue1);
        generator.setEntitiesPerArrival(1);
        generator.setFirstArrivalTime(0);
        generator.setInterArrivalTime(1);
        generator.setPrototypeEntity(simEntity);

        server1.setWaitQueue(queue1);
        server1.setServiceTime(2);
        server1.setNextComponent(queue2);

        server2.setWaitQueue(queue2);
        server2.setServiceTime(4);
        server2.setNextComponent(sink);

        // ********************************
        // 运行模型
        // ********************************
        DesSim.initModel(DesSim.Type.STANDALONE);
        DesSim.resume(0);
        DesSim.resume(0);
        log.debug("{}", DesSim.hasEvent());
        log.debug("{}", DesSim.nextEventTime());

        // *******************************
        // 获取数据
        // *******************************

        // 输出时钟序列
        log.debug("Server:");
        log.debug("{}", DesSim.getTimePointList().toString());
        log.debug("{}",server1.getNumAddList().toString());
        log.debug("{}",server1.getNumProcessList().toString());
        log.debug("{}", server1.getNumInProgressList().toString());

    }
}
