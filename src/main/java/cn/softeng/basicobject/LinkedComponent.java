package cn.softeng.basicobject;

import cn.softeng.basicsim.Entity;
import cn.softeng.states.StateEntity;
import cn.softeng.input.EntityInput;

/**
 * @date: 12/16/2020 8:48 PM
 */
public class LinkedComponent extends StateEntity {
    protected final EntityInput<Entity> defaultEntity;
    protected final EntityInput<LinkedComponent> nextComponent;
    private long numberAdded;
    private long numberProcessed;
    private long initialNumberAdded;
    private Entity receivedEntity;
    private double releaseTime = Double.NaN;

    {
        defaultEntity = new EntityInput<>(Entity.class, "DefaultEntity", null);
        this.addInput(defaultEntity);

        nextComponent = new EntityInput<>(LinkedComponent.class, "NextComponent", null);
        this.addInput(nextComponent);
    }

}
