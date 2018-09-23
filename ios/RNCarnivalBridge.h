
#import <Foundation/Foundation.h>
#import <React/RCTBridgeDelegate.h>

@interface RNCarnivalBridge : NSObject <RCTBridgeDelegate>

@property (strong, nonatomic) NSURL *jsCodeLocation;
@property BOOL displayInAppNotifications;

/**
 * Initialise the RNCarnivalBridge.
 *
 * @param jsCodeLocation               the location to load JS code from.
 * @param appKey                       the app key provided when you registered your application.
 * @param registerForPushNotifications boolean to set whether the SDK should automatically register for push notifications.
 * @param displayInAppNotifications    boolean to set whether the SDK should automatically display in app notifications.
 * @return RNCarnivalBridge instance
 */
- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation
                                appKey:(NSString *)appKey
          registerForPushNotifications:(BOOL)registerForPushNotifications
             displayInAppNotifications:(BOOL)displayInAppNotifications;

@end
