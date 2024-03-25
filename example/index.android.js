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

const { Marigold, EngageBySailthru, MessageStream } = require('react-native-marigold');
var SDK_KEY = ''; // Put your SDK key in here.

import { NativeEventEmitter } from 'react-native'

const myModuleEvt = new NativeEventEmitter(Marigold)
myModuleEvt.addListener('inappnotification', (data) => console.log(data))

class ReactNativeSampleApp extends Component {
  render() {
    MessageStream.getMessages()
      .then(messages => {
        if (messages.length > 2) {
          MessageStream.markMessageAsRead(messages[0]);
        }

        MessageStream.presentMessageDetail(messages[0]);

        MessageStream.registerMessageImpression(MessageStream.MessageImpressionType.InAppView, messages[1]);
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

      Marigold.getDeviceId().then(function(id) {
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

    var purchaseItem1 = new EngageBySailthru.PurchaseItem(1, "title", 1234, "2345", "https://www.example.com/item1");
    var purchaseItem2 = new EngageBySailthru.PurchaseItem(3, "other item", 1534, "2346", "https://www.example.com/item2");
    var purchaseItems = [ purchaseItem1, purchaseItem2 ];
    var purchase = new EngageBySailthru.Purchase(purchaseItems);
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
          Marigold React Native Test App
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
