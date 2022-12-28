//
// Created by ByteDance on 2022/6/6.
//

#ifndef JVMTIDEMO_UTIL_H
#define JVMTIDEMO_UTIL_H

#include "config.h"

class Utils {
public :
    static Utils *getInstance() {
        static Utils utils;
        return &utils;
    }

    bool isTargetApp(const char *name) {
        return strstr(name, Config::getInstance()->getPackageName().c_str()) != NULL;
    }

    bool isTargetMethod(const char *name);

    void getThreadName(jvmtiEnv *env, jthread thread, char **name);

    void printProcessInfo();

    void printClassAndThread(jvmtiEnv *env, jthread thread, jclass klass, char *source);

    char *getMethodSignature(jvmtiEnv *env, jmethodID jmethodId);

    jstring toString(JNIEnv *env, jobject object);

    // ==================== 调用Java方法 =====================

    jint motionEvent_action(JNIEnv *env, jobject object);

    jstring motionEvent_actionToString(JNIEnv *env, jobject object);

    void initJavaCallback(JNIEnv *env);

    void exceptionCallback(jobject exception);
};

#endif //JVMTIDEMO_UTIL_H
