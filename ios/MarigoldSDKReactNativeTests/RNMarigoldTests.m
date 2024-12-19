
#import <XCTest/XCTest.h>
#import "RNMarigold.h"
#import "Kiwi.h"
#import <UserNotifications/UserNotifications.h>
#import <Marigold/Marigold.h>

// interface to expose methods for testing
@interface RNMarigold ()

-(instancetype)init;
-(void)updateLocation:(CGFloat)lat lon:(CGFloat)lon;
-(void)getDeviceID:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)setGeoIPTrackingEnabled:(BOOL)enabled resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)setGeoIPTrackingDefault:(BOOL)enabled;
-(void)setCrashHandlersEnabled:(BOOL)enabled;
-(void)logRegistrationEvent:(NSString * _Nullable)userId;
-(void)registerForPushNotifications;
-(void)syncNotificationSettings;
-(void)clearDevice:(NSInteger)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)setInAppNotificationsEnabled:(BOOL)enabled;

@end


// interfaces to match RNMarigold
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
        
        rnMarigold = [[RNMarigold alloc] init];
    });
    
    context(@"the init method", ^{
        it(@"sets the wrapper name and version", ^{
            [[marigold should] receive:@selector(setWrapperName:andVersion:)];
            RNMarigold *rnMarigold = [[RNMarigold alloc] init];
            (void)rnMarigold;
        });
    });
    
    context(@"the updateLocation method", ^{
        it(@"calls the native method", ^{
            CGFloat latitude = 10, longitude = 15;
            [[marigold should] receive:@selector(updateLocation:)];
            KWCaptureSpy *capture = [marigold captureArgument:@selector(updateLocation:) atIndex:0];
            
            [rnMarigold updateLocation:latitude lon:longitude];
            
            CLLocation *location = capture.argument;
            [[theValue(location.coordinate.latitude) should] equal:theValue(latitude)];
            [[theValue(location.coordinate.longitude) should] equal:theValue(longitude)];
        });
    });
    
    context(@"the getDeviceID:reject: method", ^{
        it(@"calls the native method", ^{
            [[marigold should] receive:@selector(deviceID:)];
            
            [rnMarigold getDeviceID:nil reject:nil];
        });
        
        it(@"returns count on success", ^{
            // Setup variables
            __block NSString *check = nil;
            NSString *deviceID = @"Device ID";
            RCTPromiseResolveBlock resolve = ^(NSString* deviceID) {
                check = deviceID;
            };
            KWCaptureSpy *capture = [marigold captureArgument:@selector(deviceID:) atIndex:0];
            
            // Start test
            [rnMarigold getDeviceID:resolve reject:nil];
            
            // Capture argument
            void (^completeBlock)(NSString * _Nullable, NSError * _Nullable) = capture.argument;
            completeBlock(deviceID, nil);
            
            // Verify result
            [[check shouldNot] beNil];
            [[check should] equal:deviceID];
        });
        
        it(@"returns error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [marigold captureArgument:@selector(deviceID:) atIndex:0];
            
            // Start test
            [rnMarigold getDeviceID:nil reject:reject];
            
            // Capture argument
            void (^completeBlock)(NSString * _Nullable, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the setGeoIPTrackingEnabled:resolve:reject: method", ^{
        it(@"calls the native method", ^{
            [[marigold should] receive:@selector(setGeoIPTrackingEnabled:withResponse:) withArguments:theValue(YES), any(), any()];
            
            [rnMarigold setGeoIPTrackingEnabled:YES resolve:nil reject:nil];
        });
        
        it(@"returns success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [marigold captureArgument:@selector(setGeoIPTrackingEnabled:withResponse:) atIndex:1];
            
            // Start test
            [rnMarigold setGeoIPTrackingEnabled:YES resolve:resolve reject:nil];
            
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
            KWCaptureSpy *capture = [marigold captureArgument:@selector(setGeoIPTrackingEnabled:withResponse:) atIndex:1];
            
            // Start test
            [rnMarigold setGeoIPTrackingEnabled:YES resolve:nil reject:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the setCrashHandlersEnabled: method", ^{
        it(@"calls the native method", ^{
            [[marigold should] receive:@selector(setCrashHandlersEnabled:)];
            
            [rnMarigold setCrashHandlersEnabled:YES];
        });
    });
    
    context(@"the logRegistrationEvent: method", ^{
        it(@"calls the native method", ^{
            [[marigold should] receive:@selector(logRegistrationEvent:)];
            [rnMarigold logRegistrationEvent:@"event"];
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
        
        it(@"requests authorization from the UNUserNotificationCenter", ^{
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
            
            it(@"registers for remote notifications", ^{
                [[mockApplication should] receive:@selector(registerForRemoteNotifications)];
                
                [rnMarigold registerForPushNotifications];
            });
        });
    });
    
    context(@"the syncNotificationSettings method", ^{
        it(@"calls the native method", ^{
            [[marigold should] receive:@selector(syncNotificationSettings)];
            
            [rnMarigold syncNotificationSettings];
        });
    });
    
    context(@"the setInAppNotificationsEnabled: method", ^{
        it(@"calls the native method", ^{
            [[marigold should] receive:@selector(setInAppNotificationsEnabled:)];
            
            [rnMarigold setInAppNotificationsEnabled:YES];
        });
    });
    
});

SPEC_END
