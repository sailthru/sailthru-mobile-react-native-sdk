
#import "RNEngageBySailthru.h"
#import <UserNotifications/UserNotifications.h>

@interface MARPurchase ()

- (nullable instancetype)initWithDictionary:(NSDictionary *)dictionary;

@end

@implementation RNEngageBySailthru

RCT_EXPORT_MODULE();

- (EngageBySailthru *)engageBySailthruWithRejecter:(RCTPromiseRejectBlock)reject {
    NSError *error;
    EngageBySailthru *engageBySailthru = [[EngageBySailthru alloc] initWithError:&error];
    if (error) {
        [RNEngageBySailthru rejectPromise:reject withError:error];
        return nil;
    }
    return engageBySailthru;
}

- (NSArray<NSString *> *)supportedEvents {
    return @[];
}

#pragma mark - Attributes
RCT_EXPORT_METHOD(setAttributes:(NSDictionary *)attributeMap resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
  {
    EngageBySailthru *engageBySailthru = [self engageBySailthruWithRejecter:reject];
    if (!engageBySailthru) {
        return;
    }

    MARAttributes *marAttributes = [[MARAttributes alloc] init];
    NSInteger mergeRule = [[attributeMap valueForKey:@"mergeRule"] integerValue];
    [marAttributes setAttributesMergeRule:(MARAttributesMergeRule)(mergeRule - 1)];

    NSDictionary *attributes = [attributeMap valueForKey:@"attributes"];

    for (NSString *key in attributes) {
        NSString *type = [[attributes valueForKey:key] valueForKey:@"type"];

        if ([type isEqualToString:@"string"]) {
            NSString *value = [[attributes valueForKey:key] valueForKey:@"value"];
            [marAttributes setString:value forKey:key];

        } else if ([type isEqualToString:@"stringArray"]) {
            NSArray<NSString *> *value = [[attributes valueForKey:key] valueForKey:@"value"];
            [marAttributes setStrings:value forKey:key];

        } else if ([type isEqualToString:@"integer"]) {
            NSNumber *value = [[attributes valueForKey:key] objectForKey:@"value"];
            [marAttributes setInteger:[value integerValue] forKey:key];

        } else if ([type isEqualToString:@"integerArray"]) {
            NSArray<NSNumber *> *value = [[attributes valueForKey:key] valueForKey:@"value"];
            [marAttributes setIntegers:value forKey:key];

        } else if ([type isEqualToString:@"boolean"]) {
            BOOL value = [[[attributes valueForKey:key] valueForKey:@"value"] boolValue];
            [marAttributes setBool:value forKey:key];

        } else if ([type isEqualToString:@"float"]) {
            NSNumber *numberValue = [[attributes valueForKey:key] objectForKey:@"value"];
            [marAttributes setFloat:[numberValue floatValue] forKey:key];

        } else if ([type isEqualToString:@"floatArray"]) {
            NSArray<NSNumber *> *value = [[attributes valueForKey:key] objectForKey:@"value"];
            [marAttributes setFloats:value forKey:key];

        } else if ([type isEqualToString:@"date"]) {
            NSNumber *millisecondsValue = [[attributes valueForKey:key] objectForKey:@"value"];
            NSNumber *value = @([millisecondsValue doubleValue] / 1000);

            if (![value isKindOfClass:[NSNumber class]]) {
                return;
            }

            NSDate *date = [NSDate dateWithTimeIntervalSince1970:[value doubleValue]];
            if (date) {
                [marAttributes setDate:date forKey:key];
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

            [marAttributes setDates:dates forKey:key];
        }
    }

    [engageBySailthru setAttributes:marAttributes withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#pragma mark - Events

RCT_EXPORT_METHOD(logEvent:(NSString *)name) {
    [[self engageBySailthruWithRejecter:nil] logEvent:name];
}

RCT_EXPORT_METHOD(logEvent:(NSString *)name withVars:(NSDictionary*)varsDict) {
    [[self engageBySailthruWithRejecter:nil] logEvent:name withVars:varsDict];
}

#pragma mark - IDs

RCT_EXPORT_METHOD(setUserId:(NSString *)userID resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    
    [[self engageBySailthruWithRejecter:reject] setUserId:userID withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setUserEmail:(NSString *)userEmail resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [[self engageBySailthruWithRejecter:reject] setUserEmail:userEmail withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#pragma mark - Recommendations

RCT_EXPORT_METHOD(trackClick:(NSString *)sectionID url:(NSString *)url resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSURL *nsUrl = [[NSURL alloc] initWithString:url];
    [[self engageBySailthruWithRejecter:reject] trackClickWithSection:sectionID andUrl:nsUrl andResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
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
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    };

    if(tags) {
        [[self engageBySailthruWithRejecter:reject] trackPageviewWithUrl:nsUrl andTags:tags andResponse:responseBlock];
    }
    else {
        [[self engageBySailthruWithRejecter:reject] trackPageviewWithUrl:nsUrl andResponse:responseBlock];
    }
}

RCT_EXPORT_METHOD(trackImpression:(NSString *)sectionID url:(NSArray *)urls resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    void (^responseBlock)(NSError * _Nullable) = ^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
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
        [[self engageBySailthruWithRejecter:reject] trackImpressionWithSection:sectionID andUrls:nsUrls andResponse:responseBlock];
    }
    else {
        [[self engageBySailthruWithRejecter:reject] trackImpressionWithSection:sectionID andResponse:responseBlock];
    }
}

#pragma mark - Profile Vars

RCT_EXPORT_METHOD(setProfileVars:(NSDictionary *)vars resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [[self engageBySailthruWithRejecter:reject] setProfileVars:vars withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(getProfileVars:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [[self engageBySailthruWithRejecter:reject] getProfileVarsWithResponse:^(NSDictionary<NSString *,id> * _Nullable vars, NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(vars);
        }
    }];
}

#pragma mark - Purchases

RCT_EXPORT_METHOD(logPurchase:(NSDictionary *)purchaseDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    MARPurchase *purchase = [[MARPurchase alloc] initWithDictionary:purchaseDict];
    [[self engageBySailthruWithRejecter:reject] logPurchase:purchase withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(logAbandonedCart:(NSDictionary *)purchaseDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    MARPurchase *purchase = [[MARPurchase alloc] initWithDictionary:purchaseDict];
    [[self engageBySailthruWithRejecter:reject] logAbandonedCart:purchase withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(clearAttributes:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [[self engageBySailthruWithRejecter:reject] clearAttributesWithResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(clearEvents:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [[self engageBySailthruWithRejecter:reject] clearEventsWithResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}


#pragma mark - Helper Fuctions

+ (void)rejectPromise:(RCTPromiseRejectBlock)reject withError:(NSError *)error {
    reject([NSString stringWithFormat:@"%ld", error.code], error.localizedDescription, error);
}


@end

