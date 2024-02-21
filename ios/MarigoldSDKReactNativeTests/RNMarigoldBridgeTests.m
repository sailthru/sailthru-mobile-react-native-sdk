#import <XCTest/XCTest.h>
#import "RNMarigoldBridge.h"
#import "RNMarigold.h"
#import "Kiwi.h"

@interface Marigold ()

- (void)setWrapperName:(NSString *)wrapperName andVersion:(NSString *)wrapperVersion;

@end

SPEC_BEGIN(RNMarigoldBridgeSpec)

describe(@"RNMarigoldBridge", ^{
    __block Marigold *marigold = nil;
    
    beforeEach(^{
        marigold = [Marigold mock];
        [marigold stub:@selector(setGeoIPTrackingDefault:)];
        [marigold stub:@selector(startEngine:withAuthorizationOption:error:)];
        [Marigold stub:@selector(new) andReturn:marigold];
    });
    
    context(@"the init method", ^{
        it(@"should throw an exception", ^{
            BOOL exceptionThrown = NO;
            @try {
                RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] init];
                (void)rnMarigoldBridge;
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
            [[marigold should] receive:@selector(setGeoIPTrackingDefault:) withArguments:theValue(YES)];
            
            RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation appKey:appKey];
            (void)rnMarigoldBridge;
        });
        
        it(@"should call startEngine", ^{
            [[marigold should] receive:@selector(startEngine:withAuthorizationOption:error:) withArguments:appKey, theValue(MARPushAuthorizationOptionFull), nil];
            
            RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation appKey:appKey];
            (void)rnMarigoldBridge;
        });
        
        it(@"should set the JS code location", ^{
            RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation appKey:appKey];
            [[rnMarigoldBridge.jsCodeLocation should] equal:jsCodeLocation];
        });
        
        it(@"should set whether to display in app notifications", ^{
            RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation appKey:appKey];
            [[theValue(rnMarigoldBridge.displayInAppNotifications) should] equal:theValue(YES)];
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
            [[marigold should] receive:@selector(setGeoIPTrackingDefault:) withArguments:theValue(NO)];
            
            RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                           appKey:appKey
                                                                          pushAuthorizationOption:MARPushAuthorizationOptionFull
                                                                             geoIpTrackingDefault:NO];
            (void)rnMarigoldBridge;
        });
        
        it(@"should call startEngine", ^{
            [[marigold should] receive:@selector(startEngine:withAuthorizationOption:error:) withArguments:appKey, theValue(MARPushAuthorizationOptionProvisional), nil];
            
            RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                           appKey:appKey
                                                                          pushAuthorizationOption:MARPushAuthorizationOptionProvisional
                                                                             geoIpTrackingDefault:NO];
            (void)rnMarigoldBridge;
        });
        
        it(@"should set the JS code location", ^{
            RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                           appKey:appKey
                                                                          pushAuthorizationOption:MARPushAuthorizationOptionFull
                                                                             geoIpTrackingDefault:NO];
            [[rnMarigoldBridge.jsCodeLocation should] equal:jsCodeLocation];
        });
        
        it(@"should set whether to display in app notifications", ^{
            RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                           appKey:appKey
                                                                          pushAuthorizationOption:MARPushAuthorizationOptionFull
                                                                             geoIpTrackingDefault:NO];
            [[theValue(rnMarigoldBridge.displayInAppNotifications) should] equal:theValue(YES)];
        });
    });
    
    context(@"the sourceURLForBridge: method", ^{
        __block NSURL *jsCodeLocation = nil;
        __block NSString *appKey = nil;
        __block RNMarigoldBridge *rnMarigoldBridge = nil;
        
        beforeEach(^{
            [marigold stub:@selector(startEngine:withAuthorizationOption:error:)];
            
            jsCodeLocation = [NSURL mock];
            appKey = [NSString mock];
            
            rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                         appKey:appKey];
        });
        
        it(@"should return jsCodeLocation", ^{
            NSURL *codeLocation = [rnMarigoldBridge sourceURLForBridge:nil];
            [[codeLocation should] equal:jsCodeLocation];
        });
    });
    
    context(@"the extraModulesForBridge: method", ^{
        __block NSURL *jsCodeLocation = nil;
        __block NSString *appKey = nil;
        __block RNMarigoldBridge *rnMarigoldBridge = nil;
        
        beforeEach(^{
            [marigold stub:@selector(startEngine:withAuthorizationOption:error:)];
            [marigold stub:@selector(setWrapperName:andVersion:)];
            
            jsCodeLocation = [NSURL mock];
            appKey = [NSString mock];
            
            rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                         appKey:appKey];
        });
        
        it(@"should return RNMarigold in array", ^{
            NSArray *modules = [rnMarigoldBridge extraModulesForBridge:nil];
            [[theValue(modules.count) should] equal:theValue(1)];
            id<RCTBridgeModule> module = [modules objectAtIndex:0];
            [[theValue([module isKindOfClass:[RNMarigold class]]) should] beYes];
        });
    });
});

SPEC_END
