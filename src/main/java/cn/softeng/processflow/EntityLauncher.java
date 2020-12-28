package cn.softeng.processflow;

import cn.softeng.basicsim.Entity;
import lombok.Setter;

/**
 * 实体启动器，用于运行时触发生成实体
 */
public class EntityLauncher extends LinkedService{

    /**
     * 每次到达要生成的实体数
     */
    @Setter
    private long entitiesPerArrival;

    /**
     * 用于生成实体的原型
     */
    @Setter
    private Entity prototypeEntity;

    /**
     * 到目前为止所生成的实体数
     */
    private long numberGenerated = 0;

    @Override
    public void earlyInit() {
        super.earlyInit();
        numberGenerated = 0;
    }

    public void doAction() {

    }

}
