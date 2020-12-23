package cn.softeng.basicobject;

import cn.softeng.basicsim.Entity;

/**
 * EntitySink 将会终止所有传给它的实体
 * @date: 12/22/2020 9:36 AM
 */
public class EntitySink extends LinkedComponent {
    @Override
    public void addEntity(Entity entity) {
        super.addEntity(entity);

        // 当nextComponent为空的情况下，只累加 numberProcessed
        this.sendToNextComponent(entity);

        // 终止加入到该组件的实体
        entity.kill();
    }
}