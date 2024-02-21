#import <XCTest/XCTest.h>
#import "RNEngageBySailthru.h"
#import "Kiwi.h"
#import <UserNotifications/UserNotifications.h>

// interface to expose methods for testing
@interface RNEngageBySailthru ()

-(instancetype)init;
-(void)setAttributes:(NSDictionary *)attributeMap resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject __deprecated_msg("use setProfileVars:withResponse: instead");
-(void)logEvent:(NSString *)name;
-(void)logEvent:(NSString *)name withVars:(NSDictionary*)varsDict;
-(void)setUserId:(NSString *)userID resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setUserEmail:(NSString *)userEmail resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)trackClick:(NSString *)sectionID url:(NSString *)url resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)trackPageview:(NSString *)url tags:(NSArray *)tags resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)trackImpression:(NSString *)sectionID url:(NSArray *)urls resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setProfileVars:(NSDictionary *)vars resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)getProfileVars:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)logPurchase:(NSDictionary *)purchaseDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)logAbandonedCart:(NSDictionary *)purchaseDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
@end

@interface EngageBySailthru ()

- (void)setWrapperName:(NSString *)wrapperName andVersion:(NSString *)wrapperVersion;

@end

SPEC_BEGIN(RNEngageBySailthruSpec)

describe(@"RNEngageBySailthru", ^{
    __block EngageBySailthru *engageBySailthru = nil;
    __block RNEngageBySailthru *rnEngageBySailthru = nil;

    beforeEach(^{
        engageBySailthru = [EngageBySailthru mock];
        [engageBySailthru stub:@selector(setWrapperName:andVersion:)];
        [EngageBySailthru stub:@selector(new) andReturn:engageBySailthru];

        rnEngageBySailthru = [[RNEngageBySailthru alloc] init];
    });
    
    
    context(@"the setAttributes method", ^{
        it(@"should call native method", ^{
            [[engageBySailthru should] receive:@selector(setAttributes:withResponse:)];
            [rnEngageBySailthru setAttributes:nil resolver:nil rejecter:nil];
        });
    });
    
    context(@"the logEvent: method", ^{
        it(@"should call native method", ^{
            NSString *event = @"Test Event";
            [[engageBySailthru should] receive:@selector(logEvent:) withArguments:event];

            [rnEngageBySailthru logEvent:event];
        });
    });
    
    context(@"the logEvent:withVars method", ^{
        it(@"should call native method", ^{
            NSString *event = @"Test Event";
            NSDictionary* eventVars = @{ @"varKey" : @"varValue" };
            [[engageBySailthru should] receive:@selector(logEvent:withVars:) withArguments:event, eventVars];
            
            [rnEngageBySailthru logEvent:event withVars:eventVars];
        });
    });
    
    context(@"the setUserID: method", ^{
        it(@"should call native method", ^{
            [[engageBySailthru should] receive:@selector(setUserId:withResponse:)];

            [rnEngageBySailthru setUserId:nil resolver:nil rejecter:nil];
        });
    });

    context(@"the setUserEmail: method", ^{
        it(@"should call native method", ^{
            [[engageBySailthru should] receive:@selector(setUserEmail:withResponse:)];

            [rnEngageBySailthru setUserEmail:nil resolver:nil rejecter:nil];
        });
    });

    context(@"the trackClick:url:resolver:rejecter: method", ^{
        it(@"should call native method", ^{
            NSString *url = @"www.notarealurl.com";

            [[engageBySailthru should] receive:@selector(trackClickWithSection:andUrl:andResponse:)];

            [rnEngageBySailthru trackClick:nil url:url resolver:nil rejecter:nil];
        });
    });

    context(@"the trackPageview:tags:resolver:rejecter: method", ^{
        context(@"when tags are nil", ^{
            it(@"should call native method without tags", ^{
                NSString *url = @"www.notarealurl.com";

                [[engageBySailthru should] receive:@selector(trackPageviewWithUrl:andResponse:)];

                [rnEngageBySailthru trackPageview:url tags:nil resolver:nil rejecter:nil];
            });
        });

        context(@"when tags are not nil", ^{
            it(@"should call native method with tags", ^{
                NSString *url = @"www.notarealurl.com";
                NSArray *tags = @[];

                [[engageBySailthru should] receive:@selector(trackPageviewWithUrl:andTags:andResponse:)];

                [rnEngageBySailthru trackPageview:url tags:tags resolver:nil rejecter:nil];
            });
        });
    });

    context(@"the trackImpression:url:resolver:rejecter: method", ^{
        context(@"when urls are nil", ^{
            it(@"should call native method without urls", ^{
                [[engageBySailthru should] receive:@selector(trackImpressionWithSection:andResponse:)];

                [rnEngageBySailthru trackImpression:nil url:nil resolver:nil rejecter:nil];
            });
        });

        context(@"when urls are not nil", ^{
            it(@"should call native method with urls", ^{
                NSArray *urls = @[];

                [[engageBySailthru should] receive:@selector(trackImpressionWithSection:andUrls:andResponse:)];

                [rnEngageBySailthru trackImpression:nil url:urls resolver:nil rejecter:nil];
            });
        });
    });
    
    
    context(@"the setProfileVars:resolver:rejecter: method", ^{
        it(@"should call native method", ^{
            NSDictionary *vars = @{};
            [[engageBySailthru should] receive:@selector(setProfileVars:withResponse:) withArguments:vars, any()];
            
            [rnEngageBySailthru setProfileVars:vars resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            NSDictionary *vars = @{};
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(setProfileVars:withResponse:) atIndex:1];
            
            // Start test
            [rnEngageBySailthru setProfileVars:vars resolver:resolve rejecter:nil];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            completeBlock(nil);
            
            // Verify result
            [[theValue(check) should] equal:theValue(YES)];
        });
        
        it(@"should return error on failure", ^{
            // Setup variables
            NSDictionary *vars = @{};
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(setProfileVars:withResponse:) atIndex:1];
            
            // Start test
            [rnEngageBySailthru setProfileVars:vars resolver:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the getProfileVars:rejecter: method", ^{
        it(@"should call native method", ^{
            [[engageBySailthru should] receive:@selector(getProfileVarsWithResponse:) withArguments:any()];
            
            [rnEngageBySailthru getProfileVars:nil rejecter:nil];
        });
        
        it(@"should return vars on success", ^{
            // Setup variables
            NSDictionary *vars = @{};
            __block NSDictionary *check = nil;
            RCTPromiseResolveBlock resolve = ^(NSDictionary *retVars) {
                check = retVars;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(getProfileVarsWithResponse:) atIndex:0];
            
            // Start test
            [rnEngageBySailthru getProfileVars:resolve rejecter:nil];
            
            // Capture argument
            void (^completeBlock)(NSDictionary *, NSError * _Nullable) = capture.argument;
            completeBlock(vars, nil);
            
            // Verify result
            [[check should] equal:vars];
        });
        
        it(@"should return error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(getProfileVarsWithResponse:) atIndex:0];
            
            // Start test
            [rnEngageBySailthru getProfileVars:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSDictionary *, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(nil, error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the logPurchase:resolver:rejecter: method", ^{
        __block NSDictionary *purchase = nil;
        beforeEach(^{
            purchase = @{@"items":@[]};
        });
        
        it(@"should call native method", ^{
            [[engageBySailthru should] receive:@selector(logPurchase:withResponse:)];
            
            [rnEngageBySailthru logPurchase:purchase resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(logPurchase:withResponse:) atIndex:1];
            
            // Start test
            [rnEngageBySailthru logPurchase:purchase resolver:resolve rejecter:nil];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            completeBlock(nil);
            
            // Verify result
            [[theValue(check) should] equal:theValue(YES)];
        });
        
        it(@"should return error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(logPurchase:withResponse:) atIndex:1];
            
            // Start test
            [rnEngageBySailthru logPurchase:purchase resolver:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
        
        describe(@"with purchase adjustments", ^{
            describe(@"with positive price", ^{
                beforeEach(^{
                    purchase = @{
                        @"items": @[],
                        @"adjustments": @[
                            @{
                                @"title":@"tax",
                                @"price":@123
                            }
                        ]
                    };
                });
                
                it(@"should include adjustments in purchase", ^{
                    KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(logPurchase:withResponse:) atIndex:0];
                    
                    // Start test
                    [rnEngageBySailthru logPurchase:purchase resolver:nil rejecter:nil];
                    
                    // Capture argument
                    MARPurchase *purchase = capture.argument;
                    
                    // Verify result
                    MARPurchaseAdjustment *adjustment = purchase.purchaseAdjustments[0];
                    [[adjustment.title should] equal:@"tax"];
                    [[adjustment.price should] equal:@123];
                });
            });
            
            describe(@"with negative price", ^{
                beforeEach(^{
                    purchase = @{
                        @"items": @[],
                        @"adjustments": @[
                            @{
                                @"title":@"tax",
                                @"price":@-123
                            }
                        ]
                    };
                });
                
                it(@"should include adjustments in purchase", ^{
                    KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(logPurchase:withResponse:) atIndex:0];
                    
                    // Start test
                    [rnEngageBySailthru logPurchase:purchase resolver:nil rejecter:nil];
                    
                    // Capture argument
                    MARPurchase *purchase = capture.argument;
                    
                    // Verify result
                    MARPurchaseAdjustment *adjustment = purchase.purchaseAdjustments[0];
                    [[adjustment.title should] equal:@"tax"];
                    [[adjustment.price should] equal:@-123];
                });
            });
        });
    });
    
    context(@"the logAbandonedCart:resolver:rejecter: method", ^{
        __block NSDictionary *purchase = nil;
        beforeEach(^{
            purchase = @{@"items":@[]};
        });
        
        it(@"should call native method", ^{
            [[engageBySailthru should] receive:@selector(logAbandonedCart:withResponse:)];
            
            [rnEngageBySailthru logAbandonedCart:purchase resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(logAbandonedCart:withResponse:) atIndex:1];
            
            // Start test
            [rnEngageBySailthru logAbandonedCart:purchase resolver:resolve rejecter:nil];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            completeBlock(nil);
            
            // Verify result
            [[theValue(check) should] equal:theValue(YES)];
        });
        
        it(@"should return error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(logAbandonedCart:withResponse:) atIndex:1];
            
            // Start test
            [rnEngageBySailthru logAbandonedCart:purchase resolver:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
});

SPEC_END

