package cn.softeng.basicobject;

import cn.softeng.basicsim.Entity;
import cn.softeng.events.EventManager;
import cn.softeng.input.Input;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @date: 12/23/2020 10:48 AM
 */
@Slf4j
public class EntityGeneratorTest {
    @Test
    public  void test_EntityGeneratorFunc_noError() {
        // 创建事件管理器
        EventManager evt = new EventManager("DefaultEventManager");

        // 创建实体，并完成用户输入
        EntityGenerator entityGenerator = new EntityGenerator();
        entityGenerator.setName("EntityGenerator1");

        long t = (long) entityGenerator.getInput("FirstArrivalTime").getValue();

//        entityGenerator.getInput("FirstArrivalTime");







        // ***************************************
        // 初始化实体
        // ***************************************

        //

//        evt.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
//        evt.resume(evt.secondsToNearestTick(Simulation.getPauseTime()));


        log.debug("allInstances size : " + Entity.getAll().size());
        entityGenerator.earlyInit();

    }




}
