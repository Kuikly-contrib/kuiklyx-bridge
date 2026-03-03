package com.tencent.kuiklyx.bridge.plugin

import com.tencent.kuikly.core.module.AnyCallbackFn
import com.tencent.kuikly.core.module.CallbackFn
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuiklyx.bridge.Bridge
import com.tencent.kuiklyx.bridge.module.TMBaseModule

/**
 * Plugin Module
 * - 本质仍为 Module, 通过 BridgeModule 进行二次分发
 * - 对应原生 PrefixPlugin
 *
 *
 * Copyright (c) 2024 TENCENT. All rights reserved.
 */
abstract class PluginModule : TMBaseModule() {

    // 插件方法
    private val methods by lazy { mutableMapOf<String, PluginMethod>() }

    override fun support(): Boolean {
        return true
    }

    /**
     * Module 名称
     *
     * @return Module 名称
     */
    override fun moduleName(): String {
        return Bridge.moduleName
    }

    /**
     * 插件名称
     *
     * @return 插件名称
     */
    abstract fun pluginName(): String

    /**
     * 获取插件方法名
     * - 调用方法名: `pluginName.methodName`
     *
     * @param method 方法名
     * @return 插件方法名: `pluginName.methodName`
     */
    protected open fun pluginMethod(method: String): String {
        val name = pluginName()
        return "$name.$method"
    }

    /**
     * 插件方法注册
     *
     * @param key    key
     * @param method 注册方法
     */
    protected fun registerMethod(key: String, method: PluginMethod) {
        methods[key] = method
    }

    /**
     * 插件方法注册
     *
     * @param pairs key to method
     */
    protected fun registerMethods(vararg pairs: Pair<String, PluginMethod>) {
        pairs.forEach { (key, method) ->
            methods[key] = method
        }
    }

    /**
     * 是否包含指定插件方法
     *
     * @param key 插件方法 key
     * @return exist or not
     */
    fun contains(key: String): Boolean {
        return this.methods.containsKey(key)
    }

    /**
     * 插件方法调用
     *
     * @param url      原始 url 地址
     * @param method   插件方法
     * @param params   插件参数
     * @param callback 插件方法回调
     * @return 返回值
     */
    fun invoke(
        url: String,
        method: String,
        params: JSONObject? = null,
        callback: CallbackFn? = null
    ): Any? {
        return methods[method]?.invoke(url, params, callback)
    }

    /**
     * call native
     * - method 将自动拼接 pluginName, 真实调用方法为 pluginName.method
     *
     * @param method 插件方法
     * @param data   请求数据
     * @param cb     callback
     * @return return value
     */
    override fun callNative(method: String, data: Any?, cb: CallbackFn?): String {
        return super.callNative(pluginMethod(method), data, cb).toString()
    }

    /**
     * call native
     * - method 将自动拼接 pluginName, 真实调用方法为 pluginName.method
     *
     * @param method 插件方法
     * @param data   请求数据 array
     * @param cb     callback
     * @return return value
     */
    override fun callNativeArray(method: String, data: Array<Any>, cb: AnyCallbackFn?) {
        super.callNativeArray(pluginMethod(method), data, cb)
    }

    /**
     * sync call<String> native
     * - method 将拼接 pluginName, 真实调用方法为 pluginName.method
     *
     * @param method 插件方法
     * @param data   请求数据
     * @param cb     callback
     * @return return value
     */
    override fun syncCallNative(method: String, data: JSONObject?, cb: CallbackFn?): String {
        return super.syncCallNative(pluginMethod(method), data, cb)
    }

    /**
     * sync call<T> native
     * - method 将拼接 pluginName, 真实调用方法为 pluginName.method
     *
     * @param method 插件方法
     * @param data   请求数据
     * @param cb     callback
     * @return return value
     */
    override fun <T> syncCallNative(method: String, data: String?, cb: CallbackFn?): T? {
        return super.syncCallNative(pluginMethod(method), data, cb)
    }

    /**
     * native 同步调用
     *
     * @param method 方法名
     * @param data   请求数据, 参数列表(参数类型仅支持String，Int，Float，ByteArray类型)
     * @param cb     请求 callback
     */
    override fun syncCallNativeArray(method: String, data: Array<Any>, cb: AnyCallbackFn?): Any? {
        return super.syncCallNativeArray(pluginMethod(method), data, cb)
    }

    /**
     * direct toNative
     */
    fun directToNative(
        keepCallbackAlive: Boolean = false,
        method: String,
        param: Any?,
        callback: CallbackFn? = null,
        syncCall: Boolean = false
    ): ReturnValue {
        return toNative(keepCallbackAlive, pluginMethod(method), param, callback, syncCall)
    }
}

internal typealias PluginMethod = (String, JSONObject?, CallbackFn?) -> Any?