# React Native : Sailthru Mobile SDK

Wraps the native Sailthru Mobile SDK for React Native apps.

## Installation

`npm install react-native-sailthru-mobile --save`

This project supports auto linking!
Running `pod install` in the `ios` folder should set up everything you need on the iOS side. On the Android side the only manual step required involves adding our maven URL to the repositories in the app level `build.gradle`:

```
maven {
    url "https://github.com/sailthru/maven-repository/raw/master/"
}
```

However, if you would like to manually integrate the SDK please follow the steps below.


### iOS

Open your Project's Xcode Project.

Drag into "Libraries" the following files from node_modules/react-native-sailthru-mobile:

 * RNMarigold.h
 * RNMarigold.m (Make sure this file's Target Membership is your main app's target)
 * RNMarigoldBridge.h
 * RNMarigoldBridge.m (Make sure this file's Target Membership is your main app's target)

Next, Install Marigold iOS SDK from Cocoapods (add `pod 'Marigold'` to your Podfile), Swift Package Manager, Carthage or install the XCFramework [manually](https://docs.mobile.sailthru.com/docs/ios-integration#section-manual-integration) (Marigold.xcframework can be obtained from node_modules/react-native-marigold).

You will then need replace the code that creates your RCTRootView with the code below. This adds the Marigold React Native modules to the root view.

```Objective-C
#import "RNMarigoldBridge.h"

- (BOOL)application:(UIApplication * )application didFinishLaunchingWithOptions:(NSDictionary * )launchOptions {
      ...
      
      [[Marigold new] startEngine:SDK_KEY withAuthorizationOption:STMPushAuthorizationOptionProvisional]; // Obtain SDK key from your Sailthru Mobile app settings
      id<RCTBridgeDelegate> moduleInitialiser = [[RNMarigoldBridge alloc]
                                                 initWithJSCodeLocation:jsCodeLocation]; // Obtain SDK key from your Sailthru Mobile app settings

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
        url "https://github.com/sailthru/maven-repository/raw/master/"
    }
}

dependencies {
    ...
    implementation project(':react-native-sailthru-mobile')
}
```


* Register module (in MainApplication.java)

```java
import com.sailthru.mobile.rnsdk.RNMarigoldPackage; // <--- import

public class MainApplication extends Application implements ReactApplication {
  ...

    @Override
    protected List<ReactPackage> getPackages() {
      List<ReactPackage> packages = new PackageList(this).getPackages();
      packages.add(new RNMarigoldPackage(getApplicationContext()));
      return packages;
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

## Development

You can setup the project locally for development and running the test suites.

### Prerequisites

You will need the following things installed on your system.

* [Git](https://git-scm.com/)
* [Node.js](https://nodejs.org/) (v18.8.0)
* [Yarn](https://yarnpkg.com/)
* [Android Studio](https://developer.android.com/studio)
* [Xcode](https://developer.apple.com/xcode/)

### Installation

Run `yarn` in the project root to install the required node dependencies.

Move into the `ios` folder and run `pod install --repo-update` to install the iOS dependencies, you can then open `MarigoldSDKReactNative.xcworkspace` in Xcode.

Open the `android` folder in Android Studio and perform a gradle sync to install the Android dependencies.

### Testing

Run `yarn test` in the project root to run the Jest test suite.

Run `Product` -> `Test` in the Xcode toolbar to run the iOS test suite.

Run the `RNMarigoldModuleTest` configuration in Android Studio to run the Android test suite.
