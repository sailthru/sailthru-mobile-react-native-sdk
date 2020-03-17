# React Native : Sailthru Mobile SDK

Wraps the native Sailthru Mobile SDK for React Native apps.

## Installation

`npm install react-native-sailthru-mobile --save`


### iOS

Open your Project's Xcode Project.

Drag into "Libraries" the following files from node_modules/react-native-sailthru-mobile:

 * RNSailthruMobile.h
 * RNSailthruMobile.m (Make sure this file's Target Membership is your main app's target)
 * RNSailthruMobileBridge.h
 * RNSailthruMobileBridge.m (Make sure this file's Target Membership is your main app's target)

Next, Install Sailthru Mobile iOS SDK from Cocoapods (add `pod 'SailthruMobile'` to your Podfile) or install the framework [manually](https://docs.mobile.sailthru.com/docs/ios-integration#section-manual-integration) (SailthruMobile.framework can be obtained from node_modules/react-native-sailthru-mobile).

You will then need replace the code that creates your RCTRootView with the code below. This adds the SailthruMobile React Native modules to the root view.

```Objective-C
#import "RNSailthruMobileBridge.h"

- (BOOL)application:(UIApplication * )application didFinishLaunchingWithOptions:(NSDictionary * )launchOptions {
      ...
      id<RCTBridgeDelegate> moduleInitialiser = [[RNSailthruMobileBridge alloc]
                                                 initWithJSCodeLocation:jsCodeLocation   // JS Code location used here should be same location used before
                                                 appKey:SDK_KEY];                        // Obtain SDK key from your Sailthru Mobile app settings

      RCTBridge * bridge = [[RCTBridge alloc] initWithDelegate:moduleInitialiser launchOptions:launchOptions];

      RCTRootView * rootView = [[RCTRootView alloc]
                                initWithBridge:bridge
                                moduleName:@"YOUR_MODULE_NAME"
                                initialProperties:nil];
      ...
}
```

Build and Run from Xcode.

## Android

* In `android/settings.gradle`

```gradle
...
include ':react-native-sailthru-mobile'
project(':react-native-sailthru-mobile').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-sailthru-mobile/android')
```

* In `android/app/build.gradle`

```gradle
...
repositories {
    google()

    maven {
        url "https://github.com/carnivalmobile/maven-repository/raw/master/"
    }
}

dependencies {
    ...
    compile project(':react-native-sailthru-mobile')
    compile 'com.sailthru.mobile.sdk:sailthru-mobile:11.+'
}
```


* Register module (in MainApplication.java)

```java
import com.reactlibrary.RNSailthruMobilePackage; // <--- import

public class MainApplication extends Application implements ReactApplication {
  ...

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new RNSailthruMobilePackage(getApplicationContext(), // Should pass in application context
                                      SDK_KEY)                 // Obtain SDK key from your Sailthru Mobile app settings
      );
    }
  ...

    @Override
    public void onCreate() {
      super.onCreate();
      SoLoader.init(this, /* native exopackage */ false);
    }
  ...
}

}
```

Finally, make sure your `compileSdkVersion` is set to 26 or higher and buildToolsVersion is "26.0.2" or higher


Note: You may see an error about missing bundle on Android if you don't have the server running first. You an start the server by running `react-native start` and relaunch from Android Studio.

For push set up, follow the usual [Android Integration](https://docs.mobile.sailthru.com/docs/android-integration) documentation.

## Example

We have provided an example JS file for both iOS and Android. Examples of the promises-based wrapper can be found there.
