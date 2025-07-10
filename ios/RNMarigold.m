
#import "RNMarigold.h"
#import "RNMessageStream.h"
#import <UserNotifications/UserNotifications.h>

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
    [_marigold setWrapperName:@"React Native" andVersion:@"15.0.0"];
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

RCT_EXPORT_METHOD(setGeoIPTrackingEnabledWithPromise:(BOOL)enabled resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
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

@end
