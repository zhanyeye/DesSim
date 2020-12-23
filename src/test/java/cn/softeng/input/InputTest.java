package cn.softeng.input;

import cn.softeng.basicobject.LinkedComponent;
import cn.softeng.basicobject.Queue;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @date: 12/23/2020 8:19 PM
 */
@Slf4j
public class InputTest {
    @Test
    public void test_input_noError() {
        BooleanInput booleanInput = new BooleanInput("boolean", false);
        log.info(booleanInput.toString());
        booleanInput.updateValue(true);
        log.info(booleanInput.toString());

        StringInput stringInput = new StringInput("string", "11111111");
        log.info(stringInput.toString());
        stringInput.updateValue("22222222");
        log.info(stringInput.toString());

        Queue q1 = new Queue();
        q1.setName("queue1");
        Queue q2 = new Queue();
        q1.setName("queue2");
        EntityInput<LinkedComponent> entityInput = new EntityInput<>(LinkedComponent.class,"entityInput",q1);
        log.info(entityInput.toString());
        entityInput.updateValue(q2);
        log.info(entityInput.toString());
    }
}
