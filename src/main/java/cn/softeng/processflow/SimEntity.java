package cn.softeng.processflow;

import cn.softeng.states.StateEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * 由实体生成其生成，在模型流程中流动的模型
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
