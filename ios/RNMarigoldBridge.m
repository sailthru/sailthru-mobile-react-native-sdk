
#import "RNMarigoldBridge.h"
#import "RNMarigold.h"
#import <Marigold/Marigold.h>

@implementation RNMarigoldBridge

-(instancetype)init {
    [NSException raise:@"Unsupported Method" format:@"Default initializer should not be called"];
    return nil;
}

- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey {
    return [self initWithJSCodeLocation:jsCodeLocation appKey:appKey pushAuthorizationOption:MARPushAuthorizationOptionFull geoIpTrackingDefault:YES];
}

- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey
               pushAuthorizationOption:(MARPushAuthorizationOption)pushAuthorizationOption
                  geoIpTrackingDefault:(BOOL)geoIpTrackingDefault {
    self = [super init];
    if(self) {
        Marigold *marigold = [Marigold new];
        [marigold setGeoIPTrackingDefault:geoIpTrackingDefault];
        [marigold startEngine:appKey withAuthorizationOption:pushAuthorizationOption error:nil];
        _jsCodeLocation = jsCodeLocation;
        _displayInAppNotifications = YES;
    }
    return self;
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge {
    return self.jsCodeLocation;
}

- (NSArray<id<RCTBridgeModule>> *)extraModulesForBridge:(RCTBridge *)bridge {
    RNMarigold *rnMarigold = [[RNMarigold alloc] initWithDisplayInAppNotifications:self.displayInAppNotifications];
    NSArray *modules = @[ rnMarigold ];
    return modules;
}

@end
