#include <jni.h>
#include <string>
#include "config.h"
#include "jvmti_callbacks.h"

static jvmtiEnv *localJvmtiEnv;

jvmtiEnv *CreateJvmtiEnv(JavaVM *vm) {
    jvmtiEnv *jvmti_env;
    jint result = vm->GetEnv((void **) &jvmti_env, JVMTI_VERSION_1_2);
    if (result != JNI_OK) {
        return nullptr;
    }

    return jvmti_env;
}

/**
 * 设置监听事件
 * @param jvmti  jvmti指针
 * @param mode  是否开启
 * @param event_type  监听的事件
 */
void SetEventNotification(jvmtiEnv *jvmti, jvmtiEventMode mode,
                          jvmtiEvent event_type) {
    jvmtiError err = jvmti->SetEventNotificationMode(mode, event_type, nullptr);
}

/**
 * 开启JVMTI所有的调试能力
 * @param jvmti
 */
void SetAllCapabilities(jvmtiEnv *jvmti) {
    jvmtiCapabilities caps;
    jvmtiError error;
    error = jvmti->GetPotentialCapabilities(&caps);
    error = jvmti->AddCapabilities(&caps);
}

/**
 * JVMTI加载时回调
 */
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