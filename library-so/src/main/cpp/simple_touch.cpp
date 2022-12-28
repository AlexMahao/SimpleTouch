#include <jni.h>
#include <string>
#include <link.h>
#include "config.h"
#include "jvmti_callbacks.h"
#include "logger.h"
#include "release/xdl.h"

static jvmtiEnv *localJvmtiEnv;

const char *libart = nullptr;

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


extern "C"
JNIEXPORT void JNICALL initJVMTI(JNIEnv *env, jclass clazz, jclass target,
                                 jbyteArray bytes) {
    ALOGI("==========init JVMTI  =======");
    // 设置方法进入&方法退出的回调方法，回调至JvmtiCallbacks
    jvmtiEventCallbacks callbacks;
    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.MethodEntry = &JvmtiCallbacks::methodEntry;
    callbacks.MethodExit = &JvmtiCallbacks::methodExit;
    callbacks.Exception = &JvmtiCallbacks::exception;

    int error = localJvmtiEnv->SetEventCallbacks(&callbacks, sizeof(callbacks));

    ALOGI("Agent_OnAttach_callbacks  SetEventCallbacks result = %d", error);

    // 设置监听的事件：方法进入&方法退出
    SetEventNotification(localJvmtiEnv, JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY);
    SetEventNotification(localJvmtiEnv, JVMTI_ENABLE, JVMTI_EVENT_METHOD_EXIT);
    SetEventNotification(localJvmtiEnv, JVMTI_ENABLE, JVMTI_EVENT_EXCEPTION);
}

void JNICALL
JvmTINativeMethodBind(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread, jmethodID method,
                      void *address, void **new_address_ptr) {
    ALOGI("===========NativeMethodBind===============");
    jclass clazz = jni_env->FindClass("com/sparbothy/library_so/NativeLib");
    // hook init 方法，完成初始化
    jmethodID methodid = jni_env->GetStaticMethodID(clazz, "initJvmti", "()V");
    if (methodid == method) {
        *new_address_ptr = reinterpret_cast<void *>(&initJVMTI);
    }
    ALOGI("JvmTINativeMethodBind %d", jni_env);
    Utils::getInstance()->initJavaCallback(jni_env);
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
    callbacks.NativeMethodBind = &JvmTINativeMethodBind;


    int error = localJvmtiEnv->SetEventCallbacks(&callbacks, sizeof(callbacks));

    ALOGI("Agent_OnAttach_callbacks  SetEventCallbacks result = %d", error);

    // 设置监听的事件：方法进入&方法退出
    SetEventNotification(localJvmtiEnv, JVMTI_ENABLE, JVMTI_EVENT_NATIVE_METHOD_BIND);

    return JNI_OK;
}

int dl_iterate_callback(dl_phdr_info *info, size_t size, void *data) {
    // find libart
    if (strstr(info->dlpi_name, "libart.so")) {
        libart = info->dlpi_name;
    }
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_sparbothy_library_1so_NativeLib_initInReleaseMode(JNIEnv *env, jclass clazz) {
    xdl_iterate_phdr(dl_iterate_callback, nullptr, XDL_FULL_PATHNAME);
    // open libart.so
    void *dl = xdl_open(libart, XDL_ALWAYS_FORCE_LOAD);
    if (dl != nullptr) {
        LOGE("open libart.so successful!");
        // find SetJdwpAllowed function pointer
        auto (*SetJdwpAllowed)(bool) = reinterpret_cast<void (*)(bool)>(
                xdl_sym(dl, "_ZN3art3Dbg14SetJdwpAllowedEb", nullptr));
        if (SetJdwpAllowed != nullptr) {
            SetJdwpAllowed(true);
            LOGE("SetJdwpAllowed successful!");
        } else {
            LOGE("SetJdwpAllowed failed!");
        }
        // find setJavaDebuggable function pointer
        auto (*setJavaDebuggable)(void *, bool) = reinterpret_cast<void (*)(void *, bool)>(
                xdl_sym(dl, "_ZN3art7Runtime17SetJavaDebuggableEb", nullptr));
        // get runtime parameter(java Runtime??)
        void **instance_ = static_cast<void **>(
                xdl_sym(dl, "_ZN3art7Runtime9instance_E", nullptr));
        if (setJavaDebuggable != nullptr) {
            setJavaDebuggable(*instance_, true);
            LOGE("setJavaDebuggable successful!");
        } else {
            LOGE("setJavaDebuggable failed!");
        }
        xdl_close(dl);
    } else {
        LOGE("open libart.so failed!");
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_sparbothy_library_1so_NativeLib_initJvmti(JNIEnv *env, jclass clazz) {
    // 空方法，用于钩子函数，完成hook
}