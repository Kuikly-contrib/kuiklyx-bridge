# kuiklyx-bridge

> **版本**: 1.0.1  

---


## 概述

`kuiklyx-bridge` 是一个基于 Kotlin Multiplatform (KMP) 的 **Kuikly 统一插件路由组件**，提供跨平台的插件注册、分发和调用能力。


---



## 依赖接入

###  Kuikly 侧（共享模块）

```kotlin
// build.gradle.kts
sourceSets {
    val commonMain by getting {
        dependencies {
            implementation("com.tencent.kuiklybase:shared_bridge:1.0.1-2.0.21")
        }
    }
}

// build.ohos.gradle.kts
val ohosArm64Main by sourceSets.getting {
    dependencies {
        implementation("com.tencent.kuiklybase:shared_bridge-ohosarm64:1.0.1-2.0.21-KBA-010")
    }
}
```
###  仓库配置

```kotlin
repositories {
    maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent") }
}
```

###  Android 原生侧

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.tencent.kuiklybase:bridge:1.0.1-2.0.21")
}
```

###  iOS 原生侧

```ruby
# Podfile
pod 'KuiklyxBridgeNative'
```


---

## Kuikly 侧使用（跨平台 Kotlin）

###  创建插件 PluginModule

继承 `PluginModule`，实现 `pluginName()` 并定义业务方法：

```kotlin
import com.tencent.kuikly.core.module.CallbackFn
import com.tencent.kuiklyx.bridge.plugin.PluginModule

/**
 * UI 相关插件
 */
internal class UIPlugin : PluginModule() {

    override fun pluginName(): String {
        return PLUGIN_NAME
    }

    /**
     * 打开 Kuikly 页面
     * 
     * @param info 页面信息
     * @param callback 回调函数（可选）
     */
    fun openKuikly(info: String, callback: CallbackFn? = null) {
        callNative(OPEN_KUIKLY, info, callback)
    }

    /**
     * 打开一个 URL
     */
    fun openUrl(url: String, callback: CallbackFn? = null) {
        callNative(OPEN_URL, url, callback)
    }

    /**
     * 同步获取某个配置值
     */
    fun getConfig(key: String): String {
        return syncCallNative<String>(GET_CONFIG, key, null) ?: ""
    }

    companion object {
        const val PLUGIN_NAME = "ui"

        private const val OPEN_KUIKLY = "openKuikly"
        private const val OPEN_URL = "openUrl"
        private const val GET_CONFIG = "getConfig"
    }
}
```

> **说明**：`callNative` 内部会自动拼接 `pluginName`，即 `callNative("openKuikly", ...)` 实际调用原生方法 `ui.openKuikly`。

###  创建页面基类 TMBasePager

继承 `TMBasePager`，注册插件和自定义 Module：

```kotlin
import com.tencent.kuikly.core.module.Module
import com.tencent.kuiklyx.bridge.pager.TMBasePager
import com.tencent.kuiklyx.bridge.plugin.PluginModule

internal abstract class BasePager : TMBasePager() {

    /**
     * 注册 Bridge 插件
     * 
     * 返回 Map<插件名, 插件工厂函数>
     * - 插件采用懒加载方式，只有首次 getPlugin 时才会创建实例
     */
    override fun createPlugins(): Map<String, () -> PluginModule> {
        return mapOf(
            UIPlugin.PLUGIN_NAME to { UIPlugin() },
            PlayerPlugin.PLUGIN_NAME to { PlayerPlugin() },
            SharePlugin.PLUGIN_NAME to { SharePlugin() },
        )
    }

    /**
     * 注册额外的 Module（可选）
     * 
     * ⚠️ 必须包含 super.createExternalModules()，否则 BridgeModule 不会被注册
     */
    override fun createExternalModules(): Map<String, Module>? {
        return super.createExternalModules().orEmpty() + mapOf(
            ReportModule.MODULE_NAME to ReportModule()
        )
    }
}
```

> **⚠️ 重要**：重写 `createExternalModules` 时 **必须** 调用 `super.createExternalModules().orEmpty()`，否则 `BridgeModule` 将不会被注册，导致所有插件不可用。

###  调用插件

在 Kuikly 页面中调用插件：

```kotlin
import com.tencent.kuiklyx.bridge.Bridge

// 获取插件并调用（推荐写法 - 安全可空链式调用）
Bridge.getPlugin<UIPlugin>(UIPlugin.PLUGIN_NAME)
    ?.openKuikly("dialog_icon_image") { result ->
        // 处理回调结果
        val jsonResult = JsonResult(result)
        if (jsonResult.isSuccess()) {
            // 成功处理
        }
    }

// 同步调用
val config = Bridge.getPlugin<UIPlugin>(UIPlugin.PLUGIN_NAME)
    ?.getConfig("theme_color") ?: "default"

// 跨页面调用插件（指定 page id）
Bridge.getPlugin<UIPlugin>(UIPlugin.PLUGIN_NAME, targetPageId)
    ?.openUrl("https://example.com")
```

> **注意**：`getPlugin` 返回可空类型 `T?`，支持动态化产物在低版本下的安全兼容。业务侧需自行处理获取失败的降级逻辑。

###  注册自定义 Module

除了 Plugin 外，也可以注册独立的 Module：

```kotlin
import com.tencent.kuiklyx.bridge.module.TMBaseModule

class ReportModule : TMBaseModule() {

    override fun support(): Boolean = true

    override fun moduleName(): String = MODULE_NAME

    fun reportEvent(event: String, params: String?) {
        callNative("report", params, null)
    }

    companion object {
        const val MODULE_NAME = "ReportModule"
    }
}
```

###  插件方法注册（PluginMethod）

在 `PluginModule` 中可以注册接收原生分发的方法回调：

```kotlin
class UIPlugin : PluginModule() {

    override fun pluginName() = "ui"

    init {
        // 注册插件方法，当原生调用 "ui.onThemeChanged" 时触发
        registerMethod("onThemeChanged") { url, params, callback ->
            val theme = params?.optString("theme")
            // 处理主题变化
            callback?.invoke(JsonResult(0, "ok").toJSONObject())
        }

        // 批量注册
        registerMethods(
            "onNetworkChanged" to { url, params, callback ->
                // 处理网络变化
                null
            },
            "onUserLogin" to { url, params, callback ->
                // 处理用户登录
                null
            }
        )
    }
}
```

###  callNative 方法详解

`PluginModule` 和 `TMBaseModule` 提供多种与原生通信的方法：

| 方法 | 说明 | 参数类型 | 返回类型 |
|:-----|:-----|:-----|:-----|
| `callNative(method, data, cb)` | **异步**调用原生 | `data: Any?`（String 或 JSONObject） | `Any?` |
| `callNativeArray(method, data, cb)` | **异步**调用原生 | `data: Array<Any>`（支持 String/Int/Float/ByteArray） | `void` |
| `syncCallNative(method, data, cb)` | **同步**调用原生 | `data: JSONObject?` | `String` |
| `syncCallNative<T>(method, data, cb)` | **同步**调用原生（泛型） | `data: String?` | `T?` |
| `syncCallNativeArray(method, data, cb)` | **同步**调用原生 | `data: Array<Any>` | `Any?` |
| `directToNative(...)` | 直接调用原生 | 完整控制参数 | `ReturnValue` |

```kotlin
// 示例：各种调用方式
class MyPlugin : PluginModule() {
    override fun pluginName() = "my"

    // 异步调用（最常用）
    fun doAction(info: String, cb: CallbackFn? = null) {
        callNative("doAction", info, cb)
    }

    // 异步调用（多参数）
    fun sendData(name: String, age: Int, cb: AnyCallbackFn? = null) {
        callNativeArray("sendData", arrayOf(name, age), cb)
    }

    // 同步调用
    fun getValue(key: String): String {
        return syncCallNative("getValue", JSONObject().apply { put("key", key) }, null)
    }

    // 直接调用（需保持回调存活）
    fun subscribe(event: String, cb: CallbackFn?) {
        directToNative(
            keepCallbackAlive = true,
            method = "subscribe",
            param = event,
            callback = cb
        )
    }
}
```

###  获取 Pager 实例

在 Module 中可获取当前或指定的 Pager 实例：

```kotlin
class MyPlugin : PluginModule() {
    override fun pluginName() = "my"

    fun checkPlatform() {
        // 安全获取 Pager（可空）
        val pager = getPager<IPager>()
        val isIOS = pager?.pageData?.isIOS ?: false

        // 要求获取 Pager（找不到时抛出 PagerNotFoundException）
        val requiredPager = requirePager<IPager>()
    }
}
```

---

## Android 原生侧使用

Android 原生侧需要接收 Kuikly 层的 `callNative` 调用，并实现具体的原生逻辑。

###  注册 KRBridgeModule

`KRBridgeModule` 是 Android 端的桥接入口，需要在 Kuikly 渲染器中注册：

```kotlin
// 在 KuiklyRender 初始化时注册
KuiklyRenderEngine.registerModule(
    KRBridgeModule.MODULE_NAME,  // "HRBridgeModule"
    KRBridgeModule::class.java
)
```

###  创建 KuiklyPlugin（前缀插件）

继承 `KuiklyPlugin`，实现 `pluginName()` 并注册方法：

```kotlin
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.kuiklyx.knative.bridge.ctx.KNativeContext
import com.tencent.kuiklyx.knative.bridge.ext.JsonResult
import com.tencent.kuiklyx.knative.bridge.plugin.KuiklyPlugin

class UIPluginNative : KuiklyPlugin() {

    override fun pluginName(): String = "ui"

    init {
        // 使用 pluginMethod 简化注册
        registerMethod("openKuikly", pluginMethod { context, params, callback ->
            val activity = context.getCurrentActivity() ?: return@pluginMethod null
            // 执行打开 Kuikly 页面逻辑
            // ...
            callback?.invoke(JsonResult(0, "success").toJSONObject())
            null
        })

        registerMethod("openUrl", pluginMethod { context, params, callback ->
            val activity = context.getCurrentActivity() ?: return@pluginMethod null
            // 执行打开 URL 逻辑
            // ...
            callback?.invoke(JsonResult(0, "success").toJSONObject())
            null
        })
    }
}
```

也可以使用 `KuiklyPluginMethod` 接口注册更复杂的方法：

```kotlin
class FilePlugin : KuiklyPlugin() {

    override fun pluginName(): String = "file"

    init {
        registerMethod("upload", object : KuiklyPluginMethod {
            override fun invoke(
                context: KNativeContext,
                params: String?,
                callback: KuiklyRenderCallback?
            ): Any? {
                // 处理文件上传
                return null
            }

            override fun invoke(
                context: KNativeContext,
                params: Any?,
                callback: KuiklyRenderCallback?
            ): Any? {
                // 处理 Any 类型参数
                return null
            }

            override fun onDestroy() {
                // 清理资源
            }
        })
    }
}
```

###  注册插件到 KuiklyPluginManager

在 App 初始化时注册插件：

```kotlin
// 单个注册
KuiklyPluginManager.registerPlugin("ui", UIPluginNative())

// 批量注册
KuiklyPluginManager.registerPlugins(mapOf(
    "ui" to UIPluginNative(),
    "player" to PlayerPluginNative(),
    "share" to SharePluginNative(),
))
```

###  动态注册/注销插件方法

可以在运行时动态添加或移除某个插件的方法：

```kotlin
// 动态注册方法到已有的 "ui" 插件
KuiklyPluginManager.registerPluginMethod(
    pluginName = "ui",
    methodName = "showToast",
    method = object : KuiklyPluginMethod {
        override fun invoke(
            context: KNativeContext,
            params: String?,
            callback: KuiklyRenderCallback?
        ): Any? {
            Toast.makeText(context.getCurrentActivity(), params, Toast.LENGTH_SHORT).show()
            callback?.invoke(JsonResult(0).toJSONObject())
            return null
        }
    }
)

// 动态注销方法
KuiklyPluginManager.unregisterPluginMethod("ui", "showToast")

// 注销整个插件
KuiklyPluginManager.unregisterPlugin("ui")
```

###  上下文 KNativeContext

`KNativeContext` 封装了 Kuikly 原生上下文信息：

```kotlin
// 在插件方法中使用
pluginMethod { context, params, callback ->
    // 获取当前 Activity
    val activity = context.getCurrentActivity()

    if (activity != null) {
        // 执行需要 Activity 的操作
        activity.startActivity(Intent(activity, TargetActivity::class.java))
    }
    null
}
```

###  自定义日志

可替换默认的日志实现：

```kotlin
// 关闭日志
KBLog.enable = false

// 自定义日志实现
KBLog.logger = object : KBLogService {
    override fun d(tag: String, msg: String?) { /* 自定义 debug 日志 */ }
    override fun i(tag: String, msg: String?) { /* 自定义 info 日志 */ }
    override fun w(tag: String, msg: String?) { /* 自定义 warn 日志 */ }
    override fun e(tag: String, msg: String?) { /* 自定义 error 日志 */ }
    override fun e(tag: String, msg: String?, e: Throwable) { /* 自定义 error + throwable 日志 */ }
}
```

---

## iOS 原生侧使用

###  注册 HRBridgeModule

在 Kuikly iOS 渲染器初始化时注册 `HRBridgeModule`：

```objc
// 注册 BridgeModule
[KuiklyRenderEngine registerModule:@"HRBridgeModule" 
                         withClass:[HRBridgeModule class]];
```

###  创建 iOS 插件

创建一个继承 `KKBridgePluginHelper` 并遵循 `IKuiklyBridgePlugin` 协议的类：

```objc
// UIPluginNative.h
#import "KKBridgePluginHelper.h"

@interface UIPluginNative : KKBridgePluginHelper

@end
```

```objc
// UIPluginNative.m
#import "UIPluginNative.h"

@implementation UIPluginNative

// 注册插件方法（在 +load 或 +initialize 中调用）
+ (void)load {
    // 注册 "ui" 插件下的 "openKuikly" 方法
    REGISTER_BRIDGE_PLUGIN(@"ui", @"openKuikly");
    // 注册 "ui" 插件下的 "openUrl" 方法  
    REGISTER_BRIDGE_PLUGIN(@"ui", @"openUrl");
}

// 实现 openKuikly 方法
// ⚠️ 方法名格式: `方法名:callback:`
- (void)openKuikly:(NSDictionary *)params callback:(KuiklyRenderCallback)callback {
    // 从 params 中获取参数
    NSString *singleParam = params[@"single_param"];  // 如果参数是单个字符串
    UIViewController *controller = params[@"controller"];  // 自动注入的当前控制器
    UIView *kuiklyView = params[@"view"];  // 自动注入的 Kuikly 视图

    // 执行业务逻辑...

    // 返回成功回调
    if (callback) {
        callback(@{
            @"code": @(0),
            @"msg": @"success",
            @"data": @{@"result": @"ok"}
        });
    }
}

// 实现 openUrl 方法
- (void)openUrl:(NSDictionary *)params callback:(KuiklyRenderCallback)callback {
    NSString *url = params[@"single_param"];
    // 打开 URL...
    
    if (callback) {
        callback(@{@"code": @(0), @"msg": @"success"});
    }
}

@end
```

###  注册 iOS 插件

**方式一：使用宏注册（推荐）**

在插件类的 `+load` 方法中使用 `REGISTER_BRIDGE_PLUGIN` 宏：

```objc
+ (void)load {
    // REGISTER_BRIDGE_PLUGIN(插件名, 方法名)
    REGISTER_BRIDGE_PLUGIN(@"ui", @"openKuikly");
    REGISTER_BRIDGE_PLUGIN(@"ui", @"openUrl");
    REGISTER_BRIDGE_PLUGIN(@"player", @"play");
}
```

**方式二：手动注册**

```objc
[KKBridgePluginHelper registerPlugin:@"ui" 
                              method:@"openKuikly" 
                            forClass:[UIPluginNative class]];
```

### iOS 参数说明

| 参数 Key | 说明 |
|:---------|:-----|
| `single_param` | 当 Kuikly 侧传递的是单个字符串（非 JSON）时，会以此 key 传入 |
| `controller` | 系统自动注入的当前 UIViewController（仅主线程可用） |
| `view` | 系统自动注入的当前 Kuikly UIView（仅主线程可用） |
| 其他 key | 当 Kuikly 侧传递 JSON 字符串时，解析后的各个字段 |

---

## JsonResult 回调数据处理

`JsonResult` 是统一的回调数据结构，Kuikly 侧和 Android 原生侧都提供了对应的实现。

### 标准数据格式

```json
{
    "code": 0,
    "msg": "success",
    "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|:-----|:-----|:-----|
| `code` | Int | 状态码，`0` 表示成功，`-1` 表示失败 |
| `msg` | String | 描述信息 |
| `data` | JSONObject? | 业务数据 |

###  基本用法

```kotlin
// Kuikly 侧回调处理
Bridge.getPlugin<UIPlugin>("ui")?.openKuikly("info") { result ->
    val jsonResult = JsonResult(result)
    
    if (jsonResult.isSuccess()) {
        val data = jsonResult.data
        // 处理成功数据
    } else {
        val errorMsg = jsonResult.msg
        // 处理失败情况
    }
}
```

###  fold 模式处理

`fold` 方法支持函数式风格处理结果：

```kotlin
// 方式一：统一处理（成功返回数据，失败返回 null）
Bridge.getPlugin<UIPlugin>("ui")?.openKuikly("info") { result ->
    JsonResult(result).fold(
        transform = { data -> UserInfo.decode(data) },
        callback = { userInfo ->
            if (userInfo != null) {
                // 成功
            } else {
                // 失败
            }
        }
    )
}

// 方式二：分开处理成功和失败
Bridge.getPlugin<UIPlugin>("ui")?.openKuikly("info") { result ->
    JsonResult(result).fold(
        transform = { data -> UserInfo.decode(data) },
        success = { userInfo ->
            // 成功处理
        },
        failed = { jsonResult ->
            // 失败处理，可获取 code 和 msg
            Log.e("Error", "code=${jsonResult.code}, msg=${jsonResult.msg}")
        }
    )
}
```

###  自定义数据解析（IResultData）

实现 `IResultData` 接口来定义数据模型：

```kotlin
// Kuikly 侧
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuiklyx.bridge.ext.IResultData

class UserInfo : IResultData {
    var name: String = ""
    var age: Int = 0

    override fun decode(data: JSONObject?): IResultData? {
        if (data == null) return null
        return UserInfo().apply {
            name = data.optString("name", "")
            age = data.optInt("age", 0)
        }
    }

    companion object {
        fun decode(data: JSONObject?): UserInfo? {
            return UserInfo().decode(data) as? UserInfo
        }
    }
}

// 使用
val result = JsonResult(callbackResult)
val userInfo = result.toData { UserInfo.decode(it) }
```

---

## 方法路由规则

Bridge 插件路由采用 **前缀匹配** 模式：

```
调用格式: pluginName.methodName
示例:     ui.openKuikly
          player.play
          share.toWechat
```

### 路由匹配流程

1. Kuikly 侧调用 `callNative("openKuikly", data, cb)`
2. `PluginModule` 自动拼接为 `ui.openKuikly`（`pluginName().methodName`）
3. 经由 `BridgeModule` → 原生 `KRBridgeModule/HRBridgeModule` 传递
4. 原生侧解析前缀：`prefix = "ui"`, `method = "openKuikly"`
5. 匹配到对应的 `KuiklyPlugin(pluginName="ui")`，调用 `openKuikly` 方法

### Android 匹配优先级

```
1. 旧模式：直接按完整方法名 "ui.openKuikly" 匹配插件
2. 新模式：按前缀 "ui" 匹配插件，再分发 "openKuikly" 方法
3. 匹配失败：返回 {"code": -1, "msg": "NoSuchNativePlugin"}
```

### iOS 匹配优先级

```
1. 直接按方法名 "ui.openKuikly" 在 pluginMap 中匹配
2. 按前缀 "ui" 匹配插件类，再按 "openKuikly" 匹配方法
3. 匹配失败：回调 {"code": -1, "message": "method does not exist"}
```



---

## 常见问题

### Q1: 插件调用没有反应？

检查以下几项：
- 确认 `BasePager` 的 `createPlugins()` 中已注册对应插件
- 确认重写 `createExternalModules` 时包含了 `super.createExternalModules()`
- 确认原生侧已注册对应的 Plugin 和方法
- 确认插件名和方法名拼写一致

### Q2: Bridge.moduleName 可以自定义吗？

可以，通过 `Bridge.moduleName = "CustomModuleName"` 设置，但需保证 Kuikly 侧和原生侧使用相同的 moduleName。默认值为 `"HRBridgeModule"`。

### Q3: callNative 和 syncCallNative 的区别？

- `callNative`：异步调用，结果通过 callback 回调返回
- `syncCallNative`：同步调用，直接返回结果（会阻塞当前线程直到原生返回）

### Q4: iOS 端的 params 中 "single_param" 是什么？

当 Kuikly 侧传递的参数是一个纯字符串（而非 JSON 格式）时，iOS 端无法将其解析为字典，因此会以 `@{@"single_param": 原始字符串}` 的形式传入。

### Q5: 如何支持一个插件多个方法在不同类中实现？

**iOS 端** 天然支持，通过 `REGISTER_BRIDGE_PLUGIN` 宏可以将同一插件的不同方法注册到不同类：

```objc
// ClassA.m
REGISTER_BRIDGE_PLUGIN(@"ui", @"openUrl");     // ui.openUrl → ClassA

// ClassB.m
REGISTER_BRIDGE_PLUGIN(@"ui", @"openDialog");  // ui.openDialog → ClassB
```

**Android 端** 通过 `KuiklyPluginManager.registerPluginMethod` 动态注册方法到已有插件。

### Q6: 插件方法的生命周期是怎样的？

- **Kuikly 侧**：`PluginModule` 实例采用懒加载，首次 `getPlugin` 时创建，随 Pager 生命周期销毁
- **Android 原生侧**：`KuiklyPlugin` 需手动注册到 `KuiklyPluginManager`，`KRBridgeModule.onDestroy()` 时会触发所有已注册插件的 `onDestroy()` 方法
- **iOS 原生侧**：插件实例由 `HRBridgeModule` 维护，随 `pluginInstance` 字典缓存
