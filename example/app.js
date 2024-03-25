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

const { Marigold, EngageBySailthru, MessageStream } = require('react-native-marigold');
var SDK_KEY = ''; // Put your SDK key in here.
import { NativeEventEmitter } from 'react-native'

const instructions = Platform.select({
  ios: 'Press Cmd+R to reload,\n' +
    'Cmd+D or shake for dev menu',
  android: 'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});


export default class App extends Component {
  render() {
    MessageStream.getMessages()
      .then(messages => {
        if (messages.length > 2) {
          Marigold.markMessageAsRead(messages[0]);
          Marigold.presentMessageDetail(messages[0]);
          Marigold.registerMessageImpression(Marigold.MessageImpressionType.InAppView, messages[1]);
        }
      })
      .catch(e => {
        console.log(e);
    });

    var attrMap = new EngageBySailthru.AttributeMap();
    attrMap.setString("string_key", "This is the string value");
    attrMap.setStringArray("strings_key", ["This is first value", "This is the second value"]);
    attrMap.setDate("date_key", new Date());
    attrMap.setDateArray("dates_key", [new Date(), new Date(), new Date()]);
    attrMap.setFloat("float_key", 3.141);
    attrMap.setFloatArray("floats_key", [1.1, 2.2, 3.3, 4.4]);
    attrMap.setInteger("integer_key", 3);
    attrMap.setIntegerArray("integers_key", [1, 2, 3, 4]);
    attrMap.setBoolean("boolean_key", true);

    EngageBySailthru.setAttributes(attrMap).catch(e => {
      console.log(e);
    });

    Marigold.updateLocation(-41.292178, 174.777535); //Marigold Wellington.

    EngageBySailthru.logEvent("This is my event");

    var eventVars = {
      "varKey" : "varValue"
    };
    EngageBySailthru.logEvent("this is my event with vars", eventVars);

    MessageStream.getUnreadCount().then(function(count) {
      console.log(count);
    }, function(e){
      console.log(e);
    });

    Marigold.getDeviceID().then(function(id) {
      console.log(id);
    }, function(e){
      console.log(e);
    });

    EngageBySailthru.setUserId("person").then(result => {
      console.log("Set User ID Success");
    }, e => {
      console.log(e);
    });

    EngageBySailthru.setUserEmail("person@domain.com").then(result => {
      console.log("Set User Email Success");
    }, e => {
      console.log(e);
    });

    var profileVars = {
      "string_key" : "string_value",
      "boolean_key" : true
    };
    EngageBySailthru.setProfileVars(profileVars).then(result => {
      console.log("Set Profile Vars Success");
    }).catch(e => {
      console.log(e);
    });

    EngageBySailthru.getProfileVars().then(profileVars => {
      console.log(profileVars);
    }).catch(e => {
      console.log(e);
    });

    var purchaseItem1 = new Marigold.PurchaseItem(1, "title", 1234, 2345, "www.example.com/item1");
    var purchaseItem2 = new Marigold.PurchaseItem(3, "other item", 1534, 2346, "www.example.com/item2");
    var purchaseItems = [ purchaseItem1, purchaseItem2 ];
    var purchase = new Marigold.Purchase(purchaseItems);
    EngageBySailthru.logPurchase(purchase).then(result => {
      console.log("Purchase Log Success");
    }).catch(e => {
      console.log(e);
    });

    EngageBySailthru.logAbandonedCart(purchase).then(result => {
      console.log("Abandoned Cart Log Success");
    }).catch(e => {
      console.log(e);
    });

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
