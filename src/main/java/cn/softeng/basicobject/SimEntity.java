package cn.softeng.basicobject;

import cn.softeng.states.StateEntity;

/**
 * @date: 12/16/2020 7:55 PM
 */
public class SimEntity extends StateEntity {
    public SimEntity() {}

    @Override
    public void earlyInit() {
        super.earlyInit();
    }

    @Override
    public String getInitialState() {
        return "None";
    }

}
