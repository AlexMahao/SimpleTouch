//
// Created by ByteDance on 2022/6/6.
//
#include "jvmti_callbacks.h"

// 方法进入时push，方法退出时pop
static vector<string> methodList;

void JvmtiCallbacks::methodEntry(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread,
                                 jmethodID method) {

    log(jvmti_env, jni_env, thread, method, true, false);
}


void
JvmtiCallbacks::methodExit(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread, jmethodID method,
                           jboolean was_popped_by_exception, jvalue return_value) {

    log(jvmti_env, jni_env, thread, method, false, return_value.z);
}

void JvmtiCallbacks::log(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread, jmethodID method,
                         jboolean is_entry, jboolean return_value) {

    // 获取方法签名
    char *methodName;
    char *methodSignature;
    jvmti_env->GetMethodName(method, &methodName, &methodSignature, NULL);
    // 根据方法签名判断是否是 onTouchEvent等
    if (!Utils::getInstance()->isTargetMethod(methodName)) {
        return;
    }

//    if (!Utils::getInstance()->isTargetApp(className)) {
//        return;
//    }
    // 获取线程信息
    char *threadName;
    Utils::getInstance()->getThreadName(jvmti_env, thread, &threadName);

    // 获取本地变量表，用于获取方法入参
    jint entry_count = 0;
    jvmtiLocalVariableEntry *table_ptr = NULL;
    jvmti_env->GetLocalVariableTable(method, &entry_count, &table_ptr);

    const char *motionEvent;
//    // 当前程序执行的位置
//    jlocation cur_loc;
//    jvmti_env->GetFrameLocation(thread, 0, NULL, &cur_loc);
//
//    // 返回参数的个数
//    jint param_size = 0; // 为什么是2
//    jvmti_env->GetArgumentsSize(method, &param_size);

    for (int j = 0; j < entry_count; j++) {
        if (strstr(table_ptr[j].signature, "MotionEvent") != NULL) {
            jobject param_obj;
            //
//            ALOGI("mahao %d %s %s %d>>", param_size, table_ptr[j].name, table_ptr[j].signature,
//                  table_ptr[j].slot);
            // 根据本地变量表获取到的索引获取入参对象，即MotionEvent对象
            jvmti_env->GetLocalObject(thread, 0, table_ptr[j].slot, &param_obj);
            // 调用MotionEvent对象的actionToString方法，获取ACTION_DOWN等事件类型。
            if (param_obj != NULL) {
                motionEvent = jni_env->GetStringUTFChars(
                        Utils::getInstance()->motionEvent_actionToString(jni_env, param_obj), 0);
                jni_env->DeleteLocalRef(param_obj);
            } else {
                motionEvent = "unknown";
            }

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
}


void JvmtiCallbacks::exception(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread, jmethodID method,
                               jlocation location, jobject exception, jmethodID catch_method,
                               jlocation catch_location) {
    Utils::getInstance()->exceptionCallback(exception);
}



