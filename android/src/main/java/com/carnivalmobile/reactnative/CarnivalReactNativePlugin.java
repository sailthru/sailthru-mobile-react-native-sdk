package com.carnivalmobile.reactnative;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;

import com.carnival.sdk.AttributeMap;
import com.carnival.sdk.Carnival;
import com.carnival.sdk.CarnivalImpressionType;
import com.carnival.sdk.Message;
import com.carnival.sdk.MessageActivity;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class CarnivalReactNativePlugin extends ReactContextBaseJavaModule {

    private static final String IMPRESSION_TYPE_IN_APP_VIEW = "IMPRESSION_TYPE_IN_APP_VIEW";
    private static final String IMPRESSION_TYPE_STREAM_VIEW = "IMPRESSION_TYPE_STREAM_VIEW";
    private static final String IMPRESSION_TYPE_DETAIL_VIEW = "IMPRESSION_TYPE_DETAIL_VIEW";

    public CarnivalReactNativePlugin(ReactApplicationContext appContext) {
        super(appContext);
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(IMPRESSION_TYPE_IN_APP_VIEW, 0);
        constants.put(IMPRESSION_TYPE_STREAM_VIEW, 1);
        constants.put(IMPRESSION_TYPE_DETAIL_VIEW, 2);

        return constants;
    }

    @Override
    public String getName() {
        return "CarnivalReactNativePlugin";
    }

    @ReactMethod
    public void startEngine(String appKey) {
        Carnival.startEngine(getReactApplicationContext(), appKey);
    }

    @ReactMethod
    public void setTags(ReadableArray reactTags) {
        ArrayList<String> tags = new ArrayList<>(reactTags.size());
        for (int i = 0; 0 < reactTags.size(); i++) {
            tags.add(reactTags.getString(i));
        }
        Carnival.setTags(tags);
    }

    @ReactMethod
    public void getTags(final Promise promise) {
        Carnival.getTags(new Carnival.TagsHandler() {
            @Override
            public void onSuccess(List<String> list) {
                promise.resolve(list);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject("carnival.tags", error.getMessage());
            }
        });
    }

    @ReactMethod
    public void updateLocation(double latitude, double longitude) {
        Location location = new Location("React-Native");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        Carnival.updateLocation(location);
    }

    @ReactMethod
    public void getDeviceId(final Promise promise) {
        Carnival.getDeviceId(new Carnival.CarnivalHandler<String>() {
            @Override
            public void onSuccess(String s) {
                promise.resolve(s);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject("carnival.device", error.getMessage());
            }
        });
    }

    @ReactMethod
    public void logEvent(String value) {
        Carnival.logEvent(value);
    }

    @ReactMethod
    public void getMessages(final Promise promise) {
        Carnival.getMessages(new Carnival.MessagesHandler() {
            @Override
            public void onSuccess(ArrayList<Message> messages) {

                WritableArray array = new WritableNativeArray();
                try {
                    Method toJsonMethod = Message.class.getDeclaredMethod("toJSON");
                    toJsonMethod.setAccessible(true);

                    for (Message message : messages) {
                        JSONObject messageJson = (JSONObject) toJsonMethod.invoke(message);
                        array.pushMap(convertJsonToMap(messageJson));
                    }
                } catch (Exception e) {
                    promise.reject("carnival.messages", e.getMessage());
                }
                promise.resolve(array);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject("carnival.messages", error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setBoolean(String key, Boolean value, final Promise promise) {
        AttributeMap map = new AttributeMap();
        map.putBoolean(key, value);
        Carnival.setAttributes(map, new Carnival.AttributesHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject("carnival.attributes", error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setInteger(String key, int value, final Promise promise) {
        AttributeMap map = new AttributeMap();
        map.putInt(key, value);
        Carnival.setAttributes(map, new Carnival.AttributesHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject("carnival.attributes", error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setIntegers(String key, ReadableArray value, final Promise promise) {
        ArrayList<Integer> list = new ArrayList<>();

        for (int i = 0; i < value.size(); i++) {
            list.add(value.getInt(i));
        }

        AttributeMap map = new AttributeMap();
        map.putIntArray(key, list);
        Carnival.setAttributes(map, new Carnival.AttributesHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject("carnival.attributes", error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setFloat(String key, float value, final Promise promise) {
        AttributeMap map = new AttributeMap();
        map.putFloat(key, value);
        Carnival.setAttributes(map, new Carnival.AttributesHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject("carnival.attributes", error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setFloats(String key, ReadableArray value, final Promise promise) {
        ArrayList<Float> list = new ArrayList<>();

        for (int i = 0; i < value.size(); i++) {
            list.add((float) value.getDouble(i));
        }

        AttributeMap map = new AttributeMap();
        map.putFloatArray(key, list);
        Carnival.setAttributes(map, new Carnival.AttributesHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject("carnival.attributes", error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setString(String key, String value, final Promise promise) {
        AttributeMap map = new AttributeMap();
        map.putString(key, value);
        Carnival.setAttributes(map, new Carnival.AttributesHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject("carnival.attributes", error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setStrings(String key, ReadableArray value, final Promise promise) {
        ArrayList<String> list = new ArrayList<>();

        for (int i = 0; i < value.size(); i++) {
            list.add(value.getString(i));
        }

        AttributeMap map = new AttributeMap();
        map.putStringArray(key, list);
        Carnival.setAttributes(map, new Carnival.AttributesHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject("carnival.attributes", error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setDate(String key, int value, final Promise promise) {
        Date date = new Date(value * 1000);
        AttributeMap map = new AttributeMap();
        map.putDate(key, date);
        Carnival.setAttributes(map, new Carnival.AttributesHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject("carnival.attributes", error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setDates(String key, ReadableArray value, final Promise promise) {
        ArrayList<Date> list = new ArrayList<>();

        for (int i = 0; i < value.size(); i++) {

            list.add(new Date((long) value.getDouble(i) * 1000));
        }

        AttributeMap map = new AttributeMap();
        map.putDateArray(key, list);
        Carnival.setAttributes(map, new Carnival.AttributesHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(Error error) {
                promise.reject("carnival.attributes", error.getMessage());
            }
        });
    }

    @ReactMethod
    public void removeAttribute(String key) {
        Carnival.removeAttribute(key);
    }

    @ReactMethod
    public void setUserId(String userId) {
        Carnival.setUserId(userId, null);
    }

    @ReactMethod
    public void getUnreadCount(Promise promise) {
        int unreadCount = Carnival.getUnreadMessageCount();
        promise.resolve(unreadCount);
    }

    @ReactMethod
    public void removeMessage(ReadableMap messageMap) {
        Message message = getMessage(messageMap);
        Carnival.deleteMessage(message, null);
    }

    @ReactMethod
    public void registerMessageImpression(ReadableMap messageMap, int typeCode) {
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
                promise.reject("carnival.messages", error.getMessage());
            }
        });
    }



    @ReactMethod
    public void setInAppNotificationsEnabled(boolean enabled) {
        Carnival.setInAppNotificationsEnabled(enabled);
    }

    @ReactMethod
    public void showMessageDetail(String messageId) {
        Intent i = new Intent(getCurrentActivity(), MessageActivity.class);
        i.putExtra(Carnival.EXTRA_MESSAGE_ID, messageId);
        Activity activity = getCurrentActivity();
        if (activity != null) {
            getCurrentActivity().startActivity(i);
        }
    }

    /*
     * Helper Methods
     */

    private static WritableMap convertJsonToMap(JSONObject jsonObject) throws JSONException {
        WritableMap map = new WritableNativeMap();

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                map.putMap(key, convertJsonToMap((JSONObject) value));
            } else if (value instanceof  JSONArray) {
                map.putArray(key, convertJsonToArray((JSONArray) value));
            } else if (value instanceof  Boolean) {
                map.putBoolean(key, (Boolean) value);
            } else if (value instanceof  Integer) {
                map.putInt(key, (Integer) value);
            } else if (value instanceof  Double) {
                map.putDouble(key, (Double) value);
            } else if (value instanceof String)  {
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
            } else if (value instanceof  JSONArray) {
                array.pushArray(convertJsonToArray((JSONArray) value));
            } else if (value instanceof  Boolean) {
                array.pushBoolean((Boolean) value);
            } else if (value instanceof  Integer) {
                array.pushInt((Integer) value);
            } else if (value instanceof  Double) {
                array.pushDouble((Double) value);
            } else if (value instanceof String)  {
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

    private Message getMessage(ReadableMap messageMap) {

        Message message = null;
        try {
            JSONObject messageJson = convertMapToJson(messageMap);
            Constructor<Message> constructor;
            constructor = Message.class.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            message = constructor.newInstance(messageJson.toString());
        } catch (Exception e) {
            //wat
        }
        return message;
    }

}
