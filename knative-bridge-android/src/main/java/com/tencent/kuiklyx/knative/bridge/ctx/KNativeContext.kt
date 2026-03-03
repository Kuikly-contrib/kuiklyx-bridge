package com.tencent.kuiklyx.knative.bridge.ctx

import android.app.Activity
import android.view.ContextThemeWrapper
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule

/**
 * Kuikly native 上下文
 *
 * Copyright (c) 2023 TENCENT. All rights reserved.
 */
class KNativeContext(
    private val module: KuiklyRenderBaseModule,
    private val contextActivity: Activity? = null,
) {

    /**
     * 获取当前 activity
     *
     * @return activity or null
     */
    fun getCurrentActivity(): Activity? {
        if (contextActivity != null) {
            return contextActivity
        }
        if (module.activity != null) {
            return module.activity
        }
        if (module.context is ContextThemeWrapper) {
            val ctx = (module.context as ContextThemeWrapper).baseContext
            if (ctx is Activity) {
                return ctx
            }
        }
        return null
    }
}