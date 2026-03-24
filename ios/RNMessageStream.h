#import <React/RCTEventEmitter.h>
#import <Marigold/Marigold.h>
#import <UserNotifications/UserNotifications.h>

#ifdef RCT_NEW_ARCH_ENABLED
#import <ReactNativeMarigoldSpec/ReactNativeMarigoldSpec.h>
@interface RNMessageStream : NativeRNMessageStreamSpecBase <NativeRNMessageStreamSpec, MARMessageStreamDelegate>
#else
#import <React/RCTBridgeModule.h>
@interface RNMessageStream : RCTEventEmitter <RCTBridgeModule, MARMessageStreamDelegate>

/**
 * Return array of supported RN events.
 *
 * @return array containing supported events strings.
 */
- (NSArray<NSString *> *)supportedEvents;
#endif

@property BOOL displayInAppNotifications;
@property (nonatomic, strong) dispatch_semaphore_t eventSemaphore;
@property (nonatomic, assign) BOOL defaultInAppNotification;
@property (nonatomic, assign) BOOL inAppNotificationHandled;
@property (nonatomic, strong) dispatch_semaphore_t fullScreenSemaphore;
@property (nonatomic, assign) BOOL fullScreenMessageHandled;

- (void)emitInAppNotification:(NSDictionary *)payload;
- (void)emitFullScreenMessage:(NSDictionary *)payload;

/**
 * Initialize RNMarigold and set whether to automatically display in app notifications.
 *
 * @param displayInAppNotifications set whether the SDK should automatically display in app notifications.
 * @return RNMarigold instance.
 */
- (instancetype)initWithDisplayInAppNotifications:(BOOL)displayInAppNotifications;

+ (nullable instancetype)sharedInstance;
+ (void)setSharedInstance:(nullable RNMessageStream *)instance;

- (void)handleFullScreenMessage:(MARMessage *)message fallback:(dispatch_block_t)fallback;
- (void)handleFullScreenMessageWithUserInfo:(NSDictionary *)userInfo fallback:(dispatch_block_t)fallback;

@end