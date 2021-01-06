package cn.softeng.processflow;

import cn.softeng.basicsim.Entity;
import lombok.extern.slf4j.Slf4j;

/**
 * EntitySink 将会终止所有传给它的实体
 * @date: 12/22/2020 9:36 AM
 */
@Slf4j
public class EntitySink extends LinkedComponent {

    public EntitySink() {}

    public EntitySink(String name) {
        setName(name);
    }

    public EntitySink(Integer id) {
        setName(String.valueOf(id));
    }

    @Override
    public void addEntity(Entity entity) {
        super.addEntity(entity);

        // 当 nextComponent为空的情况下，只累加 numberProcessed
        this.sendToNextComponent(entity);

        // 终止加入到该组件的实体
        entity.kill();
    }


    @Override
    public void updateStatistics() {
        log.debug("Sink: {} -> NumAdd: {}, NumberProcessed: {}, NumInProcess: {}", this.getName(), this.getNumberAdded(), this.getNumberProcessed(), this.getNumberInProgress());
        numAddMap.put(getSimTicks(), getNumberAdded());
        numInProgressMap.put(getSimTicks(), getNumberInProgress());
        numProcessedMap.put(getSimTicks(), getNumberProcessed());
    }

}
