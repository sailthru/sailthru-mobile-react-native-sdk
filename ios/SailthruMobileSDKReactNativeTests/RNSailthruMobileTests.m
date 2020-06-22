
#import <XCTest/XCTest.h>
#import "RNSailthruMobile.h"
#import "Kiwi.h"
#import <UserNotifications/UserNotifications.h>

// interface to expose methods for testing
@interface RNSailthruMobile ()

-(instancetype)init;
// getMessages method
-(void)resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setAttributes:(NSDictionary *)attributeMap resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)updateLocation:(CGFloat)lat lon:(CGFloat)lon;
-(void)logEvent:(NSString *)name;
-(void)logEvent:(NSString *)name withVars:(NSDictionary*)varsDict;
-(void)getUnreadCount:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)markMessageAsRead:(NSDictionary*)jsDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)removeMessage:(NSDictionary *)jsDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)presentMessageDetail:(NSDictionary *)jsDict;
-(void)dismissMessageDetail;
-(void)registerMessageImpression:(NSInteger)impressionType forMessage:(NSDictionary *)jsDict;
-(void)getDeviceID:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setUserId:(NSString *)userID resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setUserEmail:(NSString *)userEmail resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)getRecommendations:(NSString *)sectionID resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)trackClick:(NSString *)sectionID url:(NSString *)url resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)trackPageview:(NSString *)url tags:(NSArray *)tags resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)trackImpression:(NSString *)sectionID url:(NSArray *)urls resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setGeoIPTrackingEnabled:(BOOL)enabled;
-(void)setGeoIPTrackingEnabled:(BOOL)enabled resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setGeoIPTrackingDefault:(BOOL)enabled;
-(void)setCrashHandlersEnabled:(BOOL)enabled;
-(void)registerForPushNotifications;
-(void)clearDevice:(NSInteger)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setProfileVars:(NSDictionary *)vars resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)getProfileVars:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)logPurchase:(NSDictionary *)purchaseDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)logAbandonedCart:(NSDictionary *)purchaseDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
@end


// interfaces to match RNSailthruMobile

@interface STMMessage ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end
@interface SailthruMobile ()

- (void)setWrapperName:(NSString *)wrapperName andVersion:(NSString *)wrapperVersion;

@end



SPEC_BEGIN(RNSailthruMobileSpec)

describe(@"RNSailthruMobile", ^{
    __block SailthruMobile *sailthruMobile = nil;
    __block STMMessageStream *messageStream = nil;
    __block RNSailthruMobile *rnSailthruMobile = nil;
         
    beforeEach(^{
        sailthruMobile = [SailthruMobile mock];
        [sailthruMobile stub:@selector(setWrapperName:andVersion:)];
        [SailthruMobile stub:@selector(new) andReturn:sailthruMobile];
    
        messageStream = [STMMessageStream mock];
        [messageStream stub:@selector(setDelegate:)];
        [STMMessageStream stub:@selector(new) andReturn:messageStream];
    
        rnSailthruMobile = [[RNSailthruMobile alloc] initWithDisplayInAppNotifications:YES];
    });
         
    context(@"the init method", ^{
        it(@"should throw an exception", ^{
            BOOL exceptionThrown = NO;
            @try {
                RNSailthruMobile *rnSailthruMobile = [[RNSailthruMobile alloc] init];
                (void)rnSailthruMobile;
            }
            @catch(NSException *e) {
                exceptionThrown = YES;
            }
            [[theValue(exceptionThrown) should] beYes];
        });
    });

    context(@"the initWithDisplayInAppNotifications method", ^{
        it(@"should set message stream delegate as self", ^{
            [[messageStream should] receive:@selector(setDelegate:)];
            RNSailthruMobile *rnSailthruMobile = [[RNSailthruMobile alloc] initWithDisplayInAppNotifications:YES];
            (void)rnSailthruMobile;
        });

        it(@"should set the wrapper name and version", ^{
            [[sailthruMobile should] receive:@selector(setWrapperName:andVersion:)];
            RNSailthruMobile *rnSailthruMobile = [[RNSailthruMobile alloc] initWithDisplayInAppNotifications:YES];
            (void)rnSailthruMobile;
        });

        it(@"should set the displayInAppNotifications", ^{
            RNSailthruMobile *rnSailthruMobile = [[RNSailthruMobile alloc] initWithDisplayInAppNotifications:NO];
            [[theValue(rnSailthruMobile.displayInAppNotifications) should] beNo];
        });
    });

    context(@"the getMessages method", ^{
        it(@"should call native method", ^{
            [[messageStream should] receive:@selector(messages:)];
            [rnSailthruMobile resolver:nil rejecter:nil];
        });

        it(@"should return message array on success", ^{
            // Setup variables
            __block NSArray *check = nil;
            NSArray *inputArray = @[];
            RCTPromiseResolveBlock resolve = ^(NSArray* count) {
                check = count;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(messages:) atIndex:0];

            // Start test
            [rnSailthruMobile resolver:resolve rejecter:nil];

            // Capture argument
            void (^completeBlock)(NSArray * _Nullable, NSError * _Nullable) = capture.argument;
            completeBlock(inputArray, nil);

            // Verify result
            [[check shouldNot] beNil];
            [[check should] beKindOfClass:[NSArray class]];
        });

        it(@"should return error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(messages:) atIndex:0];

            // Start test
            [rnSailthruMobile resolver:nil rejecter:reject];

            // Capture argument
            void (^completeBlock)(NSUInteger, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);

            // Verify result
            [[check should] equal:error];
        });
    });

    context(@"the setAttributes method", ^{
        it(@"should call native method", ^{
            [[sailthruMobile should] receive:@selector(setAttributes:withResponse:)];
            [rnSailthruMobile setAttributes:nil resolver:nil rejecter:nil];
        });
    });

    context(@"the updateLocation method", ^{
        it(@"should call native method", ^{
            CGFloat latitude = 10, longitude = 15;
            [[sailthruMobile should] receive:@selector(updateLocation:)];
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(updateLocation:) atIndex:0];

            [rnSailthruMobile updateLocation:latitude lon:longitude];

            CLLocation *location = capture.argument;
            [[theValue(location.coordinate.latitude) should] equal:theValue(latitude)];
            [[theValue(location.coordinate.longitude) should] equal:theValue(longitude)];
        });
    });

    context(@"the logEvent: method", ^{
        it(@"should call native method", ^{
            NSString *event = @"Test Event";
            [[sailthruMobile should] receive:@selector(logEvent:) withArguments:event];

            [rnSailthruMobile logEvent:event];
        });
    });
    
    context(@"the logEvent:withVars method", ^{
        it(@"should call native method", ^{
            NSString *event = @"Test Event";
            NSDictionary* eventVars = @{ @"varKey" : @"varValue" };
            [[sailthruMobile should] receive:@selector(logEvent:withVars:) withArguments:event, eventVars];
            
            [rnSailthruMobile logEvent:event withVars:eventVars];
        });
    });

    context(@"the getUnreadCount method", ^{
        it(@"should call native method", ^{
            [[messageStream should] receive:@selector(unreadCount:)];

            [rnSailthruMobile getUnreadCount:nil rejecter:nil];
        });

        it(@"should return count on success", ^{
            // Setup variables
            __block NSNumber *check = nil;
            NSUInteger count = 5;
            RCTPromiseResolveBlock resolve = ^(NSNumber* count) {
                check = count;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(unreadCount:) atIndex:0];

            // Start test
            [rnSailthruMobile getUnreadCount:resolve rejecter:nil];

            // Capture argument
            void (^completeBlock)(NSUInteger, NSError * _Nullable) = capture.argument;
            completeBlock(count, nil);

            // Verify result
            [[check shouldNot] beNil];
            [[check should] equal:@(count)];
        });

        it(@"should return error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(unreadCount:) atIndex:0];

            // Start test
            [rnSailthruMobile getUnreadCount:nil rejecter:reject];

            // Capture argument
            void (^completeBlock)(NSUInteger, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);

            // Verify result
            [[check should] equal:error];
        });
    });

    context(@"the markMessageAsRead:resolver:rejecter: method", ^{
        it(@"should call native method", ^{
            [[messageStream should] receive:@selector(markMessageAsRead:withResponse:)];

            [rnSailthruMobile markMessageAsRead:nil resolver:nil rejecter:nil];
        });
    });

    context(@"the removeMessage:resolver:rejecter: method", ^{
        it(@"should call native method", ^{
            [[messageStream should] receive:@selector(removeMessage:withResponse:)];

            [rnSailthruMobile removeMessage:nil resolver:nil rejecter:nil];
        });
    });

    context(@"the presentMessageDetail: method", ^{
        it(@"should call native method", ^{
            [[expectFutureValue(messageStream) shouldEventuallyBeforeTimingOutAfter(5)] receive:@selector(presentMessageDetailForMessage:)];
            
            [rnSailthruMobile presentMessageDetail:nil];
        });
    });

    context(@"the dismissMessageDetail method", ^{
        it(@"should call native method", ^{
            [[expectFutureValue(messageStream) shouldEventuallyBeforeTimingOutAfter(5)] receive:@selector(dismissMessageDetail)];

            [rnSailthruMobile dismissMessageDetail];
        });
    });

    context(@"the registerMessageImpression:forMessage: method", ^{
        it(@"should call native method", ^{
            [[messageStream should] receive:@selector(registerImpressionWithType:forMessage:)];

            [rnSailthruMobile registerMessageImpression:1 forMessage:nil];
        });
    });

    context(@"the getDeviceID:rejecter: method", ^{
        it(@"should call native method", ^{
            [[sailthruMobile should] receive:@selector(deviceID:)];

            [rnSailthruMobile getDeviceID:nil rejecter:nil];
        });

        it(@"should return count on success", ^{
            // Setup variables
            __block NSString *check = nil;
            NSString *deviceID = @"Device ID";
            RCTPromiseResolveBlock resolve = ^(NSString* deviceID) {
                check = deviceID;
            };
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(deviceID:) atIndex:0];

            // Start test
            [rnSailthruMobile getDeviceID:resolve rejecter:nil];

            // Capture argument
            void (^completeBlock)(NSString * _Nullable, NSError * _Nullable) = capture.argument;
            completeBlock(deviceID, nil);

            // Verify result
            [[check shouldNot] beNil];
            [[check should] equal:deviceID];
        });

        it(@"should return error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(deviceID:) atIndex:0];

            // Start test
            [rnSailthruMobile getDeviceID:nil rejecter:reject];

            // Capture argument
            void (^completeBlock)(NSString * _Nullable, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);

            // Verify result
            [[check should] equal:error];
        });
    });

    context(@"the setUserID: method", ^{
        it(@"should call native method", ^{
            [[sailthruMobile should] receive:@selector(setUserId:withResponse:)];

            [rnSailthruMobile setUserId:nil resolver:nil rejecter:nil];
        });
    });

    context(@"the setUserEmail: method", ^{
        it(@"should call native method", ^{
            [[sailthruMobile should] receive:@selector(setUserEmail:withResponse:)];

            [rnSailthruMobile setUserEmail:nil resolver:nil rejecter:nil];
        });
    });

    context(@"the getRecommendations:resolver:rejecter: method", ^{
        it(@"should call native method", ^{
            [[sailthruMobile should] receive:@selector(recommendationsWithSection:withResponse:)];

            [rnSailthruMobile getRecommendations:nil resolver:nil rejecter:nil];
        });

        it(@"should return recommendations on success", ^{
            // Setup variables
            __block NSArray *check = nil;
            NSString *sectionID = @"not-a-real-section";
            NSArray *returnedItems = @[];
            RCTPromiseResolveBlock resolve = ^(NSArray* contentItems) {
                check = contentItems;
            };
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(recommendationsWithSection:withResponse:) atIndex:1];

            // Start test
            [rnSailthruMobile getRecommendations:sectionID resolver:resolve rejecter:nil];

            // Capture argument
            void (^completeBlock)(NSArray * _Nullable, NSError * _Nullable) = capture.argument;
            completeBlock(returnedItems, nil);

            // Verify result
            [[check shouldNot] beNil];
            [[check should] equal:returnedItems];
        });

        it(@"should return error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(recommendationsWithSection:withResponse:) atIndex:1];

            // Start test
            [rnSailthruMobile getRecommendations:nil resolver:nil rejecter:reject];

            // Capture argument
            void (^completeBlock)(NSString * _Nullable, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);

            // Verify result
            [[check should] equal:error];
        });
    });

    context(@"the trackClick:url:resolver:rejecter: method", ^{
        it(@"should call native method", ^{
            NSString *url = @"www.notarealurl.com";

            [[sailthruMobile should] receive:@selector(trackClickWithSection:andUrl:andResponse:)];

            [rnSailthruMobile trackClick:nil url:url resolver:nil rejecter:nil];
        });
    });

    context(@"the trackPageview:tags:resolver:rejecter: method", ^{
        context(@"when tags are nil", ^{
            it(@"should call native method without tags", ^{
                NSString *url = @"www.notarealurl.com";

                [[sailthruMobile should] receive:@selector(trackPageviewWithUrl:andResponse:)];

                [rnSailthruMobile trackPageview:url tags:nil resolver:nil rejecter:nil];
            });
        });

        context(@"when tags are not nil", ^{
            it(@"should call native method with tags", ^{
                NSString *url = @"www.notarealurl.com";
                NSArray *tags = @[];

                [[sailthruMobile should] receive:@selector(trackPageviewWithUrl:andTags:andResponse:)];

                [rnSailthruMobile trackPageview:url tags:tags resolver:nil rejecter:nil];
            });
        });
    });

    context(@"the trackImpression:url:resolver:rejecter: method", ^{
        context(@"when urls are nil", ^{
            it(@"should call native method without urls", ^{
                [[sailthruMobile should] receive:@selector(trackImpressionWithSection:andResponse:)];

                [rnSailthruMobile trackImpression:nil url:nil resolver:nil rejecter:nil];
            });
        });

        context(@"when urls are not nil", ^{
            it(@"should call native method with urls", ^{
                NSArray *urls = @[];

                [[sailthruMobile should] receive:@selector(trackImpressionWithSection:andUrls:andResponse:)];

                [rnSailthruMobile trackImpression:nil url:urls resolver:nil rejecter:nil];
            });
        });
    });

    context(@"the setGeoIPTrackingEnabled: method", ^{
        it(@"should call native method", ^{
            [[sailthruMobile should] receive:@selector(setGeoIPTrackingEnabled:)];

            [rnSailthruMobile setGeoIPTrackingEnabled:YES];
        });
    });
    
    context(@"the setGeoIPTrackingEnabled:resolver:rejecter: method", ^{
        it(@"should call native method", ^{
            [[sailthruMobile should] receive:@selector(setGeoIPTrackingEnabled:withResponse:) withArguments:theValue(YES), any(), any()];
            
            [rnSailthruMobile setGeoIPTrackingEnabled:YES resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(setGeoIPTrackingEnabled:withResponse:) atIndex:1];
            
            // Start test
            [rnSailthruMobile setGeoIPTrackingEnabled:YES resolver:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(setGeoIPTrackingEnabled:withResponse:) atIndex:1];
            
            // Start test
            [rnSailthruMobile setGeoIPTrackingEnabled:YES resolver:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });

    context(@"the setCrashHandlersEnabled: method", ^{
        it(@"should call native method", ^{
            [[sailthruMobile should] receive:@selector(setCrashHandlersEnabled:)];

            [rnSailthruMobile setCrashHandlersEnabled:YES];
        });
    });
    
    context(@"the clearDevice:resolver:rejecter: method", ^{
        it(@"should call native method", ^{
            [[sailthruMobile should] receive:@selector(clearDeviceData:withResponse:) withArguments:theValue(STMDeviceDataTypeAttributes), any()];
            
            [rnSailthruMobile clearDevice:STMDeviceDataTypeAttributes resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(clearDeviceData:withResponse:) atIndex:1];
            
            // Start test
            [rnSailthruMobile clearDevice:STMDeviceDataTypeAttributes resolver:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(clearDeviceData:withResponse:) atIndex:1];
            
            // Start test
            [rnSailthruMobile clearDevice:STMDeviceDataTypeAttributes resolver:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the setProfileVars:resolver:rejecter: method", ^{
        it(@"should call native method", ^{
            NSDictionary *vars = @{};
            [[sailthruMobile should] receive:@selector(setProfileVars:withResponse:) withArguments:vars, any()];
            
            [rnSailthruMobile setProfileVars:vars resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            NSDictionary *vars = @{};
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(setProfileVars:withResponse:) atIndex:1];
            
            // Start test
            [rnSailthruMobile setProfileVars:vars resolver:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(setProfileVars:withResponse:) atIndex:1];
            
            // Start test
            [rnSailthruMobile setProfileVars:vars resolver:nil rejecter:reject];
            
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
            [[sailthruMobile should] receive:@selector(getProfileVarsWithResponse:) withArguments:any()];
            
            [rnSailthruMobile getProfileVars:nil rejecter:nil];
        });
        
        it(@"should return vars on success", ^{
            // Setup variables
            NSDictionary *vars = @{};
            __block NSDictionary *check = nil;
            RCTPromiseResolveBlock resolve = ^(NSDictionary *retVars) {
                check = retVars;
            };
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(getProfileVarsWithResponse:) atIndex:0];
            
            // Start test
            [rnSailthruMobile getProfileVars:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(getProfileVarsWithResponse:) atIndex:0];
            
            // Start test
            [rnSailthruMobile getProfileVars:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSDictionary *, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(nil, error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the registerForPushNotifications method", ^{
        __block NSProcessInfo *mockInfo = nil;
        __block UNAuthorizationOptions options = UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound;
        __block NSOperationQueue *mockQueue;
        beforeEach(^{
            mockQueue = [NSOperationQueue mock];
            [mockQueue stub:@selector(addOperationWithBlock:)];
            [NSOperationQueue stub:@selector(mainQueue) andReturn:mockQueue];
            
            NSOperatingSystemVersion version;
            version.majorVersion = 10;
            version.minorVersion = 0;
            version.patchVersion = 0;
            
            mockInfo = [NSProcessInfo mock];
            [mockInfo stub:@selector(operatingSystemVersion) andReturn:theValue(version)];
            
            [NSProcessInfo stub:@selector(processInfo) andReturn:mockInfo];
        });
        
        context(@"on iOS 10+", ^{
            __block UNUserNotificationCenter *mockCenter;
            beforeEach(^{
                mockCenter = [UNUserNotificationCenter mock];
                [UNUserNotificationCenter stub:@selector(currentNotificationCenter) andReturn:mockCenter];
            });
            
            it(@"should request authorization from the UNUserNotificationCenter", ^{
                [[mockCenter should] receive:@selector(requestAuthorizationWithOptions:completionHandler:)];
                
                [rnSailthruMobile registerForPushNotifications];
            });
        });
        
        context(@"on iOS 8-9", ^{
            __block UIApplication *mockApplication;
            beforeEach(^{
                mockApplication = [UIApplication mock];
                [UIApplication stub:@selector(sharedApplication) andReturn:mockApplication];
                
                NSOperatingSystemVersion version;
                version.majorVersion = 8;
                version.minorVersion = 0;
                version.patchVersion = 0;
                
                mockInfo = [NSProcessInfo mock];
                [mockInfo stub:@selector(operatingSystemVersion) andReturn:theValue(version)];
                
                [NSProcessInfo stub:@selector(processInfo) andReturn:mockInfo];
            });
            
            it(@"should register user notification settings with UIApplication", ^{
                UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:(UIUserNotificationType)options categories:nil];
                [[mockApplication should] receive:@selector(registerUserNotificationSettings:) withArguments:settings];
                
                [rnSailthruMobile registerForPushNotifications];
            });
        });
        
        context(@"if application is not registered for remote notifications", ^{
            __block UNUserNotificationCenter *mockCenter;
            __block UIApplication *mockApplication;
            beforeEach(^{
                mockCenter = [UNUserNotificationCenter mock];
                [mockCenter stub:@selector(requestAuthorizationWithOptions:completionHandler:)];
                [UNUserNotificationCenter stub:@selector(currentNotificationCenter) andReturn:mockCenter];
                
                mockApplication = [UIApplication mock];
                [mockApplication stub:@selector(isRegisteredForRemoteNotifications) andReturn:theValue(NO)];
                [UIApplication stub:@selector(sharedApplication) andReturn:mockApplication];
                
                
            });
            
            it(@"should register for remote notifications", ^{
                [[mockApplication should] receive:@selector(registerForRemoteNotifications)];
                KWCaptureSpy *queueCapture = [mockQueue captureArgument:@selector(addOperationWithBlock:) atIndex:0];
                
                [rnSailthruMobile registerForPushNotifications];
                
                void (^opBlock)(void) = queueCapture.argument;
                opBlock();
            });
        });
    });
    
    context(@"the logPurchase:resolver:rejecter: method", ^{
        __block NSDictionary *purchase = nil;
        beforeEach(^{
            purchase = @{@"items":@[]};
        });
        
        it(@"should call native method", ^{
            [[sailthruMobile should] receive:@selector(logPurchase:withResponse:)];
            
            [rnSailthruMobile logPurchase:purchase resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(logPurchase:withResponse:) atIndex:1];
            
            // Start test
            [rnSailthruMobile logPurchase:purchase resolver:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(logPurchase:withResponse:) atIndex:1];
            
            // Start test
            [rnSailthruMobile logPurchase:purchase resolver:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the logAbandonedCart:resolver:rejecter: method", ^{
        __block NSDictionary *purchase = nil;
        beforeEach(^{
            purchase = @{@"items":@[]};
        });
        
        it(@"should call native method", ^{
            [[sailthruMobile should] receive:@selector(logAbandonedCart:withResponse:)];
            
            [rnSailthruMobile logAbandonedCart:purchase resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(logAbandonedCart:withResponse:) atIndex:1];
            
            // Start test
            [rnSailthruMobile logAbandonedCart:purchase resolver:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [sailthruMobile captureArgument:@selector(logAbandonedCart:withResponse:) atIndex:1];
            
            // Start test
            [rnSailthruMobile logAbandonedCart:purchase resolver:nil rejecter:reject];
            
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
