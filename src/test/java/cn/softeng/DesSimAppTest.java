package cn.softeng;

import static org.junit.Assert.assertTrue;

import cn.softeng.basicobject.EntityGenerator;
import cn.softeng.basicobject.EntitySink;
import cn.softeng.basicsim.Entity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * Unit test for simple DesSimApp.
 */
@Slf4j
public class DesSimAppTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public <T extends Entity> void  shouldAnswerWithTrue() {
        Entity ent = new EntityGenerator();
        log.info(ent.getClass().toString());
        Class t = ent.getClass();
//        Class<T> tt = ent.getClass();
        log.debug(t.toString());
        T test;
        try {
            test =(T) t.newInstance();
            log.debug(test.getClass().toString());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

}
