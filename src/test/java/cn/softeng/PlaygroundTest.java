package cn.softeng;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @date: 12/29/2020 2:33 PM
 */
@Slf4j
public class PlaygroundTest {
    @Test
    public void testLinkedHashMap() {
        Map<Long, Long> map =  new LinkedHashMap<>();
        map.put(1L, 1L);
        map.put(2L, 2L);
        map.put(3L, 3L);
        map.put(4L, 4L);
        map.put(5L, 5L);
        map.put(6L, 6L);
        map.put(7L, 7L);
        map.put(8L, 8L);
        for (Map.Entry<Long, Long> entry : map.entrySet()) {
            log.debug("{}-{}", entry.getKey(), entry.getValue());
        }
    }
}
