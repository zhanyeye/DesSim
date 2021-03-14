package cn.softeng.processflow;

import cn.softeng.basicsim.Entity;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

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
        SimEntity simEntity = (SimEntity) entity;
//        Map<String, Object> map = simEntity.getAttribute();
//        for (Map.Entry entry : map.entrySet()) {
//            log.debug(entry.getKey() + ": " + entry.getValue());
//        }
        // 当 nextComponent为空的情况下，只累加 numberProcessed
        this.sendToNextComponent(entity);
        // 终止加入到该组件的实体
        entity.kill();
    }


    @Override
    public void updateStatistics() {
//        log.debug("Sink: {} -> NumAdd: {}, NumberProcessed: {}, NumInProcess: {}", this.getName(), this.getNumberAdded(), this.getNumberProcessed(), this.getNumberInProgress());
        numAddMap.put(getSimTime(), getNumberAdded());
        numInProgressMap.put(getSimTime(), getNumberInProgress());
        numProcessedMap.put(getSimTime(), getNumberProcessed());
    }

    @Override
    public void clearStatistics() {
        numAddMap.clear();
        numInProgressMap.clear();
        numProcessedMap.clear();
    }

}
