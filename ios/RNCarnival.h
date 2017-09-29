
#if __has_include("RCTBridgeModule.h")
    #import "RCTBridgeModule.h"
    #import "RCTEventEmitter.h"
#else
    #import <React/RCTBridgeModule.h>
    #import <React/RCTEventEmitter.h>
#endif

#include <Carnival/Carnival.h>

@interface RNCarnival : RCTEventEmitter <RCTBridgeModule, CarnivalMessageStreamDelegate>
    - (NSArray<NSString *> *)supportedEvents;
@end
  
