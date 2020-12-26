package cn.softeng;

import static org.junit.Assert.assertTrue;

import cn.softeng.processflow.EntityGenerator;
import cn.softeng.basicsim.Entity;
import cn.softeng.processflow.EntitySink;
import cn.softeng.processflow.Queue;
import cn.softeng.processflow.Server;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * Unit test for simple DesSimApp.
 */
@Slf4j
public class DesSimTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        EntityGenerator generator = new EntityGenerator();
        Queue queue = new Queue();
        Server server = new Server();
        EntitySink sink = new EntitySink();

        for (Entity entity : Entity.getClonesOfIterator(Entity.class)) {
            entity.updateStatistics();
        }

    }

}
