package cn.softeng.processflow;

import cn.softeng.basicsim.Entity;
import cn.softeng.states.StateEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.*;


/**
 * LinkedComponent是用来形成一个组件链，来处理穿过系统实体
 * 其子类有：EntityGenerator, Server, EntitySink ...
 * @date: 12/16/2020 8:48 PM
 */
public class LinkedComponent extends StateEntity {
    /**
     * 输出obj的默认值。 通常，obj设置为此对象接收的最后一个实体。
     * 在接收其第一个实体之前，将obj设置为DefaultEntity提供的对象。
     * 如果未提供DefaultEntity的输入，则obj设置为null，直到接收到第一个实体。
     */
    @Setter
    protected Entity defaultEntity;
    /**
     * 已处理实体要传递的下一个组件
     */
    @Setter
    protected LinkedComponent nextComponent;
    /**
     * 初始化后，从上游添加的此组件的实体数
     */
    private long numberAdded;
    /**
     * 初始化后，此组件处理的实体数
     */
    private long numberProcessed;
    /**
     * 初始化期间，从上游添加到此组件的实体数
     */
    private long initialNumberAdded;
    /**
     * 初始化期间此组件处理的实体数目
     */
    private long initialNumberProcessed;
    /**
     * 该组件最近收到的实体
     */
    private Entity receivedEntity;
    /**
     *
     */
    private double releaseTime = Double.NaN;

    {
        defaultEntity = null;
        nextComponent = null;
    }

    @Override
    public void earlyInit() {
        super.earlyInit();
        numberAdded = 0;
        numberProcessed = 0;
        initialNumberAdded = 0;
        initialNumberProcessed = 0;
        receivedEntity = defaultEntity;
        releaseTime = Double.NaN;
    }

    /**
     * 从上游组件中接收指定实体
     * @param ent
     */
    public void addEntity(Entity ent) {
        this.registerEntity(ent);
    }

    /**
     * 组件接收指定实体，同时更新numberAdded
     * @param ent
     */
    protected void registerEntity(Entity ent) {
        receivedEntity = ent;
        numberAdded++;
    }

    /**
     * 直接设置接收的实体，被Queue组件调用
     * @param entity
     */
    protected void setReceivedEntity(Entity entity) {
        receivedEntity = entity;
    }

    /**
     * 将指定实体传送给指定的下游组件
     * @param entity
     */
    public void sendToNextComponent(Entity entity) {
        numberProcessed++;
        releaseTime = this.getSimTicks();
        if (nextComponent != null) {
            nextComponent.addEntity(entity);
        }
    }

    /**
     * 该组件的已处理实体数+1
     */
    public void incrementNumberProcessed() {
        numberProcessed++;
    }

    // *****************************
    //  统计相关数据
    // *****************************


    protected Map<Long, Long> numAddMap = new LinkedHashMap<>();

    protected Map<Long, Long> numProcessedMap = new LinkedHashMap<>();

    protected Map<Long, Long> numInProgressMap = new LinkedHashMap<>();

    /**
     * 返回numAdd各时钟序列所对应的值
     * @return
     */
    public List<Long> getNumAddList() {
        return new ArrayList<>(numAddMap.values());
    }

    /**
     * 返回numProcess各时钟序列所对应的值
     * @return
     */
    public List<Long> getNumProcessedList() {
        return new ArrayList<>(numProcessedMap.values());
    }

    /**
     * 返回numInProcess各时钟序列所对应的值
     * @return
     */
    public List<Long> getNumInProgressList() {
        return new ArrayList<>(numInProgressMap.values());
    }

    /**
     * 返回在整个仿真运行（包括初始化期间）中从上游接收到的实体数。
     * @return
     */
    public long getNumberAdded() {
        return initialNumberAdded + numberAdded;
    }

    /**
     * 返回在整个仿真运行（包括初始化期间）中已传递给下游的实体数。
     * @return
     */
    public long getNumberProcessed() {
        return initialNumberProcessed + numberProcessed;
    }

    /**
     * 返回以收到，但未处理完的实体数
     * @return
     */
    public long getNumberInProgress() {
        return initialNumberAdded + numberAdded - initialNumberProcessed - numberProcessed;
    }

    @Override
    public void clearStatistics() {
        super.clearStatistics();
        initialNumberAdded = numberAdded;
        initialNumberProcessed = numberProcessed;
        numberAdded = 0;
        numberProcessed = 0;
    }

    /**
     * 获取该组件最近接收到的实体
     * @return
     */
    public Entity getReceivedEntity() {
        return receivedEntity;
    }
}
