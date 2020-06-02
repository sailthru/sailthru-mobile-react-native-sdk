
package com.sailthru.mobile.rnsdk;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.sailthru.mobile.sdk.model.AttributeMap;
import com.sailthru.mobile.sdk.SailthruMobile;
import com.sailthru.mobile.sdk.MessageStream;
import com.sailthru.mobile.sdk.enums.ImpressionType;
import com.sailthru.mobile.sdk.model.ContentItem;
import com.sailthru.mobile.sdk.model.Message;
import com.sailthru.mobile.sdk.MessageActivity;
import com.sailthru.mobile.sdk.model.Purchase;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * React native module for the Sailthru Mobile SDK.
 */
public class RNSailthruMobileModule extends ReactContextBaseJavaModule implements MessageStream.OnInAppNotificationDisplayListener {

    protected final static String ERROR_CODE_DEVICE = "sailthru.mobile.device";
    protected final static String ERROR_CODE_MESSAGES = "sailthru.mobile.messages";
    protected final static String ERROR_CODE_RECOMMENDATIONS = "sailthru.mobile.recommendations";
    protected final static String ERROR_CODE_TRACKING = "sailthru.mobile.tracking";
    protected final static String ERROR_CODE_VARS = "sailthru.mobile.vars";
    protected final static String ERROR_CODE_PURCHASE = "sailthru.mobile.purchase";
    protected final static String MESSAGE_ID = "id";

    private boolean displayInAppNotifications;

    private ReactApplicationContext reactApplicationContext;

    @VisibleForTesting
    SailthruMobile sailthruMobile = new SailthruMobile();
    @VisibleForTesting
    MessageStream messageStream = new MessageStream();

    public RNSailthruMobileModule(ReactApplicationContext reactContext, boolean displayInAppNotifications) {
        super(reactContext);
        reactApplicationContext = reactContext;
        this.displayInAppNotifications = displayInAppNotifications;

        messageStream.setOnInAppNotificationDisplayListener(this);
        setWrapperInfo(sailthruMobile);
    }

    @Override
    public boolean shouldPresentInAppNotification(@NonNull Message message) {
        try {
            Method toJsonMethod = Message.class.getDeclaredMethod("toJSON");
            toJsonMethod.setAccessible(true);
            JSONObject messageJson = (JSONObject) toJsonMethod.invoke(message);
            if (messageJson == null) return displayInAppNotifications;

            WritableMap writableMap = convertJsonToMap(messageJson);
            reactApplicationContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("inappnotification", writableMap);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return displayInAppNotifications;
    }

    @Override
    public String getName() {
        return "RNSailthruMobile";
    }

    protected static void setWrapperInfo(SailthruMobile sailthruMobile) {
        try {
            Log.d("RNSailthruMobileModule", "setWrapperInfo");
            Class<?>[] cArg = new Class[2];
            cArg[0] = String.class;
            cArg[1] = String.class;

            Method setWrapperMethod = SailthruMobile.class.getDeclaredMethod("setWrapper", cArg);
            setWrapperMethod.setAccessible(true);
            setWrapperMethod.invoke(sailthruMobile, "React Native", "4.1.1");
            Log.d("RNSailthruMobileModule", "setWrapperInfo CALLED");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void registerForPushNotifications(boolean optInForPush) {
        // noop. It's here to share signatures with iOS.
    }

    @ReactMethod
    public void updateLocation(double latitude, double longitude) {
        Location location = new Location("React-Native");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        sailthruMobile.updateLocation(location);
    }

    @ReactMethod
    public void getDeviceID(final Promise promise) {
        sailthruMobile.getDeviceId(new SailthruMobile.SailthruMobileHandler<String>() {
            @Override
            public void onSuccess(String s) {
                promise.resolve(s);
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void logEvent(String value) {
        sailthruMobile.logEvent(value);
    }

    @ReactMethod
    public void logEvent(String eventName, ReadableMap varsMap) throws JSONException {
        JSONObject varsJson = convertMapToJson(varsMap);
        sailthruMobile.logEvent(eventName, varsJson);
    }

    @ReactMethod
    public void setAttributes(ReadableMap attributeMap, final Promise promise) throws JSONException {
        JSONObject attributeMapJson = convertMapToJson(attributeMap);
        JSONObject attributes = attributeMapJson.getJSONObject("attributes");
        AttributeMap stAttributeMap = new AttributeMap();
        stAttributeMap.setMergeRules(attributeMap.getInt("mergeRule"));

        Iterator<String> keys = attributes.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject attribute = attributes.getJSONObject(key);
            String attributeType = attribute.getString("type");
            switch (attributeType) {
                case "string":
                    stAttributeMap.putString(key, attribute.getString("value"));

                    break;
                case "stringArray": {
                    ArrayList<String> array = new ArrayList<>();
                    JSONArray values = attribute.getJSONArray("value");
                    for (int i = 0; i < values.length(); i++) {
                        array.add((String) values.get(i));
                    }

                    stAttributeMap.putStringArray(key, array);

                    break;
                }
                case "integer":
                    stAttributeMap.putInt(key, attribute.getInt("value"));

                    break;
                case "integerArray": {
                    ArrayList<Integer> array = new ArrayList<>();
                    JSONArray values = attribute.getJSONArray("value");
                    for (int i = 0; i < values.length(); i++) {
                        Integer j = values.getInt(i);
                        array.add(j);
                    }

                    stAttributeMap.putIntArray(key, array);

                    break;
                }
                case "boolean":
                    stAttributeMap.putBoolean(key, attribute.getBoolean("value"));

                    break;
                case "float":
                    stAttributeMap.putFloat(key, (float) attribute.getDouble("value"));

                    break;
                case "floatArray": {
                    ArrayList<Float> array = new ArrayList<>();
                    JSONArray values = attribute.getJSONArray("value");
                    for (int i = 0; i < values.length(); i++) {
                        Float value = Float.parseFloat(values.get(i).toString());
                        array.add(value);
                    }

                    stAttributeMap.putFloatArray(key, array);

                    break;
                }
                case "date":
                    Date value = new Date(attribute.getLong("value"));
                    stAttributeMap.putDate(key, value);

                    break;
                case "dateArray": {
                    ArrayList<Date> array = new ArrayList<>();
                    JSONArray values = attribute.getJSONArray("value");
                    for (int i = 0; i < values.length(); i++) {
                        long dateValue = values.getLong(i);
                        Date date = new Date(dateValue);
                        array.add(date);
                    }

                    stAttributeMap.putDateArray(key, array);
                    break;
                }
            }
        }

        sailthruMobile.setAttributes(stAttributeMap, new SailthruMobile.AttributesHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(error.getLocalizedMessage(), error);
            }
        });
    }

    @ReactMethod
    public void getMessages(final Promise promise) {
        messageStream.getMessages(new MessageStream.MessagesHandler() {
            @Override
            public void onSuccess(@NonNull ArrayList<Message> messages) {

                WritableArray array = getWritableArray();
                try {
                    Method toJsonMethod = Message.class.getDeclaredMethod("toJSON");
                    toJsonMethod.setAccessible(true);

                    for (Message message : messages) {
                        JSONObject messageJson = (JSONObject) toJsonMethod.invoke(message);
                        if (messageJson == null)
                            continue;
                        array.pushMap(convertJsonToMap(messageJson));
                    }
                    promise.resolve(array);
                } catch (NoSuchMethodException e) {
                    promise.reject(ERROR_CODE_MESSAGES, e.getMessage());
                } catch (IllegalAccessException e) {
                    promise.reject(ERROR_CODE_MESSAGES, e.getMessage());
                } catch (JSONException e) {
                    promise.reject(ERROR_CODE_MESSAGES, e.getMessage());
                } catch (InvocationTargetException e) {
                    promise.reject(ERROR_CODE_MESSAGES, e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(ERROR_CODE_MESSAGES, error.getMessage());
            }
        });
    }

    // Moved out to separate method for testing as WritableNativeArray cannot be mocked
    WritableArray getWritableArray() {
        return new WritableNativeArray();
    }

    WritableMap convertJsonToMap(JSONObject jsonObject) throws JSONException {
        WritableMap map = new WritableNativeMap();

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                map.putMap(key, convertJsonToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                map.putArray(key, convertJsonToArray((JSONArray) value));
            } else if (value instanceof Boolean) {
                map.putBoolean(key, (Boolean) value);
            } else if (value instanceof Integer) {
                map.putInt(key, (Integer) value);
            } else if (value instanceof Double) {
                map.putDouble(key, (Double) value);
            } else if (value instanceof String) {
                map.putString(key, (String) value);
            } else {
                map.putString(key, value.toString());
            }
        }
        return map;
    }

    WritableArray convertJsonToArray(JSONArray jsonArray) throws JSONException {
        WritableArray array = new WritableNativeArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject) {
                array.pushMap(convertJsonToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                array.pushArray(convertJsonToArray((JSONArray) value));
            } else if (value instanceof Boolean) {
                array.pushBoolean((Boolean) value);
            } else if (value instanceof Integer) {
                array.pushInt((Integer) value);
            } else if (value instanceof Double) {
                array.pushDouble((Double) value);
            } else if (value instanceof String) {
                array.pushString((String) value);
            } else {
                array.pushString(value.toString());
            }
        }
        return array;
    }

    JSONObject convertMapToJson(ReadableMap readableMap) throws JSONException {
        JSONObject object = new JSONObject();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    object.put(key, JSONObject.NULL);
                    break;
                case Boolean:
                    object.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    object.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    object.put(key, readableMap.getString(key));
                    break;
                case Map:
                    object.put(key, convertMapToJson(readableMap.getMap(key)));
                    break;
                case Array:
                    object.put(key, convertArrayToJson(readableMap.getArray(key)));
                    break;
            }
        }
        return object;
    }

    JSONArray convertArrayToJson(ReadableArray readableArray) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    array.put(readableArray.getDouble(i));
                    break;
                case String:
                    array.put(readableArray.getString(i));
                    break;
                case Map:
                    array.put(convertMapToJson(readableArray.getMap(i)));
                    break;
                case Array:
                    array.put(convertArrayToJson(readableArray.getArray(i)));
                    break;
            }
        }
        return array;
    }

    JSONObject convertPurchaseMapToJson(ReadableMap readableMap) throws JSONException {
        JSONObject object = new JSONObject();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    object.put(key, JSONObject.NULL);
                    break;
                case Boolean:
                    object.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    object.put(key, readableMap.getInt(key));
                    break;
                case String:
                    object.put(key, readableMap.getString(key));
                    break;
                case Map:
                    object.put(key, convertPurchaseMapToJson(readableMap.getMap(key)));
                    break;
                case Array:
                    object.put(key, convertPurchaseArrayToJson(readableMap.getArray(key)));
                    break;
            }
        }
        return object;
    }

    JSONArray convertPurchaseArrayToJson(ReadableArray readableArray) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    array.put(readableArray.getDouble(i));
                    break;
                case String:
                    array.put(readableArray.getString(i));
                    break;
                case Map:
                    array.put(convertPurchaseMapToJson(readableArray.getMap(i)));
                    break;
                case Array:
                    array.put(convertPurchaseArrayToJson(readableArray.getArray(i)));
                    break;
            }
        }
        return array;
    }

    @ReactMethod
    public void setUserId(String userId) {
        sailthruMobile.setUserId(userId, null);
    }

    @ReactMethod
    public void setUserEmail(String userEmail) {
        sailthruMobile.setUserEmail(userEmail, null);
    }

    @ReactMethod
    public void getUnreadCount(final Promise promise) {
        messageStream.getUnreadMessageCount(new MessageStream.MessageStreamHandler<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                promise.resolve(integer);
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(ERROR_CODE_MESSAGES, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void removeMessage(ReadableMap messageMap) {
        Message message = getMessage(messageMap);
        messageStream.deleteMessage(message, null);
    }

    @ReactMethod
    public void registerMessageImpression(int typeCode, ReadableMap messageMap) {
        Message message = getMessage(messageMap);
        ImpressionType type = null;

        if (typeCode == 0) type = ImpressionType.IMPRESSION_TYPE_IN_APP_VIEW;
        else if (typeCode == 1) type = ImpressionType.IMPRESSION_TYPE_STREAM_VIEW;
        else if (typeCode == 2) type = ImpressionType.IMPRESSION_TYPE_DETAIL_VIEW;

        if (type != null) {
            messageStream.registerMessageImpression(type, message);
        }
    }

    @ReactMethod
    public void markMessageAsRead(ReadableMap messageMap, final Promise promise) {
        Message message = getMessage(messageMap);
        messageStream.setMessageRead(message, new MessageStream.MessagesReadHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(ERROR_CODE_MESSAGES, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void presentMessageDetail(ReadableMap message) {
        String messageId = message.getString(MESSAGE_ID);
        Activity activity = currentActivity();
        if (activity != null) {
            Intent i = MessageActivity.intentForMessage(activity, null, messageId);
            activity.startActivity(i);
        }
    }

    // wrapped to expose for testing
    protected Activity currentActivity() {
        return getCurrentActivity();
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void dismissMessageDetail() {
        // noop. It's here to share signatures with iOS.
    }

    /*
    TRACK SPM
     */
    @ReactMethod
    public void getRecommendations(String sectionId, final Promise promise) {
        sailthruMobile.getRecommendations(sectionId, new SailthruMobile.RecommendationsHandler() {

            @Override
            public void onSuccess(@NonNull ArrayList<ContentItem> contentItems) {
                WritableArray array = getWritableArray();
                try {
                    for (ContentItem contentItem : contentItems) {
                        array.pushMap(convertJsonToMap(contentItem.toJSON()));
                    }
                    promise.resolve(array);
                } catch (Exception e) {
                    promise.reject(ERROR_CODE_RECOMMENDATIONS, e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(ERROR_CODE_RECOMMENDATIONS, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void trackClick(String sectionId, String url, final Promise promise) {
        try {
            sailthruMobile.trackClick(sectionId, new URI(url), new SailthruMobile.TrackHandler() {
                @Override
                public void onSuccess() {
                    promise.resolve(true);
                }

                @Override
                public void onFailure(@NonNull Error error) {
                    promise.reject(ERROR_CODE_TRACKING, error.getMessage());
                }
            });
        } catch (URISyntaxException e) {
            promise.reject(ERROR_CODE_TRACKING, e.getMessage());
        }
    }

    @ReactMethod
    public void trackPageview(String url, ReadableArray tags, final Promise promise) {
        try {
            List<String> convertedTags = null;
            if (tags != null) {
                convertedTags = new ArrayList<>();
                for (int i = 0; i < tags.size(); i++) {
                    convertedTags.add(tags.getString(i));
                }
            }

            sailthruMobile.trackPageview(new URI(url), convertedTags, new SailthruMobile.TrackHandler() {
                @Override
                public void onSuccess() {
                    promise.resolve(true);
                }

                @Override
                public void onFailure(@NonNull Error error) {
                    promise.reject(ERROR_CODE_TRACKING, error.getMessage());
                }
            });
        } catch (URISyntaxException e) {
            promise.reject(ERROR_CODE_TRACKING, e.getMessage());
        }
    }

    @ReactMethod
    public void trackImpression(String sectionId, ReadableArray urls, final Promise promise) {
        try {
            List<URI> convertedUrls = null;
            if (urls != null) {
                convertedUrls = new ArrayList<>();
                for (int i = 0; i < urls.size(); i++) {
                    convertedUrls.add(new URI(urls.getString(i)));
                }
            }

            sailthruMobile.trackImpression(sectionId, convertedUrls, new SailthruMobile.TrackHandler() {
                @Override
                public void onSuccess() {
                    promise.resolve(true);
                }

                @Override
                public void onFailure(@NonNull Error error) {
                    promise.reject(ERROR_CODE_TRACKING, error.getMessage());
                }
            });
        } catch (URISyntaxException e) {
            promise.reject(ERROR_CODE_TRACKING, e.getMessage());
        }
    }

    @ReactMethod
    public void setGeoIPTrackingEnabled(boolean enabled) {
        sailthruMobile.setGeoIpTrackingEnabled(enabled);
    }

    @ReactMethod
    public void setGeoIPTrackingEnabled(boolean enabled, final Promise promise) {
        sailthruMobile.setGeoIpTrackingEnabled(enabled, new SailthruMobile.SailthruMobileHandler<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                promise.resolve(true);
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage());
            }
        });
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void setCrashHandlersEnabled(boolean enabled) {
        // noop. It's here to share signatures with iOS.
    }

    @ReactMethod
    public void clearDevice(int options, final Promise promise) {
        sailthruMobile.clearDevice(options, new SailthruMobile.SailthruMobileHandler<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                promise.resolve(true);
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setProfileVars(ReadableMap vars, final Promise promise) throws JSONException {
        JSONObject varsJson = convertMapToJson(vars);
        sailthruMobile.setProfileVars(varsJson, new SailthruMobile.SailthruMobileHandler<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                promise.resolve(true);
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(ERROR_CODE_VARS, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getProfileVars(final Promise promise) {
        sailthruMobile.getProfileVars(new SailthruMobile.SailthruMobileHandler<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    WritableMap vars = convertJsonToMap(jsonObject);
                    promise.resolve(vars);
                } catch (JSONException e) {
                    promise.reject(ERROR_CODE_VARS, e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(ERROR_CODE_VARS, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void logPurchase(ReadableMap purchaseMap, final Promise promise) throws JSONException {
        Purchase purchase = getPurchaseInstance(purchaseMap, promise);
        if (purchase != null) {
            sailthruMobile.logPurchase(purchase, new SailthruMobile.SailthruMobileHandler<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    promise.resolve(true);
                }

                @Override
                public void onFailure(@NonNull Error error) {
                    promise.reject(ERROR_CODE_PURCHASE, error.getMessage());
                }
            });
        }
    }

    @ReactMethod
    public void logAbandonedCart(ReadableMap purchaseMap, final Promise promise) throws JSONException {
        Purchase purchase = getPurchaseInstance(purchaseMap, promise);
        if (purchase != null) {
            sailthruMobile.logAbandonedCart(purchase, new SailthruMobile.SailthruMobileHandler<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    promise.resolve(true);
                }

                @Override
                public void onFailure(@NonNull Error error) {
                    promise.reject(ERROR_CODE_PURCHASE, error.getMessage());
                }
            });
        }
    }

    @VisibleForTesting
    Purchase getPurchaseInstance(ReadableMap purchaseMap, final Promise promise) throws JSONException {
        JSONObject purchaseJson = convertPurchaseMapToJson(purchaseMap);
        try {
          Constructor<Purchase> purchaseConstructor = Purchase.class.getDeclaredConstructor(JSONObject.class);
          purchaseConstructor.setAccessible(true);
          return purchaseConstructor.newInstance(purchaseJson);

        } catch (NoSuchMethodException e) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage());
        } catch (IllegalAccessException e) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage());
        } catch (InvocationTargetException e) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage());
        } catch (InstantiationException e) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage());
        }
        return null;
    }

    /*
     * Helper Methods
     */

    protected Message getMessage(ReadableMap messageMap) {

        Message message = null;
        try {
            JSONObject messageJson = convertMapToJson(messageMap);
            Constructor<Message> constructor;
            constructor = Message.class.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            message = constructor.newInstance(messageJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }
}
