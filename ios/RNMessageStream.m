
#import "RNMessageStream.h"
#import <UserNotifications/UserNotifications.h>

@interface MARMessage ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end

@interface RNMessageStream()

@property (nonatomic, strong) MARMessageStream *messageStream;

@end


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
        _eventSemaphore = dispatch_semaphore_create(0);

        [_messageStream setDelegate:self];
    }
    return self;
}

- (NSArray<NSString *> *)supportedEvents {
    return @[@"inappnotification"];
}

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
    __block BOOL inAppNotificationHandled = YES;

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSMutableDictionary *payload = [NSMutableDictionary dictionaryWithDictionary:[message dictionary]];

        if ([message attributes]) {
            [payload setObject:[message attributes] forKey:@"attributes"];
        }
        [self sendEventWithName:@"inappnotification" body:payload];
        
        @synchronized (self) {
            inAppNotificationHandled = NO;
        }
        dispatch_semaphore_signal(self.eventSemaphore);
    });

    dispatch_semaphore_wait(self.eventSemaphore, dispatch_time(DISPATCH_TIME_NOW, 5 * NSEC_PER_SEC));

    @synchronized (self) {
        return inAppNotificationHandled;
    }
}

RCT_EXPORT_METHOD(notifyInAppHandled:(BOOL)handled) {
    @synchronized (self) {
        self.inAppNotificationHandled = !handled;
        if (handled) {
            dispatch_semaphore_signal(self.eventSemaphore);
        }
    }
}

RCT_EXPORT_METHOD(useDefaultInAppNotification:(BOOL)useDefault) {
    self.defaultInAppNotification = useDefault;
}

#pragma mark - Messages
// Note: We use promises for our return values, not callbacks.

RCT_REMAP_METHOD(getMessages, resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.messageStream messages:^(NSArray * _Nullable messages, NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve([RNMessageStream arrayOfMessageDictionariesFromMessageArray:messages]);
        }
    }];
}

#pragma mark - Message Stream

RCT_EXPORT_METHOD(getUnreadCount:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.messageStream unreadCount:^(NSUInteger unreadCount, NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve(@(unreadCount));
        }
    }];
}


RCT_EXPORT_METHOD(markMessageAsRead:(NSDictionary*)jsDict resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.messageStream markMessageAsRead:[RNMessageStream messageFromDict:jsDict] withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(removeMessage:(NSDictionary *)jsDict resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.messageStream removeMessage:[RNMessageStream messageFromDict:jsDict] withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

RCT_EXPORT_METHOD(presentMessageDetail:(NSDictionary *)jsDict) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.messageStream presentMessageDetailForMessage:[RNMessageStream messageFromDict:jsDict]];
    });
}

RCT_EXPORT_METHOD(dismissMessageDetail) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.messageStream dismissMessageDetail];
    });
}

RCT_EXPORT_METHOD(registerMessageImpression:(NSInteger)impressionType forMessage:(NSDictionary *)jsDict) {
    [self.messageStream registerImpressionWithType:impressionType forMessage:[RNMessageStream messageFromDict:jsDict]];
}

RCT_EXPORT_METHOD(clearMessages:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self.messageStream clearMessagesWithResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNMessageStream rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
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

+ (MARMessage *) messageFromDict:(NSDictionary *)jsDict {
    return [[MARMessage alloc] initWithDictionary:jsDict];
}

@end
