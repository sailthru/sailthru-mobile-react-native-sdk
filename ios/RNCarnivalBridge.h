
#import <Foundation/Foundation.h>
#import <React/RCTBridgeDelegate.h>

#include <SailthruMobile/SailthruMobile.h>

@interface RNCarnivalBridge : NSObject <RCTBridgeDelegate>

@property (strong, nonatomic) NSURL *jsCodeLocation;

/// Set to false to turn off in app notifications
@property BOOL displayInAppNotifications;

/**
 * Initialise the RNCarnivalBridge.
 *
 * @param jsCodeLocation               the location to load JS code from.
 * @param appKey                       the app key provided when you registered your application.
 * @return RNCarnivalBridge instance
 */
- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey;

/**
 * Initialise the RNCarnivalBridge.
 *
 * @param jsCodeLocation               the location to load JS code from.
 * @param appKey                       the app key provided when you registered your application.
 * @param pushAuthorizationOption  push authorization option to request.
 * @param geoIpTrackingDefault         boolean to set whether the geo IP tracking should be enabled by default.
 * @return RNCarnivalBridge instance
 */
- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey
               pushAuthorizationOption:(CarnivalPushAuthorizationOption)pushAuthorizationOption
                  geoIpTrackingDefault:(BOOL)geoIpTrackingDefault;

@end
