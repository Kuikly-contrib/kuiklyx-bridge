package com.tencent.kuiklyx.bridge.ext

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * Json Result
 * Copyright (c) 2024 TENCENT. All rights reserved.
 */
class JsonResult {

    val code: Int
    val msg: String
    val data: JSONObject?

    /**
     * 使用 result(包含 code, msg, data) 进行构造
     */
    constructor(result: JSONObject?) {
        this.code = result?.optInt(CODE, FAILED) ?: FAILED
        this.msg = result?.optString(MSG) ?: ""
        this.data = result?.optJSONObject(DATA)
    }

    constructor(code: Int, msg: String? = "", data: JSONObject? = null) {
        this.code = code
        this.msg = msg ?: ""
        this.data = data
    }

    /**
     * convert data to T
     * - 若 T 类型非 IResultData, 可自行扩展, 参考宿主 JsonResultExt#foldJce
     *
     * @param transform transform data to T
     * @return data
     */
    inline fun <reified T : IResultData> toData(transform: (JSONObject?) -> T?): T? {
        return data?.let { transform(it) }
    }

    /**
     * fold result
     * - 若 T 类型非 IResultData, 可自行扩展, 参考宿主 JsonResultExt#foldJce
     *
     * @param transform transform data to T or null
     * @param callback callback result or null
     *
     */
    inline fun <reified T : IResultData> fold(
        transform: (JSONObject?) -> T?,
        callback: (T?) -> Unit
    ) {
        if (SUCCESS == code && data != null) {
            callback(data.let(transform))
        } else {
            callback(null)
        }
    }

    /**
     * fold result
     * - 若 T 类型非 IResultData, 可自行扩展, 参考宿主 JsonResultExt#foldJce
     *
     * @param transform transform data to T
     * @param success success callback
     * @param failed failed callback JsonResult
     */
    inline fun <reified T : IResultData> fold(
        transform: (JSONObject?) -> T?,
        success: (T?) -> Unit,
        failed: (JsonResult) -> Unit
    ) {
        if (SUCCESS == code && data != null) {
            success(data.let(transform))
        } else {
            failed(this)
        }
    }

    override fun toString(): String {
        return "code=$code, msg=$msg, data=$data"
    }

    /**
     * is success
     *
     * @return true is successful
     */
    fun isSuccess(): Boolean {
        return SUCCESS == code
    }

    /**
     * is failed
     *
     * @return true is failed
     */
    fun isFailed(): Boolean {
        return SUCCESS != code
    }

    /**
     * result to JSONObject
     *
     * @return JSONObject
     */
    fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(CODE, code)
            put(MSG, msg)
            put(DATA, data)
        }
    }

    companion object {
        private const val CODE = "code"
        private const val MSG = "msg"
        private const val DATA = "data"
        const val SUCCESS = 0
        const val FAILED = -1
    }
}

/**
 * 结果数据解析接口
 */
interface IResultData {
    fun decode(data: JSONObject?): IResultData?
}

/**
 * 统一 JsonResult 回调类型
 */
typealias JsonResultCallback = (data: JsonResult) -> Unit