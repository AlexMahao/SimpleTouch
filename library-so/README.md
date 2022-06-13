
### 重构版本优点

原方案：
- 添加原框架maven依赖，并在activity中调用init方法。
- 在运行时动态生成View的hook dex，在加载到内存中。

新方案：

使用JVMTI监听Debug应用的方法回调。

优点：
- 不需要在apk里面添加依赖，即硬编码，通过adb命令即插即用。
- 不生成代理View，减少内存占用

缺点：
- 仅支持Android8.0以上的Debug应用


### 使用方式

#### 1、将so文件放入待测试应用的私有目录

1）选择当前应用对应arm版本的 `SO`文件位置：[SO文件地址](./so)

2）通过AS左侧的Device File Explorer工具，将`SO`文件放入到当前待测试应用的私有目录。

以当前demo工程举例，放入文件位置为：
```
/data/data/com.spearbothy.simpletouch/libsimple_touch.so
```

> 上步骤2），因为手机没有root，暂时没有找到合适的adb命令直接push...

#### 2、通过adb命令启动应用并挂载代理

```shell
adb shell am start --attach-agent /data/data/com.spearbothy.simpletouch/libsimple_touch.so -S com.spearbothy.simpletouch/.MainActivity
```

#### 3、控制台过滤TAG`Simple_Touch`，并触摸应用，可看到日志

```
// 线程#全路径类名+hashCode#方法名#ActionMode#方法走向(>>调用 << 返回) 返回值
I/Simple_Touch: main#Lcom/android/internal/policy/DecorView;185412568#dispatchTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Landroidx/appcompat/app/AppCompatDelegateImpl$AppCompatWindowCallback;151497521#dispatchTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Lcom/spearbothy/simpletouch/MainActivity;66354895#dispatchTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Lcom/android/internal/policy/DecorView;185412568#dispatchTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Lcom/android/internal/policy/DecorView;185412568#onInterceptTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Lcom/android/internal/policy/DecorView;185412568#onInterceptTouchEvent()#ACTION_DOWN << false
I/Simple_Touch: main#Landroid/widget/LinearLayout;122880534#dispatchTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Landroid/widget/LinearLayout;122880534#onInterceptTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Landroid/widget/LinearLayout;122880534#onInterceptTouchEvent()#ACTION_DOWN << false
I/Simple_Touch: main#Landroid/widget/FrameLayout;201246615#dispatchTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Landroid/widget/FrameLayout;201246615#onInterceptTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Landroid/widget/FrameLayout;201246615#onInterceptTouchEvent()#ACTION_DOWN << false
I/Simple_Touch: main#Landroidx/appcompat/widget/ActionBarOverlayLayout;34942340#dispatchTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Landroidx/appcompat/widget/ActionBarOverlayLayout;34942340#onInterceptTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Landroidx/appcompat/widget/ActionBarOverlayLayout;34942340#onInterceptTouchEvent()#ACTION_DOWN << false
I/Simple_Touch: main#Landroidx/appcompat/widget/ContentFrameLayout;251316589#dispatchTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Landroidx/appcompat/widget/ContentFrameLayout;251316589#onInterceptTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Landroidx/appcompat/widget/ContentFrameLayout;251316589#onInterceptTouchEvent()#ACTION_DOWN << false
I/Simple_Touch: main#Landroid/widget/LinearLayout;24455586#dispatchTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Landroid/widget/LinearLayout;24455586#onInterceptTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Landroid/widget/LinearLayout;24455586#onInterceptTouchEvent()#ACTION_DOWN << false
I/Simple_Touch: main#Lcom/spearbothy/simpletouch/CustomView;115230259#dispatchTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Lcom/spearbothy/simpletouch/CustomView;115230259#onTouchEvent()#ACTION_DOWN >>
I/Simple_Touch: main#Lcom/spearbothy/simpletouch/CustomView;115230259#onTouchEvent()#ACTION_DOWN << true
I/Simple_Touch: main#Lcom/spearbothy/simpletouch/CustomView;115230259#dispatchTouchEvent()#ACTION_DOWN << true
I/Simple_Touch: main#Landroid/widget/LinearLayout;24455586#dispatchTouchEvent()#ACTION_DOWN << true
I/Simple_Touch: main#Landroidx/appcompat/widget/ContentFrameLayout;251316589#dispatchTouchEvent()#ACTION_DOWN << true
I/Simple_Touch: main#Landroidx/appcompat/widget/ActionBarOverlayLayout;34942340#dispatchTouchEvent()#ACTION_DOWN << true
I/Simple_Touch: main#Landroid/widget/FrameLayout;201246615#dispatchTouchEvent()#ACTION_DOWN << true
I/Simple_Touch: main#Landroid/widget/LinearLayout;122880534#dispatchTouchEvent()#ACTION_DOWN << true
I/Simple_Touch: main#Lcom/android/internal/policy/DecorView;185412568#dispatchTouchEvent()#ACTION_DOWN << true
I/Simple_Touch: main#Lcom/spearbothy/simpletouch/MainActivity;66354895#dispatchTouchEvent()#ACTION_DOWN << true
I/Simple_Touch: main#Landroidx/appcompat/app/AppCompatDelegateImpl$AppCompatWindowCallback;151497521#dispatchTouchEvent()#ACTION_DOWN << true
I/Simple_Touch: main#Lcom/android/internal/policy/DecorView;185412568#dispatchTouchEvent()#ACTION_DOWN << true

```

### 原理介绍

> 核心原理及文档
> - [JVMTI接口文档](https://docs.oracle.com/javase/8/docs/platform/jvmti/jvmti.html)
> - [Android JVMTI 支持](https://source.android.google.cn/devices/tech/dalvik/art-ti?hl=zh-cn)
> 
> 其余相关文档可搜索JVMTI等查询。

#### 1、编写JVMTI `Agent_OnAttach`回调实现

具体源码：[Agent_OnAttach](./src/main/cpp/simple_touch.cpp)

```c++
extern "C" JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM *vm, char *options, void *reserved) {
    ALOGI("Agent_OnAttach options %s", options);
    // 创建jvmti指针
    localJvmtiEnv = CreateJvmtiEnv(vm);

    if (localJvmtiEnv == nullptr) {
        ALOGI("Agent_OnAttach  jvmti_env err");
        return JNI_ERR;
    }
    // 开启调试功能
    SetAllCapabilities(localJvmtiEnv);

    // 设置方法进入&方法退出的回调方法，回调至JvmtiCallbacks
    jvmtiEventCallbacks callbacks;
    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.MethodEntry = &JvmtiCallbacks::methodEntry;
    callbacks.MethodExit = &JvmtiCallbacks::methodExit;
    int error = localJvmtiEnv->SetEventCallbacks(&callbacks, sizeof(callbacks));

    ALOGI("Agent_OnAttach_callbacks  SetEventCallbacks result = %d", error);

    // 设置监听的事件：方法进入&方法退出
    SetEventNotification(localJvmtiEnv, JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY);
    SetEventNotification(localJvmtiEnv, JVMTI_ENABLE, JVMTI_EVENT_METHOD_EXIT);

    return JNI_OK;
}
```

#### 2、监听到回调后，对事件进行过滤，并输出日志

具体源码：[jvmti_callbacks.cpp](./src/main/cpp/jvmti_callbacks.cpp)

回调方法的签名参数如下：

```c++
   static void JNICALL methodEntry(jvmtiEnv *jvmti_env, // jvmti指针
                                    JNIEnv *jni_env, // jni指针
                                    jthread thread, // 所在线程
                                    jmethodID method // 方法标识
    );

    static void JNICALL methodExit(jvmtiEnv *jvmti_env, // jvmti 指针
                                   JNIEnv *jni_env, // jni指针
                                   jthread thread, // 所在线程
                                   jmethodID method, // 方法标识
                                   jboolean was_popped_by_exception, // 是否是因为异常退出
                                   jvalue return_value // 返回值
                                   );
```

在回调方法中，我们结合`jvmti`指针&`jni`指针暴露的方法，获取更多信息，具体信息如下：

```c++
    // 获取方法签名
    char *methodName;
    char *methodSignature;
    jvmti_env->GetMethodName(method, &methodName, &methodSignature, NULL);
    // 根据方法签名判断是否是 onTouchEvent等
    if (!Utils::getInstance()->isTargetMethod(methodName)) {
        return;
    }

    // 获取线程信息
    char *threadName;
    Utils::getInstance()->getThreadName(jvmti_env, thread, &threadName);

    // 获取本地变量表，用于获取方法入参
    jint entry_count = 0;
    jvmtiLocalVariableEntry *table_ptr = NULL;
    jvmti_env->GetLocalVariableTable(method, &entry_count, &table_ptr);

    const char *motionEvent;
    for (int j = 0; j < entry_count; j++) {
        if (strstr(table_ptr[j].signature, "MotionEvent") != NULL) {
            jobject param_obj;
            // 根据本地变量表获取到的索引获取入参对象，即MotionEvent对象
            jvmti_env->GetLocalObject(thread, 0, table_ptr[j].slot, &param_obj);
            // 调用MotionEvent对象的actionToString方法，获取ACTION_DOWN等事件类型。
            motionEvent = jni_env->GetStringUTFChars(
                    Utils::getInstance()->motionEvent_actionToString(jni_env, param_obj), 0);
            // 释放资源
            jni_env->DeleteLocalRef(param_obj);
            jvmti_env->Deallocate(reinterpret_cast<unsigned char *>(table_ptr[j].signature));
            jvmti_env->Deallocate(reinterpret_cast<unsigned char *>(table_ptr[j].name));
            jvmti_env->Deallocate(
                    reinterpret_cast<unsigned char *>(table_ptr[j].generic_signature));
        }
    }
    
    // 获取调用当前方法的对象
    jobject instance;
    jvmti_env->GetLocalInstance(thread, 0, &instance);
    // 获取当前对象的全路径类名
    jclass klass = jni_env->GetObjectClass(instance);
    char *className;
    jvmti_env->GetClassSignature(klass, &className, NULL);
    // 获取对象的hashCode
    jint hashCode;
    jvmti_env->GetObjectHashCode(instance, &hashCode);
    
    // 根据信息组装，输出日志
    // ...

```

#### 3、补充注意
`methodEntry` & `methodExit`的回调，是以单一方法粒度回调，例如子类实现了`onTouchEvent()`，并在方法内部调用了`super.onTouchEvent()`，则进入和进出对子类和父类都会回调，即共四次。

但对于当前工具，期望是以对象为粒度，即只关注某一个对象最终对事件分发的处理结果，因此加了一些代码兼容，具体如下：

```c++
 if (is_entry) {
        // 如果当前进入对象和方法和前一个相同，则只输出第一个
        string lastMethod = string(className) + to_string(hashCode) + string(methodName);
        if (!methodList.empty()) {
            string preMethod = methodList.back();
            if (lastMethod != preMethod) {
                ALOGI("%s#%s%d#%s()#%s >>", threadName, className, hashCode, methodName,
                      motionEvent);
            }
        } else {
            ALOGI("%s#%s%d#%s()#%s >>", threadName, className, hashCode, methodName, motionEvent);
        }

        methodList.push_back(lastMethod);
    } else {
        // 如果退出的对象和方法和前一个退出相同，则只输出最后一个
        methodList.pop_back();

        string lastMethod = methodList.back();
        string curMethod = string(className) + to_string(hashCode) + string(methodName);
        if (curMethod != lastMethod) {
            ALOGI("%s#%s%d#%s()#%s << %s", threadName, className, hashCode, methodName, motionEvent,
                  return_value ? "true" : "false");
        }
    }

```


