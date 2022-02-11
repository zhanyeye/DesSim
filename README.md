<p align="center">
    <img width="180" src="https://raw.githubusercontent.com/zhanyeye/Figure-bed/win-pic/img/20210602141834.png">
    <h2 align="center">DesSim</h2>
</p>

#### 项目介绍：

一个简单的离散事件仿真程序，能够动态的构建模型，基于 [JaamSim](https://github.com/jaamsim/jaamsim) 开发
<details close>
 <summary>👉 离散事件仿真介绍 👈（点击查看详情~） </summary>
 <br/>

&ensp;&ensp;&ensp;在控制领域，通常将研究对象称之为“系统”。根据系统状态是否随时间连续变化，分为离散系统和连续系统。离散事件系统被大量的应用在生产调度，库存管理和物流制造等领域。这些场景的业务流程可以被描述为一系列独立的离散事件序列。在两个相邻的离散事件之间，系统状态是保持不变的，因此离散系统的变化难以采用微分或者差分方程的形式描述，而且这些变化一般会具有随机性。针对离散系统的这些特性，一方面可以基于概率论、随机过程等在理论上进行分析，但是随着系统复杂程度的增加，计算机仿真已经成为研究离散系统的重要手段。

&ensp;&ensp;&ensp;为了对系统进行建模，我们需要知道离散事件系统的一些基本要素，这有助于帮助我们理解实体模型和事件之间的关系，以及仿真策略所采用的视角（仿真策略主要有3种，这里不再介绍了）。

1. 实体：用于描述系统中的对象。可分为临时实体（由外部进入系统的，完成相应操作后，最终离开系统）和永久实体（永久性地驻留在系统中）两大类。
1. 事件：引起系统状态变化的行为。离散系统由事件驱动，在仿真模型中建立事件表管理系统中的各类事件。
1. 活动：表示相邻两个事件之间的持续过程，其开始和结束都是由事件引起的。
1. 进程：进程由若干个有序事件及若干个有序活动组成，一个进程描述了它所包括的事件及活动之间的逻辑关系及时序关系。
1. 仿真时钟：用于模拟实际系统的时间属性，一般是仿真的主要自变量。
1. 统计计数器：用于统计系统的状态随着事件的不断发生变化的情况。

在回顾了离散事件的基本概念以后，我们再来看一下仿真程序的主要成分，DESSIM也可分为下面几个部分。

1. [仿真时钟](https://github.com/zhanyeye/DesSim/blob/3cda71270aa5fe55e5646d750f22dc06e0b38d03/src/main/java/cn/softeng/events/EventManager.java#L74-L78)：提供仿真时间的当前值；
1. [事件表](https://github.com/zhanyeye/DesSim/blob/3cda71270aa5fe55e5646d750f22dc06e0b38d03/src/main/java/cn/softeng/events/EventManager.java#L43-L46)：由策划和事件调度生成的事件名称、时间的二维表，即有关未来事件的表；
1. [系统的状态变量](https://github.com/zhanyeye/DesSim/blob/3cda71270aa5fe55e5646d750f22dc06e0b38d03/src/main/java/cn/softeng/processflow/LinkedComponent.java#L31-L43)：描述系统状态的变量；
1. [初始化子程序](https://github.com/zhanyeye/DesSim/blob/master/src/main/java/cn/softeng/basicsim/InitModelTarget.java)：用于模型初始化；
1. 事件子程序：每一类事件的具体实现；
1. [调度子程序](https://github.com/zhanyeye/DesSim/blob/3cda71270aa5fe55e5646d750f22dc06e0b38d03/src/main/java/cn/softeng/events/EventManager.java#L315-L458)：将未来事件插入事件表中的子程序；
1. [时钟推进子程序](https://github.com/zhanyeye/DesSim/blob/3cda71270aa5fe55e5646d750f22dc06e0b38d03/src/main/java/cn/softeng/events/EventManager.java#L441-L445)：根据事件表决定下次（最早发生的）事件，然后将仿真时钟推进到事件发生时刻；
1. [统计计数器](https://github.com/zhanyeye/DesSim/blob/3cda71270aa5fe55e5646d750f22dc06e0b38d03/src/main/java/cn/softeng/basicsim/Entity.java#L281-L284)：用来存放与系统性能分析有关的统计数据的各个变量值；
1. 主程序：调用上述各种子程序并完成仿真任务全过程。

 
</details>


支持的模型组件有：
- **EntityGenerator** : 实体生成器，根据指定时间间隔生成实体 
- **EntityLauncher** : 实体启动器，被用户触发后生成实体 
- **Queue** : 队列，用于存放等待中的实体
- **Server** : 服务，延时组件，模拟处理实体所消耗的时间
- **EntitySink** : 实体回收器，回收处理完毕的实体
- **Assign** : 为经过的临时实体设置不同的属性，按照用户设置的权重进行分配



#### DESSIM 实现原理

##### 1.事件调度的策略

DESSIM 主要采用的模拟策略是 事件调度法，它从事件的视角去抽象真实的系统，事件发生可能会导致系统状态变化，也可能会触发新的未来事件。每一个事件都会有对应的时间点，事件调度器会按照时间顺序调度事件，并推进仿真时钟。整个调度流程就如下图所示

<p align="center">
    <img src="https://zhanyeye-img.oss-accelerate.aliyuncs.com/20210205144224.png">
</p>

1. 首先初始化子程序将初始化事件加入事件表，事件调度器开始运行。
1. 事件调度器从事件表中取出最近将要发生的事件。
1. 事件调度器为该事件分配一个线程，开始执行事件。
1. 若事件执行过程中触发了新的事件，则将该事件添加到事件表中的适当位置。
1. 更新系统状态变量。
1. 如有需要（事件调度器的下个事件事件大于仿真时钟时间），推进仿真时钟
1. 回到第2步，再次从事件表中取出最近的事件开始调度
1. 一直循环达到目标时间 或 因为某些原因事件表为空才结束执行

看到这里，你可能会对事件调度有一个大概的了解，但是仍然会有一些疑问。整个仿真都是围绕着"事件"进行调度，那"事件"到底是什么呢？又是如何实现的呢？这里我先按下不表。等到合适的时候再向您介绍。

##### 2.实体模型的实现

在了解事件调度的原理之后，我们还需要了解仿真模型的构建。目前 DESSIM 提供了1个零时实体，5个模型组件。用户可以通过创建模型的组件对象，并设置它们的前驱组件和后继组件，来构建一个流程模型。它们的描述信息如下。

| 模型组件          | 描述                                                         |
| ----------------- | ------------------------------------------------------------ |
| `SimEntity`       | 临时实体，是流经模型中各组件的对象，它是整个模型中被加工处理的对象 |
| `EntityGenerator` | 该组件负责自动创建一系列的临时实体, 并将这些临时实体传递给流程中的下一个组件。 |
| `EntityLauncher`  | 该组件在用户触发后，会产生指定数量的临时实体传递给下一个组件 |
| `Server`          | Server 组件模拟加工传入的临时实体，它会将临时实体延迟一段时间后再释放给后继组件 |
| `Queue`           | Queue 组件用于存放等待被服务的临时实体，通常和Server组件配合使用 |
| `EntitySink`      | 用于回收或销毁将要离开系统的临时实体                         |

通过上面模型组件之间的链接，就可以构建一个简单的离散事件仿真模型，下面列举了2个使用示例。

- [Generator模式示例](https://github.com/zhanyeye/DesSim/blob/master/README.md#generator%E6%A8%A1%E5%BC%8F%E7%A4%BA%E4%BE%8B)
- [Launcher模式示例](https://github.com/zhanyeye/DesSim/blob/master/README.md#launcher%E6%A8%A1%E5%BC%8F%E7%A4%BA%E4%BE%8B)

看完使用示例，你会发现这些组件对象组装成了一个链式结构 ，你可能好奇这些组件是如何实现的呢？临时实体又是如何在这些模型组件之间传递的呢？接下来我会通过类图向您介绍模型实体/组件的实现。

<p align="center">
    <img width="850px" src="https://zhanyeye-img.oss-accelerate.aliyuncs.com/20210218123803.png">
</p>

**Entity**  
一个抽象的基类。封装了仿真实体或组件所需要的基本方法和数据。模型中的仿真实体或组件都继承与该基类。

- `earlyInit()` 该方法用于初始化模型组件的基本数据，每一组件都会重写该方法，有自己的实现。
- `startUp()` 该方法是在完成初始化后调用，用于启动组件开始运行，生成临时实体的组件实现该方法。
- `getSimTicks()` 用于从当前正运行线程所持有的事件管理器获取仿真时钟。
- `kill()` 用于销毁该实体，当临时实体被收集后会调用该方法。
- `updateStatistics()` 定义了更新实体统计数据的接口，模型中各组件会有自己的实现
- `scheduleProcess(long ticks, int priority, ProcessTarget t)` ，该方法是用于将操作封装成相应的事件，加入事件列表中。

**StateEntity** 
该类继承自Entity, 是一个中间层，它在Entity的基础上添加了实体状态，服务时间等属性，主要负责统计实体的状态和服务时间的数据。例如：临时实体在模型组件之间传递时，其状态可能会改变，服务时间也会不断累加，都是在这一层实现的。

**SimEntity**  
临时实体，继承自Enity，该实体模拟的是在流程模型中被传递的对象，可以由 `EntityGenerator` ， `EntityLauncher`  等的对象生成，他们会在流程模型对象之间传递，最后离开系统。而且它们每次到达模型中的一个对象，就可能会触发一些对应的事件。

**LinkedComponent**    
链式组件，继承自Entity, 是一个中间层，它为模型组件赋予设置前驱和后继组件的能力，模型中所有具体组件都继承自该类，来获得构建链式的流程模型的能力，同时它可以接收传递穿过模型的零时实体。

- 该类包含一个指向后继组件的nextComponent字段。这样它的子类，也就是每个具体的组件，可以通过设置后继组件来完成模型的构建。
- 该类包含 `receivedEntity` 字段，用于暂时存储前驱组件传来的临时实体。
- `sendToNextComponent(Entity entity)` 方法用于将接受到的实体传递给下游组件，它调用下游组件的 `addEntity()` 。
- `addEntity(Entity ent)` 方法，用于向该组件中传递实体。
```java
public class LinkedComponent extends StateEntity {
    
    protected LinkedComponent nextComponent;
    private Entity receivedEntity;
    
    // 其他字段
    
    /**
     * 将指定实体传送给指定的下游组件
     * @param entity
     */
    public void sendToNextComponent(Entity entity) {
        // 该组件处理实体数量+1
        numberProcessed++;
        // 获取仿真时钟的时间
        releaseTime = this.getSimTime();
        if (nextComponent != null) {
            nextComponent.addEntity(entity);
        }
    }
    
    /**
     * 从前驱组件中接收指定实体
     * @param ent
     */
    public void addEntity(Entity ent) {
        this.registerEntity(ent);
        receivedEntity = ent;
        numberAdded++;
    }
    
    // 其他方法
}
```

**EntitySink**  
继承自LinkedComponent，该模型组件用于回收即将离开系统的临时实体，实体进入该组件后会被直接销毁。

- 该组件重写了addEntity()方法, 传递给该组件的临时实体都会被销毁。
```java
public class EntitySink extends LinkedComponent {
    // 其他字段 ...
    @Override
    public void addEntity(Entity entity) {
        super.addEntity(entity);
        // 当 nextComponent为空的情况下，只累加 numberProcessed
        this.sendToNextComponent(entity);
        // 终止加入到该组件的实体
        entity.kill();
    }
    // ...
} 
```

**Queue**  
队列组件，用于保存等待服务的临时实体，队列组件主要和 Server 组件配合使用，当一个Server组件忙碌时，到达的实体都会暂存再 Queue 组件中，当 Server 组件空闲后，它会从队列组件中取出实体继续加工。

- 该组件中有一个实体有序集合 `itemSet` ，用于暂时存放等待服务的实体。
- 该类重写了 `addEntity()`  每当有实体加入后，会将实体加入到一个有序集合中，并向队列的用户发送通知

**LinkedService**  
继承自 LinkedComponent, 该类是一个中间层，该层主要作用是封装组件的实体离开事件，并将其插入事件表，等待调度。由这些未来事件来驱动临时实体在各个组件之间传递。同时也提供了钩子函数，让子类来实现具体的处理操作。

```java
public class LinkedService extends LinkedComponent implements QueueUser {
    // 其他字段 ...
    /**
     * 为该组件服务的队列有新实体到达
     * 该方法会由为它服务的队列调用
     */
    @Override
    public void queueChanged() {
        // 该组件再次处理实体到达操作
        this.restartAction();
    }

    private void restartAction() {
        // 判断该组件当前是否空闲
        if (this.isIdle()) {
            // 该操作是否会被暂停
            if (processKilled) {
                processKilled = false;
                boolean bool = this.updateForStoppage(startTime, stopWorkTime, getSimTime());
                if (bool) {
                    this.setBusy(true);
                    this.setPresentState();
                    duration -= stopWorkTime - startTime;
                    startTime = this.getSimTime();
                    this.scheduleProcess(duration, 5, endActionTarget, endActionHandle);
                    return;
                }
            }
            // 否则，为这个新的实体开始工作
            this.startAction();
            return;
        }

        // 如果该服务组件不是空闲的记录下它的状态
        this.setPresentState();
    }

    /**
     * 组件开始处理实体
     */
    protected final void startAction() {
        // 执行LinkedService子类的特别处理操作
        double simTime = this.getSimTime();
        if (!this.startProcessing(simTime)) {
            this.stopAction();
            return;
        }

        // 设置状态
        if (!isBusy()) {
            this.setBusy(true);
            this.setPresentState();
        }

        // 调度服务完成
        startTime = simTime;
        duration = this.getProcessingTime(simTime);
        // 合成一个实体离开事件，加入事件表
        this.scheduleProcess(duration, 5, endActionTarget, endActionHandle);
    }
    
    /**
     * 命令模式中的 ConcreteCommand, 用于执行 LinkedService 的 endAction()
     */
    private static class EndActionTarget extends EntityTarget<LinkedService> {
        EndActionTarget(LinkedService ent) {
            super(ent, "endAction");
        }

        @Override
        public void process() {
            entity.endAction();
        }
    }
    
    /**
     * 完成一个实体的处理
     * (相当于命令模式中的接收者，是真正执行命令操作的功能代码)
     */
    final void endAction() {
        // 执行此LinkedService子类所需的任何特殊处理
        this.endProcessing(this.getSimTime());
        // 处理下一个实体
        this.startAction();
    }
    
    // ...
    
}
```

**EntityGenerator**  
该类继承自 `LinkedServer` ，该组件用于自动产生临时实体，可以设置产生间隔，产生的数量，和第一次产生的事件。

- setEntitiesPerArrival()   设置每次达到多少实体
- setFirstArrivalTime()     设置第一次产生实体的时间
- setInterArrivalTime()     设置产生实体的间隔时间
- 该类重写了 endProcessing() 方法，实现了实体离开的操作
```java
public class EntityGenerator extends LinkedService {
    
    // 其他字段 ...
    
    @Override
    protected void endProcessing(double simTime) {
        // 获取创建实体的数量
        int num = (int) entitiesPerArrival;
        // 创建临时实体，并送给下游组件
        for (int i = 0; i < num; i++) {
            numberGenerated++;
            Entity proto = prototypeEntity;
            StringBuilder sb = new StringBuilder();
            sb.append(this.getName()).append("_").append(numberGenerated);
            Entity entity = Entity.fastCopy(proto, sb.toString());
            entity.earlyInit();
            // 将实体传送给链中的下一个元素
            this.sendToNextComponent(entity);
        }
    }
 
    // 其他字段 ...
}
```

**EntityLauncher**   
该类继承自 `LinkedServer` ，用于在用户触发时生成指定数量的临时实体传递给下游组件。产生实体的细节与EntityGenerator相似，只是在被调用是才会被执行，且执行一次。

**Server**   
该类继承自 `LinkedServer` ，它用于模拟临时实体在组件中加工一段时间再释放的过程，它实现了LinkedServer 类的钩子函数，来实现加工指定时间后，再将实体传递个下游组件。

```java
public class Server extends LinkedService {
    
    // 其他字段 ...
    
    /**
     * 当实体被处理时，调用的钩子函数，server组件会从队列中取出一个临时实体来加工
     * @param simTime 当前的仿真时间
     * @return
     */
    @Override
    protected boolean startProcessing(double simTime) {
        if (waitQueue.isEmpty()) {
            return false;
        }
        // 从队列中删除第一个实体
        this.servedEntity = this.getNextEntityFromQueue();
        return true;
    }

    /**
     * 当时实体处理结束时，会调用的钩子函数，server会将实体传递给下一个组件
     * @param simTime 当前的仿真时间
     */
    @Override
    protected void endProcessing(double simTime) {
        // 将实体发送到链中的下一个组件
        this.sendToNextComponent(servedEntity);
        servedEntity = null;
    }

    /**
     * 这是一个钩子函数，返回该组件加工实体消耗的时间，用于实体离开时间的计算
     * @param simTime 当前的仿真时间
     * @return
     */
    @Override
    protected double getProcessingTime(double simTime) {
        return serviceTime;
    }
    
    // 其他函数 ...
    
}
```

##### 3.命令模式的应用

整个 DesSim 都是围绕着这个命令模式来实现的。命令模式的关键之处就是把请求封装成为对象，也就是命令对象，并定义了统一的执行操作的接口，这个命令对象可以被存储、转发、执行 等，整个命令模式都是围绕这个对象在进行，它的类图如下:

<p align="center">
    <img width="700px" src="https://zhanyeye-img.oss-cn-shanghai.aliyuncs.com/20210207183606.png">
</p>

在"事件调度策略"中，我们发现"事件"像一个对象一样，可以被传递，可以被保存在集合中并排序，即使这个事件被创建很久了，它仍然能够被调度执行。可是"事件"本应该是一些列操作，它是如何做到被存储，排序和执行调度的呢？


在DESSIM中，事件Event主要由 调度时间，事件处理内容（命令对象）和一个事件句柄构成。也就是说一个事件会包含一个命令对象。命令对象封装了接受者和接受者的动作，从而将调度事件的事件管理器和真正执行事件的组件对象解耦。当需要执行事件的时候，只需要调用target.process()方法即可，这样包含着命令对象的事件对象就可以被存储，转发，和执行。
```java
/**
 * @Description: 事件抽象类
 */
public class Event {
    /**
     * 事件发生的时间
     */
    long schedTick;
    /**
     * 事件处理内容（命令对象）
     */
    ProcessTarget target;
    /**
     * 持有一个该事件的引用，方便在事件队列中找到对应的事件
     */
    EventHandle handle;
    
    // .....
}

```


ProcessTarget 类就是一个命令对象，只声明了可以执行的方法接口，具体操作需要由具体命令来实现。
```
public abstract class ProcessTarget {

    /**
     * 停止执行该target(命令)
     */
    void kill() {}

    /**
     * 执行target(命令)对应的操作, 即event执行的内容
     */
    public abstract void process();

}
```


同样的在"实体模型实现"一节中，我们发现，当"临时实体"在流程组件中传递时，会触发产生对应的事件，这些组件是如何产生事件的呢？


我们用Queue组件来举例子：在组件中包含了会被触发的操作（相当于接收者），它是事件要执行的内容。在组件类中也定义了具体命令（内部类），用来持有持有接收者对象，并将调用请求委派个这个接收者。具体命令对象的创建以及接收者的设置也是在该类中完成的。当需要触发事件时，将这个命令对象和调度时间封装成为一个事件对象，插入事件表的合适位置。具体如下：


1. 接受者：它是真正执行命令的对象。当队列发生改变时，它需要通知它的所有用户，Queue中有具体实现。
```java
public class Queue extends LinkedComponent {
    ...
        
    /**
     * 该Queue组件的使用者
     */
    private final ArrayList<QueueUser> userList;
    
    /**
     * 告诉每一个队列的使用者，队列发生改变
     */
    public notifyQueueChanged() {
        for (QueueUser each : userList) {
            each.queueChanged();
        }
    }
    ...
}   
```


2. 具体命令：通常会持有接收者，并调用接收者的功能来完成命令要执行的操作，这里的 DoQueueChanged 具体命令持有了接受者queue对象，将通知工作委派个接受者queue的notifyQueueChanged方法。
```java
/**
 * 队列变化通知Target, 用于通知Queue的使用者,Queue发生了改变
 * 命令模式中具体命令的实现
 */
private static class DoQueueChangedTarget extends ProcessTarget {
    /**
     * 持有相应的接收者对象: 被通知的Queue实例
     */
    private final Queue queue;

    /**
     * 构造方法，传入相应的接收者对象
     * @param q 被通知的Queue实例
     */
    public DoQueueChanged(Queue q) {
        queue = q;
    }

    @Override
    public void process() {
        // 告诉每一个队列的使用者，队列发生改变
        queue.notifyQueueChanged()
    }

}
```


3. 客户端：创建具体的命令对象，并且设置命令对象的接收者。
```java
public class Queue extends LinkedComponent {
    // 其他代码...
    private final DoQueueChangedTarget userUpdate = new DoQueueChangedTarget(this);
    private final EventHandle userUpdateHandle = new EventHandle();
    // 其他代码...
}     
```


4. 将命令对象和调度时间封装成一个事件，交给事件调度器，插入事件表。
```java
public class Queue extends LinkedComponent {
    
    // 其他代码...
    
    /**
     * 队列组件接受到新的实体
     * @param entity
     */
    @Override
    public void addEntity(Entity entity) {
        super.addEntity(entity);

        // 将临时实体封装成实体项
        QueueEntry entry = new QueueEntry(entity, getSimTime());

        // 将实体项添加到集合中
        itemSet.add(entry);

        // 通知该队列的所有用户
        if (!userUpdateHandle.isScheduled()) {
            // 封装一个0时刻后执行userUpdateTarget操作的事件，交给事件调度器，插入事件表
            EventManager.scheduleSeconds(0, 2, false, userUpdateTarget, userUpdateHandle);
        }
    }
    
    // 其他代码...
    
}
```

#### DESSIM 运行流程

##### 初始化操作
当用户通过创建对象，设置好组件之间的链接关系和参数，构建好模型后，会调用DesSim.initModel() 进行初始操作。初始化操做首先清空事件管理器，再封装一个 0 被调度的初始化事件，它包含一个初始化命令对象，该事件会被加入到事件表中，在0时刻被调度执行。如果是Generator模式，DesSim.initModel() 方法还会接受一个起始时间initTime参数，初始化后仿真时钟被推进到 initTime 时刻。如果是Launcher模式，仿真时钟仍留在0时刻。

初始化事件中的初始化命令对象如下，它首先会调用模型中所有组件的earlyInit()初始化方法去给每个组件设置默认值，然后再会将每个模型和它的 startUp() 启动方法封装成一个具体的 StartUpTarget 启动命令。再封装成启动事件，加入到事件表中等待执行。


```java
/**
 * 初始化模型的target,在模型运行前,初始化模型中各个组件（命令模式中的 ConcreteCommand）
 * @date: 12/23/2020 12:18 PM
 */
@Slf4j
public class InitModelTarget extends ProcessTarget {

    @Override
    public void process() {
        // 初始化每一个实体
        for (Entity each : Entity.getClonesOfIterable(Entity.class)) {
            each.earlyInit();
        }

        // 调用每一个实体的启动方法
        long startTime = 0;
        for (Entity each : Entity.getClonesOfIterator(Entity.class)) {
            EventManager.scheduleTicks(startTime, 5, true, new StartUpTarget(each), null);
        }
    }

    @Override
    public String getDescription() {
        return "SimulationInit";
    }
}
```


##### 实体的产生
Generator 模式中，EntityGenerator组件的 startUp() 方法调用了startAction()，它将 ①生成临时实体的操作，②将临时实体传递给下一个组件的操作，③重新调用startAction() 方法产生实体 等操作封装到 endActionTarget 具体命令对象当中，再封装成事件，插入事件表中等待执行。Launcher 模式中，只有被调用才会去封装产生实体事件，事件的发生时间也是由用户指定。
```java
public class EntityGenerator extends LinkedService {
    
    // 其他代码...

    @Override
    public void startUp() {
        super.startUp();
        // Start generating entities
        this.startAction();
    }

    /**
     * 组件开始处理实体
     */
    protected final void startAction() {
        // 获取仿真时间
        double simTime = this.getSimTime();
        boolean bool = this.startProcessing(simTime);
        if (!bool) {
            this.stopAction();
            return;
        }

        // 设置状态
        if (!isBusy()) {
            this.setBusy(true);
            this.setPresentState();
        }

        // 调度服务完成
        startTime = simTime;
        duration = this.getProcessingTime(simTime);
        this.scheduleProcess(duration, 5, endActionTarget, endActionHandle);
    }


    @Override
    protected void endProcessing(double simTime) {
        // 创建一个新的实体
        int num = (int) entitiesPerArrival;
        for (int i = 0; i < num; i++) {
            numberGenerated++;
            Entity proto = prototypeEntity;
            StringBuilder sb = new StringBuilder();
            sb.append(this.getName()).append("_").append(numberGenerated);
            Entity entity = Entity.fastCopy(proto, sb.toString());
            entity.earlyInit();
            // 将实体传送给链中的下一个元素
            this.sendToNextComponent(entity);

        }
    }
    // 其他代码...
}
```


##### 实体在组件之间传递
临时实体由Generator或者Launcher组件产生后会传递给下游组件，例如Server组件接收到实体后，会判断当前是否忙碌，若忙碌，则将实体放到为它服务的队列组件中，待空闲时再从队列中拿实体，否则话，开始模拟实体加工过程，它会将实体加工过程的一些动作（包括更新系统状态，将实体传递给下一个组件等）操作封装成一个实体离开的未来事件，加入事件表中。当时间调度器执行到该事件时，临时实体也会再次传递给后继组件，直到被回收。所以实体在组件之间的传递，也是通过事件来驱动的。一直这样执行直到仿真停止。而在仿真时钟推进的过程中，各个组件累计的统计数据就是我们需要的分析结果。


##### 事件的调度 
在DESSIM中，事件管理器EventManager负责事件调度，仿真时钟的推进，事件表的管理和事件的封装。
事件调度及仿真时钟的推进的原理如下面的伪代码所示，（由于细节非常复杂，只能在这简单介绍了）事件调度会在一个while循环中反复执行，首先读取队首事件，若队首事件为空或仿真时钟已达到目标时间，则停止执行。若队首事件的调度时间等于当前仿真时钟的话，就执行队首事件，并将以执行事件删除。若队首事件的调度时间大于仿真时间的话，则推进仿真时钟
```java
//事件调度原理介绍伪代码
while (true) {

    从时间表中获取队首事件；
    
    if (队首事件为空 || 仿真时钟已经到达目标时间) {
    	停止执行；
    }
    
    if (队首事件的调度时间 == 当前仿真时钟) {
    	执行该队首事件；
        将该队首事件删除；
    }
    
    if (队首事件的调度时间 > 当前仿真时钟) {
    	if (队首事件的时间 > 仿真的目标时间) ｛
        	推进仿真时钟到目标时间；
        ｝else {
        	推进仿真时钟到队首事件的调度时间
        }
    }
    
}
```




这里的事件表是一个优先队列，它将队列中的事件会按照调度时间和优先级进行排序。若出现了事件的调度时间相同的情况，则会按照事件优先级属性进行排序。具体实现是一个红黑树。此外，EventManager 还对外提供了封装事件的方法， 用户调用该方法传入执行操作的线程，等待时间，命令对象，优先级，等参数，就可以封装一个事件，并加入到事件表的合适位置。
```java
public final class EventManager {
    // 其他代码...
    private void scheduleTicks(Process cur, long waitLength, int eventPriority, boolean fifo, ProcessTarget t, EventHandle handle) {
        assertCanSchedule();
        long schedTick = calculateEventTime(waitLength);
        EventNode node = getEventNode(schedTick, eventPriority);
        Event evt = getEvent(node, t, handle);

        if (handle != null) {
            if (handle.isScheduled()) {
                throw new ProcessError("Tried to schedule using an EventHandle already in use");
            }
            handle.event = evt;
        }
        if (trcListener != null) {
            disableSchedule();
            trcListener.traceSchedProcess(schedTick, eventPriority, t);
            enableSchedule();
        }
        node.addEvent(evt, fifo);
    }
    // 其他代码...
}
```

#### 使用指南

使用的前将DesSim打包成jar包，并在你的项目中引用。[[参考]](https://www.jianshu.com/p/257dcca702f7)

##### Generator模式示例

```java
// ************************************************
// 定义模型, 同时设置标识符，(先定义出所有组件，在给组件赋值)
// ************************************************

EntityGenerator generator = new EntityGenerator("EntityGenerator");
SimEntity simEntity = new SimEntity("DefaultEntity");
Queue queue1 = new Queue("Queue1");
Queue queue2 = new Queue("Queue2");
Server server1 = new Server("Server1");
Server server2 = new Server("Server2");
EntitySink sink = new EntitySink("EntitySink");

// ******************************
// 为模型属性赋值
// ******************************

generator.setNextComponent(queue1);
generator.setEntitiesPerArrival(1);
generator.setFirstArrivalTime(7);
generator.setInterArrivalTime(7);
generator.setPrototypeEntity(simEntity);

server1.setWaitQueue(queue1);
server1.setServiceTime(5);
server1.setNextComponent(queue2);

server2.setWaitQueue(queue2);
server2.setServiceTime(5);
server2.setNextComponent(sink);

// ********************************
// 运行模型
// ********************************

// 初始化模型（模型类别和初始化时间）
DesSim.initModel(DesSim.Type.Generator, 0);

log.debug("hasEvent:{}", DesSim.hasEvent());
log.debug("minEventTime:{}", DesSim.nextEventTime());

// 仿真时钟推进到 50时刻
DesSim.resume(50);

log.debug("hasEvent:{}", DesSim.hasEvent());
log.debug("minEventTime:{}", DesSim.nextEventTime());

// *******************************
// 获取统计数据
// *******************************

log.debug("{}", DesSim.getEntity("Server1").getClass());
log.debug("{}", DesSim.getTimePointList().toString());
log.debug("{}", DesSim.getDataList("Server1", DesSim.NumberAdded).toString());
log.debug("{}", DesSim.getDataList("Server1", DesSim.NumberProcessed).toString());
log.debug("{}", DesSim.getDataList("Server1", DesSim.NumberInProgress).toString());
```



##### Launcher模式示例

```java
// *************************************************
// 定义模型, 同时设置标识符，(先定义出所有组件，在给组件赋值)
// *************************************************

EntityLauncher launcher = new EntityLauncher("launcher");
Queue queue1 = new Queue("queue1");
Queue queue2 = new Queue("queue2");
Server server1 = new Server("server1");
Server server2 = new Server("server2");
EntitySink sink = new EntitySink("sink");

// ******************************
// 为模型属性赋值
// ******************************

launcher.setNextComponent(queue1);
server1.setWaitQueue(queue1);
server1.setServiceTime(5);
server1.setNextComponent(queue2);
server2.setWaitQueue(queue2);
server2.setServiceTime(5);
server2.setNextComponent(sink);


// ********************************
// 运行模型
// ********************************

DesSim.initModel(DesSim.Type.Launcher);

log.debug("hasEvent:{}", DesSim.hasEvent());
log.debug("nextEventTime:{}", DesSim.nextEventTime());
log.debug("currentTime:{}", DesSim.currentSimTime());

DesSim.inject(0, 1);

log.debug("hasEvent:{}", DesSim.hasEvent());
log.debug("nextEventTime:{}", DesSim.nextEventTime());
log.debug("currentTime: {}", DesSim.currentSimTime());

// 仿真时钟推进到 7时刻
DesSim.resume(7);

// 7时刻注入一个实体
DesSim.inject(7,1);

// 仿真时钟推进到 15时刻
DesSim.resume(15);

// 15时刻注入一个实体
DesSim.inject(15, 1);

// 仿真时钟推进到30时刻
DesSim.resume(30);

log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");

// *******************************
// 获取统计数据
// *******************************

// 输出时钟序列
log.debug("Server:");
log.debug("{}", DesSim.getTimePointList().toString());
log.debug("{}",server1.getNumAddList().toString());
log.debug("{}",server1.getNumProcessedList().toString());
log.debug("{}", server1.getNumInProgressList().toString());
```
