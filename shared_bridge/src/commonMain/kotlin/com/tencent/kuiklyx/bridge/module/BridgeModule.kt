package com.tencent.kuiklyx.bridge.module

import com.tencent.kuiklyx.bridge.Bridge

/**
 * 通用 BridgeModule
 *
 * - 代理 Module: 用作插件分发
 * - 禁止直接添加 toNative 方法
 *
 *
 * Copyright (c) 2023 TENCENT. All rights reserved.
 */
internal class BridgeModule : ProxyModule() {

    override fun support(): Boolean {
        return true
    }

    override fun moduleName(): String {
        return Bridge.moduleName
    }
}