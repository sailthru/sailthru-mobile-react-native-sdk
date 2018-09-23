#import <XCTest/XCTest.h>
#import "RNCarnivalBridge.h"
#import "RNCarnival.h"
#import "Kiwi.h"

SPEC_BEGIN(RNCarnivalBridgeSpec)

describe(@"RNCarnival", ^{
    context(@"the init method", ^{
        it(@"should throw an exception", ^{
            BOOL exceptionThrown = NO;
            @try {
                RNCarnivalBridge *rnCarnivalBridge = [[RNCarnivalBridge alloc] init];
                (void)rnCarnivalBridge;
            }
            @catch(NSException *e) {
                exceptionThrown = YES;
            }
            [[theValue(exceptionThrown) should] beYes];
        });
    });
    
    context(@"the initWithDisplayInAppNotifications method", ^{
        __block NSURL *jsCodeLocation = nil;
        __block NSString *appKey = nil;
        BOOL registerForPushNotifications = YES;
        BOOL displayInAppNotifications = NO;
        
        beforeEach(^{
            [Carnival stub:@selector(startEngine:registerForPushNotifications:)];
            
            jsCodeLocation = [NSURL mock];
            appKey = [NSString mock];
        });
        
        it(@"should call startEngine", ^{
            [[Carnival should] receive:@selector(startEngine:registerForPushNotifications:) withArguments:appKey, theValue(registerForPushNotifications)];
            RNCarnivalBridge *rnCarnivalBridge = [[RNCarnivalBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                           appKey:appKey
                                                                     registerForPushNotifications:registerForPushNotifications
                                                                        displayInAppNotifications:displayInAppNotifications];
            (void)rnCarnivalBridge;
        });

        it(@"should set JS code location", ^{
            RNCarnivalBridge *rnCarnivalBridge = [[RNCarnivalBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                           appKey:appKey
                                                                     registerForPushNotifications:registerForPushNotifications
                                                                        displayInAppNotifications:displayInAppNotifications];
            [[rnCarnivalBridge.jsCodeLocation should] equal:jsCodeLocation];
        });

        it(@"should set the displayInAppNotifications", ^{
            RNCarnivalBridge *rnCarnivalBridge = [[RNCarnivalBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                                           appKey:appKey
                                                                     registerForPushNotifications:registerForPushNotifications
                                                                        displayInAppNotifications:displayInAppNotifications];
            [[theValue(rnCarnivalBridge.displayInAppNotifications) should] equal:theValue(displayInAppNotifications)];
        });
    });
    
    context(@"the sourceURLForBridge: method", ^{
        __block NSURL *jsCodeLocation = nil;
        __block NSString *appKey = nil;
        __block RNCarnivalBridge *rnCarnivalBridge = nil;
        BOOL registerForPushNotifications = YES;
        BOOL displayInAppNotifications = NO;
        
        beforeEach(^{
            [Carnival stub:@selector(startEngine:registerForPushNotifications:)];
            
            jsCodeLocation = [NSURL mock];
            appKey = [NSString mock];
            
            rnCarnivalBridge = [[RNCarnivalBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                         appKey:appKey
                                                   registerForPushNotifications:registerForPushNotifications
                                                      displayInAppNotifications:displayInAppNotifications];
        });
        
        it(@"should return jsCodeLocation", ^{
            NSURL *codeLocation = [rnCarnivalBridge sourceURLForBridge:nil];
            [[codeLocation should] equal:jsCodeLocation];
        });
    });
    
    context(@"the extraModulesForBridge: method", ^{
        __block NSURL *jsCodeLocation = nil;
        __block NSString *appKey = nil;
        __block RNCarnivalBridge *rnCarnivalBridge = nil;
        BOOL registerForPushNotifications = YES;
        BOOL displayInAppNotifications = NO;
        
        beforeEach(^{
            [Carnival stub:@selector(startEngine:registerForPushNotifications:)];
            
            jsCodeLocation = [NSURL mock];
            appKey = [NSString mock];
            
            rnCarnivalBridge = [[RNCarnivalBridge alloc] initWithJSCodeLocation:jsCodeLocation
                                                                         appKey:appKey
                                                   registerForPushNotifications:registerForPushNotifications
                                                      displayInAppNotifications:displayInAppNotifications];
        });
        
        it(@"should return RNCarnivalModule in array", ^{
            NSArray *modules = [rnCarnivalBridge extraModulesForBridge:nil];
            [[theValue(modules.count) should] equal:theValue(1)];
            id<RCTBridgeModule> module = [modules objectAtIndex:0];
            [[theValue([module isKindOfClass:[RNCarnival class]]) should] beYes];
        });
    });
});

SPEC_END
