//
//  HRBridgeModule.m
//  Kotlin 交接层
//

#import "HRBridgeModule.h"

#define TAG @"BridgeModule"

@implementation HRBridgeModule

@synthesize hr_rootView;

- (instancetype)init
{
    if (self = [super init])
    {

    }
    return self;
}

#pragma mark - override KRBaseModule
- (NSString * _Nullable)hrv_callWithMethod:(NSString *)method
                                    params:(NSString *)params
                                  callback:(KuiklyRenderCallback)callback
{
    NSDictionary *paramsDic = nil;
    if (params.length > 0)
    {
        // params兼容字符串和json，如果能序列化json就当json处理，序列化失败则认为是单参数string
        paramsDic = [self dictionaryWithJsonString:params];
        if (paramsDic == nil)
        {
            paramsDic = @{SINGLE_PARAM: params};
        }
    }

    id<IKuiklyBridgePlugin> plugin = [self pluginInstanceFor:method];
    if (plugin == nil)
    {
        // 继续查找
        NSString *prefix = [[method componentsSeparatedByString:@"."] firstObject];
        if (prefix.length > 0)
        {
            plugin = [self pluginInstanceFor:prefix];
        }
    }
    if(plugin == nil)
    {
        callback( @{
            @"code":@(-1),
            @"message": @"method does not exist",
        } );
        return nil;
    }
    else
    {
        UIViewController *controller = [self currentViewController];
        UIView *kuiklyView = [self currentKuiklyView];
        return [plugin invokeMethod:method params:paramsDic controller:controller view:kuiklyView callback:callback];
    }

}

- (id<IKuiklyBridgePlugin>)pluginInstanceFor:(NSString *)method
{
    if (self.pluginInstance == nil)
    {
        self.pluginInstance = [[NSMutableDictionary alloc] init];
    }

    id<IKuiklyBridgePlugin> instance = nil;
    Class class = [KKBridgePluginHelper pluginClassFor:method];
    NSString *className = NSStringFromClass(class);
    if (!class || className.length < 1)
    {
        return nil;
    }

    // 插件池有没有缓存
    @synchronized (self.pluginInstance)
    {
        instance = [self.pluginInstance objectForKey:className];
        // 没有就创建
        if (!instance)
        {
            instance = [[class alloc] init];
            [self.pluginInstance setObject:instance forKey:className];
        }
    }
    return instance;
}

// 转发HRBridgeModule的方法到具体的插件
- (BOOL)dispatchMethod:(NSString *)method params:(NSDictionary *)params callback:(KuiklyRenderCallback)callback
{
    id<IKuiklyBridgePlugin> plugin = [self pluginInstanceFor:method];
    if(plugin == nil)
    {
        return NO;
    }
    UIViewController *controller = [self currentViewController];
    UIView *kuiklyView = [self currentKuiklyView];
    id result = [plugin invokeMethod:method params:params controller:controller view:kuiklyView callback:callback];
    return result != nil;
}

#pragma mark - Private

- (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString {
    if (jsonString.length == 0)
    {
        return nil;
    }

    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    if (!data)
    {
        NSLog(@"Failed to convert string to data: %@", jsonString);
        return nil;
    }

    @try {
        NSError *error = nil;
        id jsonObject = [NSJSONSerialization JSONObjectWithData:data
                                                        options:NSJSONReadingMutableContainers
                                                          error:&error];
        if (error)
        {
            NSLog(@"JSON parsing error: %@, data: %@", error.localizedDescription, jsonString);
            return nil;
        }

        if (![jsonObject isKindOfClass:[NSDictionary class]])
        {
            NSLog(@"JSON object is not a dictionary, actual type: %@, data: %@",
                  [jsonObject class], jsonString);
            return nil;
        }
        return (NSDictionary *)jsonObject;
    }
    @catch (NSException *exception) {
        NSLog(@"JSON parsing exception: %@, data: %@", exception.reason, jsonString);
        return nil;
    }
}

- (UIViewController *)currentViewController
{
    if (![NSThread isMainThread])
    {
        return nil;
    }
    UIViewController *controller = self.hr_rootView.kr_viewController;
    return controller;
}

- (UIView *)currentKuiklyView
{
    if (![NSThread isMainThread])
    {
        return nil;
    }
    return self.hr_rootView.superview;
}

@end

