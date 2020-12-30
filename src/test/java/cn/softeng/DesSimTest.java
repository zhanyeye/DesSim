package cn.softeng;

import cn.softeng.basicsim.Entity;
import cn.softeng.processflow.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import sun.security.krb5.internal.crypto.Des;

import java.util.Map;

/**
 * Unit test for simple DesSim.
 */
@Slf4j
public class DesSimTest
{
    /**
     * 水平调度测试
     */
    @Test
    public void testSerialScheduling() throws InterruptedException, InstantiationException, IllegalAccessException, NoSuchFieldException {

        // *****************************
        // 定义模型, 设置标识符
        // *****************************
        EntityLauncher launcher = DesSim.createModelInstance("EntityLauncher", 1);
        Queue queue = DesSim.createModelInstance("Queue", 2);
        Server server1 = DesSim.createModelInstance("Server", 3);
        Server server2 = DesSim.createModelInstance("Server", 4);
        EntitySink sink = DesSim.createModelInstance("EntitySink", 5);

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
        DesSim.serialScheduling(0, 100);

        // *******************************
        // 获取数据
        // *******************************

        // 输出时钟序列
        log.debug("{}", DesSim.getTimePointList().toString());


        // Junit本身是不支持普通的多线程测试的，这是因为Junit的底层实现上，是用System.exit退出用例执行的。
        // JVM终止了，在测试线程启动的其他线程自然也无法执行。所以手动睡眠主线程。
        while (true) {
            Thread.sleep(1);
        }
    }

    @Test
    public void testParallelScheduling() throws InterruptedException, IllegalAccessException, InstantiationException {
        EntityLauncher launcher = DesSim.createModelInstance("EntityLauncher", 1);
        Queue queue1 = DesSim.createModelInstance("Queue",2);
        Queue queue2 = DesSim.createModelInstance("Queue", 3);
        Server server1 = DesSim.createModelInstance("Server", 4);
        Server server2 = DesSim.createModelInstance("Server", 5);
        EntitySink sink = DesSim.createModelInstance("EntitySink", 6);

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
        DesSim.resume(7);
        log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");

        DesSim.parallelScheduling(7, 1);
        DesSim.resume(10);
        DesSim.resume(100);
        DesSim.parallelScheduling(100, 1);
        DesSim.resume(200);
        log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");


        log.debug("{}", server1.getName());
        log.debug("getNumAddMap");
        for (Map.Entry<Long, Long> entry : server1.getNumAddMap().entrySet()) {
            log.debug("{} - {}", entry.getKey(), entry.getValue());
        }
        log.debug("getNumInProgress");
        for (Map.Entry<Long, Long> entry : server1.getNumInProgress().entrySet()) {
            log.debug("{} - {}", entry.getKey(), entry.getValue());
        }
        log.debug(server1.getName());
        log.debug("getNumProcessed");
        for (Map.Entry<Long, Long> entry : server1.getNumProcessedMap().entrySet()) {
            log.debug("{} - {}", entry.getKey(), entry.getValue());
        }

        // Junit本身是不支持普通的多线程测试的，这是因为Junit的底层实现上，是用System.exit退出用例执行的。
        // JVM终止了，在测试线程启动的其他线程自然也无法执行。所以手动睡眠主线程。
        while (true) {
            Thread.sleep(1);
        }
    }

}
