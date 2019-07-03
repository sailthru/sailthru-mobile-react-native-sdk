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
    Carnival.getMessages()
      .then(messages => {
        if (messages.length > 2) {
          Carnival.markMessageAsRead(messages[0]);
          Carnival.presentMessageDetail(messages[0]);
          Carnival.registerMessageImpression(Carnival.MessageImpressionType.InAppView, messages[1]);
        }
      })
      .catch(e => {
        console.log(e);
    });

    Carnival.getRecommendations("sectionID").then(function(contentItemsArray) {
      // Content items contain data for recommended items
    }).catch(function(error) {
      // Handle errors here
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

    Carnival.getDeviceID().then(function(id) {
      console.log(id);
    }, function(e){
      console.log(e);
    });

    Carnival.setUserId("person");
    Carnival.setUserEmail("person@domain.com");

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

    var purchaseItem1 = new Carnival.PurchaseItem(1, "title", 1234, 2345, "www.example.com/item1");
    var purchaseItem2 = new Carnival.PurchaseItem(3, "other item", 1534, 2346, "www.example.com/item2");
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
