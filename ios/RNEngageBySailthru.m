
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

