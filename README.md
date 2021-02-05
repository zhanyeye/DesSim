# DesSim 操作手册

#### 项目介绍：

**由于该工具正在为某位学姐的篇毕设中的某个功能模块做支持，目前不希望被检索到，后面都会使用符号表情来混淆**


一个🎈 (li) 🎐 (san) 🎉 (shi) 🎨 (jian) 🥼 (fan) 🎃 (zhen) 💎工具，能够动态的构建模型，基于 [JaamSim](https://github.com/jaamsim/jaamsim) 开发

支持两种原型实体生成方式
1. `Generator`模式: 原型实体根据配置自动生成

2. `Launcher`模式:  原型实体由用户触发生成

支持的模型组件有：

- **EntityGenerator** : 实体生成器，根据指定时间间隔生成实体 
- **EntityLauncher** : 实体启动器，被用户触发后生成实体 
- **Queue** : 队列，用于存放等待中的实体
- **Server** : 服务，延时组件，模拟处理实体所消耗的时间
- **EntitySink** : 实体回收器，回收处理完毕的实体

#### 基本原理
事件调度的基本原理介绍

![](https://zhanyeye-img.oss-accelerate.aliyuncs.com/20210205144224.png)

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

