package cn.softeng;

import cn.softeng.basicsim.Entity;
import cn.softeng.basicsim.InitModelTarget;
import cn.softeng.events.EventManager;
import cn.softeng.processflow.*;
import cn.softeng.processflow.Queue;
import lombok.extern.slf4j.Slf4j;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * DES 对外调度接口
 */
@Slf4j
public class DesSim {

    /**
     * 负责调度DES的事件管理器
     */
    private static EventManager eventManager = new EventManager("DesSim");

    /**
     * 选择的仿真模式
     */
    private static Type desType;

    /**
     * 添加到组件的实体数量
     */
    public static final String NumberAdded = "NumberAdded";
    /**
     * 已经处理完毕的实体数量
     */
    public static final String NumberProcessed = "NumberProcessed";
    /**
     * 正在处理中的实体数量
     */
    public static final String NumberInProgress = "NumberInProgress";

    /**
     * 初始化模型，确认DES类型
     * @param type DES类型: (包括：水平，垂直，单机)
     */
    public static void initModel(Type type) {
        desType = type;
        // 清空时间管理的状态
        eventManager.clear();
        // 初始化已创建的模型组件
        eventManager.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
    }

    /**
     * 水平模式下：注入后会立即执行
     * 垂直模式下：出入后不会立即执行，需要手动触发
     * @param scheduleTime
     * @param num
     */
    public static void inject(int scheduleTime, int num) {
        if (desType == Type.HORIZONTAL) {
            serialScheduling(scheduleTime, num);
        } else if (desType == Type.VERTICAL) {
            parallelScheduling(scheduleTime, num);
        } else if (desType == Type.STANDALONE) {
           log.info("单机模式不支持 inject 操作 ！！！");
        }
    }

    /**
     * DES 被串行调度
     * @param scheduleTime 注入时间
     * @param num 注入个数
     */
    public static void serialScheduling(int scheduleTime, int num) {
        // 找到启动器实体，调用其scheduleOneAction方法，添加一个启动事件
        for (Entity entity : Entity.getAll()) {
            if (entity.getClass() == EntityLauncher.class) {
                EntityLauncher launcher = (EntityLauncher) entity;
                launcher.scheduleOneAction(eventManager, scheduleTime, num);
                break;
            }
        }
        // 调度器启动
        eventManager.resume(Long.MAX_VALUE);
        while (eventManager.isRunning()) {
            try { Thread.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
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
    public static void resume(long time) {
        eventManager.resume(time);
        while (eventManager.isRunning()) {
            try { Thread.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    /**
     * 执行事件直到指定时刻
     * @param time
     */
    public static void doEvent(long time) {
        resume(time);
    }

    /**
     * 获取模型的时钟序列
     * @return
     */
    public static List<Long> getTimePointList() {
        return new Vector<>(eventManager.getTimePointSet());
    }

    /**
     * 通id获取对应的组件
     * @param id
     * @return
     */
    public static LinkedComponent getEntity(int id) {
        return getEntity(String.valueOf(id));
    }

    /**
     * 更具实体表示获取实体
     * @param identifier 实体表示
     * @return
     */
    public static LinkedComponent getEntity(String identifier) {
        Entity ret = Entity.getNamedEntity(identifier);
        return (LinkedComponent) ret;
    }

    /**
     * 事件队列中即将执行的事件的时间
     * @return
     */
    public static int nextEventTime() {
        return (int) eventManager.getNextTick();
    }

    /**
     * DES系统当前仿真时间
     * @return
     */
    public static long currentTime() {
        return eventManager.getTicks();
    }

    /**
     * 事件队列中是否有事件
     * @return
     */
    public static boolean hasEvent() {
        return eventManager.hasEvent();
    }

    /**
     * 获取指定组件的特定属性
     * @param identifier 组件的标识符
     * @param attr 属性
     * @return
     */
    public static long getCurentData(int identifier, String attr) {
        LinkedComponent linkedComponent =  getEntity(identifier);
        if (attr == NumberAdded) {
            return linkedComponent.getTotalNumberAdded();
        } else if (attr == NumberInProgress) {
            return linkedComponent.getNumberInProgress();
        } else if (attr == NumberProcessed) {
            return linkedComponent.getTotalNumberProcessed();
        }
        throw new InvalidParameterException("attr 不存在");
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

    public static void main(String[] args) {
        System.out.println("Hello ! this is DesSim");
    }

}
