
#import "RNCarnival.h"
#import <UserNotifications/UserNotifications.h>

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

-(instancetype)init {
    [NSException raise:@"Unsupported Method" format:@"Default initializer should not be called"];
    return nil;
}

-(instancetype)initWithDisplayInAppNotifications:(BOOL)displayNotifications {
    self = [super init];
    if(self) {
        self.displayInAppNotifications = displayNotifications;
        [CarnivalMessageStream setDelegate:self];
        [Carnival setWrapperName:@"React Native" andVersion:@"2.3.0"];
    }
    return self;
}

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
    return self.displayInAppNotifications;
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
    }

    [Carnival setAttributes:carnivalAttributeMap withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
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

RCT_EXPORT_METHOD(logEvent:(NSString *)name withVars:(NSDictionary*)varsDict) {
    [Carnival logEvent:name withVars:varsDict];
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

RCT_EXPORT_METHOD(presentMessageDetail:(NSDictionary *)jsDict) {
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


#pragma mark - Recommendations

RCT_EXPORT_METHOD(getRecommendations:(NSString *)sectionID resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
  [Carnival recommendationsWithSection:sectionID withResponse:^(NSArray * _Nullable contentItems, NSError * _Nullable error) {
    if(error) {
      [RNCarnival rejectPromise:reject withError:error];
    } else {
      resolve([RNCarnival arrayOfContentItemsDictionaryFromContentItemsArray:contentItems]);
    }
  }];
}

RCT_EXPORT_METHOD(trackClick:(NSString *)sectionID url:(NSString *)url resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSURL *nsUrl = [[NSURL alloc] initWithString:url];
    [Carnival trackClickWithSection:sectionID andUrl:nsUrl andResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(trackPageview:(NSString *)url tags:(NSArray *)tags resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSURL *nsUrl = [[NSURL alloc] initWithString:url];
    void (^responseBlock)(NSError * _Nullable) = ^(NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    };

    if(tags) {
        [Carnival trackPageviewWithUrl:nsUrl andTags:tags andResponse:responseBlock];
    }
    else {
        [Carnival trackPageviewWithUrl:nsUrl andResponse:responseBlock];
    }
}

RCT_EXPORT_METHOD(trackImpression:(NSString *)sectionID url:(NSArray *)urls resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    void (^responseBlock)(NSError * _Nullable) = ^(NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    };

    if(urls) {
        NSMutableArray *nsUrls = [[NSMutableArray alloc] init];
        for (NSString *url in urls) {
            NSURL *nsUrl = [[NSURL alloc] initWithString:url];
            [nsUrls addObject:nsUrl];
        }
        [Carnival trackImpressionWithSection:sectionID andUrls:nsUrls andResponse:responseBlock];
    }
    else {
        [Carnival trackImpressionWithSection:sectionID andResponse:responseBlock];
    }
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
    UNAuthorizationOptions options = UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound;
    if ([[NSProcessInfo processInfo] operatingSystemVersion].majorVersion >= 10) {
        [[UNUserNotificationCenter currentNotificationCenter] requestAuthorizationWithOptions:options completionHandler:^(BOOL granted, NSError * _Nullable error) {}];
    }
    else {
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:(UIUserNotificationType)options categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
    }
    
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        if(![[UIApplication sharedApplication] isRegisteredForRemoteNotifications]) {
            [[UIApplication sharedApplication] registerForRemoteNotifications];
        }
    }];
}

RCT_EXPORT_METHOD(clearDevice:(NSInteger)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival clearDeviceData:(CarnivalDeviceDataType)options withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#pragma mark - Profile Vars

RCT_EXPORT_METHOD(setProfileVars:(NSDictionary *)vars resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival setProfileVars:vars withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(getProfileVars:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [Carnival getProfileVarsWithResponse:^(NSDictionary<NSString *,id> * _Nullable vars, NSError * _Nullable error) {
        if (error) {
            [RNCarnival rejectPromise:reject withError:error];
        } else {
            resolve(vars);
        }
    }];
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

+ (NSArray *)arrayOfContentItemsDictionaryFromContentItemsArray:(NSArray *)contentItemsArray {
  NSMutableArray *items = [NSMutableArray array];
  for (CarnivalContentItem *contentItem in contentItemsArray) {
    [items addObject:[contentItem dictionary]];
  }
  return items;
}

@end
