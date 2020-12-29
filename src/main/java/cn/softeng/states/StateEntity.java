package cn.softeng.states;

import java.util.HashMap;

import cn.softeng.basicsim.Entity;
import cn.softeng.events.EventManager;

/**
 * @date: 12/16/2020 6:05 PM
 */
public class StateEntity extends Entity {
    /**
     * The present state of the entity
     */
    private StateRecord presentState;
    private final HashMap<String, StateRecord> states;
    private long lastStateCollectionTick;
    private long workingTicks;

    public StateEntity() {
        states = new HashMap<>();
    }

    @Override
    public void earlyInit() {
        // 为实体属性设置默认值
        super.earlyInit();
        // 初始化状态数据
        this.initStateData();
    }

    @Override
    public void lateInit() {
        super.lateInit();
    }

    /**
     * 初始化状态实体，
     */
    private void initStateData() {
        lastStateCollectionTick = 0;
        if (EventManager.hasCurrent()) {
            // 获取仿真当前时钟刻度
            lastStateCollectionTick = getSimTicks();
        }
        // 累计工作时间刻度
        workingTicks = 0;
        states.clear();

        // 给状态实体添加一个初始状态
        String initState = getInitialState().intern();
        StateRecord init = new StateRecord(initState, isValidWorkingState(initState));
        init.startTick = lastStateCollectionTick;
        presentState = init;
        states.put(init.name, init);
    }

    /**
     * Get the name of the initial state this Entity will be initialized with.
     * @return
     */
    public String getInitialState() {
        return "Idle";
    }

    /**
     * Tests the given state name to see if it is valid for this Entity.
     * @param state
     * @return
     */
    public boolean isValidState(String state) {
        return "Idle".equals(state) || "Working".equals(state);
    }

    /**
     * Tests the given state name to see if it is counted as working hours when in
     * that state..
     * @param state
     * @return
     */
    public boolean isValidWorkingState(String state) {
        return "Working".equals(state);
    }

    public void setPresentState() {}

}
