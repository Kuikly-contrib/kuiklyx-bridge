package com.tencent.kuiklyx.knative.bridge.plugin

import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.kuiklyx.knative.bridge.ctx.KNativeContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Prefix Plugin
 *
 * - 规则: `前缀名.方法名`
 * - 前缀映射插件, 移除前缀后的方法名进行分发
 *
 * Copyright (c) 2024 TENCENT. All rights reserved.
 */
abstract class KuiklyPlugin : IKuiklyPlugin {

    private val methods = ConcurrentHashMap<String, KuiklyPluginMethod>()

    /**
     * 插件名称
     * - [pluginName].methodName
     *
     * @return 插件名称
     */
    abstract fun pluginName(): String

    /**
     * 注册插件方法
     *
     * @param key    key
     * @param method 插件方法
     */
    fun registerMethod(key: String, method: KuiklyPluginMethod): Boolean {
        return if (this.methods.containsKey(key)) {
            false
        } else {
            this.methods[key] = method
            true
        }
    }

    /**
     * 注册插件方法
     *
     * @param methods 插件方法集合
     */
    fun registerMethods(methods: Map<String, KuiklyPluginMethod>) {
        this.methods.putAll(methods)
    }

    /**
     * 注册插件方法
     *
     * @param key key
     * @return 插件方法
     */
    fun unregisterMethod(key: String): KuiklyPluginMethod? {
        return this.methods.remove(key)
    }

    /**
     * 插件方法调用
     *
     * @param context  ctx
     * @param method   插件方法
     * @param params   请求参数
     * @param callback callback
     */
    override fun invoke(
        context: KNativeContext,
        method: String,
        params: Any?,
        callback: KuiklyRenderCallback?
    ): Any? {
        return when (params) {
            is String? -> methods[method]?.invoke(context, params, callback)
            else -> methods[method]?.invoke(context, params, callback)
        }
    }

    /**
     * 插件方法销毁
     */
    override fun onDestroy() {
        methods.values.forEach {
            it.onDestroy()
        }
    }

    /**
     * 获取前缀
     * - `前缀名.`
     *
     * @return 前缀
     */
    fun getPrefix(): String {
        return "${pluginName()}."
    }

    /**
     * 插件方法构造
     * - 简化构造
     *
     * @return 插件方法
     */
    fun pluginMethod(invoke: (KNativeContext, String?, KuiklyRenderCallback?) -> Any?): KuiklyPluginMethod {
        return object : KuiklyPluginMethod {
            override fun invoke(
                context: KNativeContext,
                params: String?,
                callback: KuiklyRenderCallback?
            ): Any? {
                return invoke(context, params, callback)
            }
        }
    }

    /**
     * 插件方法构造
     * - 简化构造
     *
     * @return 插件方法
     */
    fun pluginMethodAny(invoke: (KNativeContext, Any?, KuiklyRenderCallback?) -> Any?): KuiklyPluginMethod {
        return object : KuiklyPluginMethod {
            override fun invoke(
                context: KNativeContext,
                params: Any?,
                callback: KuiklyRenderCallback?
            ): Any? {
                return invoke(context, params, callback)
            }
        }
    }
}

data class PrefixMethod(val prefix: String, val method: String) {

    fun toMethod(): String {
        return if (prefix.isNotEmpty()) {
            "${prefix}.$method"
        } else {
            method
        }
    }

    companion object {

        /**
         * from method to Prefix method
         *
         * @param method from method
         * @return prefix method
         */
        fun fromMethod(method: String): PrefixMethod {
            val index = method.indexOf('.')
            return if (index == -1) {
                PrefixMethod("", method)
            } else {
                try {
                    val prefix = method.substring(0, index)
                    val submethod = method.substring(index + 1)
                    PrefixMethod(prefix, submethod)
                } catch (ignore: Exception) {
                    PrefixMethod("", method)
                }
            }
        }
    }
}