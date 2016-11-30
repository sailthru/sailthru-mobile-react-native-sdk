# React Native : Carnival SDK

Wraps the native Carnival SDK for React Native apps. 

## Installation

### iOS

Open your Project's Xcode Project. 

Drag into "Libraries" the following files from node_modules/react-native-carnival:

 * CarnivalReactNativePlugin.h
 * CarnivalReactNativePlugin.m

Next, Install Carnival iOS SDK from Cocoapods (add `pod 'Carnival'` to your Podfile) or install the framework [manually](http://docs.carnival.io/docs/ios-integration#section-manual-integration).

Build and Run from Xcode.

## Android

* In `android/setting.gradle`

```gradle
...
include ':CarnivalReactNativePlugin', ':app'
project(':CarnivalReactNativePlugin').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-carnival/android')
```

* In `android/app/build.gradle`

```gradle
...
repositories {
    maven {
        url "https://github.com/carnivalmobile/maven-repository/raw/master/"
    }
}

dependencies {
    ...
    compile project(':CarnivalReactNativePlugin')
}
```

* register module (in MainApplication.java)

```java
import com.carnivalmobile.reactnative.CarnivalReactPackage;  // <--- import

public class MainApplication extends Application implements ReactApplication {
  ...

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new CarnivalReactPackage()
      );
    }
  ...

}
```

## Example

We have provided an example JS file for both iOS and Android. Examples of the promised-based wrapper can be found there.


