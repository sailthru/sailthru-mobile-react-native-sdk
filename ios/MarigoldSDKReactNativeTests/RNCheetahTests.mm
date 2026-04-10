#import <XCTest/XCTest.h>
#import "RNCheetah.h"
#import "Kiwi.h"
#import <Marigold/Marigold.h>

#ifndef RCT_NEW_ARCH_ENABLED
// interface to expose methods for testing
@interface RNCheetah ()
-(void)logRegistrationEvent:(NSString *)userId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
@end
#endif

SPEC_BEGIN(RNCheetahSpec)

describe(@"RNCheetah", ^{
    __block MARCheetah *cheetah = nil;
    __block RNCheetah *rnCheetah = nil;
    NSString *testID = @"user ID";

    beforeEach(^{
        cheetah = [MARCheetah mock];
        [cheetah stub:@selector(initWithError:) andReturn:cheetah];
        [MARCheetah stub:@selector(alloc) andReturn:cheetah];
        rnCheetah = [[RNCheetah alloc] init];
    });
    
    context(@"the logRegistrationEvent:resolve:reject: method", ^{
        it(@"calls the native method", ^{
            KWCaptureSpy *capture = [cheetah captureArgument:@selector(logRegistrationEvent:withResponse:) atIndex:0];

            [rnCheetah logRegistrationEvent:testID resolve:nil reject:nil];
            
            NSString *userId = capture.argument;
            [[userId should] equal:testID];
        });
        
        context(@"with nil user ID", ^{
            it(@"calls the native method", ^{
                KWCaptureSpy *capture = [cheetah captureArgument:@selector(logRegistrationEvent:withResponse:) atIndex:0];

                [rnCheetah logRegistrationEvent:nil resolve:nil reject:nil];
                
                NSString *userId = capture.argument;
                [[userId should] beNil];
            });
        });

        it(@"returns success", ^{
            // Setup variables
            __block BOOL check = NO;
            RCTPromiseResolveBlock resolve = ^(NSObject *ignored) {
                check = YES;
            };
            KWCaptureSpy *capture = [cheetah captureArgument:@selector(logRegistrationEvent:withResponse:) atIndex:1];

            // Start test
            [rnCheetah logRegistrationEvent:testID resolve:resolve reject:nil];

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
            KWCaptureSpy *capture = [cheetah captureArgument:@selector(logRegistrationEvent:withResponse:) atIndex:1];

            // Start test
            [rnCheetah logRegistrationEvent:testID resolve:nil reject:reject];

            // Capture argument
            void (^completeBlock)(NSError * _Nullable) = capture.argument;
            NSError *error = [NSError errorWithDomain:@"test" code:1 userInfo:nil];
            completeBlock(error);

            // Verify result
            [[check should] equal:error];
        });
    });
});

SPEC_END

