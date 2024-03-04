
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#include <Marigold/Marigold.h>

@interface RNEngageBySailthru : RCTEventEmitter <RCTBridgeModule>

/**
 * Return array of supported RN events.
 *
 * @return array containing supported events strings.
 */
- (NSArray<NSString *> *)supportedEvents;
@end
