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


var SailthruMobile = require('react-native-sailthru-mobile');
var SDK_KEY = ''; // Put your SDK key in here.

import { NativeEventEmitter } from 'react-native'

const myModuleEvt = new NativeEventEmitter(SailthruMobile)
myModuleEvt.addListener('inappnotification', (data) => console.log(data))

class ReactNativeSampleApp extends Component {
  render() {
    SailthruMobile.getMessages()
      .then(messages => {
        if (messages.length > 2) {
          SailthruMobile.markMessageAsRead(messages[0]);
        }

        SailthruMobile.presentMessageDetail(messages[0]);

        SailthruMobile.registerMessageImpression(SailthruMobile.MessageImpressionType.InAppView, messages[1]);
      })
      .catch(e => {
        console.log(e);
      });

      var attrMap = new SailthruMobile.AttributeMap();
      attrMap.setString("string_key", "This is the string value");
      attrMap.setStringArray("strings_key", ["This is first value", "This is the second value"]);
      attrMap.setDate("date_key", new Date());
      attrMap.setDateArray("dates_key", [new Date(), new Date(), new Date()]);
      attrMap.setFloat("float_key", 3.141);
      attrMap.setFloatArray("floats_key", [1.1, 2.2, 3.3, 4.4]);
      attrMap.setInteger("integer_key", 3);
      attrMap.setIntegerArray("integers_key", [1, 2, 3, 4]);
      attrMap.setBoolean("boolean_key", true);

      SailthruMobile.setAttributes(attrMap).catch(e => {
        console.log(e);
      });

      SailthruMobile.updateLocation(-41.292178, 174.777535); //SailthruMobile Wellington.

      SailthruMobile.logEvent("This is my event");

      var eventVars = {
        "varKey" : "varValue"
      };
      SailthruMobile.logEvent("this is my event with vars", eventVars);

      SailthruMobile.getUnreadCount().then(function(count) {
        console.log(count);
      }, function(e){
        console.log(e);
      });

      SailthruMobile.getDeviceId().then(function(id) {
        console.log(id);
      }, function(e){
        console.log(e);
      });

      SailthruMobile.setUserId("person").then(result => {
        console.log("Set User ID Success");
      }, e => {
        console.log(e);
      });

      SailthruMobile.setUserEmail("person@domain.com").then(result => {
        console.log("Set User Email Success");
      }, e => {
        console.log(e);
      });

      var profileVars = {
        "string_key" : "string_value",
        "boolean_key" : true
      };
      SailthruMobile.setProfileVars(profileVars).then(result => {
        console.log("Set Profile Vars Success");
      }).catch(e => {
        console.log(e);
      });

      SailthruMobile.getProfileVars().then(profileVars => {
        console.log(profileVars);
      }).catch(e => {
        console.log(e);
      });

    var purchaseItem1 = new SailthruMobile.PurchaseItem(1, "title", 1234, "2345", "https://www.example.com/item1");
    var purchaseItem2 = new SailthruMobile.PurchaseItem(3, "other item", 1534, "2346", "https://www.example.com/item2");
    var purchaseItems = [ purchaseItem, purchaseItem2 ];
    var purchase = new SailthruMobile.Purchase(purchaseItems);
    SailthruMobile.logPurchase(purchase).then(result => {
      console.log("Purchase Log Success");
    }).catch(e => {
      console.log(e);
    });

    SailthruMobile.logAbandonedCart(purchase).then(result => {
      console.log("Abandoned Cart Log Success");
    }).catch(e => {
      console.log(e);
    });

    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Sailthru Mobile React Native Test App
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
