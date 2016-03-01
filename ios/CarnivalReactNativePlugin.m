//
//  CarnivalReactNativePlugin.m
//  CarnivalRNTestApp
//
//  Created by Sam Jarman on 2/11/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#import "CarnivalReactNativePlugin.h"
#import  <Carnival/Carnival.h>

@interface CarnivalMessage ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end

@implementation CarnivalReactNativePlugin

// Expose this module to the React Native bridge
RCT_EXPORT_MODULE()


#pragma mark - Initialization

RCT_EXPORT_METHOD(startEngine:(NSString *)key registerForPushNotifications:(BOOL)registerForPushNotifications) {
    [Carnival startEngine:key registerForPushNotifications:registerForPushNotifications ignoreAutoAnalyticsSources:@[CarnivalAutoAnalyticsSourceAll]]; // Turn off Auto-Analytics for React Native
}

#pragma mark - Messages
// Note: We use promises for our return values, not callbacks.

RCT_REMAP_METHOD(getMessages, resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [CarnivalMessageStream messages:^(NSArray * _Nullable messages, NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve([CarnivalReactNativePlugin arrayOfMessageDictionariesFromMessageArray:messages]);
        }
    }];
}

#pragma mark - Attributes

/* The parameter order is reverse that of the native iOS SDK, so we can match interfaces to make it easier for React Native Devs */

RCT_EXPORT_METHOD(setString:(NSString *)key forValue:(NSString *)string resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival setString:string forKey:key withResponse:^(NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setStrings:(NSString *)key forValue:(NSArray *)array resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival setStrings:array forKey:key withResponse:^(NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setDate:(NSString *)key forValue:(nonnull NSNumber *)dateValue resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival setDate:[NSDate dateWithTimeIntervalSince1970:[dateValue floatValue]] forKey:key withResponse:^(NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setDates:(NSString *)key forValue:(NSArray *)array resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    
    NSMutableArray *dateArray = [NSMutableArray array];
    for (id value in array) {
        [dateArray addObject:[NSDate dateWithTimeIntervalSince1970:[value floatValue]]];
    }
    
    [Carnival setDates:dateArray forKey:key withResponse:^(NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setFloat:(NSString *)key forValue:(CGFloat )theFloat resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival setFloat:theFloat forKey:key withResponse:^(NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setFloats:(NSString *)key forValue:(NSArray *)array resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival setFloats:array forKey:key withResponse:^(NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setInteger:(NSString *)key forValue:(NSInteger)theInteger resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival setInteger:theInteger forKey:key withResponse:^(NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setIntegers:(NSString *)key forValue:(NSArray *)array resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival setIntegers:array forKey:key withResponse:^(NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setBool:(NSString *)key forValue:(BOOL)theBool resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival setBool:theBool forKey:key withResponse:^(NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
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
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(@(unreadCount));
        }
    }];
}


RCT_EXPORT_METHOD(markMessageAsRead:(NSDictionary*)jsDict resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [CarnivalMessageStream markMessageAsRead:[CarnivalReactNativePlugin messageFromDict:jsDict] withResponse:^(NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(removeMessage:(NSDictionary *)jsDict resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [CarnivalMessageStream removeMessage:[CarnivalReactNativePlugin messageFromDict:jsDict] withResponse:^(NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(presentDetailForMessage:(NSDictionary *)jsDict) {
    [CarnivalMessageStream presentMessageDetailForMessage:[CarnivalReactNativePlugin messageFromDict:jsDict]];
}

RCT_EXPORT_METHOD(dismissMessageDetail) {
    [CarnivalMessageStream dismissMessageDetail];
}

RCT_EXPORT_METHOD(registerMessageImpression:(NSInteger)impressionType forMessage:(NSDictionary *)jsDict) {
    [CarnivalMessageStream registerImpressionWithType:impressionType forMessage:[CarnivalReactNativePlugin messageFromDict:jsDict]];
}



#pragma mark - IDs

RCT_EXPORT_METHOD(getDeviceID:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival deviceID:^(NSString * _Nullable deviceID, NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(deviceID);
        }
    }];
}

RCT_EXPORT_METHOD(setUserId:(NSString *)userID resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival setUserId:userID withResponse:^(NSError * _Nullable error) {
        if (error) {
            [CarnivalReactNativePlugin rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#pragma mark - Switches


RCT_EXPORT_METHOD(setInAppNotificationsEnabled:(BOOL)enabled) {
    [Carnival setInAppNotificationsEnabled:enabled];
}

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
