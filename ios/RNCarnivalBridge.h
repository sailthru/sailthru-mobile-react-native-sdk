
#import <Foundation/Foundation.h>
#import <React/RCTBridgeDelegate.h>

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
 * @param registerForPushNotifications boolean to set whether the SDK should automatically register for push notifications.
 * @param geoIpTrackingDefault         boolean to set whether the geo IP tracking should be enabled by default.
 * @return RNCarnivalBridge instance
 */
- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey
          registerForPushNotifications:(BOOL)registerForPushNotifications
                  geoIpTrackingDefault:(BOOL)geoIpTrackingDefault;

/**
 * Initialise the RNCarnivalBridge.
 *
 * @deprecated This method has been deprecated. Please use initWithJSCodeLocation:appKey: or initWithJSCodeLocation:appKey:registerForPushNotifications:geoIpTrackingDefault: and set the displayInAppNotifications property directly if you wish to override the default value.
 * @param jsCodeLocation               the location to load JS code from.
 * @param appKey                       the app key provided when you registered your application.
 * @param registerForPushNotifications boolean to set whether the SDK should automatically register for push notifications.
 * @param displayInAppNotifications    boolean to set whether the SDK should automatically display in app notifications.
 * @return RNCarnivalBridge instance
 */
- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey
          registerForPushNotifications:(BOOL)registerForPushNotifications
             displayInAppNotifications:(BOOL)displayInAppNotifications __attribute__((deprecated("Use - initWithJSCodeLocation:appKey: or initWithJSCodeLocation:appKey:registerForPushNotifications:geoIpTrackingDefault:")));

@end
