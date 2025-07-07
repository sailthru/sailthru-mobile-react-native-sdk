#import <XCTest/XCTest.h>
#import "RNMessageStream.h"
#import "Kiwi.h"
#import <UserNotifications/UserNotifications.h>
#import <OCMock/OCMock.h>


// interface to expose methods for testing
@interface RNMessageStream ()

-(void)getMessage:(NSString *)messageId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)getUnreadCount:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)markMessageAsRead:(NSDictionary*)jsDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)removeMessage:(NSDictionary *)jsDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)presentMessageDetail:(NSDictionary *)jsDict;
-(void)dismissMessageDetail;
-(void)registerMessageImpression:(NSInteger)impressionType forMessage:(NSDictionary *)jsDict;
-(void)clearMessages:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
-(void)useDefaultInAppNotification:(BOOL)useDefault;
-(void)notifyInAppHandled:(BOOL)handled;

@end

SPEC_BEGIN(RNMessageStreamSpec)

describe(@"RNMessageStream", ^{
    __block MARMessageStream *messageStream = nil;
    __block RNMessageStream *rnMessageStream = nil;

    beforeEach(^{
        messageStream = [MARMessageStream mock];
        [messageStream stub:@selector(setDelegate:)];
        [MARMessageStream stub:@selector(new) andReturn:messageStream];
        rnMessageStream = [[RNMessageStream alloc] init];
    });
    
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
    
    context(@"the getMessage method", ^{
        it(@"calls the native method", ^{
            [[messageStream should] receive:@selector(messageFor:withCompletion:)];
            [rnMessageStream getMessage:@"test" resolver:nil rejecter:nil];
        });
        
        it(@"returns the message array on success", ^{
            // Setup variables
            __block NSArray *check = nil;
            MARMessage *message = [MARMessage new];
            RCTPromiseResolveBlock resolve = ^(NSArray* count) {
                check = count;
            };
            KWCaptureSpy *messageIdCapture = [messageStream captureArgument:@selector(messageFor:withCompletion:) atIndex:0];
            KWCaptureSpy *completionCapture = [messageStream captureArgument:@selector(messageFor:withCompletion:) atIndex:1];
            
            // Start test
            [rnMessageStream getMessage:@"test" resolver:resolve rejecter:nil];
            
            // Capture arguments
            NSString *messageId = messageIdCapture.argument;
            [[messageId should] equal:@"test"];
            void (^completeBlock)(MARMessage * _Nullable, NSError * _Nullable) = completionCapture.argument;
            completeBlock(message, nil);
            
            // Verify result
            [[check shouldNot] beNil];
            [[check should] beKindOfClass:[NSDictionary class]];
        });
        
        it(@"returns the error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(messageFor:withCompletion:) atIndex:1];
            
            // Start test
            [rnMessageStream getMessage:@"test" resolver:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSUInteger, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the getMessages method", ^{
        it(@"calls the native method", ^{
            [[messageStream should] receive:@selector(messages:)];
            [rnMessageStream resolver:nil rejecter:nil];
        });
        
        it(@"returns the message array on success", ^{
            // Setup variables
            __block NSArray *check = nil;
            NSArray *inputArray = @[];
            RCTPromiseResolveBlock resolve = ^(NSArray* count) {
                check = count;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(messages:) atIndex:0];
            
            // Start test
            [rnMessageStream resolver:resolve rejecter:nil];
            
            // Capture argument
            void (^completeBlock)(NSArray * _Nullable, NSError * _Nullable) = capture.argument;
            completeBlock(inputArray, nil);
            
            // Verify result
            [[check shouldNot] beNil];
            [[check should] beKindOfClass:[NSArray class]];
        });
        
        it(@"returns the error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(messages:) atIndex:0];
            
            // Start test
            [rnMessageStream resolver:nil rejecter:reject];
            
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
            
            [rnMessageStream getUnreadCount:nil rejecter:nil];
        });
        
        it(@"returns the count on success", ^{
            // Setup variables
            __block NSNumber *check = nil;
            NSUInteger count = 5;
            RCTPromiseResolveBlock resolve = ^(NSNumber* count) {
                check = count;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(unreadCount:) atIndex:0];
            
            // Start test
            [rnMessageStream getUnreadCount:resolve rejecter:nil];
            
            // Capture argument
            void (^completeBlock)(NSUInteger, NSError * _Nullable) = capture.argument;
            completeBlock(count, nil);
            
            // Verify result
            [[check shouldNot] beNil];
            [[check should] equal:@(count)];
        });
        
        it(@"returns the error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(unreadCount:) atIndex:0];
            
            // Start test
            [rnMessageStream getUnreadCount:nil rejecter:reject];
            
            // Capture argument
            void (^completeBlock)(NSUInteger, NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(0, error);
            
            // Verify result
            [[check should] equal:error];
        });
    });
    
    context(@"the markMessageAsRead:resolver:rejecter: method", ^{
        it(@"calls the native method", ^{
            [[messageStream should] receive:@selector(markMessageAsRead:withResponse:)];
            
            [rnMessageStream markMessageAsRead:nil resolver:nil rejecter:nil];
        });
    });
    
    context(@"the removeMessage:resolver:rejecter: method", ^{
        it(@"calls the native method", ^{
            [[messageStream should] receive:@selector(removeMessage:withResponse:)];
            
            [rnMessageStream removeMessage:nil resolver:nil rejecter:nil];
        });
    });
    
    context(@"the presentMessageDetail: method", ^{
        it(@"calls the native method", ^{
            [[expectFutureValue(messageStream) shouldEventuallyBeforeTimingOutAfter(5)] receive:@selector(presentMessageDetailForMessage:)];
            
            [rnMessageStream presentMessageDetail:nil];
        });
    });
    
    context(@"the dismissMessageDetail method", ^{
        it(@"calls the native method", ^{
            [[expectFutureValue(messageStream) shouldEventuallyBeforeTimingOutAfter(5)] receive:@selector(dismissMessageDetail)];
            
            [rnMessageStream dismissMessageDetail];
        });
    });
    
    context(@"the registerMessageImpression:forMessage: method", ^{
        it(@"calls the native method", ^{
            [[messageStream should] receive:@selector(registerImpressionWithType:forMessage:)];
            
            [rnMessageStream registerMessageImpression:1 forMessage:nil];
        });
    });
    
    context(@"the clearMessages method", ^{
        it(@"calls the native method", ^{
            [[messageStream should] receive:@selector(clearMessagesWithResponse:)];
            
            [rnMessageStream clearMessages:nil rejecter:nil];
        });
        
        it(@"returns success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(clearMessagesWithResponse:) atIndex:0];
            
            // Start test
            [rnMessageStream clearMessages:resolve rejecter:nil];
            
            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            completeBlock(nil);
            
            // Verify result
            [[theValue(check) should] equal:theValue(YES)];
        });
        
        it(@"returns the error on failure", ^{
            // Setup variables
            __block NSError *check = nil;
            RCTPromiseRejectBlock reject = ^(NSString* e, NSString* f, NSError* error) {
                check = error;
            };
            KWCaptureSpy *capture = [messageStream captureArgument:@selector(clearMessagesWithResponse:) atIndex:0];
            
            // Start test
            [rnMessageStream clearMessages:nil rejecter:reject];
            
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
            [[mockRnMessageStream stub] emitInAppNotification:[OCMArg any]];
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

                [[mockRnMessageStream expect] emitInAppNotification:@{@"title": @"Testing", @"type": @"MARMessageTypeText", @"text": @"Test Body", @"attributes": @{@"attributeKey": @"attributeValue"}}];
                
                dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                    check = [rnMessageStream shouldPresentInAppNotificationForMessage:marMessage];
                });
                
                [mockRnMessageStream notifyInAppHandled:NO];
                [[expectFutureValue(theValue(check)) shouldEventually] beYes];
            });

            it(@"returns NO when defaultInAppNotification is NO and notifyInAppHandled is YES", ^{
                [mockRnMessageStream useDefaultInAppNotification:NO];

                [[mockRnMessageStream expect] emitInAppNotification:@{@"title": @"Testing", @"type": @"MARMessageTypeText", @"text": @"Test Body", @"attributes": @{@"attributeKey": @"attributeValue"}}];
                
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
