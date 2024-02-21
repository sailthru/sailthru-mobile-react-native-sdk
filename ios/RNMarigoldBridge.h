
#import <Foundation/Foundation.h>
#import <React/RCTBridgeDelegate.h>

#include <Marigold/Marigold.h>

@interface RNMarigoldBridge : NSObject <RCTBridgeDelegate>

@property (strong, nonatomic) NSURL *jsCodeLocation;

/// Set to false to turn off in app notifications
@property BOOL displayInAppNotifications;

/**
 * Initialise the RNMarigoldBridge.
 *
 * @param jsCodeLocation               the location to load JS code from.
 * @param appKey                       the app key provided when you registered your application.
 * @return RNMarigoldBridge instance
 */
- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey;

/**
 * Initialise the RNMarigoldBridge.
 *
 * @param jsCodeLocation               the location to load JS code from.
 * @param appKey                       the app key provided when you registered your application.
 * @param pushAuthorizationOption  push authorization option to request.
 * @param geoIpTrackingDefault         boolean to set whether the geo IP tracking should be enabled by default.
 * @return RNMarigoldBridge instance
 */
- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey
               pushAuthorizationOption:(MARPushAuthorizationOption)pushAuthorizationOption
                  geoIpTrackingDefault:(BOOL)geoIpTrackingDefault;

@end
