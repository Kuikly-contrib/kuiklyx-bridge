package com.tencent.kuiklyx.knative.bridge

import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.kuiklyx.knative.bridge.ctx.KNativeContext
import com.tencent.kuiklyx.knative.bridge.log.KBLog

/**
 * Kuikly 桥接层
 *
 * Copyright (c) 2023 TENCENT. All rights reserved.
 */
class KRBridgeModule : KuiklyRenderBaseModule() {

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        KBLog.i(TAG, "call $method with $params")
        return KuiklyPluginManager.call(KNativeContext(this), method, params, callback)
    }

    override fun call(method: String, params: Any?, callback: KuiklyRenderCallback?): Any? {
        KBLog.i(TAG, "call $method with any.")
        return KuiklyPluginManager.call(KNativeContext(this), method, params, callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        KBLog.i(TAG, "[onDestroy]")
        KuiklyPluginManager.onDestroy()
    }

    companion object {
        private const val TAG = "BridgeModule"

        const val MODULE_NAME = "HRBridgeModule"
    }
}