
#import "RNEngageBySailthru.h"
#import <UserNotifications/UserNotifications.h>
#import <Marigold/Marigold.h>

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

#pragma mark - Attributes

RCT_EXPORT_METHOD(setAttributes:(NSDictionary *)attributeMap resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
  {
    EngageBySailthru *engageBySailthru = [self engageBySailthruWithRejecter:reject];
    if (!engageBySailthru) {
        return;
    }

    MARAttributes *marAttributes = [MARAttributes new];
    NSInteger mergeRule = [[attributeMap valueForKey:@"mergeRule"] integerValue];
    [marAttributes setAttributesMergeRule:(MARAttributesMergeRule)mergeRule];

    NSDictionary *attributes = [attributeMap valueForKey:@"attributes"];
    [attributes enumerateKeysAndObjectsUsingBlock:^(NSString *  _Nonnull key, NSDictionary *  _Nonnull attribute, BOOL * _Nonnull stop) {
        NSString *type = [attribute valueForKey:@"type"];

        if ([type isEqualToString:@"string"]) {
            NSString *value = [attribute valueForKey:@"value"];
            [marAttributes setString:value forKey:key];

        } else if ([type isEqualToString:@"stringArray"]) {
            NSArray<NSString *> *value = [attribute valueForKey:@"value"];
            [marAttributes setStrings:value forKey:key];

        } else if ([type isEqualToString:@"integer"]) {
            NSNumber *value = [attribute objectForKey:@"value"];
            [marAttributes setInteger:[value integerValue] forKey:key];

        } else if ([type isEqualToString:@"integerArray"]) {
            NSArray<NSNumber *> *value = [attribute valueForKey:@"value"];
            [marAttributes setIntegers:value forKey:key];

        } else if ([type isEqualToString:@"boolean"]) {
            BOOL value = [[attribute valueForKey:@"value"] boolValue];
            [marAttributes setBool:value forKey:key];

        } else if ([type isEqualToString:@"float"]) {
            NSNumber *numberValue = [attribute objectForKey:@"value"];
            [marAttributes setFloat:[numberValue floatValue] forKey:key];

        } else if ([type isEqualToString:@"floatArray"]) {
            NSArray<NSNumber *> *value = [attribute objectForKey:@"value"];
            [marAttributes setFloats:value forKey:key];

        } else if ([type isEqualToString:@"date"]) {
            NSNumber *millisecondsValue = [attribute objectForKey:@"value"];
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
            NSArray<NSNumber *> *value = [attribute objectForKey:@"value"];
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
    }];

    [engageBySailthru setAttributes:marAttributes withCompletion:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(removeAttribute:(NSString *)key resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[self engageBySailthruWithRejecter:reject] removeAttributeWithKey:key withCompletion:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(clearAttributes:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[self engageBySailthruWithRejecter:reject] clearAttributesWithCompletion:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#pragma mark - Events

RCT_EXPORT_METHOD(logEvent:(NSString *)name vars:(NSDictionary*)varsDict) {
    [[self engageBySailthruWithRejecter:nil] logEvent:name withVars:varsDict];
}

#pragma mark - IDs

RCT_EXPORT_METHOD(setUserId:(NSString *)userID resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    
    [[self engageBySailthruWithRejecter:reject] setUserId:userID withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(setUserEmail:(NSString *)userEmail resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    [[self engageBySailthruWithRejecter:reject] setUserEmail:userEmail withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#pragma mark - Recommendations

RCT_EXPORT_METHOD(trackClick:(NSString *)sectionID url:(NSString *)url resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    NSURL *nsUrl = [[NSURL alloc] initWithString:url];
    [[self engageBySailthruWithRejecter:reject] trackClickWithSection:sectionID andUrl:nsUrl andResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(trackPageview:(NSString *)url tags:(NSArray *)tags resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
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

RCT_EXPORT_METHOD(trackImpression:(NSString *)sectionID urls:(NSArray *)urls resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
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

RCT_EXPORT_METHOD(setProfileVars:(NSDictionary *)vars resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[self engageBySailthruWithRejecter:reject] setProfileVars:vars withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(getProfileVars:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[self engageBySailthruWithRejecter:reject] getProfileVarsWithResponse:^(NSDictionary<NSString *,id> * _Nullable vars, NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(vars);
        }
    }];
}

#pragma mark - Purchases

RCT_EXPORT_METHOD(logPurchase:(NSDictionary *)purchaseDict resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    MARPurchase *purchase = [[MARPurchase alloc] initWithDictionary:purchaseDict];
    [[self engageBySailthruWithRejecter:reject] logPurchase:purchase withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(logAbandonedCart:(NSDictionary *)purchaseDict resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    MARPurchase *purchase = [[MARPurchase alloc] initWithDictionary:purchaseDict];
    [[self engageBySailthruWithRejecter:reject] logAbandonedCart:purchase withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(clearEvents:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[self engageBySailthruWithRejecter:reject] clearEventsWithResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNEngageBySailthru rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeRNEngageBySailthruSpecJSI>(params);
}
#endif


#pragma mark - Helper Fuctions

+ (void)rejectPromise:(RCTPromiseRejectBlock)reject withError:(NSError *)error {
    reject([NSString stringWithFormat:@"%ld", error.code], error.localizedDescription, error);
}


@end

