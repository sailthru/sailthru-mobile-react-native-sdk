
#import <XCTest/XCTest.h>
#import "RNMarigold.h"
#import "Kiwi.h"
#import <UserNotifications/UserNotifications.h>

// interface to expose methods for testing
@interface RNMarigold ()

-(instancetype)init;
-(void)startEngine:(NSString*)sdkKey;
-(void)resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)updateLocation:(CGFloat)lat lon:(CGFloat)lon;
-(void)getUnreadCount:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)markMessageAsRead:(NSDictionary*)jsDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)removeMessage:(NSDictionary *)jsDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)presentMessageDetail:(NSDictionary *)jsDict;
-(void)dismissMessageDetail;
-(void)registerMessageImpression:(NSInteger)impressionType forMessage:(NSDictionary *)jsDict;
-(void)getDeviceID:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setGeoIPTrackingEnabled:(BOOL)enabled;
-(void)setGeoIPTrackingEnabled:(BOOL)enabled resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)setGeoIPTrackingDefault:(BOOL)enabled;
-(void)setCrashHandlersEnabled:(BOOL)enabled;
-(void)logRegistrationEvent:(NSString * _Nullable)userId;
-(void)registerForPushNotifications;
-(void)syncNotificationSettings;
-(void)clearDevice:(NSInteger)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;

@end


// interfaces to match RNMarigold

@interface MARMessage ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end
@interface Marigold ()

- (void)setWrapperName:(NSString *)wrapperName andVersion:(NSString *)wrapperVersion;

@end



SPEC_BEGIN(RNMarigoldSpec)

describe(@"RNMarigold", ^{
    __block Marigold *marigold = nil;
    __block MARMessageStream *messageStream = nil;
    __block RNMarigold *rnMarigold = nil;
    
    beforeEach(^{
        marigold = [Marigold mock];
        [marigold stub:@selector(setWrapperName:andVersion:)];
        [Marigold stub:@selector(new) andReturn:marigold];
        
        messageStream = [MARMessageStream mock];
        [messageStream stub:@selector(setDelegate:)];
        [MARMessageStream stub:@selector(new) andReturn:messageStream];
        
        rnMarigold = [[RNMarigold alloc] initWithDisplayInAppNotifications:YES];
    });
    
    context(@"the init method", ^{
        it(@"should set message stream delegate as self", ^{
            [[messageStream should] receive:@selector(setDelegate:)];
            RNMarigold *rnMarigold = [[RNMarigold alloc] init];
            (void)rnMarigold;
        });
        
        it(@"should set the wrapper name and version", ^{
            [[marigold should] receive:@selector(setWrapperName:andVersion:)];
            RNMarigold *rnMarigold = [[RNMarigold alloc] init];
            (void)rnMarigold;
        });
        
        it(@"should set the displayInAppNotifications to YES", ^{
            RNMarigold *rnMarigold = [[RNMarigold alloc] init];
            [[theValue(rnMarigold.displayInAppNotifications) should] beYes];
        });
    });
    
    context(@"the initWithDisplayInAppNotifications method", ^{
        it(@"should set message stream delegate as self", ^{
            [[messageStream should] receive:@selector(setDelegate:)];
            RNMarigold *rnMarigold = [[RNMarigold alloc] initWithDisplayInAppNotifications:YES];
            (void)rnMarigold;
        });
        
        it(@"should set the wrapper name and version", ^{
            [[marigold should] receive:@selector(setWrapperName:andVersion:)];
            RNMarigold *rnMarigold = [[RNMarigold alloc] initWithDisplayInAppNotifications:YES];
            (void)rnMarigold;
        });
        
        it(@"should set the displayInAppNotifications", ^{
            RNMarigold *rnMarigold = [[RNMarigold alloc] initWithDisplayInAppNotifications:NO];
            [[theValue(rnMarigold.displayInAppNotifications) should] beNo];
        });
    });
    
    context(@"the startEngine: method", ^{
        it(@"should call native method", ^{
            NSString *testKey = @"TESTKEY";
            [[marigold should] receive:@selector(startEngine:withAuthorizationOption:error:) withArguments:testKey, theValue(MARPushAuthorizationOptionNoRequest), nil];
            [rnMarigold startEngine:testKey];
        });
    });
    
    context(@"the getMessages method", ^{
        it(@"should call native method", ^{
            [[messageStream should] receive:@selector(messages:)];
            [rnMarigold resolver:nil rejecter:nil];
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
            [rnMarigold resolver:resolve rejecter:nil];
            
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
            [rnMarigold resolver:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSUInteger, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the updateLocation method", ^{
        it(@"should call native method", ^{
            CGFloat latitude = 10, longitude = 15;
            [[marigold should] receive:@selector(updateLocation:)];
            KWCaptureSpy *capture = [marigold captureArgument:@selector(updateLocation:) atIndex:0];
            
            [rnMarigold updateLocation:latitude lon:longitude];
            
            CLLocation *location = capture.argument;
            [[theValue(location.coordinate.latitude) should] equal:theValue(latitude)];
            [[theValue(location.coordinate.longitude) should] equal:theValue(longitude)];
        });
    });
    
    context(@"the getUnreadCount method", ^{
        it(@"should call native method", ^{
            [[messageStream should] receive:@selector(unreadCount:)];
            
            [rnMarigold getUnreadCount:nil rejecter:nil];
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
            [rnMarigold getUnreadCount:resolve rejecter:nil];
            
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
            [rnMarigold getUnreadCount:nil rejecter:reject];
            
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
            
            [rnMarigold markMessageAsRead:nil resolver:nil rejecter:nil];
        });
    });
    
    context(@"the removeMessage:resolver:rejecter: method", ^{
        it(@"should call native method", ^{
            [[messageStream should] receive:@selector(removeMessage:withResponse:)];
            
            [rnMarigold removeMessage:nil resolver:nil rejecter:nil];
        });
    });
    
    context(@"the presentMessageDetail: method", ^{
        it(@"should call native method", ^{
            [[expectFutureValue(messageStream) shouldEventuallyBeforeTimingOutAfter(5)] receive:@selector(presentMessageDetailForMessage:)];
            
            [rnMarigold presentMessageDetail:nil];
        });
    });
    
    context(@"the dismissMessageDetail method", ^{
        it(@"should call native method", ^{
            [[expectFutureValue(messageStream) shouldEventuallyBeforeTimingOutAfter(5)] receive:@selector(dismissMessageDetail)];
            
            [rnMarigold dismissMessageDetail];
        });
    });
    
    context(@"the registerMessageImpression:forMessage: method", ^{
        it(@"should call native method", ^{
            [[messageStream should] receive:@selector(registerImpressionWithType:forMessage:)];
            
            [rnMarigold registerMessageImpression:1 forMessage:nil];
        });
    });
    
    context(@"the getDeviceID:rejecter: method", ^{
        it(@"should call native method", ^{
            [[marigold should] receive:@selector(deviceID:)];
            
            [rnMarigold getDeviceID:nil rejecter:nil];
        });
        
        it(@"should return count on success", ^{
            // Setup variables
            __block NSString *check = nil;
            NSString *deviceID = @"Device ID";
            RCTPromiseResolveBlock resolve = ^(NSString* deviceID) {
                check = deviceID;
            };
            KWCaptureSpy *capture = [marigold captureArgument:@selector(deviceID:) atIndex:0];
            
            // Start test
            [rnMarigold getDeviceID:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [marigold captureArgument:@selector(deviceID:) atIndex:0];
            
            // Start test
            [rnMarigold getDeviceID:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSString * _Nullable, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    
    
    context(@"the setGeoIPTrackingEnabled: method", ^{
        it(@"should call native method", ^{
            [[marigold should] receive:@selector(setGeoIPTrackingEnabled:)];
            
            [rnMarigold setGeoIPTrackingEnabled:YES];
        });
    });
    
    context(@"the setGeoIPTrackingEnabled:resolver:rejecter: method", ^{
        it(@"should call native method", ^{
            [[marigold should] receive:@selector(setGeoIPTrackingEnabled:withResponse:) withArguments:theValue(YES), any(), any()];
            
            [rnMarigold setGeoIPTrackingEnabled:YES resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [marigold captureArgument:@selector(setGeoIPTrackingEnabled:withResponse:) atIndex:1];
            
            // Start test
            [rnMarigold setGeoIPTrackingEnabled:YES resolver:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [marigold captureArgument:@selector(setGeoIPTrackingEnabled:withResponse:) atIndex:1];
            
            // Start test
            [rnMarigold setGeoIPTrackingEnabled:YES resolver:nil rejecter:reject];
            
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
            [[marigold should] receive:@selector(setCrashHandlersEnabled:)];
            
            [rnMarigold setCrashHandlersEnabled:YES];
        });
    });
    
    context(@"the logRegistrationEvent: method", ^{
        it(@"should call native method", ^{
            [[marigold should] receive:@selector(logRegistrationEvent:)];
            [rnMarigold logRegistrationEvent:@"event"];
        });
    });
    
    context(@"the clearDevice:resolver:rejecter: method", ^{
        it(@"should call native method", ^{
            [[marigold should] receive:@selector(clearDeviceData:withResponse:) withArguments:theValue(MARDeviceDataTypeMessageStream), any()];
            
            [rnMarigold clearDevice:MARDeviceDataTypeMessageStream resolver:nil rejecter:nil];
        });
        
        it(@"should return success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [marigold captureArgument:@selector(clearDeviceData:withResponse:) atIndex:1];
            
            // Start test
            [rnMarigold clearDevice:MARDeviceDataTypeMessageStream resolver:resolve rejecter:nil];
            
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
            KWCaptureSpy *capture = [marigold captureArgument:@selector(clearDeviceData:withResponse:) atIndex:1];
            
            // Start test
            [rnMarigold clearDevice:MARDeviceDataTypeMessageStream resolver:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    
    context(@"the registerForPushNotifications method", ^{
        __block NSProcessInfo *mockInfo = nil;
        __block UNUserNotificationCenter *mockCenter;
        
        beforeEach(^{
            NSOperatingSystemVersion version;
            version.majorVersion = 10;
            version.minorVersion = 0;
            version.patchVersion = 0;
            
            mockInfo = [NSProcessInfo mock];
            [mockInfo stub:@selector(operatingSystemVersion) andReturn:theValue(version)];
            
            [NSProcessInfo stub:@selector(processInfo) andReturn:mockInfo];
            
            mockCenter = [UNUserNotificationCenter mock];
            [mockCenter stub:@selector(requestAuthorizationWithOptions:completionHandler:)];
            [UNUserNotificationCenter stub:@selector(currentNotificationCenter) andReturn:mockCenter];
        });
        
        it(@"should request authorization from the UNUserNotificationCenter", ^{
            [[mockCenter should] receive:@selector(requestAuthorizationWithOptions:completionHandler:)];
            
            [rnMarigold registerForPushNotifications];
        });
        
        context(@"if application is not registered for remote notifications", ^{
            __block UIApplication *mockApplication;
            beforeEach(^{
                mockApplication = [UIApplication mock];
                [mockApplication stub:@selector(isRegisteredForRemoteNotifications) andReturn:theValue(NO)];
                [UIApplication stub:@selector(sharedApplication) andReturn:mockApplication];
            });
            
            it(@"should register for remote notifications", ^{
                [[mockApplication should] receive:@selector(registerForRemoteNotifications)];
                
                [rnMarigold registerForPushNotifications];
            });
        });
    });
    
    context(@"the syncNotificationSettings method", ^{
        it(@"should call native method", ^{
            [[marigold should] receive:@selector(syncNotificationSettings)];
            
            [rnMarigold syncNotificationSettings];
        });
    });
    
});

SPEC_END
