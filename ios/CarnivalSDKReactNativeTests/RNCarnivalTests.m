
#import <XCTest/XCTest.h>
#import "RNCarnival.h"
#import "Kiwi.h"
#import <UserNotifications/UserNotifications.h>

// interface to expose methods for testing
@interface RNCarnival ()

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
@end


// interfaces to match RNCarnival

@interface CarnivalMessage ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end
@interface Carnival ()

+ (void)setWrapperName:(NSString *)wrapperName andVersion:(NSString *)wrapperVersion;

@end



SPEC_BEGIN(RNCarnivalSpec)

describe(@"RNCarnival", ^{
    context(@"the init method", ^{
        it(@"should throw an exception", ^{
            BOOL exceptionThrown = NO;
            @try {
                RNCarnival *rnCarnival = [[RNCarnival alloc] init];
                (void)rnCarnival;
            }
            @catch(NSException *e) {
                exceptionThrown = YES;
            }
            [[theValue(exceptionThrown) should] beYes];
        });
    });

    context(@"the initWithDisplayInAppNotifications method", ^{
        beforeEach(^{
            [Carnival stub:@selector(setWrapperName:andVersion:)];
        });

        it(@"should set message stream delegate as self", ^{
            [[CarnivalMessageStream should] receive:@selector(setDelegate:)];
            RNCarnival *rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
            (void)rnCarnival;
        });

        it(@"should set the wrapper name and version", ^{
            [[Carnival should] receive:@selector(setWrapperName:andVersion:)];
            RNCarnival *rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
            (void)rnCarnival;
        });

        it(@"should set the displayInAppNotifications", ^{
            RNCarnival *rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:NO];
            [[theValue(rnCarnival.displayInAppNotifications) should] beNo];
        });
    });

    context(@"the getMessages method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [CarnivalMessageStream stub:@selector(messages:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[CarnivalMessageStream should] receive:@selector(messages:)];
            [rnCarnival resolver:nil rejecter:nil];
        });

        it(@"should return message array on success", ^{
            // Setup variables
            __block NSArray *check = nil;
            NSArray *inputArray = @[];
            RCTPromiseResolveBlock resolve = ^(NSArray* count) {
                check = count;
            };
            KWCaptureSpy *capture = [CarnivalMessageStream captureArgument:@selector(messages:) atIndex:0];

            // Start test
            [rnCarnival resolver:resolve rejecter:nil];

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
            KWCaptureSpy *capture = [CarnivalMessageStream captureArgument:@selector(messages:) atIndex:0];

            // Start test
            [rnCarnival resolver:nil rejecter:reject];

            // Capture argument
            void (^completeBlock)(NSUInteger, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);

            // Verify result
            [[check should] equal:error];
        });
    });

    context(@"the setAttributes method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(setAttributes:withResponse:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[Carnival should] receive:@selector(setAttributes:withResponse:)];
            [rnCarnival setAttributes:nil resolver:nil rejecter:nil];
        });
    });

    context(@"the updateLocation method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(updateLocation:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            CGFloat latitude = 10, longitude = 15;
            [[Carnival should] receive:@selector(updateLocation:)];
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(updateLocation:) atIndex:0];

            [rnCarnival updateLocation:latitude lon:longitude];

            CLLocation *location = capture.argument;
            [[theValue(location.coordinate.latitude) should] equal:theValue(latitude)];
            [[theValue(location.coordinate.longitude) should] equal:theValue(longitude)];
        });
    });

    context(@"the logEvent: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(logEvent:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            NSString *event = @"Test Event";
            [[Carnival should] receive:@selector(logEvent:) withArguments:event];

            [rnCarnival logEvent:event];
        });
    });
    
    context(@"the logEvent:withVars method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(logEvent:withVars:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });
        
        it(@"should call native method", ^{
            NSString *event = @"Test Event";
            NSDictionary* eventVars = @{ @"varKey" : @"varValue" };
            [[Carnival should] receive:@selector(logEvent:withVars:) withArguments:event, eventVars];
            
            [rnCarnival logEvent:event withVars:eventVars];
        });
    });

    context(@"the getUnreadCount method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [CarnivalMessageStream stub:@selector(unreadCount:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[CarnivalMessageStream should] receive:@selector(unreadCount:)];

            [rnCarnival getUnreadCount:nil rejecter:nil];
        });

        it(@"should return count on success", ^{
            // Setup variables
            __block NSNumber *check = nil;
            NSUInteger count = 5;
            RCTPromiseResolveBlock resolve = ^(NSNumber* count) {
                check = count;
            };
            KWCaptureSpy *capture = [CarnivalMessageStream captureArgument:@selector(unreadCount:) atIndex:0];

            // Start test
            [rnCarnival getUnreadCount:resolve rejecter:nil];

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
            KWCaptureSpy *capture = [CarnivalMessageStream captureArgument:@selector(unreadCount:) atIndex:0];

            // Start test
            [rnCarnival getUnreadCount:nil rejecter:reject];

            // Capture argument
            void (^completeBlock)(NSUInteger, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);

            // Verify result
            [[check should] equal:error];
        });
    });

    context(@"the markMessageAsRead:resolver:rejecter: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [CarnivalMessageStream stub:@selector(markMessageAsRead:withResponse:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[CarnivalMessageStream should] receive:@selector(markMessageAsRead:withResponse:)];

            [rnCarnival markMessageAsRead:nil resolver:nil rejecter:nil];
        });
    });

    context(@"the removeMessage:resolver:rejecter: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [CarnivalMessageStream stub:@selector(removeMessage:withResponse:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[CarnivalMessageStream should] receive:@selector(removeMessage:withResponse:)];

            [rnCarnival removeMessage:nil resolver:nil rejecter:nil];
        });
    });

    context(@"the presentMessageDetail: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [CarnivalMessageStream stub:@selector(presentMessageDetailForMessage:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[CarnivalMessageStream should] receive:@selector(presentMessageDetailForMessage:)];
            
            [rnCarnival presentMessageDetail:nil];
        });
    });

    context(@"the dismissMessageDetail method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [CarnivalMessageStream stub:@selector(dismissMessageDetail)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[CarnivalMessageStream should] receive:@selector(dismissMessageDetail)];

            [rnCarnival dismissMessageDetail];
        });
    });

    context(@"the registerMessageImpression:forMessage: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [CarnivalMessageStream stub:@selector(registerImpressionWithType:forMessage:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[CarnivalMessageStream should] receive:@selector(registerImpressionWithType:forMessage:)];

            [rnCarnival registerMessageImpression:1 forMessage:nil];
        });
    });

    context(@"the getDeviceID:rejecter: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(deviceID:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[Carnival should] receive:@selector(deviceID:)];

            [rnCarnival getDeviceID:nil rejecter:nil];
        });

        it(@"should return count on success", ^{
            // Setup variables
            __block NSString *check = nil;
            NSString *deviceID = @"Device ID";
            RCTPromiseResolveBlock resolve = ^(NSString* deviceID) {
                check = deviceID;
            };
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(deviceID:) atIndex:0];

            // Start test
            [rnCarnival getDeviceID:resolve rejecter:nil];

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
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(deviceID:) atIndex:0];

            // Start test
            [rnCarnival getDeviceID:nil rejecter:reject];

            // Capture argument
            void (^completeBlock)(NSString * _Nullable, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);

            // Verify result
            [[check should] equal:error];
        });
    });

    context(@"the setUserID: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(setUserId:withResponse:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[Carnival should] receive:@selector(setUserId:withResponse:)];

            [rnCarnival setUserId:nil resolver:nil rejecter:nil];
        });
    });

    context(@"the setUserEmail: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(setUserEmail:withResponse:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[Carnival should] receive:@selector(setUserEmail:withResponse:)];

            [rnCarnival setUserEmail:nil resolver:nil rejecter:nil];
        });
    });

    context(@"the getRecommendations:resolver:rejecter: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(recommendationsWithSection:withResponse:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[Carnival should] receive:@selector(recommendationsWithSection:withResponse:)];

            [rnCarnival getRecommendations:nil resolver:nil rejecter:nil];
        });

        it(@"should return recommendations on success", ^{
            // Setup variables
            __block NSArray *check = nil;
            NSString *sectionID = @"not-a-real-section";
            NSArray *returnedItems = @[];
            RCTPromiseResolveBlock resolve = ^(NSArray* contentItems) {
                check = contentItems;
            };
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(recommendationsWithSection:withResponse:) atIndex:1];

            // Start test
            [rnCarnival getRecommendations:sectionID resolver:resolve rejecter:nil];

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
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(recommendationsWithSection:withResponse:) atIndex:1];

            // Start test
            [rnCarnival getRecommendations:nil resolver:nil rejecter:reject];

            // Capture argument
            void (^completeBlock)(NSString * _Nullable, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);

            // Verify result
            [[check should] equal:error];
        });
    });

    context(@"the trackClick:url:resolver:rejecter: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(trackClickWithSection:andUrl:andResponse:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            NSString *url = @"www.notarealurl.com";

            [[Carnival should] receive:@selector(trackClickWithSection:andUrl:andResponse:)];

            [rnCarnival trackClick:nil url:url resolver:nil rejecter:nil];
        });
    });

    context(@"the trackPageview:tags:resolver:rejecter: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        context(@"when tags are nil", ^{
            it(@"should call native method without tags", ^{
                NSString *url = @"www.notarealurl.com";

                [Carnival stub:@selector(trackPageviewWithUrl:andResponse:)];
                [[Carnival should] receive:@selector(trackPageviewWithUrl:andResponse:)];

                [rnCarnival trackPageview:url tags:nil resolver:nil rejecter:nil];
            });
        });

        context(@"when tags are not nil", ^{
            it(@"should call native method with tags", ^{
                NSString *url = @"www.notarealurl.com";
                NSArray *tags = @[];

                [Carnival stub:@selector(trackPageviewWithUrl:andTags:andResponse:)];
                [[Carnival should] receive:@selector(trackPageviewWithUrl:andTags:andResponse:)];

                [rnCarnival trackPageview:url tags:tags resolver:nil rejecter:nil];
            });
        });
    });

    context(@"the trackImpression:url:resolver:rejecter: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        context(@"when urls are nil", ^{
            it(@"should call native method without urls", ^{
                [Carnival stub:@selector(trackImpressionWithSection:andResponse:)];
                [[Carnival should] receive:@selector(trackImpressionWithSection:andResponse:)];

                [rnCarnival trackImpression:nil url:nil resolver:nil rejecter:nil];
            });
        });

        context(@"when urls are not nil", ^{
            it(@"should call native method with urls", ^{
                NSArray *urls = @[];

                [Carnival stub:@selector(trackImpressionWithSection:andUrls:andResponse:)];
                [[Carnival should] receive:@selector(trackImpressionWithSection:andUrls:andResponse:)];

                [rnCarnival trackImpression:nil url:urls resolver:nil rejecter:nil];
            });
        });
    });

    context(@"the setGeoIPTrackingEnabled: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(setGeoIPTrackingEnabled:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[Carnival should] receive:@selector(setGeoIPTrackingEnabled:)];

            [rnCarnival setGeoIPTrackingEnabled:YES];
        });
    });
    
    context(@"the setGeoIPTrackingEnabled:resolver:rejecter: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(setGeoIPTrackingEnabled:withResponse:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });
        
        it(@"should call native method", ^{
            [[Carnival should] receive:@selector(setGeoIPTrackingEnabled:withResponse:) withArguments:theValue(YES), any(), any()];
            
            [rnCarnival setGeoIPTrackingEnabled:YES resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(setGeoIPTrackingEnabled:withResponse:) atIndex:1];
            
            // Start test
            [rnCarnival setGeoIPTrackingEnabled:YES resolver:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(setGeoIPTrackingEnabled:withResponse:) atIndex:1];
            
            // Start test
            [rnCarnival setGeoIPTrackingEnabled:YES resolver:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });

    context(@"the setCrashHandlersEnabled: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(setCrashHandlersEnabled:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[Carnival should] receive:@selector(setCrashHandlersEnabled:)];

            [rnCarnival setCrashHandlersEnabled:YES];
        });
    });

    context(@"the registerForPushNotifications method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(setCrashHandlersEnabled:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });

        it(@"should call native method", ^{
            [[Carnival should] receive:@selector(setCrashHandlersEnabled:)];

            [rnCarnival setCrashHandlersEnabled:YES];
        });
    });
    
    context(@"the clearDevice:resolver:rejecter: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(clearDeviceData:withResponse:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });
        
        it(@"should call native method", ^{
            [[Carnival should] receive:@selector(clearDeviceData:withResponse:) withArguments:theValue(CarnivalDeviceDataTypeAttributes), any()];
            
            [rnCarnival clearDevice:CarnivalDeviceDataTypeAttributes resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(clearDeviceData:withResponse:) atIndex:1];
            
            // Start test
            [rnCarnival clearDevice:CarnivalDeviceDataTypeAttributes resolver:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(clearDeviceData:withResponse:) atIndex:1];
            
            // Start test
            [rnCarnival clearDevice:CarnivalDeviceDataTypeAttributes resolver:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the setProfileVars:resolver:rejecter: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(setProfileVars:withResponse:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });
        
        it(@"should call native method", ^{
            NSDictionary *vars = @{};
            [[Carnival should] receive:@selector(setProfileVars:withResponse:) withArguments:vars, any()];
            
            [rnCarnival setProfileVars:vars resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            NSDictionary *vars = @{};
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(setProfileVars:withResponse:) atIndex:1];
            
            // Start test
            [rnCarnival setProfileVars:vars resolver:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(setProfileVars:withResponse:) atIndex:1];
            
            // Start test
            [rnCarnival setProfileVars:vars resolver:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the getProfileVars:rejecter: method", ^{
        __block RNCarnival *rnCarnival = nil;
        beforeEach(^{
            [Carnival stub:@selector(getProfileVarsWithResponse:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
        });
        
        it(@"should call native method", ^{
            [[Carnival should] receive:@selector(getProfileVarsWithResponse:) withArguments:any()];
            
            [rnCarnival getProfileVars:nil rejecter:nil];
        });
        
        it(@"should return vars on success", ^{
            // Setup variables
            NSDictionary *vars = @{};
            __block NSDictionary *check = nil;
            RCTPromiseResolveBlock resolve = ^(NSDictionary *retVars) {
                check = retVars;
            };
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(getProfileVarsWithResponse:) atIndex:0];
            
            // Start test
            [rnCarnival getProfileVars:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [Carnival captureArgument:@selector(getProfileVarsWithResponse:) atIndex:0];
            
            // Start test
            [rnCarnival getProfileVars:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSDictionary *, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(nil, error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the registerForPushNotifications method", ^{
        __block RNCarnival *rnCarnival = nil;
        __block NSProcessInfo *mockInfo = nil;
        __block UNAuthorizationOptions options = UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound;
        __block NSOperationQueue *mockQueue;
        beforeEach(^{
            [Carnival stub:@selector(getProfileVarsWithResponse:)];
            rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:YES];
            
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
                
                [rnCarnival registerForPushNotifications];
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
                
                [rnCarnival registerForPushNotifications];
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
                
                [rnCarnival registerForPushNotifications];
                
                void (^opBlock)(void) = queueCapture.argument;
                opBlock();
            });
        });
    });
});

SPEC_END
