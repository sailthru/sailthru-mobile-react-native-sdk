//
//  CarnivalSDKReactNativeTests.m
//  CarnivalSDKReactNativeTests
//
//  Created by Ian Stewart on 5/09/18.
//  Copyright © 2018 Carnival. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "RNCarnival.h"
#import "Kiwi.h"

// interface for adding methods to test
@interface RNCarnival ()

-(instancetype)init;
-(void)startEngine:(NSString *)key registerForPushNotifications:(BOOL)registerForPushNotifications __attribute__((deprecated));

@end


// interfaces to match RNCarnival

@interface CarnivalMessage ()

- (nullable instancetype)initWithDictionary:(nonnull NSDictionary *)dictionary;
- (nonnull NSDictionary *)dictionary;

@end
@interface Carnival ()

+ (void)setWrapperName:(NSString *)wrapperName andVersion:(NSString *)wrapperVersion;

@end



SPEC_BEGIN(RNCarnivalSpec)

describe(@"RNCarnival", ^{
    context(@"the init method", ^{
        beforeEach(^{
            [Carnival stub:@selector(setWrapperName:andVersion:)];
        });
        
        it(@"should message stream delegate as self", ^{
            [[CarnivalMessageStream should] receive:@selector(setDelegate:)];
            [[RNCarnival alloc] init];
        });
        
        it(@"should set the wrapper name and version", ^{
            [[Carnival should] receive:@selector(setWrapperName:andVersion:)];
            [[RNCarnival alloc] init];
        });
        
        it(@"should set the wrapper name and version", ^{
            [[Carnival should] receive:@selector(setWrapperName:andVersion:)];
            RNCarnival *rnCarnival = [[RNCarnival alloc] init];
            [rnCarnival startEngine:@"" registerForPushNotifications:NO];
        });
    });
});

SPEC_END
