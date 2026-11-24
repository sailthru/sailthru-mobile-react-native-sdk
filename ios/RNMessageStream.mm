
#import "RNMessageStream.h"
#import <React/RCTInvalidating.h>
#import <UserNotifications/UserNotifications.h>

@interface RNMessageStream () <MARMessageStreamDelegate, RCTInvalidating>

@end

@interface MARMessage ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end

@interface RNMessageStream()

@property (nonatomic, strong) MARMessageStream *messageStream;
@property (nonatomic, assign) BOOL presentationInvalidated;
@property (atomic, assign) BOOL notificationPreparationEnabled;
@property (atomic, assign) BOOL detailPreparationEnabled;
@property (nonatomic, strong) NSMutableDictionary<NSString *, NSDictionary *> *pendingPresentations;

@end

using JS::NativeRNMessageStream::RNMessage;

@implementation RNMessageStream

RCT_EXPORT_MODULE();

- (instancetype)init {
    self = [super init];
    if(self) {
        _messageStream = [MARMessageStream new];
        _pendingPresentations = [NSMutableDictionary dictionary];
        _defaultInAppNotification = YES;
        _notificationTimeoutSeconds = 2.0;
        self.eventSemaphore = dispatch_semaphore_create(0);
        
        [_messageStream setDelegate:self];
    }
    return self;
}

- (BOOL)respondsToSelector:(SEL)selector {
    if (selector == @selector(prepareInAppNotificationForMessage:requestID:completion:)) return self.notificationPreparationEnabled;
    if (selector == @selector(prepareMessageDetailForMessage:requestID:completion:)) return self.detailPreparationEnabled;
    return [super respondsToSelector:selector];
}

RCT_EXPORT_METHOD(setPresentationCallbacksEnabled:(BOOL)notification detail:(BOOL)detail) {
    @synchronized (self) {
        if (self.presentationInvalidated) return;
        BOOL cancelNotification = self.notificationPreparationEnabled && !notification;
        BOOL cancelDetail = self.detailPreparationEnabled && !detail;
        self.notificationPreparationEnabled = notification;
        self.detailPreparationEnabled = detail;
        if (cancelNotification) [self cancelPendingPresentationRequestsOfKind:@"notification"];
        if (cancelDetail) [self cancelPendingPresentationRequestsOfKind:@"detail"];
    }
}

- (void)invalidate {
    @synchronized (self) {
        self.presentationInvalidated = YES;
        self.notificationPreparationEnabled = NO;
        self.detailPreparationEnabled = NO;
        [self.pendingPresentations removeAllObjects];
    }
    [self.messageStream removeDelegateIfCurrent:self];
}

- (void)cancelPendingPresentationRequestsOfKind:(NSString *)kind {
    NSMutableArray *completions = [NSMutableArray array];
    @synchronized (self) {
        for (NSString *requestID in self.pendingPresentations.allKeys) {
            NSDictionary *pending = self.pendingPresentations[requestID];
            if ([pending[@"kind"] isEqualToString:kind]) {
                [completions addObject:pending[@"completion"]];
                [self.pendingPresentations removeObjectForKey:requestID];
            }
        }
    }
    for (void (^completion)(MARInAppPresentationDecision *) in completions) completion([MARInAppPresentationDecision suppress]);
}

- (void)prepareInAppNotificationForMessage:(MARMessage *)message requestID:(NSString *)requestID
                                completion:(void (^)(MARInAppPresentationDecision *))completion {
    [self emitPresentationRequest:message requestID:requestID kind:@"notification" completion:completion];
}

- (void)prepareMessageDetailForMessage:(MARMessage *)message requestID:(NSString *)requestID
                            completion:(void (^)(MARInAppPresentationDecision *))completion {
    [self emitPresentationRequest:message requestID:requestID kind:@"detail" completion:completion];
}

- (void)emitPresentationRequest:(MARMessage *)message requestID:(NSString *)requestID kind:(NSString *)kind
                     completion:(void (^)(MARInAppPresentationDecision *))completion {
    @synchronized (self) {
        BOOL enabled = [kind isEqualToString:@"notification"] ? self.notificationPreparationEnabled : self.detailPreparationEnabled;
        if (!enabled) { completion([MARInAppPresentationDecision suppress]); return; }
        self.pendingPresentations[requestID] = @{@"kind": kind, @"completion": [completion copy]};
    }
    __weak RNMessageStream *weakSelf = self;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        RNMessageStream *stream = weakSelf;
        @synchronized (stream) { [stream.pendingPresentations removeObjectForKey:requestID]; }
    });
    NSDictionary *event = @{@"requestId": requestID, @"kind": kind, @"message": [message dictionary]};
    @synchronized (self) {
        if (self.pendingPresentations[requestID]) [self emitOnPresentationRequest:event];
    }
}

RCT_EXPORT_METHOD(completePresentation:(NSString *)requestID action:(NSString *)action configuration:(NSDictionary *)configuration) {
    void (^completion)(MARInAppPresentationDecision *);
    @synchronized (self) {
        completion = self.pendingPresentations[requestID][@"completion"];
        [self.pendingPresentations removeObjectForKey:requestID];
    }
    if (!completion) return;
    MARInAppPresentationDecision *decision;
    if ([action isEqualToString:@"suppress"]) decision = [MARInAppPresentationDecision suppress];
    else if ([action isEqualToString:@"handled"]) decision = [MARInAppPresentationDecision handled];
    else if ([action isEqualToString:@"display"] && [configuration isKindOfClass:[NSDictionary class]]) {
        decision = [MARInAppPresentationDecision displayWithConfiguration:[MARInAppPresentationConfiguration configurationWithDictionary:configuration]];
    } else decision = [MARInAppPresentationDecision display];
    completion(decision);
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
    if (message._st_inapp_presentation() != nil) {
        messageDict[@"_st_inapp_presentation"] = message._st_inapp_presentation();
    }
    return [[MARMessage alloc] initWithDictionary:messageDict];
}

@end
