package cn.softeng;

import cn.softeng.processflow.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


import java.util.Map;

/**
 * Unit test for simple DesSim.
 */
@Slf4j
public class DesSimTest {


    @Test
    public void testEntityLauncherScheduling() throws IllegalAccessException, InstantiationException {

        // ***********************************************
        // 定义模型, 同时设置标识符，(先定义出所有组件，在给组件赋值)
        // ***********************************************
        EntityLauncher launcher = new EntityLauncher(1);
        Queue queue1 = new Queue(2);
        Server server1 = new Server(3);
        Queue queue2 = new Queue(4);
        Server server2 = new Server(5);
        EntitySink sink = new EntitySink(6);

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

        DesSim.initModel(DesSim.Type.Launcher);

        DesSim.inject(0, 1);

        DesSim.doEvent(0);
        DesSim.doEvent(7);

        log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");
        log.debug("{}", DesSim.minEventTime());

        DesSim.inject(7,1);

        DesSim.doEvent(10);
        DesSim.doEvent(15);

        DesSim.inject(15, 1);

        DesSim.doEvent(50);

        log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");

        // *******************************
        // 获取数据
        // *******************************

        // 输出时钟序列
        log.debug("{} - {}", DesSim.getEntity(3).getClass().toString(), DesSim.getEntity(3).getName());
        log.debug("{}", DesSim.getDesCLockList().toString());
        log.debug("{}", DesSim.getDataList(3, DesSim.NumberAdded).toString());
        log.debug("{}", DesSim.getDataList(3, DesSim.NumberInProgress).toString());
        log.debug("{}", DesSim.getDataList(3, DesSim.NumberProcessed).toString());

        // 输出时钟序列
        log.debug("{} - {}", DesSim.getEntity(5).getClass().toString(), DesSim.getEntity(5).getName());
        log.debug("{}", DesSim.getDesCLockList().toString());
        log.debug("{}", DesSim.getDataList(5, DesSim.NumberAdded).toString());
        log.debug("{}", DesSim.getDataList(5, DesSim.NumberInProgress).toString());
        log.debug("{}", DesSim.getDataList(5, DesSim.NumberProcessed).toString());


    }

    @Test
    public void testEntityGeneratorScheduling() {
        // ************************************************
        // 定义模型, 同时设置标识符，(先定义出所有组件，在给组件赋值)
        // ************************************************
        EntityGenerator generator = new EntityGenerator(1);
        Queue queue1 = new Queue(2);
        Server server1 = new Server(3);
        Queue queue2 = new Queue(4);
        Server server2 = new Server(5);
        EntitySink sink = new EntitySink(6);


        // ******************************
        // 为模型属性赋值
        // ******************************
        generator.setNextComponent(queue1);
        generator.setEntitiesPerArrival(1);
        generator.setFirstArrivalTime(0);
        generator.setInterArrivalTime(5);


        server1.setWaitQueue(queue1);
        server1.setServiceTime(5);
        server1.setNextComponent(queue2);

        server2.setWaitQueue(queue2);
        server2.setServiceTime(5);
        server2.setNextComponent(sink);

        // ********************************
        // 运行模型
        // ********************************
        DesSim.initModel(DesSim.Type.Generator);

        DesSim.resume(50);
        log.debug("hasEvent:{}", DesSim.hasEvent());
        log.debug("minEventTime:{}", DesSim.minEventTime());

        // *******************************
        // 获取数据
        // *******************************

        // 输出时钟序列
        // 输出时钟序列
        log.debug("{}", DesSim.getEntity(3).getClass());
        log.debug("{}", DesSim.getDesCLockList().toString());
        log.debug("{}", DesSim.getDataList(3, DesSim.NumberAdded).toString());
        log.debug("{}", DesSim.getDataList(3, DesSim.NumberProcessed).toString());
        log.debug("{}", DesSim.getDataList(3, DesSim.NumberInProgress).toString());
    }

}
