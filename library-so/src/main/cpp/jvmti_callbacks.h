//
// Created by ByteDance on 2022/6/6.
//

#ifndef JVMTIDEMO_JVMTI_CALLBACKS_H
#define JVMTIDEMO_JVMTI_CALLBACKS_H

#include "config.h"
#include "util.h"

class JvmtiCallbacks {
public:
    static void JNICALL methodEntry(jvmtiEnv *jvmti_env,
                                    JNIEnv *jni_env,
                                    jthread thread, // 所在线程
                                    jmethodID method // 方法标识
    );

    static void JNICALL methodExit(jvmtiEnv *jvmti_env,
                                   JNIEnv *jni_env,
                                   jthread thread,
                                   jmethodID method, // 方法标识
                                   jboolean was_popped_by_exception, // 是否是因为异常退出
                                   jvalue return_value);


    static void exception(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread, jmethodID method,
                          jlocation location, jobject exception, jmethodID catch_method,
                          jlocation catch_location);

private:
    static void log(jvmtiEnv *jvmti_env,
                    JNIEnv *jni_env,
                    jthread thread,
                    jmethodID method, // 方法标识
                    jboolean is_entry, // 是否是因为异常退出
                    jboolean return_value);


};

#endif //JVMTIDEMO_JVMTI_CALLBACKS_H
