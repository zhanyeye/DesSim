# DesSim 操作手册

### 项目介绍：
一个离散事件仿真工具，支持动态的构建模型，基于 JaamSim 开发

支持的调度模式有三种：
+ 水平（串行调度）
+ 垂直（并行调度）
+ 单机运行

支持的模型组件有：

<details>
    <summary><b>EntityGenerator</b> : 实体生成器，根据指定时间间隔生成实体 （只适用于单机模式）</summary>
    content!!!
</details>

<details>
    <summary><b>EntityLauncher</b> : 实体启动器，被用户触发后生成实体 （适用于水平，垂直模式）</summary>
    content!!!
</details>

<details>
    <summary><b>Queue</b> : 队列，用于存放等待中的实体</summary>
    content!!!
</details>

<details>
    <summary><b>Server</b> : 服务，延时组件，模拟处理实体所消耗的时间</summary>
    content!!!
</details>

<details>
    <summary><b>EntitySink</b> : 实体回收器，回收处理完毕的实体</summary>
    content!!!
</details>



### 使用指南
使用的前置操作：将DesSim打包成jar包，并在你的项目中引用。[[参考]](https://www.jianshu.com/p/257dcca702f7)


#### 水平调度示例

#### 垂直调度示例

#### 单机调度示例


