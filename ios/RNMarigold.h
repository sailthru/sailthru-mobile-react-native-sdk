#ifdef RCT_NEW_ARCH_ENABLED
#import <ReactNativeMarigoldSpec/ReactNativeMarigoldSpec.h>
@interface RNMarigold : NSObject <NativeRNMarigoldSpec>
#else
#import <React/RCTBridgeModule.h>
@interface RNMarigold : NSObject <RCTBridgeModule>
#endif
@end
