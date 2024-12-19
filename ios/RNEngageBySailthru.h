#ifdef RCT_NEW_ARCH_ENABLED
#import <ReactNativeMarigoldSpec/ReactNativeMarigoldSpec.h>
@interface RNEngageBySailthru : NSObject <NativeRNEngageBySailthruSpec>
#else
#import <React/RCTBridgeModule.h>
@interface RNEngageBySailthru : NSObject <RCTBridgeModule>
#endif
@end
