
#import "RNMessageStream.h"
#import <UserNotifications/UserNotifications.h>

@interface RNMessageStream () <MARMessageStreamDelegate>

@end

@interface MARMessage ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end

@interface RNMessageStream()

@property (nonatomic, strong) MARMessageStream *messageStream;

@end

#ifdef RCT_NEW_ARCH_ENABLED
using JS::NativeRNMessageStream::RNMessage;
#endif

@implementation RNMessageStream

RCT_EXPORT_MODULE();

- (instancetype)init {
    return [self initWithDisplayInAppNotifications:YES];
}

- (instancetype)initWithDisplayInAppNotifications:(BOOL)displayNotifications {
    self = [super init];
    if(self) {
        _displayInAppNotifications = displayNotifications;
        _messageStream = [MARMessageStream new];
        _defaultInAppNotification = YES;
        self.eventSemaphore = dispatch_semaphore_create(0);
        
        [_messageStream setDelegate:self];
    }
    return self;
}

#ifndef RCT_NEW_ARCH_ENABLED
- (NSArray<NSString *> *)supportedEvents {
    return @[@"inappnotification"];
}
#endif

- (BOOL)shouldPresentInAppNotificationForMessage:(MARMessage *)message {
    if (self.defaultInAppNotification) {
        return YES;
    }
    __block BOOL result = YES;

    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        result = [self emitWithTimeout:message];

        dispatch_semaphore_signal(semaphore);
    });

    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);

    return result;
}

- (BOOL)emitWithTimeout:(MARMessage *)message {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSMutableDictionary *payload = [NSMutableDictionary dictionaryWithDictionary:[message dictionary]];

        if ([message attributes]) {
            [payload setObject:[message attributes] forKey:@"attributes"];
        }
        [self emitInAppNotification:payload];
        
        @synchronized (self) {
            self.inAppNotificationHandled = NO;
        }
    });

    dispatch_semaphore_wait(self.eventSemaphore, dispatch_time(DISPATCH_TIME_NOW, 5 * NSEC_PER_SEC));

    @synchronized (self) {
        return self.inAppNotificationHandled;
    }
}

- (void)emitInAppNotification:(NSDictionary *)payload {
#ifdef RCT_NEW_ARCH_ENABLED
    [self emitOnInAppNotification: payload];
#else
    [self sendEventWithName:@"inappnotification" body:payload];
#endif
}

RCT_EXPORT_METHOD(notifyInAppHandled:(BOOL)handled) {
    @synchronized (self) {
        self.inAppNotificationHandled = !handled;
        dispatch_semaphore_signal(self.eventSemaphore);
    }
}

RCT_EXPORT_METHOD(useDefaultInAppNotification:(BOOL)useDefault) {
    self.defaultInAppNotification = useDefault;
}

#pragma mark - Messages
// Note: We use promises for our return values, not callbacks.

RCT_EXPORT_METHOD(getMessages:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [self.messageStream messages:^(NSArray * _Nullable messages, NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve([RNMessageStream arrayOfMessageDictionariesFromMessageArray:messages]);
        }
    }];
}

#pragma mark - Message Stream

RCT_EXPORT_METHOD(getUnreadCount:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [self.messageStream unreadCount:^(NSUInteger unreadCount, NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve(@(unreadCount));
        }
    }];
}

#ifdef RCT_NEW_ARCH_ENABLED
RCT_EXPORT_METHOD(markMessageAsRead:(RNMessage &)message resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    MARMessage *marMessage = [RNMessageStream messageFromRNMessage:message];
#else
    RCT_EXPORT_METHOD(markMessageAsRead:(NSDictionary*)jsDict resolve:(RCTPromiseResolveBlock)resolve
                      reject:(RCTPromiseRejectBlock)reject) {
    MARMessage *marMessage = [RNMessageStream messageFromDict:jsDict];
#endif
    [self.messageStream markMessageAsRead:marMessage withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#ifdef RCT_NEW_ARCH_ENABLED
RCT_EXPORT_METHOD(removeMessage:(RNMessage &)message resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    MARMessage *marMessage = [RNMessageStream messageFromRNMessage:message];
#else
RCT_EXPORT_METHOD(removeMessage:(NSDictionary*)jsDict resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    MARMessage *marMessage = [RNMessageStream messageFromDict:jsDict];
#endif
    [self.messageStream removeMessage:marMessage withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#ifdef RCT_NEW_ARCH_ENABLED
RCT_EXPORT_METHOD(presentMessageDetail:(RNMessage &)message) {
    MARMessage *marMessage = [RNMessageStream messageFromRNMessage:message];
#else
RCT_EXPORT_METHOD(presentMessageDetail:(NSDictionary*)jsDict) {
    MARMessage *marMessage = [RNMessageStream messageFromDict:jsDict];
#endif
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.messageStream presentMessageDetailForMessage:marMessage];
    });
}

RCT_EXPORT_METHOD(dismissMessageDetail) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.messageStream dismissMessageDetail];
    });
}

#ifdef RCT_NEW_ARCH_ENABLED
RCT_EXPORT_METHOD(registerMessageImpression:(double)impressionType message:(RNMessage &)message) {
    MARMessage *marMessage = [RNMessageStream messageFromRNMessage:message];
#else
RCT_EXPORT_METHOD(registerMessageImpression:(double)impressionType message:(NSDictionary*)jsDict) {
    MARMessage *marMessage = [RNMessageStream messageFromDict:jsDict];
#endif
    [self.messageStream registerImpressionWithType:(MARImpressionType)impressionType forMessage:marMessage];
}

RCT_EXPORT_METHOD(clearMessages:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [self.messageStream clearMessagesWithResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeRNMessageStreamSpecJSI>(params);
}
#endif

#pragma mark - Helper Functions

+ (void)rejectPromise:(RCTPromiseRejectBlock)reject withError:(NSError *)error {
    reject([NSString stringWithFormat:@"%ld", error.code], error.localizedDescription, error);
}

+ (NSArray *)arrayOfMessageDictionariesFromMessageArray:(NSArray *)messageArray {
    NSMutableArray *messageDictionaries = [NSMutableArray array];
    for (MARMessage *message in messageArray) {
        [messageDictionaries addObject:[message dictionary]];
    }
    return messageDictionaries;
}

#ifdef RCT_NEW_ARCH_ENABLED
+ (MARMessage *) messageFromRNMessage:(RNMessage &)message {
    NSMutableDictionary *messageDict = [[NSMutableDictionary alloc] initWithDictionary:@{
        @"id": message.id_(),
        @"is_read": @(message.is_read()),
    }];
    if (message.title() != nil) {
        messageDict[@"title"] = message.title();
    }
    if (message.text() != nil) {
        messageDict[@"text"] = message.text();
    }
    if (message.type() != nil) {
        messageDict[@"html_text"] = message.type();
    }
    if (message.html_text() != nil) {
        messageDict[@"html_text"] = message.html_text();
    }
    if (message.custom() != nil) {
        messageDict[@"custom"] = message.custom();
    }
    if (message.url() != nil) {
        messageDict[@"url"] = message.url();
    }
    if (message.card_image_url() != nil) {
        messageDict[@"card_image_url"] = message.card_image_url();
    }
    if (message.card_media_url() != nil) {
        messageDict[@"card_media_url"] = message.card_media_url();
    }
    if (message.created_at() != nil) {
        messageDict[@"created_at"] = message.created_at();
    }
    return [[MARMessage alloc] initWithDictionary:messageDict];
}
#else
+ (MARMessage *) messageFromDict:(NSDictionary *)jsDict {
    return [[MARMessage alloc] initWithDictionary:jsDict];
}
#endif

@end
