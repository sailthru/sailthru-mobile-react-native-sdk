
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#include <Carnival/Carnival.h>

@interface RNCarnival : RCTEventEmitter <RCTBridgeModule, CarnivalMessageStreamDelegate>
    - (NSArray<NSString *> *)supportedEvents;
@end
  
