package cn.softeng;

import cn.softeng.basicsim.Entity;
import cn.softeng.basicsim.InitModelTarget;
import cn.softeng.events.EventManager;
import cn.softeng.processflow.EntityLauncher;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * DES 对外调度接口
 */
@Slf4j
public class DesSim {

    /**
     * 负责调度DES的事件管理器
     */
    private static EventManager eventManager = new EventManager("DesSim");
    private static Type desType;

    /**
     * 初始化模型，确认DES类型
     * @param type DES类型: (包括：水平，垂直，单机)
     */
    public static void initModel(Type type) {
        desType = type;
        if (type == Type.HORIZONTAL) {
            InitModelTarget.vertical = false;
        } else if (type == Type.VERTICAL) {
            InitModelTarget.vertical = true;
        } else {

        }
        eventManager.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
    }

    /**
     * DES 被串行调度
     * @param scheduleTime 注入时间
     * @param num 注入个数
     */
    public static void serialScheduling(int scheduleTime, int num) throws InterruptedException {
        for (Entity entity : Entity.getAll()) {
            if (entity.getClass() == EntityLauncher.class) {
                EntityLauncher launcher = (EntityLauncher) entity;
                launcher.scheduleOneAction(eventManager, scheduleTime, num);
                break;
            }
        }
        eventManager.resume(Long.MAX_VALUE);
        while (eventManager.isRunning()) {
            Thread.sleep(1);
        }
    }

    /**
     * DES被并行的调度，调用时必须确保DES调度正在运行，否则会报错
     * @param scheduleTime 时间调度时间
     * @param num 注入个数
     */
    public static void parallelScheduling(int scheduleTime, int num) {
        for (Entity entity : Entity.getAll()) {
            if (entity.getClass() == EntityLauncher.class) {
                EntityLauncher launcher = (EntityLauncher) entity;
                launcher.scheduleAction(eventManager, scheduleTime, num);
                break;
            }
        }
    }

    /**
     * 执行事件直到指定时刻
     * @param time
     */
    public static void resume(long time) throws InterruptedException {
        eventManager.resume(time);
        while (eventManager.isRunning()) {
            Thread.sleep(1);
        }
    }

    /**
     * 事件队列中即将执行的事件的时间
     * @return
     */
    public static long nextEventTime() {
        return eventManager.getCurrentTick().get();
    }

    /**
     * 事件队列中是否有事件
     * @return
     */
    public static boolean hasEvent() {
        return eventManager.hasEvent();
    }

    /**
     * DES系统当前仿真时间
     * @return
     */
    public long currentSimTime() {
        return eventManager.getTicks();
    }

    /**
     * DES运行类型枚举类
     */
    public enum Type {
        /**
         * DES被串行调度 （所谓水平？）
         */
        HORIZONTAL,
        /**
         * DES被并行调度 （所谓垂直？）
         */
        VERTICAL,
        /**
         * DES 单机运行
         */
        STANDALONE
    }


}
