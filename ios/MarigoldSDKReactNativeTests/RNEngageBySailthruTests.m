#import <XCTest/XCTest.h>
#import "RNEngageBySailthru.h"
#import <Marigold/EngageBySailthru.h>
#import "Kiwi.h"
#import <UserNotifications/UserNotifications.h>

// interface to expose methods for testing
@interface RNEngageBySailthru ()

-(EngageBySailthru *)engageBySailthruWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setAttributes:(NSDictionary *)attributeMap resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)removeAttribute:(NSString *)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)clearAttributes:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)logEvent:(NSString *)name;
-(void)logEventWithVars:(NSString *)name vars:(NSDictionary*)varsDict;
-(void)setUserId:(NSString *)userID resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setUserEmail:(NSString *)userEmail resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)trackClick:(NSString *)sectionID url:(NSString *)url resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)trackPageview:(NSString *)url tags:(NSArray *)tags resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)trackImpression:(NSString *)sectionID url:(NSArray *)urls resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setProfileVars:(NSDictionary *)vars resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)getProfileVars:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)logPurchase:(NSDictionary *)purchaseDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)logAbandonedCart:(NSDictionary *)purchaseDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)clearEvents:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
@end

SPEC_BEGIN(RNEngageBySailthruSpec)

describe(@"RNEngageBySailthru", ^{
    __block EngageBySailthru *engageBySailthru = nil;
    __block RNEngageBySailthru *rnEngageBySailthru = nil;

    beforeEach(^{
        engageBySailthru = [EngageBySailthru mock];
        [engageBySailthru stub:@selector(initWithError:) andReturn:engageBySailthru];
        [EngageBySailthru stub:@selector(alloc) andReturn:engageBySailthru];
        rnEngageBySailthru = [[RNEngageBySailthru alloc] init];
    });
    
    context(@"the setAttributes:resolver:rejecter: method", ^{
        it(@"calls the native method", ^{
            [[engageBySailthru should] receive:@selector(setAttributes:withCompletion:)];

            [rnEngageBySailthru setAttributes:nil resolver:nil rejecter:nil];
        });
        
        it(@"converts the attributes map", ^{
            NSDate *date1 = [NSDate now];
            NSDate *date2 = [[NSDate now] dateByAddingTimeInterval:1234];
            NSDictionary *attributeMap = @{
                @"mergeRule": @1,
                @"attributes": @{
                    @"stringAttr": @{
                        @"type": @"string",
                        @"value": @"hi"
                    },
                    @"stringsAttr": @{
                        @"type": @"stringArray",
                        @"value": @[ @"hello", @"there", @"buddy" ]
                    },
                    @"integerAttr": @{
                        @"type": @"integer",
                        @"value": @5
                    },
                    @"integersAttr": @{
                        @"type": @"integerArray",
                        @"value": @[ @1, @2, @3 ]
                    },
                    @"booleanAttr": @{
                        @"type": @"boolean",
                        @"value": @1
                    },
                    @"floatAttr": @{
                        @"type": @"float",
                        @"value": @1.5
                    },
                    @"floatsAttr": @{
                        @"type": @"floatArray",
                        @"value": @[ @1.23, @2.34, @3.45 ]
                    },
                    @"dateAttr": @{
                        @"type": @"date",
                        @"value": @((NSInteger)([date1 timeIntervalSince1970] * 1000))
                    },
                    @"datesAttr": @{
                        @"type": @"dateArray",
                        @"value": @[ @((NSInteger)([date1 timeIntervalSince1970] * 1000)), @((NSInteger)([date2 timeIntervalSince1970] * 1000)) ]
                    }
                }
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(setAttributes:withCompletion:) atIndex:0];
            
            // Start test
            [rnEngageBySailthru setAttributes:attributeMap resolver:nil rejecter:nil];
            
            // Capture argument
            MARAttributes *attributes = capture.argument;
            [[[attributes getString:@"stringAttr"] should] equal:@"hi"];
            [[[attributes getStrings:@"stringsAttr"] should] equal:@[ @"hello", @"there", @"buddy" ]];
            [[theValue([attributes getInteger:@"integerAttr" defaultValue:2]) should] equal:theValue(5)];
            [[[attributes getIntegers:@"integersAttr"] should] equal:@[ @1, @2, @3 ]];
            [[theValue([attributes getBool:@"booleanAttr" defaultValue:false]) should] beYes];
            [[theValue([attributes getFloat:@"floatAttr" defaultValue:3.45]) should] equal:theValue(1.5)];
            [[[attributes getFloats:@"floatsAttr"] should] equal:@[ @1.23, @2.34, @3.45 ]];
            NSDate *date = [attributes getDate:@"dateAttr"];
            [[theValue(floor([date timeIntervalSince1970])) should] equal:theValue(floor([date1 timeIntervalSince1970]))];
            NSArray *dates = [attributes getDates:@"datesAttr"];
            [[theValue(floor([dates[0] timeIntervalSince1970])) should] equal:theValue(floor([date1 timeIntervalSince1970]))];
            [[theValue(floor([dates[1] timeIntervalSince1970])) should] equal:theValue(floor([date2 timeIntervalSince1970]))];
            [[theValue([attributes getAttributesMergeRule]) should] equal:theValue(MARAttributesMergeRuleReplace)];
        });

        it(@"returns success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(setAttributes:withCompletion:) atIndex:1];

            // Start test
            [rnEngageBySailthru setAttributes:nil resolver:resolve rejecter:nil];

            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            completeBlock(nil);

            // Verify result
            [[theValue(check) should] equal:theValue(YES)];
        });

        it(@"returns error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(setAttributes:withCompletion:) atIndex:1];

            // Start test
            [rnEngageBySailthru setAttributes:nil resolver:nil rejecter:reject];

            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);

            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the removeAttribute:resolver:rejecter: method", ^{
        it(@"calls the native method", ^{
            [[engageBySailthru should] receive:@selector(removeAttributeWithKey:withCompletion:)];

            [rnEngageBySailthru removeAttribute:nil resolver:nil rejecter:nil];
        });
        
        it(@"uses the provided key", ^{
            NSString *key = @"test_key";
            
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(removeAttributeWithKey:withCompletion:) atIndex:0];
            
            // Start test
            [rnEngageBySailthru removeAttribute:key resolver:nil rejecter:nil];
            
            // Capture argument
            NSString *attributeKey = capture.argument;
            [[theValue([attributeKey isEqualToString:key]) should] equal:theValue(YES)];
        });

        it(@"returns success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(removeAttributeWithKey:withCompletion:) atIndex:1];

            // Start test
            [rnEngageBySailthru removeAttribute:nil resolver:resolve rejecter:nil];

            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            completeBlock(nil);

            // Verify result
            [[theValue(check) should] equal:theValue(YES)];
        });

        it(@"returns error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(removeAttributeWithKey:withCompletion:) atIndex:1];

            // Start test
            [rnEngageBySailthru removeAttribute:nil resolver:nil rejecter:reject];

            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);

            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the clearAttributes:rejecter: method", ^{
        it(@"calls the native method", ^{
            [[engageBySailthru should] receive:@selector(clearAttributesWithCompletion:)];

            [rnEngageBySailthru clearAttributes:nil rejecter:nil];
        });

        it(@"returns success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(clearAttributesWithCompletion:) atIndex:0];

            // Start test
            [rnEngageBySailthru clearAttributes:resolve rejecter:nil];

            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            completeBlock(nil);

            // Verify result
            [[theValue(check) should] equal:theValue(YES)];
        });

        it(@"returns error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(clearAttributesWithCompletion:) atIndex:0];

            // Start test
            [rnEngageBySailthru clearAttributes:nil rejecter:reject];

            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);

            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the logEvent: method", ^{
        it(@"should call native method", ^{
            NSString *event = @"Test Event";
            [[engageBySailthru should] receive:@selector(logEvent:) withArguments:event];

            [rnEngageBySailthru logEvent:event];
        });
    });
    
    context(@"the logEventWithVars:vars: method", ^{
        it(@"should call native method", ^{
            NSString *event = @"Test Event";
            NSDictionary* eventVars = @{ @"varKey" : @"varValue" };
            [[engageBySailthru should] receive:@selector(logEvent:withVars:) withArguments:event, eventVars];
            
            [rnEngageBySailthru logEventWithVars:event vars:eventVars];
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
    
    context(@"the clearEvents:rejecter: method", ^{
        it(@"should call native method", ^{
            [[engageBySailthru should] receive:@selector(clearEventsWithResponse:)];
            
            [rnEngageBySailthru clearEvents:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(clearEventsWithResponse:) atIndex:0];
            
            // Start test
            [rnEngageBySailthru clearEvents:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(clearEventsWithResponse:) atIndex:0];
            
            // Start test
            [rnEngageBySailthru clearEvents:nil rejecter:reject];
            
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

