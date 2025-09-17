#import <XCTest/XCTest.h>
#import "RNMessageStream.h"
#import "Kiwi.h"
#import <UserNotifications/UserNotifications.h>
#import <OCMock/OCMock.h>
#import <Marigold/Marigold.h>

#ifdef RCT_NEW_ARCH_ENABLED
using JS::NativeRNMessageStream::RNMessage;
#else
// interface to expose methods for testing
@interface RNMessageStream ()
-(void)getMessages:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)getUnreadCount:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)markMessageAsRead:(NSDictionary*)jsDict resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)removeMessage:(NSDictionary *)jsDict resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)presentMessageDetail:(NSDictionary *)jsDict;
-(void)dismissMessageDetail;
-(void)registerMessageImpression:(double)impressionType message:(NSDictionary *)jsDict;
-(void)clearMessages:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
-(void)useDefaultInAppNotification:(BOOL)useDefault;
-(void)notifyInAppHandled:(BOOL)handled;
@end
#endif


NSDictionary* createRNMessageContents() {
    return @{
        @"title": @"Test Title",
        @"text": @"Test Text",
        @"id": @"1234567890",
        @"type": @"text_message",
        @"is_read": @1,
    };
}

SPEC_BEGIN(RNMessageStreamSpec)

describe(@"RNMessageStream", ^{
    __block MARMessageStream *messageStream = nil;
    __block RNMessageStream *rnMessageStream = nil;
#ifdef RCT_NEW_ARCH_ENABLED
    RNMessage *rnMessage = new RNMessage(createRNMessageContents());
#else
    NSDictionary *rnMessage = createRNMessageContents();
#endif

    beforeEach(^{
        messageStream = [MARMessageStream mock];
        [messageStream stub:@selector(setDelegate:)];
        [MARMessageStream stub:@selector(new) andReturn:messageStream];
        rnMessageStream = [[RNMessageStream alloc] init];
    });

#ifdef RCT_NEW_ARCH_ENABLED
    afterAll(^{
        delete rnMessage;
    });
#endif
    
    context(@"the init method", ^{
        it(@"sets the displayInAppNotifications to YES", ^{
            RNMessageStream *rnMessageStream = [[RNMessageStream alloc] initWithDisplayInAppNotifications:YES];
            [[theValue(rnMessageStream.displayInAppNotifications) should] beYes];
        });
    });
    
    context(@"the initWithDisplayInAppNotifications method", ^{
        it(@"sets message stream delegate as self", ^{
            [[messageStream should] receive:@selector(setDelegate:)];
            RNMessageStream *rnMessageStream = [[RNMessageStream alloc] initWithDisplayInAppNotifications:YES];
            (void)rnMessageStream;
        });
        
        it(@"sets the displayInAppNotifications", ^{
            RNMessageStream *rnMessageStream = [[RNMessageStream alloc] initWithDisplayInAppNotifications:NO];
            [[theValue(rnMessageStream.displayInAppNotifications) should] beNo];
        });
    });
    
    context(@"the getMessages method", ^{
        it(@"calls the native method", ^{
            [[messageStream should] receive:@selector(messages:)];
            [rnMessageStream getMessages:nil reject:nil];
        });
        
        it(@"returns message array on success", ^{
            // Setup variables
            __block NSArray *check = nil;
            NSArray *inputArray = @[];
            RCTPromiseResolveBlock resolve = ^(NSArray* count) {
                check = count;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(messages:) atIndex:0];
            
            // Start test
            [rnMessageStream getMessages:resolve reject:nil];
            
            // Capture argument
            void (^completeBlock)(NSArray * _Nullable, NSError * _Nullable) = capture.argument;
            completeBlock(inputArray, nil);
            
            // Verify result
            [[check shouldNot] beNil];
            [[check should] beKindOfClass:[NSArray class]];
        });
        
        it(@"returns error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(messages:) atIndex:0];
            
            // Start test
            [rnMessageStream getMessages:nil reject:reject];
            
            // Capture argument
            void (^completeBlock)(NSUInteger, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the getUnreadCount method", ^{
        it(@"calls the native method", ^{
            [[messageStream should] receive:@selector(unreadCount:)];
            
            [rnMessageStream getUnreadCount:nil reject:nil];
        });
        
        it(@"returns count on success", ^{
            // Setup variables
            __block NSNumber *check = nil;
            NSUInteger count = 5;
            RCTPromiseResolveBlock resolve = ^(NSNumber* count) {
                check = count;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(unreadCount:) atIndex:0];
            
            // Start test
            [rnMessageStream getUnreadCount:resolve reject:nil];
            
            // Capture argument
            void (^completeBlock)(NSUInteger, NSError * _Nullable) = capture.argument;
            completeBlock(count, nil);
            
            // Verify result
            [[check shouldNot] beNil];
            [[check should] equal:@(count)];
        });
        
        it(@"returns error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(unreadCount:) atIndex:0];
            
            // Start test
            [rnMessageStream getUnreadCount:nil reject:reject];
            
            // Capture argument
            void (^completeBlock)(NSUInteger, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the markMessageAsRead:resolve:reject: method", ^{
        it(@"calls the native method", ^{
            [[messageStream should] receive:@selector(markMessageAsRead:withResponse:)];
#ifdef RCT_NEW_ARCH_ENABLED
            [rnMessageStream markMessageAsRead:*rnMessage resolve:nil reject:nil];
#else
            [rnMessageStream markMessageAsRead:rnMessage resolve:nil reject:nil];
#endif
        });
    });
    
    context(@"the removeMessage:resolve:reject: method", ^{
        it(@"calls the native method", ^{
            [[messageStream should] receive:@selector(removeMessage:withResponse:)];
#ifdef RCT_NEW_ARCH_ENABLED
            [rnMessageStream removeMessage:*rnMessage resolve:nil reject:nil];
#else
            [rnMessageStream removeMessage:rnMessage resolve:nil reject:nil];
#endif
        });
    });
    
    context(@"the presentMessageDetail: method", ^{
        it(@"calls the native method", ^{
            [[expectFutureValue(messageStream) shouldEventuallyBeforeTimingOutAfter(5)] receive:@selector(presentMessageDetailForMessage:)];
#ifdef RCT_NEW_ARCH_ENABLED
            [rnMessageStream presentMessageDetail:*rnMessage];
#else
            [rnMessageStream presentMessageDetail:rnMessage];
#endif
        });
    });
    
    context(@"the dismissMessageDetail method", ^{
        it(@"calls the native method", ^{
            [[expectFutureValue(messageStream) shouldEventuallyBeforeTimingOutAfter(5)] receive:@selector(dismissMessageDetail)];
            
            [rnMessageStream dismissMessageDetail];
        });
    });
    
    context(@"the registerMessageImpression:message: method", ^{
        it(@"calls the native method", ^{
            [[messageStream should] receive:@selector(registerImpressionWithType:forMessage:)];
#ifdef RCT_NEW_ARCH_ENABLED
            [rnMessageStream registerMessageImpression:1 message:*rnMessage];
#else
            [rnMessageStream registerMessageImpression:1 message:rnMessage];
#endif
        });
    });
    
    context(@"the clearMessages method", ^{
        it(@"calls the native method", ^{
            [[messageStream should] receive:@selector(clearMessagesWithResponse:)];
            
            [rnMessageStream clearMessages:nil reject:nil];
        });
        
        it(@"returns success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(clearMessagesWithResponse:) atIndex:0];
            
            // Start test
            [rnMessageStream clearMessages:resolve reject:nil];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            completeBlock(nil);
            
            // Verify result
            [[theValue(check) should] equal:theValue(YES)];
        });
        
        it(@"returns error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(clearMessagesWithResponse:) atIndex:0];
            
            // Start test
            [rnMessageStream clearMessages:nil reject:reject];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    context(@"the useDefaultInAppNotification method", ^{
        it(@"sets the default value as YES", ^{
            [rnMessageStream useDefaultInAppNotification:YES];
            
            [[theValue(rnMessageStream.defaultInAppNotification) should] equal:theValue(YES)];
        });
        it(@"sets the default value as NO", ^{
            [rnMessageStream useDefaultInAppNotification:NO];
            
            [[theValue(rnMessageStream.defaultInAppNotification) should] equal:theValue(NO)];
        });
    });
    context(@"the shouldPresentInAppNotificationForMessage method", ^{
        __block MARMessage *marMessage;
        __block BOOL check;
        __block id mockRnMessageStream;

        beforeEach(^{
            rnMessageStream = [RNMessageStream new];
            mockRnMessageStream = OCMPartialMock(rnMessageStream);
#ifdef RCT_NEW_ARCH_ENABLED
            [[mockRnMessageStream stub] emitOnInAppNotification:[OCMArg any]];
#else
            [[mockRnMessageStream stub] emitInAppNotification:[OCMArg any]];
#endif
            marMessage = [[MARMessage alloc] init];
            marMessage.title = @"Testing";
            marMessage.type = MARMessageTypeText;
            marMessage.text = @"Test Body";
            marMessage.attributes = @{@"attributeKey": @"attributeValue"};
        });

        context(@"the shouldPresentInAppNotificationForMessage method", ^{
            it(@"returns YES when defaultInAppNotification is YES", ^{
                [mockRnMessageStream useDefaultInAppNotification:YES];
                
                BOOL check = [rnMessageStream shouldPresentInAppNotificationForMessage:marMessage];

                [[theValue(check) should] equal:theValue(YES)];
            });

            it(@"returns YES when defaultInAppNotification is NO and notifyInAppHandled is NO", ^{
                [mockRnMessageStream useDefaultInAppNotification:NO];

#ifdef RCT_NEW_ARCH_ENABLED
                [[mockRnMessageStream expect] emitOnInAppNotification:@{@"title": @"Testing", @"type": @"MARMessageTypeText", @"text": @"Test Body", @"attributes": @{@"attributeKey": @"attributeValue"}}];
#else
                [[mockRnMessageStream expect] emitInAppNotification:@{@"title": @"Testing", @"type": @"MARMessageTypeText", @"text": @"Test Body", @"attributes": @{@"attributeKey": @"attributeValue"}}];
#endif
                
                dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                    check = [rnMessageStream shouldPresentInAppNotificationForMessage:marMessage];
                });
                
                [mockRnMessageStream notifyInAppHandled:NO];
                [[expectFutureValue(theValue(check)) shouldEventually] beYes];
            });

            it(@"returns NO when defaultInAppNotification is NO and notifyInAppHandled is YES", ^{
                [mockRnMessageStream useDefaultInAppNotification:NO];

#ifdef RCT_NEW_ARCH_ENABLED
                [[mockRnMessageStream expect] emitOnInAppNotification:@{@"title": @"Testing", @"type": @"MARMessageTypeText", @"text": @"Test Body", @"attributes": @{@"attributeKey": @"attributeValue"}}];
#else
                [[mockRnMessageStream expect] emitInAppNotification:@{@"title": @"Testing", @"type": @"MARMessageTypeText", @"text": @"Test Body", @"attributes": @{@"attributeKey": @"attributeValue"}}];
#endif
                
                dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                    check = [rnMessageStream shouldPresentInAppNotificationForMessage:marMessage];
                });
                
                [mockRnMessageStream notifyInAppHandled:YES];
                [[expectFutureValue(theValue(check)) shouldEventually] beNo];
            });
        });
    });
});

SPEC_END
