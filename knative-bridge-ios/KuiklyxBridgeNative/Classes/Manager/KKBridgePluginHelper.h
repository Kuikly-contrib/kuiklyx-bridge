//
//  KKBridgePluginHelper.h
//  KKNativeIOS
//

#import <Foundation/Foundation.h>
#import <KuiklyIOSRender/KRBaseModule.h>

#ifndef KUIKLYX_BRIDGE_PLUGIN_HELPER_H
#define REGISTER_BRIDGE_PLUGIN(plugin, pluginMethod) \
    [KKBridgePluginHelper registerPlugin:plugin method:pluginMethod forClass:self]

#define SINGLE_PARAM @"single_param"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface PrefixMethod : NSObject

@property(nonatomic, strong, nullable) NSString *prefix;
@property(nonatomic, strong, nullable) NSString *method;

- (instancetype)initWith:(NSString *)prefix method:(NSString *)method;

@end

@protocol IKuiklyBridgePlugin <NSObject>

@required

- (id)invokeMethod:(NSString *)method
            params:(NSDictionary *_Nullable)params
        controller:(UIViewController *)controller
              view:(UIView *)view
          callback:(KuiklyRenderCallback _Nullable)callback;

@end

@interface KKBridgePluginHelper : NSObject <IKuiklyBridgePlugin>

/**
 * 注册插件
 *
 * @param plugin      插件名  :  ui
 * @param method      插件方法 : openUrl
 * @param pluginClass 插件方法执行类
 * @return 注册结果
 */
+ (BOOL)registerPlugin:(NSString *)plugin method:(NSString *)method forClass:(Class)pluginClass;

/**
 * 根据方法名获取插件类
 *
 * @param method 方法名
 * @return 插件类 or nil
 */
+ (Class)pluginClassFor:(NSString *)method;

/**
 * 解析方法名
 *
 * @param method 方法名
 * @return PrefixMethod [插件].[方法]
 */
+ (PrefixMethod *)parseMethod:(NSString *)method;

+ (void)registerInit;

@end

NS_ASSUME_NONNULL_END
