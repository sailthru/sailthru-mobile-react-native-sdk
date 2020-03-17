
#import "RNCarnivalBridge.h"
#import "RNCarnival.h"

@implementation RNCarnivalBridge

-(instancetype)init {
    [NSException raise:@"Unsupported Method" format:@"Default initializer should not be called"];
    return nil;
}

- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey {
    return [self initWithJSCodeLocation:jsCodeLocation appKey:appKey pushAuthorizationOption:CarnivalPushAuthorizationOptionFull geoIpTrackingDefault:YES];
}

- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey
               pushAuthorizationOption:(CarnivalPushAuthorizationOption)pushAuthorizationOption
                  geoIpTrackingDefault:(BOOL)geoIpTrackingDefault {
    self = [super init];
    if(self) {
        [Carnival setGeoIPTrackingDefault:geoIpTrackingDefault];
        [Carnival startEngine:appKey withAuthorizationOption:pushAuthorizationOption];
        _jsCodeLocation = jsCodeLocation;
        _displayInAppNotifications = YES;
    }
    return self;
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge {
    return self.jsCodeLocation;
}

- (NSArray<id<RCTBridgeModule>> *)extraModulesForBridge:(RCTBridge *)bridge {
    RNCarnival *rnCarnival = [[RNCarnival alloc] initWithDisplayInAppNotifications:self.displayInAppNotifications];
    NSArray *modules = @[ rnCarnival ];
    return modules;
}

@end
