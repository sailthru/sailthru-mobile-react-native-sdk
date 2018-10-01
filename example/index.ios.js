/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View
} from 'react-native';

var Carnival = require('react-native-carnival');
var SDK_KEY = ''; // Put your SDK key in here.
import { NativeEventEmitter } from 'react-native'

const myModuleEvt = new NativeEventEmitter(Carnival)
myModuleEvt.addListener('inappnotification', (data) => console.log(data))


export default class ReactNativeSampleApp extends Component {
  render() {
    Carnival.getMessages()
      .then(messages => {
        if (messages.length > 2) {
          Carnival.markMessageAsRead(messages[0]);
          Carnival.presentMessageDetail(messages[0]);
          setTimeout(function(){ Carnival.dismissMessageDetail(); }, 5000);
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

    Carnival.setUserId("person@domain.com").catch( e => {
      console.log(e);
    });

    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <Text style={styles.instructions}>
          To get started, edit index.ios.js
        </Text>
        <Text style={styles.instructions}>
          Press Cmd+R to reload,{'\n'}
          Cmd+D or shake for dev menu
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

AppRegistry.registerComponent('ReactNativeSampleApp', () => ReactNativeSampleApp);
