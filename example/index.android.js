/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */
'use strict';
import React, {
  AppRegistry,
  Component,
  StyleSheet,
  Text,
  View
} from 'react-native';

var { NativeModules } = require('react-native');
module.exports = NativeModules.CarnivalReactNativePlugin;

var APP_KEY = "<Your Carnival App Key>"
var Carnival = NativeModules.CarnivalReactNativePlugin

class AwesomeProject extends Component {
  constructor(props) {
    super(props);
    Carnival.startEngine(APP_KEY);

    Carnival.getMessages()
      .then(messages => {
        if (messages.length > 2) {
          Carnival.markMessageAsRead(messages[0]);
        }
        Carnival.showMessageDetail(messages[0]);

        Carnival.registerMessageImpression(messages[1], Carnival.IMPRESSION_TYPE_IN_APP_VIEW);
      })
      .catch(e => {
        console.log(e);
      });

    Carnival.setString("string_key", "This is the string value").catch( e => {
      console.log(e);
    });

    Carnival.setStrings("strings_key", ["This is first value", "This is the second value"]).catch( e => {
      console.log(e);
    });

    Carnival.setDate("date_key", new Date().getTime()).catch( e => {
      console.log(e);
    });

    Carnival.setDates("dates_key", [new Date().getTime(), new Date().getTime(), new Date().getTime()]).catch( e => {
      console.log(e);
    });

    Carnival.setFloat("float_key", 3.141).catch( e => {
      console.log(e);
    });

    Carnival.setFloats("floats_key", [1.1, 2.2, 3.3, 4.4]).catch( e => {
      console.log(e);
    });

    Carnival.setInteger("integer_key", 3).catch( e => {
      console.log(e);
    });

    Carnival.setIntegers("integers_key", [1, 2, 3, 4]).catch( e => {
      console.log(e);
    });

    Carnival.setBool("boolean_key", true).catch( e => {
      console.log(e);
    });

    Carnival.updateLocation(-41.292178, 174.777535); //Carnival Wellington.

    Carnival.logEvent("This is my event");

    Carnival.getUnreadCount().then(function(count) {
      console.log(count);
    }, function(e){
      console.log(e);
    });

    Carnival.getDeviceId().then(function(id) {
      console.log(id);
    }, function(e){
      console.log(e);
    });

    Carnival.setUserId("person@domain.com");

    //Carnival.setInAppNotificationsEnabled(true);

    //Carnival.setGeoIPTrackingEnabled(true);

    //Carnival.setCrashHandlersEnabled(true);

    //Carnival.registerForPushNotifications();
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Carnival React Native Test App
        </Text>
        <Text style={styles.instructions}>
          To get started, edit index.android.js
        </Text>
        <Text style={styles.instructions}>
          Shake or press menu button for dev menu
        </Text>
        <Text style={styles.instructions}>
          Shake or press menu button for dev menu
        </Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('AwesomeProject', () => AwesomeProject);
