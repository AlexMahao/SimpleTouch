//
// Created by ByteDance on 2022/6/6.
//
#include "util.h"


void Utils::printProcessInfo() {
    int tid = (int) syscall(SYS_gettid);
    int pid = (int) syscall(SYS_getpid);
    ALOGI("pid:%d tid %d", pid, tid);
}


void Utils::getThreadName(jvmtiEnv *env, jthread thread, char **name) {
    jvmtiThreadInfo info;
    env->GetThreadInfo(thread, &info);
    *name = info.name;
}

void Utils::printClassAndThread(jvmtiEnv *env, jthread thread, jclass klass, char *source) {
    char *signature;
    env->GetClassSignature(klass, &signature, nullptr);
    char *name;
    getThreadName(env, thread, &name);
    ALOGI("%s : class = %s thread = %s", source, signature, name);
}

char *Utils::getMethodSignature(jvmtiEnv *env, jmethodID method) {

    char *methodName;
    char *methodSignature;
    env->GetMethodName(method, &methodName, &methodSignature, NULL);

    jclass klass;
    env->GetMethodDeclaringClass(method, &klass);

    char *className;
    env->GetClassSignature(klass, &className, NULL);
    char *target;
    sprintf(target, "class=%s methodName=%s,signature=%s", className, methodName, methodSignature);
    return target;
}

jstring Utils::toString(JNIEnv *env, jobject object) {
    jclass klass = env->GetObjectClass(object);
    jmethodID toStringMethodID = env->GetMethodID(klass, "toString", "()Ljava/lang/String;");
    return static_cast<jstring>(env->CallObjectMethod(object, toStringMethodID));;
}

bool Utils::isTargetMethod(const char *name) {
    for (auto method: Config::getInstance()->getTargetMethod()) {
        if (strstr(name, method.c_str()) != NULL) {
            return true;
        }
    }
    return false;
}

jstring Utils::motionEvent_actionToString(JNIEnv *env, jobject object) {
    jclass klass = env->GetObjectClass(object);
    jmethodID actionToStringMethodID = env->GetStaticMethodID(klass, "actionToString",
                                                              "(I)Ljava/lang/String;");
    return static_cast<jstring>(env->CallStaticObjectMethod(klass,
                                                            actionToStringMethodID,
                                                            motionEvent_action(env, object)));
}

jint Utils::motionEvent_action(JNIEnv *env, jobject object) {
    jclass klass = env->GetObjectClass(object);
    jmethodID getActionMethod = env->GetMethodID(klass, "getAction", "()I");
    return env->CallIntMethod(object, getActionMethod);;
}

// 回调java方法通知异常发生
static JNIEnv *jni_env = nullptr;
static jclass helperClass;

void Utils::initJavaCallback(JNIEnv *env) {
    jni_env = env;
    jclass tempHelperClass = jni_env->FindClass("com/sparbothy/library_so/NativeCallbackHelper");
    helperClass = static_cast<jclass>(env->NewGlobalRef(tempHelperClass));
}

void Utils::exceptionCallback(jobject exception) {
    jmethodID exceptionMethod = jni_env->GetStaticMethodID(helperClass, "exception", "(Ljava/lang/Object;)V");
    jni_env->CallStaticVoidMethod(helperClass, exceptionMethod, exception);
}



