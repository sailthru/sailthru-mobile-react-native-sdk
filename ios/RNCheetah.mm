
#import "RNCheetah.h"
#import <Marigold/Marigold.h>

@implementation RNCheetah

// Automatically export module as RNCheetah
RCT_EXPORT_MODULE();

- (MARCheetah *)cheetahWithRejecter:(RCTPromiseRejectBlock)reject {
    NSError *error;
    MARCheetah *cheetah = [[MARCheetah alloc] initWithError:&error];
    if (error) {
        [RNCheetah rejectPromise:reject withError:error];
        return nil;
    }
    return cheetah;
}

RCT_EXPORT_METHOD(logRegistrationEvent:(NSString * _Nullable)userId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[self cheetahWithRejecter:reject] logRegistrationEvent:userId withResponse:^(NSError * _Nullable error) {
        if (error) {
            [RNCheetah rejectPromise:reject withError:error];
        } else {
            resolve(nil);
        }
    }];
}

#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeRNCheetahSpecJSI>(params);
}
#endif

#pragma mark - Helper Functions

+ (void)rejectPromise:(RCTPromiseRejectBlock)reject withError:(NSError *)error {
    reject([NSString stringWithFormat:@"%ld", error.code], error.localizedDescription, error);
}

@end
