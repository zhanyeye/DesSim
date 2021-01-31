package cn.softeng.events;

/**
 * @date: 11/3/2020 9:20 PM
 * 事件执行的目标的抽象类，每个event都会对应一个ProcessTarget，
 * 设计模式——命令模式中的命令接口，声明执行的操作
 */
public abstract class ProcessTarget {

    /**
     * 返回该target(命令)捕获的一个线程，该方法只对 WaitTarget 有效
     * @return Process
     */
    Process getProcess() { return null; }

    /**
     * 停止执行该target(命令)
     */
    void kill() {}

    /**
     * 执行target(命令)对应的操作, 即event执行的内容
     */
    public abstract void process();

    /**
     * 返回该target(命令)的描述信息
     * @return String 描述信息
     */
    public abstract String getDescription();

    /**
     * 重写toString方法，方便debug时识别target
     * @return
     */
    @Override
    public String toString() {
        return getDescription();
    }
}