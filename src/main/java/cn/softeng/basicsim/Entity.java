package cn.softeng.basicsim;

import cn.softeng.events.EventHandle;
import cn.softeng.events.EventManager;
import cn.softeng.events.ProcessTarget;
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

    /**
     * 用于表示实体当前状态，实体同一时间可能有多个状态，所以用位运算来表示
     */
    int flags;

    static final int FLAG_ADDED = 0x20;  // entity was defined after the 'RecordEdits' flag
    static final int FLAG_GENERATED = 0x80;  // entity was created during the execution of the simulation
    static final int FLAG_DEAD = 0x0100;  // entity has been deleted
    static final int FLAG_REGISTERED = 0x0200;  // entity is included in the namedEntities HashMap
    static final int FLAG_RETAINED = 0x0400;  // entity is retained when the model is reset between runs

    static {
        allInstances = new ArrayList<>(100);
        namedEntities = new HashMap<>(100);
    }

    public Entity() {
        entityNumber = entityCount.incrementAndGet();
        synchronized (allInstances) {
            allInstances.add(this);
        }
        // 初始化实体状态为空
        flags = 0;
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
     * 封装异常信息并抛出
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

    /**
     * 返回所有创建过个的实体
     * @return
     */
    public static ArrayList<? extends Entity> getAll() {
        synchronized (allInstances) {
            return allInstances;
        }
    }

    public void kill() {
        synchronized (allInstances) {
            allInstances.remove(this);
        }
        if (!testFlag(FLAG_GENERATED)) {
            synchronized (namedEntities) {
                if (namedEntities.get(entityName) == this) {
                    namedEntities.remove(entityName);
                }
                entityName = null;
            }
        }
        setFlag(FLAG_DEAD);
    }

    public static <T extends Entity> T fastCopy(T entity, String name) {
        T ret = (T) generateEntityWithName(entity.getClass(), name);
        return ret;
    }

    private static <T extends Entity> T generateEntityWithName(Class<T> proto, String name) {
        T ent = null;
        try {
            ent = proto.newInstance();
            if (name != null) {
                ent.setName(name);
            } else {
                ent.setName(proto.getSimpleName() + "-" + ent.getEntityNumber());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ent;
    }

    public long getEntityNumber() {
        return entityNumber;
    }

    public void setName(String newName) {
        if (testFlag(FLAG_GENERATED)) {
            entityName = newName;
            return;
        }

        synchronized (namedEntities) {
            namedEntities.remove(entityName);
            entityName = newName;
            namedEntities.put(entityName, this);
        }
    }

    // *******************************
    // 实体状态变量flags的一系列未操作运算符
    // *******************************

    /**
     * 给当前状态变量flags添加新的状态
     * 利用或运算的性质，向flags添加新的转态
     * @param flag 新添加的转态
     */
    public void setFlag(int flag) {
        flags |= flag;
    }

    /**
     * 从状态变量flags中清除指定状态flag
     * @param flag 要清除的状态
     */
    public void clearFlag(int flag) {
        // 先对flag取反，在将flag与flags进行与操作
        // 从而从flags中移除flag
        flags &= ~flag;
    }

    /**
     * 测试状态变量中flags中是否包含flag状态
     * @param flag 判断是否包含该状态
     * @return
     */
    public boolean testFlag(int flag) {
        return (flags & flag) != 0;
    }



    public static <T extends Entity> ClonesOfIterable<T> getClonesOfIterable(Class<T> proto) {
        return new ClonesOfIterable<>(proto);
    }

    public final void scheduleProcessTicks(long ticks, int priority, boolean fifo, ProcessTarget t, EventHandle h) {
        EventManager.scheduleTicks(ticks, priority, fifo, t, h);
    }

    public final void scheduleProcessTicks(long ticks, int priority, ProcessTarget t, EventHandle handle) {
        EventManager.scheduleTicks(ticks, priority, false, t, handle);
    }

    public final void scheduleProcessTicks(long ticks, int priority, ProcessTarget t) {
        EventManager.scheduleTicks(ticks, priority, false, t, null);
    }



    /**
     * 重置实体收集的统计数据
     */
    public void clearStatistics() {}

}
