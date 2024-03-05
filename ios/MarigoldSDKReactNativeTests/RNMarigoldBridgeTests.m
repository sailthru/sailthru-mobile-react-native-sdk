#import <XCTest/XCTest.h>
#import "RNMarigoldBridge.h"
#import "RNMarigold.h"
#import "RNEngageBySailthru.h"
#import "RNMessageStream.h"
#import "Kiwi.h"

@interface Marigold ()

- (void)setWrapperName:(NSString *)wrapperName andVersion:(NSString *)wrapperVersion;

@end

SPEC_BEGIN(RNMarigoldBridgeSpec)

describe(@"RNMarigoldBridge", ^{
    __block Marigold *marigold = nil;
    
    beforeEach(^{
        marigold = [Marigold mock];
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
    
    context(@"the initWithJSCodeLocation: method", ^{
        __block NSURL *jsCodeLocation = nil;
        
        beforeEach(^{
            jsCodeLocation = [NSURL mock];
        });
        
        it(@"should set the JS code location", ^{
            RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation];
            [[rnMarigoldBridge.jsCodeLocation should] equal:jsCodeLocation];
        });
        
        it(@"should set whether to display in app notifications", ^{
            RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation];
            [[theValue(rnMarigoldBridge.displayInAppNotifications) should] equal:theValue(YES)];
        });
    });
    
    context(@"the initWithJSCodeLocation:pushAuthorizationOption:geoIpTrackingDefault: method", ^{
        __block NSURL *jsCodeLocation = nil;
        
        beforeEach(^{
            jsCodeLocation = [NSURL mock];
        });
        
        it(@"should set the JS code location", ^{
            RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation];
            [[rnMarigoldBridge.jsCodeLocation should] equal:jsCodeLocation];
        });
        
        it(@"should set whether to display in app notifications", ^{
            RNMarigoldBridge *rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation];
            [[theValue(rnMarigoldBridge.displayInAppNotifications) should] equal:theValue(YES)];
        });
    });
    
    context(@"the sourceURLForBridge: method", ^{
        __block NSURL *jsCodeLocation = nil;
        __block RNMarigoldBridge *rnMarigoldBridge = nil;
        
        beforeEach(^{
            jsCodeLocation = [NSURL mock];
            
            rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation];
        });
        
        it(@"should return jsCodeLocation", ^{
            NSURL *codeLocation = [rnMarigoldBridge sourceURLForBridge:nil];
            [[codeLocation should] equal:jsCodeLocation];
        });
    });
    
    context(@"the extraModulesForBridge: method", ^{
        __block NSURL *jsCodeLocation = nil;
        __block RNMarigoldBridge *rnMarigoldBridge = nil;
        
        beforeEach(^{
            [marigold stub:@selector(setWrapperName:andVersion:)];
            
            jsCodeLocation = [NSURL mock];
            
            rnMarigoldBridge = [[RNMarigoldBridge alloc] initWithJSCodeLocation:jsCodeLocation];
        });
        
        it(@"should return RNMarigold in array", ^{
            NSArray *modules = [rnMarigoldBridge extraModulesForBridge:nil];
            [[theValue(modules.count) should] equal:theValue(3)];
            id<RCTBridgeModule> module = [modules objectAtIndex:0];
            [[theValue([module isKindOfClass:[RNMarigold class]]) should] beYes];
        });
        
        it(@"should return RNEngageBySailthru in array", ^{
            NSArray *modules = [rnMarigoldBridge extraModulesForBridge:nil];
            [[theValue(modules.count) should] equal:theValue(3)];
            id<RCTBridgeModule> module = [modules objectAtIndex:1];
            [[theValue([module isKindOfClass:[RNEngageBySailthru class]]) should] beYes];
        });
        
        it(@"should return RNMessageStream in array", ^{
            NSArray *modules = [rnMarigoldBridge extraModulesForBridge:nil];
            [[theValue(modules.count) should] equal:theValue(3)];
            id<RCTBridgeModule> module = [modules objectAtIndex:2];
            [[theValue([module isKindOfClass:[RNMessageStream class]]) should] beYes];
        });
    });
});

SPEC_END
