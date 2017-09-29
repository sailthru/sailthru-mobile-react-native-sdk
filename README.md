# React Native : Carnival SDK

Wraps the native Carnival SDK for React Native apps. 

## Installation

### iOS

Open your Project's Xcode Project. 

Drag into "Libraries" the following files from node_modules/react-native-carnival:

 * RNCarnival.h
 * RNCarnival.m (Make sure this file's Target Membership is your main app's target)
 * Carnival.framework

Next, Install Carnival iOS SDK from Cocoapods (add `pod 'Carnival'` to your Podfile) or install the framework [manually](http://docs.carnival.io/docs/ios-integration#section-manual-integration).

Build and Run from Xcode.

## Android

* In `android/setting.gradle`

```gradle
...
include ':react-native-carnival'
project(':react-native-carnival').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-carnival/android')
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
    compile project(':react-native-carnival')
    compile 'com.carnival.sdk:carnival:4.+'
}
```


* register module (in MainApplication.java)

```java
import com.reactlibrary.reactnative.RNCarnivalPackage;  // <--- import

public class MainApplication extends Application implements ReactApplication {
  ...

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new RNCarnivalPackage()
      );
    }
  ...

}
```

For push set up, follow the usual [Android Integration](https://docs.carnival.io/docs/android-integration) documentation.

## Example

We have provided an example JS file for both iOS and Android. Examples of the promised-based wrapper can be found there.


