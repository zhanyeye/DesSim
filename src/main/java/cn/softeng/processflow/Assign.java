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


    /**
     * 为临时实体设置属性的键值对
     * key对应着属性名，Value对应着该属性可以有的选项的Map（Map 又是一个 Key-Value键值对）
     */
    private Map<String, Map> assignments;

    /**
     * 添加一个设置项目
     * @param name 设置属性的名称
     * @param assignment 属性可能的选项
     */
    public void addAssignment(String name, Map assignment) {
        assignment.put(name, assignment);
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

        // 遍历所有要分配的属性
        for (Map.Entry<String, Map> entry : assignments.entrySet()) {
            String assign = randomAssign(entry.getValue());
            SimEntity simEntity = (SimEntity) entity;
            simEntity.getAttribute().put(entry.getKey(), assign);
        }

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
        Random random = new Random();
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


    /**
     * 随机函数按照指定权重进行分配
     * @param weights 权重集合
     * @return
     */
    public static int random(List<Integer> weights) {
        List<Integer> weightTmp = new ArrayList<>(weights.size() + 1);
        weightTmp.add(0);
        Integer sum = 0;
        for (Integer w : weights) {
            sum += w;
            weightTmp.add(sum);
        }
        Random random = new Random();
        int rand = random.nextInt(sum);

        int index = 0;

        for (int i = weightTmp.size() - 1; i > 0; i--) {
            if (rand >= weightTmp.get(i)) {
                index = i;
                break;
            }
        }
        return weights.get(index);
    }
}
