package cn.softeng.basicsim;

import cn.softeng.processflow.EntityGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class EntityTest {


    /**
     * 测试fastCopy功能是否运行正常
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
//    @Test
    public void test_fastCopy_noError() throws InstantiationException, IllegalAccessException {
        EntityGenerator origin = new EntityGenerator();
        Entity copy = fastCopy(origin, "testFastCopy");

        log.info("getName : " + copy.getName());
        log.info("getClass : " + copy.getClass().toString());
    }


    public static <T extends Entity> T fastCopy(T entity, String name) throws IllegalAccessException, InstantiationException {
        Class proto = entity.getClass();
        T ret = (T) proto.newInstance();
        ret.setName(name);
        return ret;
    }
}
