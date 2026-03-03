package com.tencent.kuiklyx.knative.bridge.log

import android.util.Log

/**
 * Log impl
 *
 * Copyright (c) 2023 TENCENT. All rights reserved.
 */
object KBLog {

    private const val TAG = ""
    private const val TAG_OPT = "[OPT]"

    /**
     * log switch
     */
    var enable = true

    /**
     * log impl
     */
    var logger: KBLogService? = DefaultLogImpl()

    @JvmStatic
    fun d(tag: String, msg: String?) {
        if (enable) {
            logger?.d("$TAG$tag", msg)
        }
    }

    @JvmStatic
    fun i(tag: String, msg: String?) {
        if (enable) {
            logger?.i("$TAG$tag", msg)
        }
    }

    @JvmStatic
    fun w(tag: String, msg: String?) {
        if (enable) {
            logger?.w("$TAG$tag", msg)
        }
    }

    @JvmStatic
    fun e(tag: String, msg: String?) {
        if (enable) {
            logger?.e("$TAG$tag", msg)
        }
    }

    @JvmStatic
    fun e(tag: String, msg: String, tr: Throwable) {
        if (enable) {
            logger?.e("$TAG$tag", msg, tr)
        }
    }

    @JvmStatic
    fun o(tag: String, msg: String?) {
        if (enable) {
            logger?.i("$TAG_OPT$tag", msg ?: "")
        }
    }

    private class DefaultLogImpl : KBLogService {
        override fun d(tag: String, msg: String?) {
            Log.d(tag, msg ?: "")
        }

        override fun i(tag: String, msg: String?) {
            Log.i(tag, msg ?: "")
        }

        override fun w(tag: String, msg: String?) {
            Log.w(tag, msg ?: "")
        }

        override fun e(tag: String, msg: String?) {
            Log.e(tag, msg ?: "")
        }

        override fun e(tag: String, msg: String?, e: Throwable) {
            Log.e(tag, msg ?: "", e)
        }
    }
}