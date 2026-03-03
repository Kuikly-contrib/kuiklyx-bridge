package com.tencent.kuiklyx.bridge.pager

import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuiklyx.bridge.Bridge
import com.tencent.kuiklyx.bridge.module.BridgeModule
import com.tencent.kuiklyx.bridge.plugin.PluginModule

/**
 * TMBasePager
 *
 * Copyright (c) 2025 TENCENT. All rights reserved.
 */
abstract class TMBasePager : Pager() {

    /**
     * 创建业务 Module
     *
     * - 默认添加: 通用 [BridgeModule]
     * - 子类重写方式:
     * - return super.createExternalModules().orEmpty() + mapOf("bz" to BZModule())
     *
     */
    override fun createExternalModules(): Map<String, Module>? {
        return mapOf(
            Bridge.moduleName to BridgeModule().apply {
                this.registerPluginModules(createPlugins())
            }
        )
    }

    /**
     * 创建 Bridge 插件
     *
     * @return 插件列表
     */
    abstract fun createPlugins(): Map<String, () -> PluginModule>
}