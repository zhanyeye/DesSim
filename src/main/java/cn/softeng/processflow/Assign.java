package cn.softeng.processflow;

import cn.softeng.basicsim.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @date: 2021/3/10 19:58
 * 用于给流经的临时实体设计 属性
 */
@Slf4j
public class Assign extends LinkedComponent {


    private Map<String, Integer> assignments;
    Random random = new Random();

    public Assign() {

    }

    public Assign(String name) {
        setName(name);
    }


    public void addAssignment(Map<String, Integer> assign) {
        assignments = assign;
    }



    {
        assignments = new HashMap<>();
    }

    /**
     * 接受到上游传来的实体
     * @param entity
     */
    @Override
    public void addEntity(Entity entity) {
        // 调用父类 LinkedComponent的addEntity()方法
        super.addEntity(entity);

        String assign = randomAssign(assignments);
        SimEntity simEntity = (SimEntity) entity;
        simEntity.getAttribute().put("color", assign);

        // 将临时实体传递给下一个组件
        this.sendToNextComponent(entity);
    }


    public String randomAssign(Map<String, Integer> choice) {
        List<Integer> weights = new ArrayList<>(choice.values());
        List<Integer> weightTmp = new ArrayList<>(weights.size() + 1);
        weightTmp.add(0);
        Integer sum = 0;
        for (Integer w : weights) {
            sum += w;
            weightTmp.add(sum);
        }

        int rand = random.nextInt(sum);

        int index = 0;

        for (int i = weightTmp.size() - 1; i > 0; i--) {
            if (rand >= weightTmp.get(i)) {
                index = i;
                break;
            }
        }
        String res = "error";
        for (String str : choice.keySet()) {
            if (choice.get(str).equals(weights.get(index))) {
                res = str;
                break;
            }
        }
        return res;
    }

}
