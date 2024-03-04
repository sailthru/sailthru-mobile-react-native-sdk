
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
 * @return RNMarigoldBridge instance
 */
- (instancetype)initWithJSCodeLocation:(NSURL *)jsCodeLocation;

@end
