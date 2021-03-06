package cn.softeng.processflow;

import cn.softeng.states.StateEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 临时实体：由实体生成其生成，在模型流程中流动的模型
 * @date: 12/16/2020 7:55 PM
 */
@Slf4j
public class SimEntity extends StateEntity {
    /**
     * 实体属性
     */
    @Setter
    @Getter
    private Map<String, Object> attribute;

    {
        attribute = new HashMap<>();
    }

    public SimEntity() {}

    public SimEntity(String name) {
        setName(name);
    }

    public SimEntity(Integer id) {
        setName(String.valueOf(id));
    }

    @Override
    public void earlyInit() {
        super.earlyInit();
    }

    @Override
    public String getInitialState() {
        return "None";
    }


}
