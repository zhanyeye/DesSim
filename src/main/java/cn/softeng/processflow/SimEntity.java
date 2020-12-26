package cn.softeng.processflow;

import cn.softeng.states.StateEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * @date: 12/16/2020 7:55 PM
 */
@Slf4j
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
