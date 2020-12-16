package cn.softeng.basicsim;

import cn.softeng.events.EventManager;
import cn.softeng.input.Input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @date: 12/15/2020 11:41 PM
 */
public class Entity {
    /**
     * 所有创建的实体数量
     */
    private static AtomicLong entityCount = new AtomicLong(0);
    /**
     * 所有已创建实体的集合
     */
    private static final ArrayList<Entity> allInstances;
    /**
     * 所有命名实体的集合，每一个实体都有一个唯一的名字
     */
    private static final HashMap<String, Entity> namedEntities;
    /**
     * 实体的各种输入的聚合
     */
    private final ArrayList<Input<?>> inputList = new ArrayList<>();
    /**
     * 实体名称
     */
    private String entityName;
    /**
     * 实体唯一id
     */
    private final long entityNumber;

    static {
        allInstances = new ArrayList<>(100);
        namedEntities = new HashMap<>(100);
    }

    public Entity() {
        entityNumber = entityCount.incrementAndGet();
        synchronized (allInstances) {
            allInstances.add(this);
        }
    }

    /**
     * 模型运行前初始化实体
     * 该方法中操作只能依赖于自身，这样才可以在任何序列中初始化每个实体
     */
    public void earlyInit() {
        // 将实体的属性重置为初始值

    }

    /**
     * 初始化实体
     */
    public void lateInit() {

    }

    /**
     * 启动该实体的模型运行
     */
    public void startUp() {

    }

    /**
     * 仿真运行结束后的一些操作，如生成报告
     */
    public void doEnd() {

    }

    /**
     * 获取当前仿真ticks(时刻)值
     * @return
     */
    public final long getSimTicks() {
        return EventManager.simTicks();
    }

    /**
     * 向实体添加Input
     * @param in
     */
    protected void addInput(Input<?> in) {
        inputList.add(in);
    }

    /**
     * 返回指定keyword的Input
     * @param key input的关键字
     * @return
     */
    public final Input<?> getInput(String key) {
        for (Input<?> in : inputList) {
            if (key.equals(in.getKeyword())) {
                return in;
            }
        }
        return null;
    }

    /**
     * 返回实体名称
     * @return
     */
    public final String getName() {
        return entityName;
    }


    /**
     *
     * @param fmt
     * @param args
     * @throws ErrorException
     */
    public void error(String fmt, Object... args) throws ErrorException {
        final StringBuilder sb = new StringBuilder(this.getName());
        sb.append(": ");
        sb.append(String.format(fmt, args));
        throw new ErrorException(sb.toString());
    }

}
