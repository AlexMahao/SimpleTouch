
![](img/simple_touch_log.png)

<br/>

> 一个用于监听android事件分发流程的库，两行代码即可在运行时期监听事件的分发流程。

在编写一些复杂的布局时，常常由于事件分发到底是哪个`view`处理产生困扰，做法通常需要经过以下步骤:

- 自定义一个`View`，重写`disaptchTouchEvent`等方法。
- 添加`log`日志。
- 然后替换布局文件。
- 编译，通过控制台查看事件分发流程。
- 继续自定义`View` .... 如果没有发现问题，无线循环...
- 问题解决，删除之前定义的`View`，还原布局文件。

那么有没有这样的一个库，在运行时期可以动态的查看事件分发流程，不需要自定义`View`，不需要替换布局文件，不需要编写`log`，不需要每次都重新编译呢，基于这个理念，最终诞生了这个库`SimpleTouch`。


`SimpleTouch`能够在运行时期打印所有的事件分发日志，同时提供以每一次手指点击到离开为一个流程，以`json`格式写入到磁盘，便于反复分析。

对于一次完整的手指点击，事件分发的控制台日志如下

![](img/simple_touch_console.png)

### 支持功能

- 监听`View`的`dispatchTouchEvent`，`onTouchEvent`，`onInterceptTouchEvent`。
- 运行时期动态打印事件分发流程。
- 每一次完整的事件分发记录以`json`的形式写入文件。
- 去重功能，对此相同的`move`事件会自动省略。
- 提供`no-op`版本，使用时可区分`debug`和`release`。
- 提供不同模式显示


### 引入

#### 添加依赖

在项目的`app`下的`build.gradle`中添加依赖

```
debugApi 'com.spearbothy:simple-touch:1.0.3'
releaseApi 'com.spearbothy:simple-touch-no-op:1.0.3'
```

#### 初始化

在项目的`Application`的`onCreate()`中调用初始化方法`Touch.inject(this);`

```java
Touch.init(this, new Config().setSimple(false));

```	

`Config`对象提供一些配置选项

```java
public class Config {

    // 输出的日志以极简模式输出
    private boolean isSimple = true;
    // 是否延迟打印日志，延迟打印日志会在触摸事件结束之后打印，并且具有去重功能
    private boolean isDelay = true;
    // 是否保留重复的，默认不保留
    private boolean isRepeat = false;
    // 是否写入到文件
    private boolean isPrint2File = true;
}

```

#### 注入

在`Activity`的`onCreate()`的`super.onCreate(savedInstanceState);`之前调用.

```java
  @Override
    protected void onCreate(Bundle savedInstanceState) {
        Touch.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRootView = (LinearLayout) findViewById(R.id.root);
    }
```

#### 使用

编译完成之后，打开app，开始触摸吧！！！ 每一次手指离开到触摸请间隔大于1s，目的是对于每次触摸加以区分，暂时没想到合适的判断条件。

#### 备注

- 提供了`no-op`版本，该版本中包含有初始化和注入方法的空实现，以达到`debug`和`release`使用不同的版本，使`release`不包含任何注入和初始化逻辑。
- 在注入的时候有点耗时，如果页面过于复杂，会有种页面卡顿的感觉.

### 功能列表

#### `Config`配置说明

在初始化时，可以传入`Config`添加自定义配置，现提供的配置选项如下：

- `setSimple` : `log`是否已简单的模式进行输出，每一条`log`的详细程度。
- `setDelay` : 是否延迟打印日志，在控制台打印日志分为两种，一种是边触摸边打印(false)，一种是这次完成之后再打印(true)。延迟打印的好处在于，对于一次触摸可能会有多次`MOVE`事件，延迟打印可以对日志进行去重
- `setRepeat` : 是否去重，主要用于延迟打印和输出到文件时。
- `setPrint2File` : 是否将此次触摸日志保存到文件中。


#### 保存到文件说明

对于一次触摸事件，通过`logcat`显示，会有一种密密麻麻的感觉，不太好展示层次逻辑等。最好的方式其实是生成流程图，但是暂时没有好的想法生成流程图，最终以`json`的方式输出到文件中。

`json`具有一定的层次结构，所以在输出时加入了一些对应的结构话，便于展示事件流程。

对于一次手指按下到离开的操作，会有多次以根节点到子节点的方法调用，那么每一次完整的方法调用认为是一次流程，及每一次完整的方法调用可以是指`DOWN`或者`UP`或者`MOVE`等。

每一次完整的方法调用认为是一个对象，多次以一个数组的形式进行组装。

![](img/simple_touch_file.png)

在这个流程图中，`calls`是一个数组，代表方法的调用，包含当前类的方法以及对应子`View`的方法。


### 关于

有任何疑问可以通过`issue`或者以邮件的形式发送到`zziamahao@163.com`




