package com.tencent.kuiklyx.knative.bridge.plugin

import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.kuiklyx.knative.bridge.ctx.KNativeContext

/**
 * Kuikly 插件
 *
 * Copyright (c) 2023 TENCENT. All rights reserved.
 */
interface IKuiklyPlugin {

    fun invoke(
        context: KNativeContext,
        method: String,
        params: Any?,
        callback: KuiklyRenderCallback?
    ): Any?

    fun onDestroy() {}

}