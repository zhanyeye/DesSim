package cn.softeng;

import cn.softeng.processflow.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
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
        Assign assign = new Assign("assign");
        SimEntity simEntity = new SimEntity("DefaultEntity");
        Queue queue1 = new Queue("Queue1");
        Queue queue2 = new Queue("Queue2");
        Server server1 = new Server("Server1");
        Server server2 = new Server("Server2");
        EntitySink sink = new EntitySink("EntitySink");

        // ******************************
        // 为模型属性赋值
        // ******************************

//        generator.setNextComponent(queue1);
        generator.setNextComponent(assign);
        generator.setEntitiesPerArrival(1);
        generator.setFirstArrivalTime(7);
        generator.setInterArrivalTime(7);
        generator.setPrototypeEntity(simEntity);

        Map<String, Integer> map = new HashMap<>();
        map.put("red", 3);
        map.put("black", 7);

        assign.addAssignment(map);
        assign.setNextComponent(queue1);

        Map<String, Double> map1 = new HashMap<>();
        map1.put("red", 10.0);
        map1.put("black", 2.0);
        server1.setServerTimeChoice(map1);

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
        DesSim.initModel(DesSim.Type.Generator, 0);

        // 事件队列中是否有事件
        log.debug("hasEvent:{}", DesSim.hasEvent());
        // 事件队列中最近事件的时间
        log.debug("minEventTime:{}", DesSim.minEventTime());

        // 仿真时钟推进到 50时刻
        DesSim.resume(16.4588);

        // 事件队列中是否有事件
        log.debug("hasEvent:{}", DesSim.hasEvent());
        // 事件队列中最近事件的时间
        log.debug("minEventTime:{}", DesSim.minEventTime());

        // *******************************
        // 获取统计数据
        // *******************************

        log.debug("{}", DesSim.getEntity("Server1").getClass());
        log.debug("{}", DesSim.getDesClockList().toString());
        log.debug("{}", DesSim.getDataList("Server1", DesSim.NumberAdded).toString());
        log.debug("{}", DesSim.getDataList("Server1", DesSim.NumberProcessed).toString());
        log.debug("{}", DesSim.getDataList("Server1", DesSim.NumberInProgress).toString());

    }

    @Test
    public void testLauncherScheduling() {


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

        log.debug("hasEvent:{}", DesSim.hasEvent());
        // 下一个事件的发生时间
        log.debug("nextEventTime:{}", DesSim.minEventTime());

        DesSim.inject(0, 1);

        log.debug("hasEvent:{}", DesSim.hasEvent());
        // 下一个事件的发生时间
        log.debug("nextEventTime:{}", DesSim.minEventTime());

        // 仿真时钟推进到 5时刻
        DesSim.resume(5);
        // 仿真时钟推进到 7时刻
        DesSim.resume(7);

        log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");
        log.debug("{}", DesSim.minEventTime());

        DesSim.inject(7, 1);

        // 仿真时钟推进到 10时刻
        DesSim.resume(10);
        // 仿真时钟推进到 15时刻
        DesSim.resume(15);

        DesSim.inject(15, 1);
        // 仿真时钟推进到 50时刻
        DesSim.resume(16.4588);

        log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");
        log.debug("{}", DesSim.minEventTime());

        // *******************************
        // 获取统计数据
        // *******************************

        // 输出时钟序列
        log.debug("Server:");
        log.debug("getDesClockList(): {}", DesSim.getDesClockList().toString());
        log.debug("getNumAddList(): {}", server1.getNumAddList().toString());
        log.debug("getNumProcessedList(): {}", server1.getNumProcessedList().toString());
        log.debug("getNumInProgressList(): {}", server1.getNumInProgressList().toString());

    }

}
