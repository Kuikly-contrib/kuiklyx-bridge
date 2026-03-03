package com.tencent.kuiklyx.bridge.module

import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.manager.BridgeManager
import com.tencent.kuikly.core.manager.PagerManager
import com.tencent.kuikly.core.module.Module
import com.tencent.kuiklyx.bridge.plugin.PluginModule

/**
 * 二级 Module 路由插件
 *
 * - 支持注册并分发二级 PluginModule
 *
 *
 * Copyright (c) 2024 TENCENT. All rights reserved.
 */
internal abstract class ProxyModule : TMBaseModule() {

    private val factory by lazy { mutableMapOf<String, () -> Module>() }
    private val submodules by lazy { mutableMapOf<String, Module>() }

    /**
     * register module plugin
     *
     * @param name    plugin key
     * @param creator module plugin creator
     */
    fun registerPluginModule(name: String, creator: () -> PluginModule) {
        factory[name] = creator
    }

    /**
     * register module plugins
     *
     * @param modules module plugins
     */
    fun registerPluginModules(modules: Map<String, () -> PluginModule>?) {
        modules?.let {
            factory.putAll(modules)
        }
    }

    /**
     * get submodule
     *
     * @param key key
     * @return module as T or null
     */
    fun <T : Module> getModule(key: String): T? {
        return getModule(key, BridgeManager.currentPageId)
    }

    /**
     * get submodule
     *
     * @param key     key
     * @param pagerId page id
     * @return module as T or null
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Module> getModule(key: String, pagerId: String): T? {
        return getPlugin(key, pagerId) as? T?
    }

    /**
     * get module plugin
     *
     * @param key     key
     * @param pagerId page id
     * @return module plugin
     */
    fun getPlugin(key: String, pagerId: String): Module? {
        val submodule = submodules[key]
        if (submodule != null) {
            return submodule
        }
        val creator = factory[key]
        return if (creator == null) {
            KLog.i(TAG, "Create submodule creator with $key failed!")
            null
        } else {
            val module = creator.invoke()
            injectVarToModule(module, pagerId)
            submodules[key] = module
            module
        }
    }

    /**
     * inject pager id in module
     *
     * @param module  module
     * @param pagerId page id
     */
    private fun injectVarToModule(module: Module, pagerId: String) {
        module.pagerId = pagerId
        try {
            module.pageData = PagerManager.getCurrentPager().pageData
        } catch (e: Exception) {
            KLog.e(TAG, "Inject pageData to module failed: $e")
        }
    }

    companion object {
        private const val TAG = "ProxyModule"
    }
}