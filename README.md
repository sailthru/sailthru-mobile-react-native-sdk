# React Native : Marigold SDK

Wraps the native Marigold SDK for React Native apps.

> **Requires React Native New Architecture.** This SDK uses TurboModules and does not support the legacy architecture. Ensure New Architecture is enabled in your app before installing (React Native 0.76+).

## Installation

`npm install react-native-marigold --save`

Running `pod install` in the `ios` folder should set up everything you need on the iOS side. On the Android side the only manual step required involves adding our maven URL to the repositories in the app level `build.gradle`:

```
maven {
    url "https://github.com/sailthru/maven-repository/raw/master/"
}
```

You'll then need to call `startEngine` in the native SDKs when the application is created:


### iOS
Objective-C
```objc
#import <Marigold/Marigold.h>

- (BOOL)application:(UIApplication * )application didFinishLaunchingWithOptions:(NSDictionary * )launchOptions {
      ...
      [[Marigold new] startEngine:SDK_KEY withAuthorizationOption:STMPushAuthorizationOptionProvisional]; // Obtain SDK key from your Marigold app settings
      ...
}
```
Swift
```swift
import Marigold

func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    ...
    Marigold().startEngine(SDK_KEY, withAuthorizationOption: .provisional) // Obtain SDK key from your Marigold app settings
    ...
}
```

### Android
Java
```java
import com.marigold.sdk.Marigold;

public class MainApplication extends Application implements ReactApplication {

    @Override
    public void onCreate() {
      super.onCreate();
      ...
      new Marigold().startEngine(this, SDK_KEY); // Obtain SDK key from your Marigold app settings
      ...
    }

}
```
Kotlin
```kotlin
import com.marigold.sdk.Marigold

class MainApplication : Application(), ReactApplication {

  override fun onCreate() {
    super.onCreate()
    ...
     Marigold().startEngine(this, SDK_KEY) // Obtain SDK key from your Marigold app settings
    ...
  }

}
```

## Example

We have provided an example JS file for both iOS and Android. Examples of the promises-based wrapper can be found there.

## Development

You can setup the project locally for development and running the test suites.

### Prerequisites

You will need the following things installed on your system.

* [Git](https://git-scm.com/)
* [Node.js](https://nodejs.org/) (v24.12.0)
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
