package cn.softeng.events;

/**
 * @author: zhanyeye
 * @date: 11/3/2020 9:20 PM
 * @Description: 处理的内容，每个event都会包含一个 ProcessTarget
 */
public abstract class ProcessTarget {

    /**
     * 获取该处理内容的描述
     * @return String
     */
    public abstract String getDescription();

    /**
     * 具体处理细节的定义，即事件执行的内容
     */
    public abstract void process();

    /**
     * 停止处理
     */
    void kill() {}

    /**
     * 返回当前的执行的进程，好像只有WaitTarget用到了
     * @return Process
     */
    Process getProcess() {
        return null;
    }

}
