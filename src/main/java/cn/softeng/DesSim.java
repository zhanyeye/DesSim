package cn.softeng;

import cn.softeng.basicsim.InitModelTarget;
import cn.softeng.events.EventManager;
import lombok.extern.slf4j.Slf4j;

/**
 * Hello world!
 *
 */
@Slf4j
public class DesSim {

    /**
     * 负责调度DES的事件管理器
     */
    private static EventManager eventManager = new EventManager("DesSim");

    /**
     * 初始化各个组件模块
     */
    public static void initModel() {
        eventManager.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
    }

    /**
     * 单次调度
     */
    public static void singleScheduling() {
        eventManager.resume(Long.MAX_VALUE);
        log.debug("resume");

    }

    public void test() {
//        eventManager.
    }


    public static void main( String[] args )
    {
        System.out.println( "Hello ! This is DesSim SDK" );
    }
}
