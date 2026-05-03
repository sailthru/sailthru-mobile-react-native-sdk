#import <React/RCTEventEmitter.h>
#import <Marigold/Marigold.h>
#import <ReactNativeMarigoldSpec/ReactNativeMarigoldSpec.h>
@interface RNMessageStream : NativeRNMessageStreamSpecBase <NativeRNMessageStreamSpec, MARMessageStreamDelegate>

@property (nonatomic, strong) dispatch_semaphore_t eventSemaphore;
@property (nonatomic, assign) BOOL defaultInAppNotification;
@property (nonatomic, assign) BOOL inAppNotificationHandled;
- (void)emitInAppNotification:(NSDictionary *)payload;

@end
