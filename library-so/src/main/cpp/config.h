//
// Created by ByteDance on 2022/6/6.
//

#ifndef JVMTIDEMO_CONFIG_H
#define JVMTIDEMO_CONFIG_H

#include <android/log.h>
#include <unistd.h>
#include <sys/syscall.h>
#include "jvmti.h"
#include "string.h"
#include <cstdio>
#include "string"
#include "vector"

#define LOG_TAG "Simple_Touch"
using namespace std;
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)


class Config {
public:
    static Config *getInstance() {
        static Config config;
        return &config;
    }

    Config() {
        method.push_back("onTouchEvent");
        method.push_back("dispatchTouchEvent");
        method.push_back("onInterceptTouchEvent");
    }

    vector<string> getTargetMethod() {
        return method;
    }

    void setPackageName(string name) {
        packageName = name;
    }

    string getPackageName() {
        return packageName;
    }

private:
    string packageName = "";
    vector<string> method;
};

#endif //JVMTIDEMO_CONFIG_H
