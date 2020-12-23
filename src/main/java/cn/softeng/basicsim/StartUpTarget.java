package cn.softeng.basicsim;

import cn.softeng.events.ProcessTarget;

/**
 * @date: 12/23/2020 12:18 PM
 */
public class StartUpTarget extends ProcessTarget {

    final Entity entity;

    StartUpTarget(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void process() {
        entity.startUp();
    }

    @Override
    public String getDescription() {
        return entity.getName() + ". startUp";
    }
}
