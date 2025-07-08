#import <XCTest/XCTest.h>
#import "RNEngageBySailthru.h"
#import "Kiwi.h"
#import <UserNotifications/UserNotifications.h>
#import <Marigold/Marigold.h>

#ifndef RCT_NEW_ARCH_ENABLED
// interface to expose methods for testing
@interface RNEngageBySailthru ()
-(EngageBySailthru *)engageBySailthruWithResolver:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)setAttributes:(NSDictionary *)attributeMap resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)removeAttribute:(NSString *)key resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)clearAttributes:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)logEvent:(NSString *)name vars:(NSDictionary*)varsDict;
-(void)setUserId:(NSString *)userID resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)setUserEmail:(NSString *)userEmail resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)trackClick:(NSString *)sectionID url:(NSString *)url resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)trackPageview:(NSString *)url tags:(NSArray *)tags resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)trackImpression:(NSString *)sectionID urls:(NSArray *)urls resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)setProfileVars:(NSDictionary *)vars resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)getProfileVars:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)logPurchase:(NSDictionary *)purchaseDict resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)logAbandonedCart:(NSDictionary *)purchaseDict resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)clearEvents:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
@end
#endif

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
    
    context(@"the setAttributes:resolve:reject: method", ^{
        it(@"calls the native method", ^{
            [[engageBySailthru should] receive:@selector(setAttributes:withCompletion:)];

            [rnEngageBySailthru setAttributes:nil resolve:nil reject:nil];
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
            [rnEngageBySailthru setAttributes:attributeMap resolve:nil reject:nil];
            
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
            [rnEngageBySailthru setAttributes:nil resolve:resolve reject:nil];

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
            [rnEngageBySailthru setAttributes:nil resolve:nil reject:reject];

            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);

            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the removeAttribute:resolve:reject: method", ^{
        it(@"calls the native method", ^{
            [[engageBySailthru should] receive:@selector(removeAttributeWithKey:withCompletion:)];

            [rnEngageBySailthru removeAttribute:nil resolve:nil reject:nil];
        });
        
        it(@"uses the provided key", ^{
            NSString *key = @"test_key";
            
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(removeAttributeWithKey:withCompletion:) atIndex:0];
            
            // Start test
            [rnEngageBySailthru removeAttribute:key resolve:nil reject:nil];
            
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
            [rnEngageBySailthru removeAttribute:nil resolve:resolve reject:nil];

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
            [rnEngageBySailthru removeAttribute:nil resolve:nil reject:reject];

            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);

            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the clearAttributes:reject: method", ^{
        it(@"calls the native method", ^{
            [[engageBySailthru should] receive:@selector(clearAttributesWithCompletion:)];

            [rnEngageBySailthru clearAttributes:nil reject:nil];
        });

        it(@"returns success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(clearAttributesWithCompletion:) atIndex:0];

            // Start test
            [rnEngageBySailthru clearAttributes:resolve reject:nil];

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
            [rnEngageBySailthru clearAttributes:nil reject:reject];

            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);

            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the logEvent:vars method", ^{
        it(@"calls the native method", ^{
            NSString *event = @"Test Event";
            NSDictionary* eventVars = @{ @"varKey" : @"varValue" };
            [[engageBySailthru should] receive:@selector(logEvent:withVars:) withArguments:event, eventVars];
            
            [rnEngageBySailthru logEvent:event vars:eventVars];
        });
    });
    
    context(@"the setUserID: method", ^{
        it(@"calls the native method", ^{
            [[engageBySailthru should] receive:@selector(setUserId:withResponse:)];

            [rnEngageBySailthru setUserId:nil resolve:nil reject:nil];
        });
    });

    context(@"the setUserEmail: method", ^{
        it(@"calls the native method", ^{
            [[engageBySailthru should] receive:@selector(setUserEmail:withResponse:)];

            [rnEngageBySailthru setUserEmail:nil resolve:nil reject:nil];
        });
    });

    context(@"the trackClick:url:resolve:reject: method", ^{
        it(@"calls the native method", ^{
            NSString *url = @"www.notarealurl.com";

            [[engageBySailthru should] receive:@selector(trackClickWithSection:andUrl:andResponse:)];

            [rnEngageBySailthru trackClick:nil url:url resolve:nil reject:nil];
        });
    });

    context(@"the trackPageview:tags:resolve:reject: method", ^{
        context(@"when tags are nil", ^{
            it(@"calls the native method without tags", ^{
                NSString *url = @"www.notarealurl.com";

                [[engageBySailthru should] receive:@selector(trackPageviewWithUrl:andResponse:)];

                [rnEngageBySailthru trackPageview:url tags:nil resolve:nil reject:nil];
            });
        });

        context(@"when tags are not nil", ^{
            it(@"calls the native method with tags", ^{
                NSString *url = @"www.notarealurl.com";
                NSArray *tags = @[];

                [[engageBySailthru should] receive:@selector(trackPageviewWithUrl:andTags:andResponse:)];

                [rnEngageBySailthru trackPageview:url tags:tags resolve:nil reject:nil];
            });
        });
    });

    context(@"the trackImpression:urls:resolve:reject: method", ^{
        context(@"when urls are nil", ^{
            it(@"calls the native method without urls", ^{
                [[engageBySailthru should] receive:@selector(trackImpressionWithSection:andResponse:)];

                [rnEngageBySailthru trackImpression:nil urls:nil resolve:nil reject:nil];
            });
        });

        context(@"when urls are not nil", ^{
            it(@"calls the native method with urls", ^{
                NSArray *urls = @[];

                [[engageBySailthru should] receive:@selector(trackImpressionWithSection:andUrls:andResponse:)];

                [rnEngageBySailthru trackImpression:nil urls:urls resolve:nil reject:nil];
            });
        });
    });
    
    
    context(@"the setProfileVars:resolve:reject: method", ^{
        it(@"calls the native method", ^{
            NSDictionary *vars = @{};
            [[engageBySailthru should] receive:@selector(setProfileVars:withResponse:) withArguments:vars, any()];
            
            [rnEngageBySailthru setProfileVars:vars resolve:nil reject:nil];
        });
        
        it(@"returns success", ^{
            // Setup variables
            NSDictionary *vars = @{};
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(setProfileVars:withResponse:) atIndex:1];
            
            // Start test
            [rnEngageBySailthru setProfileVars:vars resolve:resolve reject:nil];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            completeBlock(nil);
            
            // Verify result
            [[theValue(check) should] equal:theValue(YES)];
        });
        
        it(@"returns error on failure", ^{
            // Setup variables
            NSDictionary *vars = @{};
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(setProfileVars:withResponse:) atIndex:1];
            
            // Start test
            [rnEngageBySailthru setProfileVars:vars resolve:nil reject:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the getProfileVars:reject: method", ^{
        it(@"calls the native method", ^{
            [[engageBySailthru should] receive:@selector(getProfileVarsWithResponse:) withArguments:any()];
            
            [rnEngageBySailthru getProfileVars:nil reject:nil];
        });
        
        it(@"returns vars on success", ^{
            // Setup variables
            NSDictionary *vars = @{};
            __block NSDictionary *check = nil;
            RCTPromiseResolveBlock resolve = ^(NSDictionary *retVars) {
                check = retVars;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(getProfileVarsWithResponse:) atIndex:0];
            
            // Start test
            [rnEngageBySailthru getProfileVars:resolve reject:nil];
            
            // Capture argument
            void (^completeBlock)(NSDictionary *, NSError * _Nullable) = capture.argument;
            completeBlock(vars, nil);
            
            // Verify result
            [[check should] equal:vars];
        });
        
        it(@"returns error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(getProfileVarsWithResponse:) atIndex:0];
            
            // Start test
            [rnEngageBySailthru getProfileVars:nil reject:reject];
            
            // Capture argument
            void (^completeBlock)(NSDictionary *, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(nil, error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the logPurchase:resolve:reject: method", ^{
        __block NSDictionary *purchase = nil;
        beforeEach(^{
            purchase = @{@"items":@[]};
        });
        
        it(@"calls the native method", ^{
            [[engageBySailthru should] receive:@selector(logPurchase:withResponse:)];
            
            [rnEngageBySailthru logPurchase:purchase resolve:nil reject:nil];
        });
        
        it(@"returns success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(logPurchase:withResponse:) atIndex:1];
            
            // Start test
            [rnEngageBySailthru logPurchase:purchase resolve:resolve reject:nil];
            
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
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(logPurchase:withResponse:) atIndex:1];
            
            // Start test
            [rnEngageBySailthru logPurchase:purchase resolve:nil reject:reject];
            
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
                
                it(@"includes adjustments in purchase", ^{
                    KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(logPurchase:withResponse:) atIndex:0];
                    
                    // Start test
                    [rnEngageBySailthru logPurchase:purchase resolve:nil reject:nil];
                    
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
                
                it(@"includes adjustments in purchase", ^{
                    KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(logPurchase:withResponse:) atIndex:0];
                    
                    // Start test
                    [rnEngageBySailthru logPurchase:purchase resolve:nil reject:nil];
                    
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
    
    context(@"the logAbandonedCart:resolve:reject: method", ^{
        __block NSDictionary *purchase = nil;
        beforeEach(^{
            purchase = @{@"items":@[]};
        });
        
        it(@"calls the native method", ^{
            [[engageBySailthru should] receive:@selector(logAbandonedCart:withResponse:)];
            
            [rnEngageBySailthru logAbandonedCart:purchase resolve:nil reject:nil];
        });
        
        it(@"returns success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(logAbandonedCart:withResponse:) atIndex:1];
            
            // Start test
            [rnEngageBySailthru logAbandonedCart:purchase resolve:resolve reject:nil];
            
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
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(logAbandonedCart:withResponse:) atIndex:1];
            
            // Start test
            [rnEngageBySailthru logAbandonedCart:purchase resolve:nil reject:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the clearEvents:reject: method", ^{
        it(@"calls the native method", ^{
            [[engageBySailthru should] receive:@selector(clearEventsWithResponse:)];
            
            [rnEngageBySailthru clearEvents:nil reject:nil];
        });
        
        it(@"returns success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(clearEventsWithResponse:) atIndex:0];
            
            // Start test
            [rnEngageBySailthru clearEvents:resolve reject:nil];
            
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
            KWCaptureSpy *capture = [engageBySailthru captureArgument:@selector(clearEventsWithResponse:) atIndex:0];
            
            // Start test
            [rnEngageBySailthru clearEvents:nil reject:reject];
            
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

