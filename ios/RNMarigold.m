
#import "RNMarigold.h"
#import <UserNotifications/UserNotifications.h>

@interface MARMessage ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end

@interface Marigold ()

- (void)setWrapperName:(NSString *)wrapperName andVersion:(NSString *)wrapperVersion;

@end

@interface MARPurchase ()

- (nullable instancetype)initWithDictionary:(NSDictionary *)dictionary;

@end

@interface RNMarigold()

@property (nonatomic, strong) Marigold *marigold;
@property (nonatomic, strong) MARMessageStream *messageStream;

@end


@implementation RNMarigold

// Automatically export module as RNMarigold
RCT_EXPORT_MODULE();

- (instancetype)init {
    return [self initWithDisplayInAppNotifications:YES];
}

- (instancetype)initWithDisplayInAppNotifications:(BOOL)displayNotifications {
    self = [super init];
    if(self) {
        _displayInAppNotifications = displayNotifications;
        _marigold = [Marigold new];
        _messageStream = [MARMessageStream new];

        [_messageStream setDelegate:self];
        [_marigold setWrapperName:@"React Native" andVersion:@"10.0.0"];
    }
    return self;
}

+ (BOOL)requiresMainQueueSetup {
    return NO;
}

- (NSArray<NSString *> *)supportedEvents {
    return @[@"inappnotification"];
}

- (BOOL)shouldPresentInAppNotificationForMessage:(MARMessage *)message {
    NSMutableDictionary *payload = [NSMutableDictionary dictionaryWithDictionary:[message dictionary]];

    if ([message attributes]) {
        [payload setObject:[message attributes] forKey:@"attributes"];
    }

    [self sendEventWithName:@"inappnotification" body:payload];
    return self.displayInAppNotifications;
}

RCT_EXPORT_METHOD(startEngine:(NSString *)sdkKey) {
    [self dispatchOnMainQueue:^{
        [self.marigold startEngine:sdkKey withAuthorizationOption:MARPushAuthorizationOptionNoRequest error:nil];
    }];
}

#pragma mark - Messages
// Note: We use promises for our return values, not callbacks.

RCT_REMAP_METHOD(getMessages, resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.messageStream messages:^(NSArray * _Nullable messages, NSError * _Nullable error) {
        if (error) {
            [RNMarigold rejectPromise:reject withError:error];
        } else {
            resolve([RNMarigold arrayOfMessageDictionariesFromMessageArray:messages]);
        }
    }];
}

#pragma mark - Location

RCT_EXPORT_METHOD(updateLocation:(CGFloat)lat lon:(CGFloat)lon) {
    [self.marigold updateLocation:[[CLLocation alloc] initWithLatitude:lat longitude:lon]];
}

#pragma mark - Message Stream

RCT_EXPORT_METHOD(getUnreadCount:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.messageStream unreadCount:^(NSUInteger unreadCount, NSError * _Nullable error) {
        if (error) {
            [RNMarigold rejectPromise:reject withError:error];
        } else {
            resolve(@(unreadCount));
        }
    }];
}


RCT_EXPORT_METHOD(markMessageAsRead:(NSDictionary*)jsDict resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.messageStream markMessageAsRead:[RNMarigold messageFromDict:jsDict] withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNMarigold rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(removeMessage:(NSDictionary *)jsDict resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.messageStream removeMessage:[RNMarigold messageFromDict:jsDict] withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNMarigold rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(presentMessageDetail:(NSDictionary *)jsDict) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.messageStream presentMessageDetailForMessage:[RNMarigold messageFromDict:jsDict]];
    });
}

RCT_EXPORT_METHOD(dismissMessageDetail) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.messageStream dismissMessageDetail];
    });
}

RCT_EXPORT_METHOD(registerMessageImpression:(NSInteger)impressionType forMessage:(NSDictionary *)jsDict) {
    [self.messageStream registerImpressionWithType:impressionType forMessage:[RNMarigold messageFromDict:jsDict]];
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

RCT_EXPORT_METHOD(clearDevice:(NSInteger)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self.marigold clearDeviceData:(MARDeviceDataType)options withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNMarigold rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#pragma mark - Helper Fuctions

+ (void)rejectPromise:(RCTPromiseRejectBlock)reject withError:(NSError *)error {
    reject([NSString stringWithFormat:@"%ld", error.code], error.localizedDescription, error);
}


+ (NSArray *)arrayOfMessageDictionariesFromMessageArray:(NSArray *)messageArray {
    NSMutableArray *messageDictionaries = [NSMutableArray array];
    for (MARMessage *message in messageArray) {
        [messageDictionaries addObject:[message dictionary]];
    }
    return messageDictionaries;
}

+ (MARMessage *) messageFromDict:(NSDictionary *)jsDict {
    return [[MARMessage alloc] initWithDictionary:jsDict];
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
