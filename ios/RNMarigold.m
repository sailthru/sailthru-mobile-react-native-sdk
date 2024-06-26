
#import "RNMarigold.h"
#import "RNMessageStream.h"
#import <UserNotifications/UserNotifications.h>
#import <objc/runtime.h>

@interface Marigold ()

- (void)setWrapperName:(NSString *)wrapperName andVersion:(NSString *)wrapperVersion;

@end

@interface MARPurchase ()

- (nullable instancetype)initWithDictionary:(NSDictionary *)dictionary;

@end

@interface RNMarigold()

@property (nonatomic, strong) Marigold *marigold;

@end


@implementation RNMarigold

// Automatically export module as RNMarigold
RCT_EXPORT_MODULE();

- (instancetype)init {
    _marigold = [Marigold new];
    [_marigold setWrapperName:@"React Native" andVersion:@"12.0.0"];
    return self;
}

+ (BOOL)requiresMainQueueSetup {
    return NO;
}

- (NSArray<NSString *> *)supportedEvents {
    return @[];
}

RCT_EXPORT_METHOD(startEngine:(NSString *)sdkKey) {
    [self dispatchOnMainQueue:^{
        [self.marigold startEngine:sdkKey withAuthorizationOption:MARPushAuthorizationOptionNoRequest error:nil];
    }];
}

#pragma mark - Location

RCT_EXPORT_METHOD(updateLocation:(CGFloat)lat lon:(CGFloat)lon) {
    [self.marigold updateLocation:[[CLLocation alloc] initWithLatitude:lat longitude:lon]];
}

#pragma mark - IDs

RCT_EXPORT_METHOD(getDeviceID:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.marigold deviceID:^(NSString * _Nullable deviceID, NSError * _Nullable error) {
        if (error) {
            [RNMarigold rejectPromise:reject withError:error];
        } else {
            resolve(deviceID);
        }
    }];
}

#pragma mark - Switches
RCT_EXPORT_METHOD(setGeoIPTrackingEnabled:(BOOL)enabled) {
    [self.marigold setGeoIPTrackingEnabled:enabled];
}

RCT_EXPORT_METHOD(setGeoIPTrackingEnabled:(BOOL)enabled resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self.marigold setGeoIPTrackingEnabled:enabled withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNMarigold rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setCrashHandlersEnabled:(BOOL)enabled) {
    [self.marigold setCrashHandlersEnabled:enabled];
}

RCT_EXPORT_METHOD(logRegistrationEvent:(NSString * _Nullable)userId) {
    [self.marigold logRegistrationEvent:userId];
}

// Push Registration
RCT_EXPORT_METHOD(registerForPushNotifications) {
    UNAuthorizationOptions options = UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound;
    [[UNUserNotificationCenter currentNotificationCenter] requestAuthorizationWithOptions:options completionHandler:^(BOOL granted, NSError * _Nullable error) {}];

    [self dispatchOnMainQueue:^{
        if(![[UIApplication sharedApplication] isRegisteredForRemoteNotifications]) {
            [[UIApplication sharedApplication] registerForRemoteNotifications];
        }
    }];
}

RCT_EXPORT_METHOD(syncNotificationSettings) {
    [self.marigold syncNotificationSettings];
}

RCT_EXPORT_METHOD(setInAppNotificationsEnabled:(BOOL)enabled) {
    [self.marigold setInAppNotificationsEnabled:enabled];
}

RCT_REMAP_METHOD(getDeviceDetails, resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    NSString *endpoint = [RNMarigold getDeviceDetails];\
    resolve(endpoint);
}

#pragma mark - Helper Functions

+ (void)rejectPromise:(RCTPromiseRejectBlock)reject withError:(NSError *)error {
    reject([NSString stringWithFormat:@"%ld", error.code], error.localizedDescription, error);
}

- (void)dispatchOnMainQueue:(dispatch_block_t) block {
    if (!block) return;
    
    if ([NSThread isMainThread]) {
        block();
    } else {
        [[NSOperationQueue mainQueue] addOperationWithBlock:block];
    }
}

+ (NSString *)getDeviceDetails {
    RNMarigold *marigold = [RNMarigold new];
    NSString *endpoint = [marigold getEndpoint];
    NSString *appId = [marigold getAppId];
    NSString *deviceId = [marigold getDeviceId];
    NSString *deviceString = [marigold getDevice];
    
    NSString *detailsString = [NSString stringWithFormat:@"Endpoint: %@\nApp ID: %@\nDevice ID: %@\nDevice: %@", endpoint, appId, deviceId, deviceString];
    
    return detailsString;
}

- (NSString *)getEndpoint {
    Class apiClientClass = objc_getClass("CPAPIClient");
    SEL sharedSelector = NSSelectorFromString(@"sharedClient");
    IMP sharedImp = [apiClientClass methodForSelector:sharedSelector];
    id (*sharedFunc)(id, SEL) = (id (*)(id, SEL))sharedImp;
    id apiClient = sharedFunc(apiClientClass, sharedSelector);
    
    SEL urlSelector = NSSelectorFromString(@"baseURL");
    IMP urlImp = [apiClient methodForSelector:urlSelector];
    NSString* (*urlFunc)(id, SEL) = (NSString* (*)(id, SEL))urlImp;
    return urlFunc(apiClient, urlSelector);
}

- (NSString *)getAppId {
    Class stateHandlerClass = objc_getClass("CPStateHandler");
    
    SEL sharedSelector = NSSelectorFromString(@"shared");
    IMP sharedImp = [stateHandlerClass methodForSelector:sharedSelector];
    id (*sharedFunc)(id, SEL) = (id (*)(id, SEL))sharedImp;
    id stateHandler = sharedFunc(stateHandlerClass, sharedSelector);
    
    SEL sdkKeySelector = NSSelectorFromString(@"sdkKey");
    IMP sdkKeyImp = [stateHandler methodForSelector:sdkKeySelector];
    id (*sdkKeyFunc)(id, SEL) = (id (*)(id, SEL))sdkKeyImp;
    id sdkKey = sdkKeyFunc(stateHandler, sdkKeySelector);
    
    SEL keySelector = NSSelectorFromString(@"key");
    IMP keyImp = [sdkKey methodForSelector:keySelector];
    NSString* (*keyFunc)(id, SEL) = (NSString* (*)(id, SEL))keyImp;
    return keyFunc(sdkKey, keySelector);
}

- (NSString *)getDeviceId {
    Class deviceClass = objc_getClass("Marigold.Device");
    
    SEL sharedSelector = NSSelectorFromString(@"shared");
    IMP sharedImp = [deviceClass methodForSelector:sharedSelector];
    id (*sharedFunc)(id, SEL) = (id (*)(id, SEL))sharedImp;
    id device = sharedFunc(deviceClass, sharedSelector);
    
    SEL deviceIdSelector = NSSelectorFromString(@"deviceId");
    IMP deviceIdImp = [device methodForSelector:deviceIdSelector];
    NSString* (*deviceIdFunc)(id, SEL) = (NSString* (*)(id, SEL))deviceIdImp;
    return deviceIdFunc(device, deviceIdSelector);
}

- (NSString *)getDevice {
    Class builderClass = objc_getClass("Marigold.DeviceDictionaryBuilder");
    id builder = [[builderClass alloc] init];
    
    SEL addAllSelector = NSSelectorFromString(@"addAll");
    IMP addAllImp = [builder methodForSelector:addAllSelector];
    id (*addAllFunc)(id, SEL) = (id (*)(id, SEL))addAllImp;
    addAllFunc(builder, addAllSelector);
    
    SEL dictionarySelector = NSSelectorFromString(@"dictionary");
    IMP dictionaryImp = [builder methodForSelector:dictionarySelector];
    NSDictionary* (*dictionaryFunc)(id, SEL) = (NSDictionary* (*)(id, SEL))dictionaryImp;
    NSDictionary *deviceDictionary = dictionaryFunc(builder, dictionarySelector);
    
    return [NSString stringWithFormat:@"%@", deviceDictionary];
}

@end
