
#import "RNCarnivalBridge.h"
#import "RNCarnival.h"
#import <Carnival/Carnival.h>

@implementation RNCarnivalBridge

-(instancetype)init {
    [NSException raise:@"Unsupported Method" format:@"Default initializer should not be called"];
    return nil;
}

- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey
          registerForPushNotifications:(BOOL)registerForPushNotifications
             displayInAppNotifications:(BOOL)displayNotifications {
    self = [super init];
    if(self) {
        [Carnival startEngine:appKey registerForPushNotifications:registerForPushNotifications];
        self.jsCodeLocation = jsCodeLocation;
        self.displayInAppNotifications = displayNotifications;
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
