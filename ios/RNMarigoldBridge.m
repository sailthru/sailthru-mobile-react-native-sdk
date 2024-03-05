
#import "RNMarigoldBridge.h"
#import "RNMarigold.h"
#import "RNEngageBySailthru.h"
#import "RNMessageStream.h"
#import <Marigold/Marigold.h>
#import <Marigold/EngageBySailthru.h>

@implementation RNMarigoldBridge

-(instancetype)init {
    [NSException raise:@"Unsupported Method" format:@"Default initializer should not be called"];
    return nil;
}

- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation {
    self = [super init];
    if(self) {
        _jsCodeLocation = jsCodeLocation;
        _displayInAppNotifications = YES;
    }
    return self;
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge {
    return self.jsCodeLocation;
}

- (NSArray<id<RCTBridgeModule>> *)extraModulesForBridge:(RCTBridge *)bridge {
    RNMarigold *rnMarigold = [[RNMarigold alloc] init];
    RNEngageBySailthru *rnEngageBySailthru = [[RNEngageBySailthru alloc] init];
    RNMessageStream *rnMessageStream = [[RNMessageStream alloc] initWithDisplayInAppNotifications:self.displayInAppNotifications];
    NSArray *modules = @[ rnMarigold, rnEngageBySailthru, rnMessageStream ];
    return modules;
}

@end
