
package com.reactlibrary;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.carnival.sdk.AttributeMap;
import com.carnival.sdk.Carnival;
import com.carnival.sdk.CarnivalImpressionType;
import com.carnival.sdk.ContentItem;
import com.carnival.sdk.Message;
import com.carnival.sdk.MessageActivity;
import com.carnival.sdk.Purchase;
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

public class RNCarnivalModule extends ReactContextBaseJavaModule implements Carnival.OnInAppNotificationDisplayListener {

    protected final static String ERROR_CODE_DEVICE = "carnival.device";
    protected final static String ERROR_CODE_MESSAGES = "carnival.messages";
    protected final static String ERROR_CODE_RECOMMENDATIONS = "carnival.recommendations";
    protected final static String ERROR_CODE_TRACKING = "carnival.tracking";
    protected final static String ERROR_CODE_VARS = "carnival.vars";
    protected final static String ERROR_CODE_PURCHASE = "carnival.purchase";
    protected final static String MESSAGE_ID = "id";

    private boolean displayInAppNotifications;

    private ReactApplicationContext reactApplicationContext;

    public RNCarnivalModule(ReactApplicationContext reactContext, boolean displayInAppNotifications) {
        super(reactContext);
        reactApplicationContext = reactContext;
        this.displayInAppNotifications = displayInAppNotifications;

        Carnival.setOnInAppNotificationDisplayListener(this);
        setWrapperInfo();
    }

    @Override
    public boolean shouldPresentInAppNotification(Message message) {
        try {
            Method toJsonMethod = null;
            toJsonMethod = Message.class.getDeclaredMethod("toJSON");
            toJsonMethod.setAccessible(true);
            JSONObject messageJson = (JSONObject) toJsonMethod.invoke(message);
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
        return "RNCarnival";
    }

    protected static void setWrapperInfo() {
        Method setWrapperMethod = null;
        try {
            Class[] cArg = new Class[2];
            cArg[0] = String.class;
            cArg[1] = String.class;

            setWrapperMethod = Carnival.class.getDeclaredMethod("setWrapper", cArg);
            setWrapperMethod.setAccessible(true);
            setWrapperMethod.invoke(null, "React Native", "4.0.0");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void registerForPushNotifications(boolean optInForPush) {
        // noop. It's here to share signatures with iOS.
    }

    @ReactMethod
    public void updateLocation(double latitude, double longitude) {
        Location location = new Location("React-Native");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        Carnival.updateLocation(location);
    }

    @ReactMethod
    public void getDeviceID(final Promise promise) {
        Carnival.getDeviceId(new Carnival.CarnivalHandler<String>() {
            @Override
            public void onSuccess(String s) {
                promise.resolve(s);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void logEvent(String value) {
        Carnival.logEvent(value);
    }

    @ReactMethod
    public void logEvent(String eventName, ReadableMap varsMap) throws JSONException {
        JSONObject varsJson = convertMapToJson(varsMap);
        Carnival.logEvent(eventName, varsJson);
    }

    @ReactMethod
    public void setAttributes(ReadableMap attributeMap, final Promise promise) throws JSONException {
        JSONObject attributeMapJson = convertMapToJson(attributeMap);
        JSONObject attributes = attributeMapJson.getJSONObject("attributes");
        AttributeMap carnivalAttributeMap = new AttributeMap();
        carnivalAttributeMap.setMergeRules(attributeMap.getInt("mergeRule"));

        Iterator<String> keys = attributes.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject attribute = attributes.getJSONObject(key);
            String attributeType = attribute.getString("type");
            if (attributeType.equals("string")) {
                carnivalAttributeMap.putString(key, attribute.getString("value"));

            } else if (attributeType.equals("stringArray")) {
                ArrayList<String> array = new ArrayList<String>();
                JSONArray values = attribute.getJSONArray("value");
                for (int i = 0; i < values.length(); i++) {
                    array.add((String) values.get(i));
                }

                carnivalAttributeMap.putStringArray(key, array);

            } else if (attributeType.equals("integer")) {
                carnivalAttributeMap.putInt(key, attribute.getInt("value"));

            } else if (attributeType.equals("integerArray")) {
                ArrayList<Integer> array = new ArrayList<Integer>();
                JSONArray values = attribute.getJSONArray("value");
                for (int i = 0; i < values.length(); i++) {
                    Object j = values.getInt(i);
                    array.add((Integer) j);
                }

                carnivalAttributeMap.putIntArray(key, array);

            } else if (attributeType.equals("boolean")) {
                carnivalAttributeMap.putBoolean(key, attribute.getBoolean("value"));

            } else if (attributeType.equals("float")) {
                carnivalAttributeMap.putFloat(key, (float) attribute.getDouble("value"));

            } else if (attributeType.equals("floatArray")) {
                ArrayList<Float> array = new ArrayList<Float>();
                JSONArray values = attribute.getJSONArray("value");
                for (int i = 0; i < values.length(); i++) {
                    Float value = Float.parseFloat(values.get(i).toString());
                    array.add(value);
                }

                carnivalAttributeMap.putFloatArray(key, array);

            } else if (attributeType.equals("date")) {
                Date value = new Date(attribute.getLong("value"));
                carnivalAttributeMap.putDate(key, value);

            } else if (attributeType.equals("dateArray")) {
                ArrayList<Date> array = new ArrayList<Date>();
                JSONArray values = attribute.getJSONArray("value");
                for (int i = 0; i < values.length(); i++) {
                    Long dateValue = values.getLong(i);
                    Date value = new Date(dateValue);
                    array.add(value);
                }

                carnivalAttributeMap.putDateArray(key, array);
            }
        }

        Carnival.setAttributes(carnivalAttributeMap, new Carnival.AttributesHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject(error.getLocalizedMessage(), error);
            }
        });
    }

    @ReactMethod
    public void getMessages(final Promise promise) {
        Carnival.getMessages(new Carnival.MessagesHandler() {
            @Override
            public void onSuccess(ArrayList<Message> messages) {

                WritableArray array = getWritableArray();
                try {
                    Method toJsonMethod = Message.class.getDeclaredMethod("toJSON");
                    toJsonMethod.setAccessible(true);

                    for (Message message : messages) {
                        JSONObject messageJson = (JSONObject) toJsonMethod.invoke(message);
                        System.out.println(message.toString());
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
            public void onFailure(Error error) {
                promise.reject(ERROR_CODE_MESSAGES, error.getMessage());
            }
        });
    }

    // Moved out to separate method for testing as WritableNativeArray cannot be mocked
    protected static WritableArray getWritableArray() {
        return new WritableNativeArray();
    }

    private static WritableMap convertJsonToMap(JSONObject jsonObject) throws JSONException {
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

    private static WritableArray convertJsonToArray(JSONArray jsonArray) throws JSONException {
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

    private static JSONObject convertMapToJson(ReadableMap readableMap) throws JSONException {
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

    private static JSONArray convertArrayToJson(ReadableArray readableArray) throws JSONException {
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

    private static JSONObject convertPurchaseMapToJson(ReadableMap readableMap) throws JSONException {
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

    private static JSONArray convertPurchaseArrayToJson(ReadableArray readableArray) throws JSONException {
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
        Carnival.setUserId(userId, null);
    }

    @ReactMethod
    public void setUserEmail(String userEmail) {
        Carnival.setUserEmail(userEmail, null);
    }

    @ReactMethod
    public void getUnreadCount(final Promise promise) {
        Carnival.getUnreadMessageCount(new Carnival.CarnivalHandler<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                promise.resolve(integer.intValue());
            }

            @Override
            public void onFailure(Error error) {
                promise.reject(ERROR_CODE_MESSAGES, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void removeMessage(ReadableMap messageMap) {
        Message message = getMessage(messageMap);
        Carnival.deleteMessage(message, null);
    }

    @ReactMethod
    public void registerMessageImpression(int typeCode, ReadableMap messageMap) {
        Message message = getMessage(messageMap);
        CarnivalImpressionType type = null;

        if (typeCode == 0) type = CarnivalImpressionType.IMPRESSION_TYPE_IN_APP_VIEW;
        else if (typeCode == 1) type = CarnivalImpressionType.IMPRESSION_TYPE_STREAM_VIEW;
        else if (typeCode == 2) type = CarnivalImpressionType.IMPRESSION_TYPE_DETAIL_VIEW;

        if (type != null) {
            Carnival.registerMessageImpression(type, message);
        }
    }

    @ReactMethod
    public void markMessageAsRead(ReadableMap messageMap, final Promise promise) {
        Message message = getMessage(messageMap);
        Carnival.setMessageRead(message, new Carnival.MessagesReadHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject(ERROR_CODE_MESSAGES, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void presentMessageDetail(ReadableMap message) {
        Intent i = new Intent(currentActivity(), MessageActivity.class);
        String messageId = message.getString(MESSAGE_ID);
        i.putExtra(Carnival.EXTRA_MESSAGE_ID, messageId);
        Activity activity = currentActivity();
        if (activity != null) {
            activity.startActivity(i);
        }
    }

    // wrapped to expose for testing
    protected Activity currentActivity() {
        return getCurrentActivity();
    }

    @ReactMethod
    public void dismissMessageDetail() {
        // noop. It's here to share signatures with iOS.
    }

    /*
    TRACK SPM
     */
    @ReactMethod
    public void getRecommendations(String sectionId, final Promise promise) {
        Carnival.getRecommendations(sectionId, new Carnival.RecommendationsHandler() {

            @Override
            public void onSuccess(ArrayList<ContentItem> contentItems) {
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
            public void onFailure(Error error) {
                promise.reject(ERROR_CODE_RECOMMENDATIONS, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void trackClick(String sectionId, String url, final Promise promise) {
        try {
            Carnival.trackClick(sectionId, new URI(url), new Carnival.TrackHandler() {
                @Override
                public void onSuccess() {
                    promise.resolve(true);
                }

                @Override
                public void onFailure(Error error) {
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

            Carnival.trackPageview(new URI(url), convertedTags, new Carnival.TrackHandler() {
                @Override
                public void onSuccess() {
                    promise.resolve(true);
                }

                @Override
                public void onFailure(Error error) {
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

            Carnival.trackImpression(sectionId, convertedUrls, new Carnival.TrackHandler() {
                @Override
                public void onSuccess() {
                    promise.resolve(true);
                }

                @Override
                public void onFailure(Error error) {
                    promise.reject(ERROR_CODE_TRACKING, error.getMessage());
                }
            });
        } catch (URISyntaxException e) {
            promise.reject(ERROR_CODE_TRACKING, e.getMessage());
        }
    }

    @ReactMethod
    public void setGeoIPTrackingEnabled(boolean enabled) {
        Carnival.setGeoIpTrackingEnabled(enabled);
    }

    @ReactMethod
    public void setGeoIPTrackingEnabled(boolean enabled, final Promise promise) {
        Carnival.setGeoIpTrackingEnabled(enabled, new Carnival.CarnivalHandler<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                promise.resolve(true);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setCrashHandlersEnabled(boolean enabled) {
        // noop. It's here to share signatures with iOS.
    }

    @ReactMethod
    public void clearDevice(int options, final Promise promise) {
        Carnival.clearDevice(options, new Carnival.CarnivalHandler<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                promise.resolve(true);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setProfileVars(ReadableMap vars, final Promise promise) throws JSONException {
        JSONObject varsJson = convertMapToJson(vars);
        Carnival.setProfileVars(varsJson, new Carnival.CarnivalHandler<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                promise.resolve(true);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject(ERROR_CODE_VARS, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getProfileVars(final Promise promise) {
        Carnival.getProfileVars(new Carnival.CarnivalHandler<JSONObject>() {
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
            public void onFailure(Error error) {
                promise.reject(ERROR_CODE_VARS, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void logPurchase(ReadableMap purchaseMap, final Promise promise) throws JSONException {
        Purchase purchase = getPurchaseInstance(purchaseMap, promise);
        Log.d("PURCHASE", purchase.toString());
        if (purchase != null) {
            Carnival.logPurchase(purchase, new Carnival.CarnivalHandler<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("PURCHASE", "SUCCESS");
                    promise.resolve(true);
                }

                @Override
                public void onFailure(Error error) {
                    Log.d("PURCHASE", "ERROR");
                    promise.reject(ERROR_CODE_PURCHASE, error.getMessage());
                }
            });
        }
    }

    @ReactMethod
    public void logAbandonedCart(ReadableMap purchaseMap, final Promise promise) throws JSONException {
        Purchase purchase = getPurchaseInstance(purchaseMap, promise);
        Log.d("ABANDONED CART", purchase.toString());
        if (purchase != null) {
            Carnival.logAbandonedCart(purchase, new Carnival.CarnivalHandler<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("ABANDONED CART", "SUCCESS");
                    promise.resolve(true);
                }

                @Override
                public void onFailure(Error error) {
                    Log.d("ABANDONED CART", "ERROR");
                    promise.reject(ERROR_CODE_PURCHASE, error.getMessage());
                }
            });
        }
    }

    private static Purchase getPurchaseInstance(ReadableMap purchaseMap, final Promise promise) throws JSONException {
        JSONObject purchaseJson = convertPurchaseMapToJson(purchaseMap);
        try {
            Constructor purchaseConstructor = Purchase.class.getDeclaredConstructor(JSONObject.class);
            purchaseConstructor.setAccessible(true);
            Purchase purchase = (Purchase) purchaseConstructor.newInstance(purchaseJson);
            return purchase;
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
