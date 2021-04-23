
#import "RNSailthruMobile.h"
#import <UserNotifications/UserNotifications.h>

@interface STMMessage ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end

@interface STMContentItem ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end

@interface SailthruMobile ()

- (void)setWrapperName:(NSString *)wrapperName andVersion:(NSString *)wrapperVersion;

@end

@interface STMPurchase ()

- (nullable instancetype)initWithDictionary:(NSDictionary *)dictionary;

@end

@interface RNSailthruMobile()

@property (nonatomic, strong) SailthruMobile *sailthruMobile;
@property (nonatomic, strong) STMMessageStream *messageStream;

@end


@implementation RNSailthruMobile

-(instancetype)init {
    [NSException raise:@"Unsupported Method" format:@"Default initializer should not be called"];
    return nil;
}

-(instancetype)initWithDisplayInAppNotifications:(BOOL)displayNotifications {
    self = [super init];
    if(self) {
        _displayInAppNotifications = displayNotifications;
        _sailthruMobile = [SailthruMobile new];
        _messageStream = [STMMessageStream new];

        [_messageStream setDelegate:self];
        [_sailthruMobile setWrapperName:@"React Native" andVersion:@"6.0.0"];
    }
    return self;
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"inappnotification"];
}

- (BOOL)shouldPresentInAppNotificationForMessage:(STMMessage *)message {
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
    [self.messageStream messages:^(NSArray * _Nullable messages, NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve([RNSailthruMobile arrayOfMessageDictionariesFromMessageArray:messages]);
        }
    }];
}

#pragma mark - Attributes
RCT_EXPORT_METHOD(setAttributes:(NSDictionary *)attributeMap resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)  {

    STMAttributes *stmAttributes = [[STMAttributes alloc] init];
    NSInteger mergeRule = [[attributeMap valueForKey:@"mergeRule"] integerValue];
    [stmAttributes setAttributesMergeRule:(STMAttributesMergeRule)(mergeRule - 1)];

    NSDictionary *attributes = [attributeMap valueForKey:@"attributes"];

    for (NSString *key in attributes) {
        NSString *type = [[attributes valueForKey:key] valueForKey:@"type"];

        if ([type isEqualToString:@"string"]) {
            NSString *value = [[attributes valueForKey:key] valueForKey:@"value"];
            [stmAttributes setString:value forKey:key];

        } else if ([type isEqualToString:@"stringArray"]) {
            NSArray<NSString *> *value = [[attributes valueForKey:key] valueForKey:@"value"];
            [stmAttributes setStrings:value forKey:key];

        } else if ([type isEqualToString:@"integer"]) {
            NSNumber *value = [[attributes valueForKey:key] objectForKey:@"value"];
            [stmAttributes setInteger:[value integerValue] forKey:key];

        } else if ([type isEqualToString:@"integerArray"]) {
            NSArray<NSNumber *> *value = [[attributes valueForKey:key] valueForKey:@"value"];
            [stmAttributes setIntegers:value forKey:key];

        } else if ([type isEqualToString:@"boolean"]) {
            BOOL value = [[[attributes valueForKey:key] valueForKey:@"value"] boolValue];
            [stmAttributes setBool:value forKey:key];

        } else if ([type isEqualToString:@"float"]) {
            NSNumber *numberValue = [[attributes valueForKey:key] objectForKey:@"value"];
            [stmAttributes setFloat:[numberValue floatValue] forKey:key];

        } else if ([type isEqualToString:@"floatArray"]) {
            NSArray<NSNumber *> *value = [[attributes valueForKey:key] objectForKey:@"value"];
            [stmAttributes setFloats:value forKey:key];

        } else if ([type isEqualToString:@"date"]) {
            NSNumber *millisecondsValue = [[attributes valueForKey:key] objectForKey:@"value"];
            NSNumber *value = @([millisecondsValue doubleValue] / 1000);

            if (![value isKindOfClass:[NSNumber class]]) {
                return;
            }

            NSDate *date = [NSDate dateWithTimeIntervalSince1970:[value doubleValue]];
            if (date) {
                [stmAttributes setDate:date forKey:key];
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

            [stmAttributes setDates:dates forKey:key];
        }
    }

    [self.sailthruMobile setAttributes:stmAttributes withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}


#pragma mark - Location

RCT_EXPORT_METHOD(updateLocation:(CGFloat)lat lon:(CGFloat)lon) {
    [self.sailthruMobile updateLocation:[[CLLocation alloc] initWithLatitude:lat longitude:lon]];
}

#pragma mark - Events

RCT_EXPORT_METHOD(logEvent:(NSString *)name) {
    [self.sailthruMobile logEvent:name];
}

RCT_EXPORT_METHOD(logEvent:(NSString *)name withVars:(NSDictionary*)varsDict) {
    [self.sailthruMobile logEvent:name withVars:varsDict];
}


#pragma mark - Message Stream

RCT_EXPORT_METHOD(getUnreadCount:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.messageStream unreadCount:^(NSUInteger unreadCount, NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(@(unreadCount));
        }
    }];
}


RCT_EXPORT_METHOD(markMessageAsRead:(NSDictionary*)jsDict resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.messageStream markMessageAsRead:[RNSailthruMobile messageFromDict:jsDict] withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(removeMessage:(NSDictionary *)jsDict resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.messageStream removeMessage:[RNSailthruMobile messageFromDict:jsDict] withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(presentMessageDetail:(NSDictionary *)jsDict) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.messageStream presentMessageDetailForMessage:[RNSailthruMobile messageFromDict:jsDict]];
    });
}

RCT_EXPORT_METHOD(dismissMessageDetail) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.messageStream dismissMessageDetail];
    });
}

RCT_EXPORT_METHOD(registerMessageImpression:(NSInteger)impressionType forMessage:(NSDictionary *)jsDict) {
    [self.messageStream registerImpressionWithType:impressionType forMessage:[RNSailthruMobile messageFromDict:jsDict]];
}



#pragma mark - IDs

RCT_EXPORT_METHOD(getDeviceID:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.sailthruMobile deviceID:^(NSString * _Nullable deviceID, NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(deviceID);
        }
    }];
}

RCT_EXPORT_METHOD(setUserId:(NSString *)userID resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.sailthruMobile setUserId:userID withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setUserEmail:(NSString *)userEmail resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.sailthruMobile setUserEmail:userEmail withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}


#pragma mark - Recommendations

RCT_EXPORT_METHOD(getRecommendations:(NSString *)sectionID resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
  [self.sailthruMobile recommendationsWithSection:sectionID withResponse:^(NSArray * _Nullable contentItems, NSError * _Nullable error) {
    if(error) {
      [RNSailthruMobile rejectPromise:reject withError:error];
    } else {
      resolve([RNSailthruMobile arrayOfContentItemsDictionaryFromContentItemsArray:contentItems]);
    }
  }];
}

RCT_EXPORT_METHOD(trackClick:(NSString *)sectionID url:(NSString *)url resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSURL *nsUrl = [[NSURL alloc] initWithString:url];
    [self.sailthruMobile trackClickWithSection:sectionID andUrl:nsUrl andResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
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
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    };

    if(tags) {
        [self.sailthruMobile trackPageviewWithUrl:nsUrl andTags:tags andResponse:responseBlock];
    }
    else {
        [self.sailthruMobile trackPageviewWithUrl:nsUrl andResponse:responseBlock];
    }
}

RCT_EXPORT_METHOD(trackImpression:(NSString *)sectionID url:(NSArray *)urls resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    void (^responseBlock)(NSError * _Nullable) = ^(NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
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
        [self.sailthruMobile trackImpressionWithSection:sectionID andUrls:nsUrls andResponse:responseBlock];
    }
    else {
        [self.sailthruMobile trackImpressionWithSection:sectionID andResponse:responseBlock];
    }
}



#pragma mark - Switches
RCT_EXPORT_METHOD(setGeoIPTrackingEnabled:(BOOL)enabled) {
    [self.sailthruMobile setGeoIPTrackingEnabled:enabled];
}

RCT_EXPORT_METHOD(setGeoIPTrackingEnabled:(BOOL)enabled resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self.sailthruMobile setGeoIPTrackingEnabled:enabled withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setCrashHandlersEnabled:(BOOL)enabled) {
    [self.sailthruMobile setCrashHandlersEnabled:enabled];
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
    [self.sailthruMobile clearDeviceData:(STMDeviceDataType)options withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#pragma mark - Profile Vars

RCT_EXPORT_METHOD(setProfileVars:(NSDictionary *)vars resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self.sailthruMobile setProfileVars:vars withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(getProfileVars:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self.sailthruMobile getProfileVarsWithResponse:^(NSDictionary<NSString *,id> * _Nullable vars, NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(vars);
        }
    }];
}


#pragma mark - Purchases

RCT_EXPORT_METHOD(logPurchase:(NSDictionary *)purchaseDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    STMPurchase *purchase = [[STMPurchase alloc] initWithDictionary:purchaseDict];
    [self.sailthruMobile logPurchase:purchase withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(logAbandonedCart:(NSDictionary *)purchaseDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    STMPurchase *purchase = [[STMPurchase alloc] initWithDictionary:purchaseDict];
    [self.sailthruMobile logAbandonedCart:purchase withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNSailthruMobile rejectPromise:reject withError:error];
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
    for (STMMessage *message in messageArray) {
        [messageDictionaries addObject:[message dictionary]];
    }
    return messageDictionaries;
}

+ (STMMessage *) messageFromDict:(NSDictionary *)jsDict {
    return [[STMMessage alloc] initWithDictionary:jsDict];
}

+ (NSArray *)arrayOfContentItemsDictionaryFromContentItemsArray:(NSArray *)contentItemsArray {
  NSMutableArray *items = [NSMutableArray array];
  for (STMContentItem *contentItem in contentItemsArray) {
    [items addObject:[contentItem dictionary]];
  }
  return items;
}

@end
