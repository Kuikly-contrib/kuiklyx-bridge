package com.tencent.kuiklyx.knative.bridge

import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.kuiklyx.knative.bridge.plugin.IKuiklyPlugin
import com.tencent.kuiklyx.knative.bridge.ctx.KNativeContext
import com.tencent.kuiklyx.knative.bridge.ext.JsonResult
import com.tencent.kuiklyx.knative.bridge.plugin.KuiklyPlugin
import com.tencent.kuiklyx.knative.bridge.plugin.KuiklyPluginMethod
import com.tencent.kuiklyx.knative.bridge.plugin.PrefixMethod
import java.util.concurrent.ConcurrentHashMap

/**
 * KuiklyPluginManager
 *
 * Copyright (c) 2025 TENCENT. All rights reserved.
 */
@Suppress("unused")
object KuiklyPluginManager {

    private val plugins = ConcurrentHashMap<String, KuiklyPlugin>()

    /**
     * 插件调用
     *
     * @param context  上下文
     * @param method   插件方法
     * @param params   请求参数
     * @param callback 回调
     */
    fun call(
        context: KNativeContext,
        method: String,
        params: Any?,
        callback: KuiklyRenderCallback?
    ): Any? {
        fun tryInvoke(plugin: IKuiklyPlugin?, method: String): Pair<Boolean, Any?>? {
            plugin ?: return null
            val result = plugin.let {
                when (params) {
                    is String? -> it.invoke(context, method, params, callback)
                    else -> it.invoke(context, method, params, callback)
                }
            }
            return Pair(true, result)
        }

        // 旧模式, 直接按方法名匹配
        tryInvoke(plugins[method], method)?.let { return it.second }
        // 新模式
        val prefixMethod = PrefixMethod.fromMethod(method)
        // 匹配 prefix 插件
        tryInvoke(plugins[prefixMethod.prefix], prefixMethod.method)?.let { return it.second }
        // 匹配不到
        val ret = JsonResult(-1, "NoSuchNativePlugin").toJSONObject()
        return callback?.invoke(ret)
    }

    /**
     * register plugin
     *
     * @param plugins KuiklyPlugin map
     */
    fun registerPlugins(plugins: Map<String, KuiklyPlugin>) {
        return this.plugins.putAll(plugins)
    }

    /**
     * register plugin
     *
     * @param method plugin method
     * @param plugin [KuiklyPlugin]
     * @return success return plugin
     */
    fun registerPlugin(method: String, plugin: KuiklyPlugin): KuiklyPlugin? {
        return plugins.put(method, plugin)
    }

    /**
     * unregister plugin
     *
     * @param method plugin method
     * @return success return plugin
     */
    fun unregisterPlugin(method: String): KuiklyPlugin? {
        return plugins.remove(method)
    }

    /**
     * register plugin
     *
     * @param pluginName plugin name
     * @param methodName method name
     * @param method [KuiklyPluginMethod]
     * @return success or not
     */
    fun registerPluginMethod(
        pluginName: String,
        methodName: String,
        method: KuiklyPluginMethod
    ): Boolean {
        val plugin = plugins[pluginName]
        return plugin?.registerMethod(methodName, method) ?: false
    }

    /**
     * register plugin
     *
     * @param pluginName plugin name
     * @param methodName method name
     * @return method removed
     */
    fun unregisterPluginMethod(
        pluginName: String,
        methodName: String
    ): KuiklyPluginMethod? {
        val plugin = plugins[pluginName]
        return plugin?.unregisterMethod(methodName)
    }

    /**
     * invoke when onDestroy
     */
    fun onDestroy() {
        plugins.forEach {
            it.value.onDestroy()
        }
    }
}