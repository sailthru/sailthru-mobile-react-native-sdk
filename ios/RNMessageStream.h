
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#include <Marigold/Marigold.h>

@interface RNMessageStream : RCTEventEmitter <RCTBridgeModule, MARMessageStreamDelegate>

@property BOOL displayInAppNotifications;

/**
 * Initialize RNMarigold and set whether to automatically display in app notifications.\
 *
 * @param displayInAppNotifications set whether the SDK should automatically display in app notifications.
 * @return RNMarigold instance.
 */
-(instancetype)initWithDisplayInAppNotifications:(BOOL)displayInAppNotifications;


/**
 * Return array of supported RN events.
 *
 * @return array containing supported events strings.
 */
- (NSArray<NSString *> *)supportedEvents;
@end
