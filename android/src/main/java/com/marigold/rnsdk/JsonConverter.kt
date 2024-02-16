package com.marigold.rnsdk

import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Class for handling conversion between Native Maps and Arrays and JSON Objects and Arrays.
 */
class JsonConverter {

    /**
     * Convert JSONObject to WritableMap
     */
    @Throws(JSONException::class)
    fun convertJsonToMap(jsonObject: JSONObject): WritableMap {
        val writableMap = createNativeMap()
        for (key in jsonObject.keys()) {
            when (val value = jsonObject[key]) {
                is JSONObject -> writableMap.putMap(key, convertJsonToMap(value))
                is JSONArray -> writableMap.putArray(key, convertJsonToArray(value))
                is Boolean -> writableMap.putBoolean(key, value)
                is Int -> writableMap.putInt(key, value)
                is Double -> writableMap.putDouble(key, value)
                is String ->  writableMap.putString(key, value)
                else -> writableMap.putString(key, value.toString())
            }
        }
        return writableMap
    }

    /**
     * Convert JSONArray to WritableArray
     */
    @Throws(JSONException::class)
    fun convertJsonToArray(jsonArray: JSONArray): WritableArray {
        val writableArray = createNativeArray()
        for (i in 0 until jsonArray.length()) {
            when (val value = jsonArray[i]) {
                is JSONObject -> writableArray.pushMap(convertJsonToMap(value))
                is JSONArray -> writableArray.pushArray(convertJsonToArray(value))
                is Boolean -> writableArray.pushBoolean(value)
                is Int -> writableArray.pushInt(value)
                is Double -> writableArray.pushDouble(value)
                is String -> writableArray.pushString(value)
                else ->  writableArray.pushString(value.toString())
            }
        }
        return writableArray
    }

    /**
     * Convert ReadableMap to JSONObject
     */
    @JvmOverloads
    @Throws(JSONException::class)
    fun convertMapToJson(readableMap: ReadableMap, doubleNumber: Boolean = true): JSONObject {
        val jsonObject = JSONObject()
        for (key in readableMap.toHashMap().keys) {
            val value = readableMap.getDynamic(key)
            when (value.type) {
                ReadableType.Null -> jsonObject.put(key, JSONObject.NULL)
                ReadableType.Boolean -> jsonObject.put(key, value.asBoolean())
                ReadableType.Number -> {
                    val numberValue = if (doubleNumber) value.asDouble() else value.asInt()
                    jsonObject.put(key, numberValue)
                }
                ReadableType.String -> jsonObject.put(key, value.asString())
                ReadableType.Map -> jsonObject.put(key, convertMapToJson(value.asMap(), doubleNumber))
                ReadableType.Array -> jsonObject.put(key, convertArrayToJson(value.asArray(), doubleNumber))
                else -> {
                }
            }
        }
        return jsonObject
    }

    /**
     * Convert ReadableArray to JSONArray
     */
    @JvmOverloads
    @Throws(JSONException::class)
    fun convertArrayToJson(readableArray: ReadableArray, doubleNumber: Boolean = true): JSONArray {
        val jsonArray = JSONArray()
        for (i in 0 until readableArray.size()) {
            val value = readableArray.getDynamic(i)
            when (value.type) {
                ReadableType.Null -> {
                }
                ReadableType.Boolean -> jsonArray.put(value.asBoolean())
                ReadableType.Number -> {
                    val numberValue = if (doubleNumber) value.asDouble() else value.asInt()
                    jsonArray.put(numberValue)
                }
                ReadableType.String -> jsonArray.put(value.asString())
                ReadableType.Map -> jsonArray.put(convertMapToJson(value.asMap(), doubleNumber))
                ReadableType.Array -> jsonArray.put(convertArrayToJson(value.asArray(), doubleNumber))
                else -> {
                }
            }
        }
        return jsonArray
    }

    @VisibleForTesting
    fun createNativeMap(): WritableMap {
        return WritableNativeMap()
    }

    @VisibleForTesting
    fun createNativeArray(): WritableArray {
        return WritableNativeArray()
    }
}