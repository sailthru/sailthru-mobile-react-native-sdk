
#import "RNCarnival.h"

@interface CarnivalMessage ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end

@interface CarnivalContentItem ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end

@interface Carnival ()

+ (void)setWrapperName:(NSString *)wrapperName andVersion:(NSString *)wrapperVersion;

@end


@implementation RNCarnival

BOOL displayInAppNotifications = YES;

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"inappnotification"];
}

- (BOOL)shouldPresentInAppNotificationForMessage:(CarnivalMessage *)message {
    NSMutableDictionary *payload = [NSMutableDictionary dictionaryWithDictionary:[message dictionary]];
    
    if ([message attributes]) {
        [payload setObject:[message attributes] forKey:@"attributes"];
    }
    
    [self sendEventWithName:@"inappnotification" body:payload];
    return displayInAppNotifications;
}

#pragma mark - Initialization

RCT_EXPORT_METHOD(startEngine:(NSString *)key registerForPushNotifications:(BOOL)registerForPushNotifications) {
    [CarnivalMessageStream setDelegate:self];
    dispatch_async(dispatch_get_main_queue(), ^{
        [Carnival startEngine:key registerForPushNotifications:registerForPushNotifications];
        [Carnival setWrapperName:@"React Native" andVersion:@"1.0.0"];
    });
}



#pragma mark - Messages
// Note: We use promises for our return values, not callbacks.

RCT_REMAP_METHOD(getMessages, resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [CarnivalMessageStream messages:^(NSArray * _Nullable messages, NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve([RNCarnival arrayOfMessageDictionariesFromMessageArray:messages]);
        }
    }];
}

RCT_EXPORT_METHOD(setDisplayInAppNotifications:(BOOL)enabled) {
    displayInAppNotifications = enabled;
}

#pragma mark - Attributes
RCT_EXPORT_METHOD(setAttributes:(NSDictionary *)attributeMap resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)  {
    
    CarnivalAttributes *carnivalAttributeMap = [[CarnivalAttributes alloc] init];
    [carnivalAttributeMap setAttributesMergeRule:(CarnivalAttributesMergeRule)[attributeMap valueForKey:@"mergeRule"]];
    
    NSDictionary *attributes = [attributeMap valueForKey:@"attributes"];

    for (NSString *key in attributes) {
        NSString *type = [[attributes valueForKey:key] valueForKey:@"type"];
        
        if ([type isEqualToString:@"string"]) {
            NSString *value = [[attributes valueForKey:key] valueForKey:@"value"];
            [carnivalAttributeMap setString:value forKey:key];
            
        } else if ([type isEqualToString:@"stringArray"]) {
            NSArray<NSString *> *value = [[attributes valueForKey:key] valueForKey:@"value"];
            [carnivalAttributeMap setStrings:value forKey:key];
            
        } else if ([type isEqualToString:@"integer"]) {
            NSNumber *value = [[attributes valueForKey:key] objectForKey:@"value"];
            [carnivalAttributeMap setInteger:[value integerValue] forKey:key];
            
        } else if ([type isEqualToString:@"integerArray"]) {
            NSArray<NSNumber *> *value = [[attributes valueForKey:key] valueForKey:@"value"];
            [carnivalAttributeMap setIntegers:value forKey:key];
            
        } else if ([type isEqualToString:@"boolean"]) {
            BOOL value = [[[attributes valueForKey:key] valueForKey:@"value"] boolValue];
            [carnivalAttributeMap setBool:value forKey:key];
            
        } else if ([type isEqualToString:@"float"]) {
            NSNumber *numberValue = [[attributes valueForKey:key] objectForKey:@"value"];
            [carnivalAttributeMap setFloat:[numberValue floatValue] forKey:key];
            
        } else if ([type isEqualToString:@"floatArray"]) {
            NSArray<NSNumber *> *value = [[attributes valueForKey:key] objectForKey:@"value"];
            [carnivalAttributeMap setFloats:value forKey:key];
            
        } else if ([type isEqualToString:@"date"]) {
            NSNumber *millisecondsValue = [[attributes valueForKey:key] objectForKey:@"value"];
            NSNumber *value = @([millisecondsValue doubleValue] / 1000);
            
            if (![value isKindOfClass:[NSNumber class]]) {
                return;
            }
            
            NSDate *date = [NSDate dateWithTimeIntervalSince1970:[value doubleValue]];
            if (date) {
                [carnivalAttributeMap setDate:date forKey:key];
            } else {
                return;
            }
            
        } else if ([type isEqualToString:@"dateArray"]) {
            NSArray<NSNumber *> *value = [[attributes valueForKey:key] objectForKey:@"value"];
            NSMutableArray<NSDate *> *dates = [[NSMutableArray alloc] init];
            for (NSNumber *millisecondsValue in value) {
                NSNumber *secondsValue = @([millisecondsValue doubleValue] / 1000);
                
                if (![secondsValue isKindOfClass:[NSNumber class]]) {
                    continue;
                }
                
                NSDate *date = [NSDate dateWithTimeIntervalSince1970:[secondsValue doubleValue]];
                if (date) {
                    [dates addObject:date];
                }
            }
            
            [carnivalAttributeMap setDates:dates forKey:key];
        }
        
        static dispatch_once_t onceToken;
        dispatch_once(&onceToken, ^{
            [Carnival setAttributes:carnivalAttributeMap withResponse:^(NSError * _Nullable error) {
                if (error) {
                    [RNCarnival rejectPromise:reject withError:error];
                } else {
                    resolve(nil);
                }
            }];
        });
    }
}


#pragma mark - Location

RCT_EXPORT_METHOD(updateLocation:(CGFloat)lat lon:(CGFloat)lon) {
    [Carnival updateLocation:[[CLLocation alloc] initWithLatitude:lat longitude:lon]];
}

#pragma mark - Events

RCT_EXPORT_METHOD(logEvent:(NSString *)name) {
    [Carnival logEvent:name];
}


#pragma mark - Message Stream

RCT_EXPORT_METHOD(getUnreadCount:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [CarnivalMessageStream unreadCount:^(NSUInteger unreadCount, NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve(@(unreadCount));
        }
    }];
}


RCT_EXPORT_METHOD(markMessageAsRead:(NSDictionary*)jsDict resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [CarnivalMessageStream markMessageAsRead:[RNCarnival messageFromDict:jsDict] withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(removeMessage:(NSDictionary *)jsDict resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [CarnivalMessageStream removeMessage:[RNCarnival messageFromDict:jsDict] withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(presentDetailForMessage:(NSDictionary *)jsDict) {
    [CarnivalMessageStream presentMessageDetailForMessage:[RNCarnival messageFromDict:jsDict]];
}

RCT_EXPORT_METHOD(dismissMessageDetail) {
    [CarnivalMessageStream dismissMessageDetail];
}

RCT_EXPORT_METHOD(registerMessageImpression:(NSInteger)impressionType forMessage:(NSDictionary *)jsDict) {
    [CarnivalMessageStream registerImpressionWithType:impressionType forMessage:[RNCarnival messageFromDict:jsDict]];
}



#pragma mark - IDs

RCT_EXPORT_METHOD(getDeviceID:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival deviceID:^(NSString * _Nullable deviceID, NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve(deviceID);
        }
    }];
}

RCT_EXPORT_METHOD(setUserId:(NSString *)userID resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival setUserId:userID withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setUserEmail:(NSString *)userEmail resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival setUserEmail:userEmail withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#pragma mark - Switches
RCT_EXPORT_METHOD(setGeoIPTrackingEnabled:(BOOL)enabled) {
    [Carnival setGeoIPTrackingEnabled:enabled];
}

RCT_EXPORT_METHOD(setCrashHandlersEnabled:(BOOL)enabled) {
    [Carnival setCrashHandlersEnabled:enabled];
}

// Push Registration
RCT_EXPORT_METHOD(registerForPushNotifications) {
    UIUserNotificationType types = UIRemoteNotificationTypeAlert | UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound;
    
    if ([[UIApplication sharedApplication] respondsToSelector:@selector(registerUserNotificationSettings:)]) { // iOS 8+
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:types categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    }
    else { //iOS 7
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:(UIRemoteNotificationType)types];
    }
}


#pragma mark - Helper Fuctions

+ (void)rejectPromise:(RCTPromiseRejectBlock)reject withError:(NSError *)error {
    reject([NSString stringWithFormat:@"%ld", error.code], error.localizedDescription, error);
}


+ (NSArray *)arrayOfMessageDictionariesFromMessageArray:(NSArray *)messageArray {
    NSMutableArray *messageDictionaries = [NSMutableArray array];
    for (CarnivalMessage *message in messageArray) {
        [messageDictionaries addObject:[message dictionary]];
    }
    return messageDictionaries;
}

+ (CarnivalMessage *) messageFromDict:(NSDictionary *)jsDict {
    return [[CarnivalMessage alloc] initWithDictionary:jsDict];
}

@end
