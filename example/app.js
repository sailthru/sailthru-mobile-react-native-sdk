/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  Platform,
  StyleSheet,
  Text,
  View
} from 'react-native';

var Carnival = require('react-native-carnival');
var SDK_KEY = ''; // Put your SDK key in here.
import { NativeEventEmitter } from 'react-native'

const instructions = Platform.select({
  ios: 'Press Cmd+R to reload,\n' +
    'Cmd+D or shake for dev menu',
  android: 'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});


export default class App extends Component<{}> {
  render() {
    Carnival.setDisplayInAppNotifications(true);
    Carnival.startEngine(SDK_KEY, true);
    Carnival.getMessages()
      .then(messages => {
        if (messages.length > 2) {
          Carnival.markMessageAsRead(messages[0]);
          Carnival.presentMessageDetail(messages[0].id);
          Carnival.registerMessageImpression(Carnival.MessageImpressionType.InAppView, messages[1]);
        }
      })
      .catch(e => {
        console.log(e);
    });

    var attrMap = new Carnival.AttributeMap();
    attrMap.setString("string_key", "This is the string value");
    attrMap.setStringArray("strings_key", ["This is first value", "This is the second value"]);
    attrMap.setDate("date_key", new Date());
    attrMap.setDateArray("dates_key", [new Date(), new Date(), new Date()]);
    attrMap.setFloat("float_key", 3.141);
    attrMap.setFloatArray("floats_key", [1.1, 2.2, 3.3, 4.4]);
    attrMap.setInteger("integer_key", 3);
    attrMap.setIntegerArray("integers_key", [1, 2, 3, 4]);
    attrMap.setBoolean("boolean_key", true);

    Carnival.setAttributes(attrMap).catch(e => {
      console.log(e);
    });

    Carnival.updateLocation(-41.292178, 174.777535); //Carnival Wellington.

    Carnival.logEvent("This is my event");

    Carnival.getUnreadCount().then(function(count) {
      console.log(count);
    }, function(e){
      console.log(e);
    });

    Carnival.getDeviceID().then(function(id) {
      console.log(id);
    }, function(e){
      console.log(e);
    });

    Carnival.setUserId("person");
    Carnival.setUserEmail("person@domain.com");

    //Carnival.setGeoIPTrackingEnabled(true);

    //Carnival.setCrashHandlersEnabled(true);

    //Carnival.registerForPushNotifications();

    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <Text style={styles.instructions}>
          To start, edit app.js.
        </Text>
        <Text style={styles.instructions}>
          {instructions}
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


