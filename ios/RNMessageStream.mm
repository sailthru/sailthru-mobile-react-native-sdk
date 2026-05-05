
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

using JS::NativeRNMessageStream::RNMessage;

@implementation RNMessageStream

RCT_EXPORT_MODULE();

- (instancetype)init {
    self = [super init];
    if(self) {
        _messageStream = [MARMessageStream new];
        _defaultInAppNotification = YES;
        _notificationTimeoutSeconds = 2.0;
        self.eventSemaphore = dispatch_semaphore_create(0);
        
        [_messageStream setDelegate:self];
    }
    return self;
}

- (BOOL)shouldPresentInAppNotificationForMessage:(MARMessage *)message {
    if (self.defaultInAppNotification) {
        return YES;
    }
    return [self emitWithTimeout:message];
}

- (BOOL)emitWithTimeout:(MARMessage *)message {
    @synchronized (self) {
        self.inAppNotificationHandled = NO;
    }

    // Drain any stale signals from previous or duplicate JS notifyInAppHandled: calls
    while (dispatch_semaphore_wait(self.eventSemaphore, DISPATCH_TIME_NOW) == 0) {}

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSMutableDictionary *payload = [NSMutableDictionary dictionaryWithDictionary:[message dictionary]];

        if ([message attributes]) {
            [payload setObject:[message attributes] forKey:@"attributes"];
        }
        [self emitInAppNotification:payload];
    });

    dispatch_semaphore_wait(self.eventSemaphore, dispatch_time(DISPATCH_TIME_NOW, (int64_t)(self.notificationTimeoutSeconds * NSEC_PER_SEC)));

    @synchronized (self) {
        return !self.inAppNotificationHandled;
    }
}

- (void)emitInAppNotification:(NSDictionary *)payload {
    [self emitOnInAppNotification: payload];
}

RCT_EXPORT_METHOD(notifyInAppHandled:(BOOL)handled) {
    @synchronized (self) {
        self.inAppNotificationHandled = handled;
        dispatch_semaphore_signal(self.eventSemaphore);
    }
}

RCT_EXPORT_METHOD(useDefaultInAppNotification:(BOOL)useDefault) {
    self.defaultInAppNotification = useDefault;
}

#pragma mark - Messages
// Note: We use promises for our return values, not callbacks.

RCT_EXPORT_METHOD(getMessage:(NSString *)messageId resolve:(RCTPromiseResolveBlock)resolve
                 reject:(RCTPromiseRejectBlock)reject) {
    [self.messageStream messageFor:messageId withCompletion:^(MARMessage * _Nullable message, NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve([message dictionary]);
        }
    }];
}

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

RCT_EXPORT_METHOD(markMessageAsRead:(RNMessage &)message resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    MARMessage *marMessage = [RNMessageStream messageFromRNMessage:message];
    [self.messageStream markMessageAsRead:marMessage withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(removeMessage:(RNMessage &)message resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    MARMessage *marMessage = [RNMessageStream messageFromRNMessage:message];
    [self.messageStream removeMessage:marMessage withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(presentMessageDetail:(RNMessage &)message) {
    MARMessage *marMessage = [RNMessageStream messageFromRNMessage:message];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.messageStream presentMessageDetailForMessage:marMessage];
    });
}

RCT_EXPORT_METHOD(dismissMessageDetail) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.messageStream dismissMessageDetail];
    });
}

RCT_EXPORT_METHOD(registerMessageImpression:(double)impressionType message:(RNMessage &)message) {
    MARMessage *marMessage = [RNMessageStream messageFromRNMessage:message];
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

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeRNMessageStreamSpecJSI>(params);
}

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
        messageDict[@"type"] = message.type();
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

@end
