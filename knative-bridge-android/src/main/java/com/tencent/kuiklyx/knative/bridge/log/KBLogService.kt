package com.tencent.kuiklyx.knative.bridge.log

/**
 * Log interface
 *
 * Copyright (c) 2023 TENCENT. All rights reserved.
 */
interface KBLogService {

    fun d(tag: String, msg: String?)

    fun i(tag: String, msg: String?)

    fun w(tag: String, msg: String?)

    fun e(tag: String, msg: String?)

    fun e(tag: String, msg: String?, e: Throwable)
}