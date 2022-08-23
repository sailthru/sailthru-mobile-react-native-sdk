
package com.sailthru.mobile.rnsdk;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.facebook.react.bridge.WritableNativeArray;
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
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.jetbrains.annotations.NotNull;
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

    private final boolean displayInAppNotifications;

    private final ReactApplicationContext reactApplicationContext;

    @VisibleForTesting
    SailthruMobile sailthruMobile = new SailthruMobile();
    @VisibleForTesting
    MessageStream messageStream = new MessageStream();
    @VisibleForTesting
    JsonConverter jsonConverter = new JsonConverter();

    public RNSailthruMobileModule(ReactApplicationContext reactContext, boolean displayInAppNotifications) {
        super(reactContext);
        reactApplicationContext = reactContext;
        this.displayInAppNotifications = displayInAppNotifications;

        messageStream.setOnInAppNotificationDisplayListener(this);
        setWrapperInfo();
    }

    @Override
    public boolean shouldPresentInAppNotification(@NonNull Message message) {
        try {
            WritableMap writableMap = jsonConverter.convertJsonToMap(message.toJSON());
            reactApplicationContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("inappnotification", writableMap);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return displayInAppNotifications;
    }

    @Override
    public @NonNull String getName() {
        return "RNSailthruMobile";
    }

    protected static void setWrapperInfo() {
        try {
            Class<?>[] cArg = new Class[2];
            cArg[0] = String.class;
            cArg[1] = String.class;

            Method setWrapperMethod = SailthruMobile.Companion.getClass().getDeclaredMethod("setWrapper", cArg);
            setWrapperMethod.setAccessible(true);
            setWrapperMethod.invoke(SailthruMobile.Companion, "React Native", "7.0.1");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void registerForPushNotifications() {
        Activity activity = currentActivity();
        if (activity == null) return;

        sailthruMobile.requestNotificationPermission(activity);
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
    public void logEvent(String eventName, ReadableMap varsMap) {
        JSONObject varsJson = null;
        try {
            varsJson = jsonConverter.convertMapToJson(varsMap);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sailthruMobile.logEvent(eventName, varsJson);
    }

    @ReactMethod
    public void setAttributes(ReadableMap readableMap, final Promise promise) {
        AttributeMap attributeMap;
        try {
            attributeMap = getAttributeMap(readableMap);
        } catch(JSONException e) {
            promise.reject(ERROR_CODE_DEVICE, e.getMessage());
            return;
        }

        sailthruMobile.setAttributes(attributeMap, new SailthruMobile.AttributesHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage());
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
                        array.pushMap(jsonConverter.convertJsonToMap(messageJson));
                    }
                    promise.resolve(array);
                } catch (NoSuchMethodException | IllegalAccessException | JSONException | InvocationTargetException e) {
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

    @ReactMethod
    public void setUserId(String userId, final Promise promise) {
        sailthruMobile.setUserId(userId, new SailthruMobile.SailthruMobileHandler<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                promise.resolve(null);
            }

            @Override
            public void onFailure(@NotNull Error error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void setUserEmail(String userEmail, final Promise promise) {
        sailthruMobile.setUserEmail(userEmail, new SailthruMobile.SailthruMobileHandler<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                promise.resolve(null);
            }

            @Override
            public void onFailure(@NotNull Error error) {
                promise.reject(ERROR_CODE_DEVICE, error.getMessage());
            }
        });
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
    public void removeMessage(ReadableMap messageMap, final Promise promise) {
        Message message;
        try {
            message = getMessage(messageMap);
        } catch (JSONException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            promise.reject(ERROR_CODE_MESSAGES, e.getMessage());
            return;
        }

        messageStream.deleteMessage(message, new MessageStream.MessageDeletedHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(@NotNull Error error) {
                promise.reject(ERROR_CODE_MESSAGES, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void registerMessageImpression(int typeCode, ReadableMap messageMap) {
        ImpressionType type;
        switch (typeCode) {
            case 0: type = ImpressionType.IMPRESSION_TYPE_IN_APP_VIEW; break;
            case 1: type = ImpressionType.IMPRESSION_TYPE_STREAM_VIEW; break;
            case 2: type = ImpressionType.IMPRESSION_TYPE_DETAIL_VIEW; break;
            default: return;
        }

        Message message;
        try {
            message = getMessage(messageMap);
        } catch (JSONException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            return;
        }

        messageStream.registerMessageImpression(type, message);
    }

    @ReactMethod
    public void markMessageAsRead(ReadableMap messageMap, final Promise promise) {
        Message message;
        try {
            message = getMessage(messageMap);
        } catch (JSONException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            promise.reject(ERROR_CODE_MESSAGES, e.getMessage());
            return;
        }

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
        if (messageId == null || activity == null) return;

        Intent i = getMessageActivityIntent(activity, messageId);
        activity.startActivity(i);
    }

    // wrapped to expose for testing
    protected Activity currentActivity() {
        return getCurrentActivity();
    }

    // wrapped for testing
    protected Intent getMessageActivityIntent(@NotNull Activity activity, @NotNull String messageId) {
        return MessageActivity.intentForMessage(activity, null, messageId);
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
                        array.pushMap(jsonConverter.convertJsonToMap(contentItem.toJSON()));
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
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            promise.reject(ERROR_CODE_TRACKING, e.getMessage());
            return;
        }

        sailthruMobile.trackClick(sectionId, uri, new SailthruMobile.TrackHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(true);
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(ERROR_CODE_TRACKING, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void trackPageview(String url, ReadableArray tags, final Promise promise) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            promise.reject(ERROR_CODE_TRACKING, e.getMessage());
            return;
        }

        List<String> convertedTags = null;
        if (tags != null) {
            convertedTags = new ArrayList<>();
            for (int i = 0; i < tags.size(); i++) {
                convertedTags.add(tags.getString(i));
            }
        }

        sailthruMobile.trackPageview(uri, convertedTags, new SailthruMobile.TrackHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(true);
            }

            @Override
            public void onFailure(@NonNull Error error) {
                promise.reject(ERROR_CODE_TRACKING, error.getMessage());
            }
        });
    }

    @ReactMethod
    public void trackImpression(String sectionId, ReadableArray urls, final Promise promise) {
        List<URI> convertedUrls = null;
        if (urls != null) {
            try {
                convertedUrls = new ArrayList<>();
                for (int i = 0; i < urls.size(); i++) {
                    convertedUrls.add(new URI(urls.getString(i)));
                }
            } catch (URISyntaxException e) {
                promise.reject(ERROR_CODE_TRACKING, e.getMessage());
                return;
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
    public void setProfileVars(ReadableMap vars, final Promise promise) {
        JSONObject varsJson;
        try {
            varsJson = jsonConverter.convertMapToJson(vars);
        } catch (JSONException e) {
            promise.reject(ERROR_CODE_VARS, e.getMessage());
            return;
        }

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
                    WritableMap vars = jsonConverter.convertJsonToMap(jsonObject);
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
    public void logPurchase(ReadableMap purchaseMap, final Promise promise) {
        Purchase purchase;
        try {
            purchase = getPurchaseInstance(purchaseMap);
        } catch (JSONException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage());
            return;
        }

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

    @ReactMethod
    public void logAbandonedCart(ReadableMap purchaseMap, final Promise promise) {
        Purchase purchase;
        try {
            purchase = getPurchaseInstance(purchaseMap);
        } catch (JSONException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            promise.reject(ERROR_CODE_PURCHASE, e.getMessage());
            return;
        }

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

    @VisibleForTesting
    @NonNull Purchase getPurchaseInstance(ReadableMap purchaseMap) throws JSONException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        JSONObject purchaseJson = jsonConverter.convertMapToJson(purchaseMap, false);
        Constructor<Purchase> purchaseConstructor = Purchase.class.getDeclaredConstructor(JSONObject.class);
        purchaseConstructor.setAccessible(true);
        return purchaseConstructor.newInstance(purchaseJson);
    }

    /*
     * Helper Methods
     */

    protected @NonNull Message getMessage(ReadableMap messageMap) throws JSONException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        JSONObject messageJson = jsonConverter.convertMapToJson(messageMap);
        Constructor<Message> constructor = Message.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(messageJson.toString());
    }

    @VisibleForTesting
    @NonNull AttributeMap getAttributeMap(ReadableMap readableMap) throws JSONException {
        JSONObject attributeMapJson = jsonConverter.convertMapToJson(readableMap);
        JSONObject attributes = attributeMapJson.getJSONObject("attributes");
        AttributeMap attributeMap = new AttributeMap();
        attributeMap.setMergeRules(attributeMapJson.getInt("mergeRule"));

        Iterator<String> keys = attributes.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject attribute = attributes.getJSONObject(key);
            String attributeType = attribute.getString("type");
            switch (attributeType) {
                case "string":
                    attributeMap.putString(key, attribute.getString("value"));

                    break;
                case "stringArray": {
                    ArrayList<String> array = new ArrayList<>();
                    JSONArray values = attribute.getJSONArray("value");
                    for (int i = 0; i < values.length(); i++) {
                        array.add((String) values.get(i));
                    }

                    attributeMap.putStringArray(key, array);

                    break;
                }
                case "integer":
                    attributeMap.putInt(key, attribute.getInt("value"));

                    break;
                case "integerArray": {
                    ArrayList<Integer> array = new ArrayList<>();
                    JSONArray values = attribute.getJSONArray("value");
                    for (int i = 0; i < values.length(); i++) {
                        Integer j = values.getInt(i);
                        array.add(j);
                    }

                    attributeMap.putIntArray(key, array);

                    break;
                }
                case "boolean":
                    attributeMap.putBoolean(key, attribute.getBoolean("value"));

                    break;
                case "float":
                    attributeMap.putFloat(key, (float) attribute.getDouble("value"));

                    break;
                case "floatArray": {
                    ArrayList<Float> array = new ArrayList<>();
                    JSONArray values = attribute.getJSONArray("value");
                    for (int i = 0; i < values.length(); i++) {
                        Float value = Float.parseFloat(values.get(i).toString());
                        array.add(value);
                    }

                    attributeMap.putFloatArray(key, array);

                    break;
                }
                case "date":
                    Date value = new Date(attribute.getLong("value"));
                    attributeMap.putDate(key, value);

                    break;
                case "dateArray": {
                    ArrayList<Date> array = new ArrayList<>();
                    JSONArray values = attribute.getJSONArray("value");
                    for (int i = 0; i < values.length(); i++) {
                        long dateValue = values.getLong(i);
                        Date date = new Date(dateValue);
                        array.add(date);
                    }

                    attributeMap.putDateArray(key, array);
                    break;
                }
            }
        }
        return attributeMap;
    }
}
