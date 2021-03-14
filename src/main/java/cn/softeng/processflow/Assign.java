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

    private Random random = new Random();

    private List<Object> shuffledList = new LinkedList<>();

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

    /**
     * 按照权重来分配属性，使用随机函数
     * @param choice
     * @return
     */
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

    /**
     * 按照权重来分配属性，使用洗牌策略
     * @param choice
     * @return
     */
    public String shuffleAssign(Map<String, Integer> choice) {
        if (shuffledList.size() == 0) {
            for (Map.Entry<String, Integer> entry : choice.entrySet()) {
                for (int i = 0; i < entry.getValue(); i++) {
                    shuffledList.add(entry.getKey());
                }
            }
        }
        Collections.shuffle(shuffledList);
        return (String) shuffledList.remove(0);
    }

    @Override
    public void updateStatistics() {
        numAddMap.put(getSimTime(), getNumberAdded());
        numInProgressMap.put(getSimTime(), getNumberInProgress());
        numProcessedMap.put(getSimTime(), getNumberProcessed());
    }

    @Override
    public void clearStatistics() {
        numAddMap.clear();
        numInProgressMap.clear();
        numProcessedMap.clear();
    }

//    public static void main(String[] args) {
//        Map<String, Integer> map = new HashMap<>();
//        map.put("red", 3);
//        map.put("black", 7);
//        Assign assign = new Assign();
//        Map<String, Integer> res = new HashMap<>();
//
//        for (int i = 0; i < 18; i++) {
//            String color = assign.shuffleAssign(map);
////            String color = assign.randomAssign(map);
//            System.out.println(color);
//            if (res.containsKey(color)) {
//                res.put(color, res.get(color) + 1);
//            } else {
//                res.put(color, 1);
//            }
//        }
//        System.out.println(res.toString());
//    }

}
