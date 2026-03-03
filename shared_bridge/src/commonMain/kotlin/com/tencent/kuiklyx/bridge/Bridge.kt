package com.tencent.kuiklyx.bridge

import com.tencent.kuikly.core.manager.BridgeManager
import com.tencent.kuikly.core.manager.PagerManager
import com.tencent.kuikly.core.module.Module
import com.tencent.kuiklyx.bridge.module.BridgeModule

/**
 * Bridge
 *
 * - 业务 Module、Plugin 获取
 * - 一级 Module 请勿随意新增, 避免动态化产物在低版本获取失败导致页面异常
 * - 优先使用 ModulePlugin
 * - module, plugin 均可空, 支持动态化产物的低版本兼容, 各业务评估低版本获取失败后的业务兼容处理
 *
 *
 * Copyright (c) 2024 TENCENT. All rights reserved.
 */
object Bridge {

    private const val DEFAULT_BRIDGE_MODULE_NAME = "HRBridgeModule"

    /**
     * 自定义 module name, 默认 `HRBridgeModule`
     */
    var moduleName: String = DEFAULT_BRIDGE_MODULE_NAME

    /**
     * 获取 bridge 插件
     *
     * @param pluginName 插件名称
     * @param id         page id
     * @return bridge plugin module
     */
    fun <T : Module> getPlugin(pluginName: String, id: String = BridgeManager.currentPageId): T? {
        return module(id)?.getModule(pluginName, id) as? T
    }

    private fun module(pagerId: String = BridgeManager.currentPageId): BridgeModule? {
        return PagerManager.getPager(pagerId).getModule(moduleName)
    }
}