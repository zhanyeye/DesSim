# DesSim 操作手册

### 项目介绍：
一个离散事件仿真工具，支持动态的构建模型，基于 JaamSim 开发

支持的调度模式有三种：
+ 水平（串行调度）
+ 垂直（并行调度）
+ 单机运行

支持的模型组件有：

**EntityGenerator** : 实体生成器，根据指定时间间隔生成实体 （只适用于单机模式）

**EntityLauncher** : 实体启动器，被用户触发后生成实体 （适用于水平，垂直模式）

**Queue** : 队列，用于存放等待中的实体

**Server** : 服务，延时组件，模拟处理实体所消耗的时间

**EntitySink** : 实体回收器，回收处理完毕的实体


### 使用指南
使用的前将DesSim打包成jar包，并在你的项目中引用。[[参考]](https://www.jianshu.com/p/257dcca702f7)


#### 水平调度示例 （只被调度一次）
```java
// *****************************
// 定义模型, 设置标识符
// *****************************
EntityLauncher launcher = DesSim.createModelInstance("EntityLauncher", 1);
Queue queue = DesSim.createModelInstance("Queue", 2);
Server server1 = DesSim.createModelInstance("Server", 3);
Server server2 = DesSim.createModelInstance("Server", 4);
EntitySink sink = DesSim.createModelInstance("EntitySink", 5);

// ******************************
// 为模型属性赋值
// ******************************

// 设置实体启动器的后继
launcher.setNextComponent(DesSim.getEntity(2));

// 设置服务的等待队列，服务时间，服务的后继
server1.setWaitQueue((Queue) DesSim.getEntity(2));
server1.setServiceTime(2);
server1.setNextComponent(DesSim.getEntity(5));

// 设置服务的等待队列，服务时间，服务的后继
server2.setWaitQueue((Queue) DesSim.getEntity(2));
server2.setServiceTime(2);
server2.setNextComponent(DesSim.getEntity(5));

// ********************************
// 运行模型
// ********************************

// 初始化模型：
DesSim.initModel(DesSim.Type.HORIZONTAL);
// 开始水平调度：0时刻调度，注入100个实体
DesSim.serialScheduling(0, 100);

// *******************************
// 获取数据
// *******************************

// 输出时钟序列
log.debug("{}", DesSim.getTimePointList().toString());
log.debug("Server:");
log.debug("{}", DesSim.getTimePointList().toString());
log.debug("{}",server1.getNumAddList().toString());
log.debug("{}",server1.getNumProcessList().toString());
log.debug("{}", server1.getNumInProgressList().toString());
```

#### 垂直调度示例

```java
// *****************************
// 定义模型, 同时设置标识符，(先定义出所有组件，在给组件赋值)
// *****************************
EntityLauncher launcher = DesSim.createModelInstance("EntityLauncher", 1);
Queue queue1 = DesSim.createModelInstance("Queue",2);
Queue queue2 = DesSim.createModelInstance("Queue", 3);
Server server1 = DesSim.createModelInstance("Server", 4);
Server server2 = DesSim.createModelInstance("Server", 5);
EntitySink sink = DesSim.createModelInstance("EntitySink", 6);

// ******************************
// 为模型属性赋值
// ******************************
launcher.setNextComponent(queue1);
server1.setWaitQueue(queue1);
server1.setServiceTime(2);
server1.setNextComponent(queue2);
server2.setWaitQueue(queue2);
server2.setServiceTime(3);
server2.setNextComponent(sink);


// ********************************
// 运行模型
// ********************************

// 初始化
DesSim.initModel(DesSim.Type.VERTICAL);

// 在5时刻注入一个实体
DesSim.inject(5, 1);

// 运行仿真，直到时间为5时刻
DesSim.resume(5);

// 运行仿真，直到时间为5时刻
DesSim.resume(7);

// 查看事件队列中是否还有事件
log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");

// 在7时刻注入一个实体
DesSim.inject(7,1);

// 运行仿真，直到时间为10时刻
DesSim.resume(10);

// 运行仿真，直到时间为15时刻
DesSim.resume(15);

// 在15时刻注入一个实体
DesSim.inject(15, 1);

// 运行仿真，直到时间为100时刻
DesSim.resume(100);

// 查看事件队列中是否还有事件
log.debug("{}", DesSim.hasEvent() ? "has Event" : "no Event");

// *******************************
// 获取数据
// *******************************

// 输出时钟序列
log.debug("{}", DesSim.getTimePointList().toString());

log.debug("Server:");
log.debug("{}", DesSim.getTimePointList().toString());
log.debug("{}",server1.getNumAddList().toString());
log.debug("{}",server1.getNumProcessList().toString());
log.debug("{}", server1.getNumInProgressList().toString());
```



#### 单机调度示例


