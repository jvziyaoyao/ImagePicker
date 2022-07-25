package com.origeek.imagePicker.util

import android.annotation.SuppressLint
import android.app.Application

object ContextUtil {

    @SuppressLint("PrivateApi")
    fun getApplicationByReflect(): Application {
        try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val thread = activityThread.getMethod("currentActivityThread").invoke(null)
            val app = activityThread.getMethod("getApplication").invoke(thread)
                ?: throw NullPointerException("Application未初始化！")
            return app as Application
        } catch (e: Exception) {
            e.printStackTrace()
        }
        throw NullPointerException("Application未初始化！")
    }

    fun getApplication(): Application? {
        var app: Application? = null
        try {
            app = getApplicationByReflect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return app
    }

}