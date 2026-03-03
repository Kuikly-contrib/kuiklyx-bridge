package com.tencent.kuiklyx.knative.bridge.plugin

import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.kuiklyx.knative.bridge.ctx.KNativeContext

/**
 * Kuikly PluginMethod
 *
 * Copyright (c) 2024 TENCENT. All rights reserved.
 */
interface KuiklyPluginMethod {

    /**
     * 插件方法调用
     * - 按需实现
     *
     * @param context  上下文封装
     * @param params   String or null
     * @param callback callback
     */
    fun invoke(
        context: KNativeContext,
        params: String?,
        callback: KuiklyRenderCallback?
    ): Any? {
        return null
    }

    /**
     * 插件方法调用
     * - 按需实现
     *
     * @param context  上下文封装
     * @param params   Any
     * @param callback callback
     */
    fun invoke(
        context: KNativeContext,
        params: Any?,
        callback: KuiklyRenderCallback?
    ): Any? {
        return null
    }

    fun onDestroy() {}
}