#ifdef RCT_NEW_ARCH_ENABLED
#import <ReactNativeMarigoldSpec/ReactNativeMarigoldSpec.h>
@interface RNCheetah : NSObject <NativeRNCheetahSpec>
#else
#import <React/RCTBridgeModule.h>
@interface RNCheetah : NSObject <RCTBridgeModule>
#endif
@end
