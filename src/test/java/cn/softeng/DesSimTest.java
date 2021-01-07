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
    public void testGeneratorScheduling() {

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
        generator.setInterArrivalTime(5);
        generator.setPrototypeEntity(simEntity);

        server1.setWaitQueue(queue1);
        server1.setServiceTime(5);
        server1.setNextComponent(queue2);

        server2.setWaitQueue(queue2);
        server2.setServiceTime(5);
        server2.setNextComponent(sink);

        // ********************************
        // 运行模型
        // ********************************

        // 初始化模型
        DesSim.initModel(DesSim.Type.Generator);
        // 仿真时钟推进到 0时刻
        DesSim.resume(0);
        // 仿真时钟推进到 50时刻
        DesSim.resume(50);

        // 事件队列中是否有事件
        log.debug("hasEvent:{}", DesSim.hasEvent());
        // 事件队列中最近事件的时间
        log.debug("minEventTime:{}", DesSim.minEventTime());

        // *******************************
        // 获取统计数据
        // *******************************

        log.debug("{}", DesSim.getEntity("Server1").getClass());
        log.debug("{}", DesSim.getTimePointList().toString());
        log.debug("{}", DesSim.getDataList("Server1", DesSim.NumberAdded).toString());
        log.debug("{}", DesSim.getDataList("Server1", DesSim.NumberProcessed).toString());
        log.debug("{}", DesSim.getDataList("Server1", DesSim.NumberInProgress).toString());

    }

    @Test
    public void testLauncherScheduling() {

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

        DesSim.initModel(DesSim.Type.Launcher);

        // 事件队列中是否有事件
        log.debug("hasEvent:{}", DesSim.hasEvent());
        // 下一个事件的发生时间
        log.debug("nextEventTime:{}", DesSim.minEventTime());

        DesSim.inject(1, 1);

        // 事件队列中是否有事件
        log.debug("hasEvent:{}", DesSim.hasEvent());
        // 下一个事件的发生时间
        log.debug("nextEventTime:{}", DesSim.minEventTime());

        // 仿真时钟推进到 5时刻
        DesSim.resume(5);
        // 仿真时钟推进到 7时刻
        DesSim.resume(7);

        log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");
        log.debug("{}", DesSim.minEventTime());

        DesSim.inject(7,1);

        // 仿真时钟推进到 10时刻
        DesSim.resume(10);
        // 仿真时钟推进到 15时刻
        DesSim.resume(15);

        DesSim.inject(15, 1);
        // 仿真时钟推进到 50时刻
        DesSim.resume(30);

        log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");

        // *******************************
        // 获取统计数据
        // *******************************

        // 输出时钟序列
        log.debug("Server:");
        log.debug("{}", DesSim.getTimePointList().toString());
        log.debug("{}",server1.getNumAddList().toString());
        log.debug("{}",server1.getNumProcessedList().toString());
        log.debug("{}", server1.getNumInProgressList().toString());

    }

}
