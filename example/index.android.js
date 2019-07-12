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

class ReactNativeSampleApp extends Component {
  constructor(props) {
    super(props);
    Carnival.getMessages()
      .then(messages => {
        if (messages.length > 2) {
          Carnival.markMessageAsRead(messages[0]);
        }

        Carnival.presentMessageDetail(messages[0]);

        Carnival.registerMessageImpression(Carnival.MessageImpressionType.InAppView, messages[1]);
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

    var eventVars = {
      "varKey" : "varValue"
    };
    Carnival.logEvent("this is my event with vars", eventVars);

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

    var profileVars = {
      "string_key" : "string_value",
      "boolean_key" : true
    };
    Carnival.setProfileVars(profileVars).then(result => {
      console.log("Set Profile Vars Success");
    }).catch(e => {
      console.log(e);
    });

    Carnival.getProfileVars().then(profileVars => {
      console.log(profileVars);
    }).catch(e => {
      console.log(e);
    });
  }

  var purchaseItem1 = new Carnival.PurchaseItem(1, "title", 1234, "2345", "https://www.example.com/item1");
  var purchaseItem2 = new Carnival.PurchaseItem(3, "other item", 1534, "2346", "https://www.example.com/item2");
  var purchaseItems = [ purchaseItem, purchaseItem2 ];
  var purchase = new Carnival.Purchase(purchaseItems);
  Carnival.logPurchase(purchase).then(result => {
    console.log("Purchase Log Success");
  }).catch(e => {
    console.log(e);
  });

  Carnival.logAbandonedCart(purchase).then(result => {
    console.log("Abandoned Cart Log Success");
  }).catch(e => {
    console.log(e);
  });

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

AppRegistry.registerComponent('ReactNativeSampleApp', () => ReactNativeSampleApp);
