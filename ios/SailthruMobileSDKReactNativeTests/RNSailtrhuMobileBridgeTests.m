#import <XCTest/XCTest.h>
#import "RNSailthruMobileBridge.h"
#import "RNSailthruMobile.h"
#import "Kiwi.h"

@interface SailthruMobile ()

- (void)setWrapperName:(NSString *)wrapperName andVersion:(NSString *)wrapperVersion;

@end

SPEC_BEGIN(RNSailthruMobileBridgeSpec)

describe(@"RNSailthruMobileBridge", ^{
    __block SailthruMobile *sailthruMobile = nil;
    
    beforeEach(^{
        sailthruMobile = [SailthruMobile mock];
        [sailthruMobile stub:@selector(setGeoIPTrackingDefault:)];
        [sailthruMobile stub:@selector(startEngine:withAuthorizationOption:)];
        [SailthruMobile stub:@selector(new) andReturn:sailthruMobile];
    });
         
    context(@"the init method", ^{
        it(@"should throw an exception", ^{
            BOOL exceptionThrown = NO;
            @try {
                RNSailthruMobileBridge *rnSailthruMobileBridge = [[RNSailthruMobileBridge alloc] init];
                (void)rnSailthruMobileBridge;
            }
            @catch(NSException *e) {
                exceptionThrown = YES;
            }
            [[theValue(exceptionThrown) should] beYes];
        });
    });
    
    context(@"the initWithJSCodeLocation:appKey: method", ^{
        __block NSURL *jsCodeLocation = nil;
        __block NSString *appKey = nil;
        
        beforeEach(^{
            jsCodeLocation = [NSURL mock];
            appKey = [NSString mock];
        });
        
        it(@"should set geo IP tracking default", ^{
            [[sailthruMobile should] receive:@selector(setGeoIPTrackingDefault:) withArguments:theValue(YES)];
            
            RNSailthruMobileBridge *rnSailthruMobileBridge = [[RNSailthruMobileBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                                             appKey:appKey];
            (void)rnSailthruMobileBridge;
        });
        
        it(@"should call startEngine", ^{
            [[sailthruMobile should] receive:@selector(startEngine:withAuthorizationOption:) withArguments:appKey, theValue(STMPushAuthorizationOptionFull)];
            
            RNSailthruMobileBridge *rnSailthruMobileBridge = [[RNSailthruMobileBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                                             appKey:appKey];
            (void)rnSailthruMobileBridge;
        });
        
        it(@"should set the JS code location", ^{
            RNSailthruMobileBridge *rnSailthruMobileBridge = [[RNSailthruMobileBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                                             appKey:appKey];
            [[rnSailthruMobileBridge.jsCodeLocation should] equal:jsCodeLocation];
        });
        
        it(@"should set whether to display in app notifications", ^{
            RNSailthruMobileBridge *rnSailthruMobileBridge = [[RNSailthruMobileBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                                             appKey:appKey];
            [[theValue(rnSailthruMobileBridge.displayInAppNotifications) should] equal:theValue(YES)];
        });
    });
    
    context(@"the initWithJSCodeLocation:appKey:pushAuthorizationOption:geoIpTrackingDefault: method", ^{
        __block NSURL *jsCodeLocation = nil;
        __block NSString *appKey = nil;
        
        beforeEach(^{
            jsCodeLocation = [NSURL mock];
            appKey = [NSString mock];
        });
        
        it(@"should set geo IP tracking default", ^{
            [[sailthruMobile should] receive:@selector(setGeoIPTrackingDefault:) withArguments:theValue(NO)];
            
            RNSailthruMobileBridge *rnSailthruMobileBridge = [[RNSailthruMobileBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                                             appKey:appKey
                                                                                            pushAuthorizationOption:STMPushAuthorizationOptionFull
                                                                                               geoIpTrackingDefault:NO];
            (void)rnSailthruMobileBridge;
        });
        
        it(@"should call startEngine", ^{
            [[sailthruMobile should] receive:@selector(startEngine:withAuthorizationOption:) withArguments:appKey, theValue(STMPushAuthorizationOptionProvisional)];
            
            RNSailthruMobileBridge *rnSailthruMobileBridge = [[RNSailthruMobileBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                                             appKey:appKey
                                                                                            pushAuthorizationOption:STMPushAuthorizationOptionProvisional
                                                                                               geoIpTrackingDefault:NO];
            (void)rnSailthruMobileBridge;
        });
        
        it(@"should set the JS code location", ^{
            RNSailthruMobileBridge *rnSailthruMobileBridge = [[RNSailthruMobileBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                                             appKey:appKey
                                                                                            pushAuthorizationOption:STMPushAuthorizationOptionFull
                                                                                               geoIpTrackingDefault:NO];
            [[rnSailthruMobileBridge.jsCodeLocation should] equal:jsCodeLocation];
        });
        
        it(@"should set whether to display in app notifications", ^{
            RNSailthruMobileBridge *rnSailthruMobileBridge = [[RNSailthruMobileBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                                             appKey:appKey
                                                                                            pushAuthorizationOption:STMPushAuthorizationOptionFull
                                                                                               geoIpTrackingDefault:NO];
            [[theValue(rnSailthruMobileBridge.displayInAppNotifications) should] equal:theValue(YES)];
        });
    });
    
    context(@"the sourceURLForBridge: method", ^{
        __block NSURL *jsCodeLocation = nil;
        __block NSString *appKey = nil;
        __block RNSailthruMobileBridge *rnSailthruMobileBridge = nil;
        
        beforeEach(^{
            [sailthruMobile stub:@selector(startEngine:withAuthorizationOption:)];
            
            jsCodeLocation = [NSURL mock];
            appKey = [NSString mock];
            
            rnSailthruMobileBridge = [[RNSailthruMobileBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                     appKey:appKey];
        });
        
        it(@"should return jsCodeLocation", ^{
            NSURL *codeLocation = [rnSailthruMobileBridge sourceURLForBridge:nil];
            [[codeLocation should] equal:jsCodeLocation];
        });
    });
    
    context(@"the extraModulesForBridge: method", ^{
        __block NSURL *jsCodeLocation = nil;
        __block NSString *appKey = nil;
        __block RNSailthruMobileBridge *rnSailthruMobileBridge = nil;
        
        beforeEach(^{
            [sailthruMobile stub:@selector(startEngine:withAuthorizationOption:)];
            [sailthruMobile stub:@selector(setWrapperName:andVersion:)];
            
            jsCodeLocation = [NSURL mock];
            appKey = [NSString mock];
            
            rnSailthruMobileBridge = [[RNSailthruMobileBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                     appKey:appKey];
        });
        
        it(@"should return RNSailthruMobile in array", ^{
            NSArray *modules = [rnSailthruMobileBridge extraModulesForBridge:nil];
            [[theValue(modules.count) should] equal:theValue(1)];
            id<RCTBridgeModule> module = [modules objectAtIndex:0];
            [[theValue([module isKindOfClass:[RNSailthruMobile class]]) should] beYes];
        });
    });
});

SPEC_END
