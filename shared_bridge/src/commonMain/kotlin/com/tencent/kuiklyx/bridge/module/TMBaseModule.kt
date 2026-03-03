package com.tencent.kuiklyx.bridge.module

import com.tencent.kuikly.core.exception.PagerNotFoundException
import com.tencent.kuikly.core.manager.BridgeManager
import com.tencent.kuikly.core.manager.PagerManager
import com.tencent.kuikly.core.module.AnyCallbackFn
import com.tencent.kuikly.core.module.CallbackFn
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.pager.IPager

/**
 * Base Module
 *
 *
 * Copyright (c) 2024 TENCENT. All rights reserved.
 */
abstract class TMBaseModule : Module() {

    /**
     * module 是否支持
     * - 原生不存在或未声明加载的 module 无法进行调用, 否则抛出异常
     * - 原生不存在的 module, kuikly 不能使用 acquireModule 获取, 可以使用 getModule 但需处理可空
     *
     * @return true is support
     */
    abstract fun support(): Boolean

    /**
     * 获取当前 pager 实例
     * - 需在 [BridgeManager.currentPageId] 初始化完成后调用
     * - 使用方式: getPager<IPager>()?.pageData?.isIOS
     * - 使用方式: getPager<BizPager>()?.biz
     *
     * @param id 默认当前 page id
     * @return pager 实例
     */
    protected inline fun <reified T : IPager> getPager(id: String = BridgeManager.currentPageId): T? {
        return try {
            PagerManager.getPager(id) as? T
        } catch (e: Exception) {
            null
        }
    }

    /**
     * require 当前 pager 实例
     * - 需在 [BridgeManager.currentPageId] 初始化完成后调用
     * - 使用方式: requirePager<IPager>().pageData?.isIOS
     * - 使用方式: requirePager<Pager>().biz
     *
     * @param id 默认当前 page id
     * @return pager 实例
     * @throws PagerNotFoundException if getPager failed
     */
    protected inline fun <reified T : IPager> requirePager(id: String = BridgeManager.currentPageId): T {
        return try {
            PagerManager.getPager(id) as T
        } catch (e: Exception) {
            throw PagerNotFoundException("requirePager not found: $id")
        }
    }

    /**
     * native 异步调用
     *
     * @param method 方法名
     * @param data   请求数据, JSONObject or String
     * @param cb     请求 callback
     */
    protected open fun callNative(method: String, data: Any?, cb: CallbackFn?): Any? {
        val param = when (data) {
            is JSONObject -> data.toString()
            is String -> data
            else -> null
        }
        return toNative(false, method, param, cb, false).returnValue
    }

    /**
     * native 异步调用
     *
     * @param method 方法名
     * @param data   请求数据, 参数列表(参数类型仅支持String，Int，Float，ByteArray类型)
     * @param cb     请求 callback
     */
    protected open fun callNativeArray(method: String, data: Array<Any>, cb: AnyCallbackFn?) {
        return asyncToNativeMethod(method, data, cb)
    }

    /**
     * native 同步调用
     *
     * @param method 方法名
     * @param data   请求数据, JSONObject
     * @param cb     请求 callback
     */
    protected open fun syncCallNative(method: String, data: JSONObject?, cb: CallbackFn?): String {
        return toNative(false, method, data?.toString(), cb, true).returnValue.toString()
    }

    /**
     * native 同步调用
     *
     * @param method 方法名
     * @param data   请求数据, String
     * @param cb     请求 callback
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <T> syncCallNative(method: String, data: String?, cb: CallbackFn?): T? {
        return toNative(false, method, data, cb, true).returnValue as? T
    }

    /**
     * native 同步调用
     *
     * @param method 方法名
     * @param data   请求数据, 参数列表(参数类型仅支持String，Int，Float，ByteArray类型)
     * @param cb     请求 callback
     */
    protected open fun syncCallNativeArray(
        method: String,
        data: Array<Any>,
        cb: AnyCallbackFn?
    ): Any? {
        return syncToNativeMethod(method, data, cb) ?: ""
    }
}