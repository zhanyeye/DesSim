package cn.softeng.events;

/**
 * @author: zhanyeye
 * @date: 11/3/2020 9:20 PM
 * 事件执行的目标的抽象类，每个event都会包含一个 ProcessTarget，
 * 类似与设计模式中命令模式中的命令对象
 */
public abstract class ProcessTarget {

    /**
     * 返回该target捕获的一个线程，该方法只对 WaitTarget 有效
     * @return Process
     */
    Process getProcess() { return null; }

    /**
     * 停止执行
     */
    void kill() {}

    /**
     * 具体一系列行为，即事件执行的内容
     */
    public abstract void process();

    /**
     * 返回该target的描述信息
     * @return String 描述信息
     */
    public abstract String getDescription();

    @Override
    public String toString() {
        return getDescription();
    }
}