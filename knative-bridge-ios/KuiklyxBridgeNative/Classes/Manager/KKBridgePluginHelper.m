//
//  KKBridgePluginHelper.m
//  KKNativeIOS
//

#import "KKBridgePluginHelper.h"

// 插件集合
static NSMutableDictionary<NSString *, NSMutableDictionary<NSString *, Class> *> *g_pluginMap = nil;

@implementation PrefixMethod

- (instancetype)initWith:(NSString *)prefix method:(NSString *)method {
    self = [super init];
    if (self) {
        _prefix = [prefix copy];
        _method = [method copy];
    }
    return self;
}
@end

@implementation KKBridgePluginHelper

- (id)invokeMethod:(NSString *)method
              params:(NSDictionary *_Nullable)params
          controller:(UIViewController *)controller
                view:(UIView *)view
            callback:(KuiklyRenderCallback _Nullable)callback {
    SEL selector = NSSelectorFromString([NSString stringWithFormat:@"%@:callback:", method]);
    if (![self respondsToSelector:selector])
    {
        PrefixMethod *pmethod = [KKBridgePluginHelper parseMethod:method];
        NSLog(@"prefix: %@, method:%@", pmethod.prefix, pmethod.method);
        selector = NSSelectorFromString([NSString stringWithFormat:@"%@:callback:", pmethod.method]);
        if (![self respondsToSelector:selector])
        {
            callback(@{
                @"code":@(-1),
                @"message": @"method not found",
            });
            return nil;
        }
    }
    NSMutableDictionary *p = [params mutableCopy];
    if (controller)
    {
        [p setObject:controller forKey:@"controller"];
    }
    if (view)
    {
        [p setObject:view forKey:@"view"];
    }
    NSMethodSignature *methodSignature = [self methodSignatureForSelector:selector];
    NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:methodSignature];
    [invocation setArgument:&p atIndex:2];
    [invocation setArgument:&callback atIndex:3];
    [invocation setSelector:selector];
    [invocation invokeWithTarget:self];
    if (strcmp(methodSignature.methodReturnType, @encode(void)) != 0)
    {
        void *returnValue;
        [invocation getReturnValue:&returnValue];
        return (__bridge id)returnValue;
    }
    return nil;
}

+ (Class)pluginClassFor:(NSString *)method
{
    PrefixMethod *pmethod = [KKBridgePluginHelper parseMethod:method];
    if (!pmethod)
    {
        return nil;
    }
    if (!pmethod.prefix)
    {
        NSMutableDictionary *methods = [g_pluginMap objectForKey:pmethod.method];
        if (methods) {
            return [methods objectForKey:pmethod.method];
        }
    }
    else
    {
        NSMutableDictionary *methods = [g_pluginMap objectForKey:pmethod.prefix];
        if (methods) {
            return [methods objectForKey:pmethod.method];
        }
    }
    return nil;
}

+ (BOOL)registerPlugin:(NSString *)plugin method:(NSString *)method forClass:(Class)pluginClass
{
    if (!g_pluginMap)
    {
        g_pluginMap = [[NSMutableDictionary alloc] init];
    }

    NSMutableDictionary *methods = g_pluginMap[plugin];
    if (!methods)
    {
        methods = [[NSMutableDictionary alloc] init];
        g_pluginMap[plugin] = methods;
    }

    methods[method] = pluginClass;
    return YES;
}

+ (PrefixMethod *)parseMethod:(NSString *)method
{
    if (!method || [method length] == 0)
    {
        return nil;
    }
    NSArray<NSString *> *components =  [method componentsSeparatedByString:@"."];
    if (components.count > 1)
    {
        NSString *prefix = components.firstObject;
        NSString *submethod = [[components subarrayWithRange:NSMakeRange(1, [components count] - 1)] componentsJoinedByString:@"."];
        NSLog(@"prefix: %@, submethod:%@", prefix, submethod);
        return [[PrefixMethod alloc] initWith:prefix method:submethod];
    }
    return [[PrefixMethod alloc] initWith:@"" method:method];
}

+ (void)registerInit
{
    
}
@end
