package com.sailthru.mobile.rnsdk

import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Class for handling conversion between Native Maps and Arrays and JSON Objects and Arrays.
 */
internal class JsonConverter {

    /**
     * Convert JSONObject to WritableMap
     */
    @Throws(JSONException::class)
    fun convertJsonToMap(jsonObject: JSONObject): WritableMap {
        val writableMap: WritableMap = createNativeMap()
        val iterator = jsonObject.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
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
        val writableArray: WritableArray = createNativeArray()
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
        val iterator = readableMap.keySetIterator()
        while (iterator.hasNextKey()) {
            val key = iterator.nextKey()
            when (readableMap.getType(key)) {
                ReadableType.Null -> jsonObject.put(key, JSONObject.NULL)
                ReadableType.Boolean -> jsonObject.put(key, readableMap.getBoolean(key))
                ReadableType.Number -> {
                    val numberValue = if (doubleNumber) readableMap.getDouble(key) else readableMap.getInt(key)
                    jsonObject.put(key, numberValue)
                }
                ReadableType.String -> jsonObject.put(key, readableMap.getString(key))
                ReadableType.Map -> readableMap.getMap(key)?.let { subMap ->
                    jsonObject.put(key, convertMapToJson(subMap, doubleNumber))
                }
                ReadableType.Array -> readableMap.getArray(key)?.let { subArray ->
                    jsonObject.put(key, convertArrayToJson(subArray, doubleNumber))
                }
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
            when (readableArray.getType(i)) {
                ReadableType.Null -> {
                }
                ReadableType.Boolean -> jsonArray.put(readableArray.getBoolean(i))
                ReadableType.Number -> {
                    val numberValue = if (doubleNumber) readableArray.getDouble(i) else readableArray.getInt(i)
                    jsonArray.put(numberValue)
                }
                ReadableType.String -> jsonArray.put(readableArray.getString(i))
                ReadableType.Map -> readableArray.getMap(i)?.let { subMap ->
                    jsonArray.put(convertMapToJson(subMap, doubleNumber))
                }
                ReadableType.Array -> readableArray.getArray(i)?.let { subArray ->
                    jsonArray.put(convertArrayToJson(subArray, doubleNumber))
                }
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