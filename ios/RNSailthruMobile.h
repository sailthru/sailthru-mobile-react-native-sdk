
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#include <SailthruMobile/SailthruMobile.h>

@interface RNSailthruMobile : RCTEventEmitter <RCTBridgeModule, SMSMessageStreamDelegate>

@property BOOL displayInAppNotifications;

/**
 * Initialize RNSailthruMobile and set whether to automatically display in app notifications.\
 *
 * @param displayInAppNotifications set whether the SDK should automatically display in app notifications.
 * @return RNSailthruMobile instance.
 */
-(instancetype)initWithDisplayInAppNotifications:(BOOL)displayInAppNotifications;

/**
 * Return array of supported RN events.
 *
 * @return array containing supported events strings.
 */
- (NSArray<NSString *> *)supportedEvents;

@end
