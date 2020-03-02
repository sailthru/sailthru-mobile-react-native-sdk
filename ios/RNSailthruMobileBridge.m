
#import "RNSailthruMobileBridge.h"
#import "RNSailthruMobile.h"
#import <SailthruMobile/SailthruMobile.h>

@implementation RNSailthruMobileBridge

-(instancetype)init {
    [NSException raise:@"Unsupported Method" format:@"Default initializer should not be called"];
    return nil;
}

- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey {
    return [self initWithJSCodeLocation:jsCodeLocation appKey:appKey pushAuthorizationOption:STMPushAuthorizationOptionFull geoIpTrackingDefault:YES];
}

- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey
               pushAuthorizationOption:(STMPushAuthorizationOption)pushAuthorizationOption
                  geoIpTrackingDefault:(BOOL)geoIpTrackingDefault {
    self = [super init];
    if(self) {
        SailthruMobile *sailthruMobile = [SailthruMobile new];
        [sailthruMobile setGeoIPTrackingDefault:geoIpTrackingDefault];
        [sailthruMobile startEngine:appKey withAuthorizationOption:pushAuthorizationOption];
        _jsCodeLocation = jsCodeLocation;
        _displayInAppNotifications = YES;
    }
    return self;
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge {
    return self.jsCodeLocation;
}

- (NSArray<id<RCTBridgeModule>> *)extraModulesForBridge:(RCTBridge *)bridge {
    RNSailthruMobile *rnSailthruMobile = [[RNSailthruMobile alloc] initWithDisplayInAppNotifications:self.displayInAppNotifications];
    NSArray *modules = @[ rnSailthruMobile ];
    return modules;
}

@end
