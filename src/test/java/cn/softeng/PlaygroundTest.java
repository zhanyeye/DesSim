package cn.softeng;

import cn.softeng.processflow.EntityLauncher;
import cn.softeng.processflow.EntitySink;
import cn.softeng.processflow.Queue;
import cn.softeng.processflow.Server;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.*;

/**
 * @date: 12/29/2020 2:33 PM
 */
@Slf4j
public class PlaygroundTest {


    @Test
    public void testSet() {
        Set<Long> set = new LinkedHashSet<>();
        set.add(9L);
        set.add(8L);
        set.add(7L);
        set.add(6L);
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            log.debug("{}", iterator.next());
        }

    }

    @Test
    public void testLinkedHashMap() {
        Map<Long, Long> map =  new LinkedHashMap<>();
        map.put(1L, 1L);
        map.put(2L, 2L);
        map.put(3L, 3L);
        map.put(4L, 4L);
        map.put(5L, 5L);
        map.put(6L, 6L);
        map.put(7L, 7L);
        map.put(8L, 8L);
        for (Map.Entry<Long, Long> entry : map.entrySet()) {
            log.debug("{}-{}", entry.getKey(), entry.getValue());
        }
    }

    @Test
    public void test() {
        EntityLauncher launcher = new EntityLauncher("launcher");
        cn.softeng.processflow.Queue q1 = new cn.softeng.processflow.Queue();
        cn.softeng.processflow.Queue q2 = new Queue();
        Server s1 = new Server();
        Server s2 = new Server();
        EntitySink sink = new EntitySink();

        launcher.setNextComponent(q1);

        s1.setWaitQueue(q1);
        s1.setServiceTime(5);
        s1.setNextComponent(q2);

        s2.setWaitQueue(q2);
        s2.setServiceTime(5);
        s2.setNextComponent(sink);

        DesSim.initModel(DesSim.Type.VERTICAL);
        DesSim.inject(0, 50);
        DesSim.resume(10);

        System.out.println(DesSim.getTimePointList());
    }
}
