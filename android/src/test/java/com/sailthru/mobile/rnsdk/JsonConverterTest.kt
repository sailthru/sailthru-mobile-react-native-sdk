package com.sailthru.mobile.rnsdk

import com.facebook.react.bridge.*
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.spy
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class JsonConverterTest {
    private val jsonConverter = spy(JsonConverter())

    @Before
    fun setup() {
        doAnswer { JavaOnlyMap() }.`when`(jsonConverter).createNativeMap()
        doAnswer { JavaOnlyArray() }.`when`(jsonConverter).createNativeArray()
    }


    /** JSON -> Map **/

    @Test
    fun testConvertJsonToMapBoolean() {
        val jsonObject = JSONObject().apply {
            put(TEST_KEY, true)
        }

        val writableMap = jsonConverter.convertJsonToMap(jsonObject)

        assertEquals(true, writableMap.getBoolean(TEST_KEY))
    }

    @Test
    fun testConvertJsonToMapInt() {
        val jsonObject = JSONObject().apply {
            put(TEST_KEY, 123)
        }

        val writableMap = jsonConverter.convertJsonToMap(jsonObject)

        assertEquals(123, writableMap.getInt(TEST_KEY))
    }

    @Test
    fun testConvertJsonToMapDouble() {
        val jsonObject = JSONObject().apply {
            put(TEST_KEY, 1.23)
        }

        val writableMap = jsonConverter.convertJsonToMap(jsonObject)

        assertEquals(1.23, writableMap.getDouble(TEST_KEY), 0.001)
    }

    @Test
    fun testConvertJsonToMapString() {
        val jsonObject = JSONObject().apply {
            put(TEST_KEY, "test string")
        }

        val writableMap = jsonConverter.convertJsonToMap(jsonObject)

        assertEquals("test string", writableMap.getString(TEST_KEY))
    }

    @Test
    fun testConvertJsonToMapMap() {
        val subObject = JSONObject().apply {
            put("some", "thing")
        }
        val jsonObject = JSONObject().apply {
            put(TEST_KEY, subObject)
        }

        val writableMap = jsonConverter.convertJsonToMap(jsonObject)

        assertEquals("thing", writableMap.getMap(TEST_KEY)?.getString("some"))
    }

    @Test
    fun testConvertJsonToMapArray() {
        val subArray = JSONArray().apply {
            put("test string")
        }
        val jsonObject = JSONObject().apply {
            put(TEST_KEY, subArray)
        }

        val writableMap = jsonConverter.convertJsonToMap(jsonObject)

        assertEquals("test string", writableMap.getArray(TEST_KEY)?.getString(0))
    }

    @Test
    fun testConvertJsonToMapFull() {
        val subObject = JSONObject().apply {
            put("some", "thing")
        }
        val subArray = JSONArray().apply {
            put("inner string")
        }
        val jsonObject = JSONObject().apply {
            put("booleanKey", true)
            put("intKey", 123)
            put("doubleKey", 1.23)
            put("stringKey", "test string")
            put("mapKey", subObject)
            put("arrayKey", subArray)
        }

        val writableMap = jsonConverter.convertJsonToMap(jsonObject)

        assertEquals(true, writableMap.getBoolean("booleanKey"))
        assertEquals(123, writableMap.getInt("intKey"))
        assertEquals(1.23, writableMap.getDouble("doubleKey"), 0.001)
        assertEquals("test string", writableMap.getString("stringKey"))
        assertEquals("thing", writableMap.getMap("mapKey")?.getString("some"))
        assertEquals("inner string", writableMap.getArray("arrayKey")?.getString(0))
    }


    /** JSON -> Array **/

    @Test
    fun testConvertJsonToArrayBoolean() {
        val jsonArray = JSONArray().apply {
            put(true)
        }

        val writableArray = jsonConverter.convertJsonToArray(jsonArray)

        assertEquals(true, writableArray.getBoolean(0))
    }

    @Test
    fun testConvertJsonToArrayInt() {
        val jsonArray = JSONArray().apply {
            put(123)
        }

        val writableArray = jsonConverter.convertJsonToArray(jsonArray)

        assertEquals(123, writableArray.getInt(0))
    }

    @Test
    fun testConvertJsonToArrayDouble() {
        val jsonArray = JSONArray().apply {
            put(1.23)
        }

        val writableArray = jsonConverter.convertJsonToArray(jsonArray)

        assertEquals(1.23, writableArray.getDouble(0), 0.001)
    }

    @Test
    fun testConvertJsonToArrayBooleanString() {
        val jsonArray = JSONArray().apply {
            put("test string")
        }

        val writableArray = jsonConverter.convertJsonToArray(jsonArray)

        assertEquals("test string", writableArray.getString(0))
    }

    @Test
    fun testConvertJsonToArrayMap() {
        val subObject = JSONObject().apply {
            put("some", "thing")
        }
        val jsonArray = JSONArray().apply {
            put(subObject)
        }

        val writableArray = jsonConverter.convertJsonToArray(jsonArray)

        assertEquals("thing", writableArray.getMap(0)?.getString("some"))
    }

    @Test
    fun testConvertJsonToArrayArray() {
        val subArray = JSONArray().apply {
            put("inner string")
        }
        val jsonArray = JSONArray().apply {
            put(subArray)
        }

        val writableArray = jsonConverter.convertJsonToArray(jsonArray)

        assertEquals("inner string", writableArray.getArray(0)?.getString(0))
    }

    @Test
    fun testConvertJsonToArrayFull() {
        val subObject = JSONObject().apply {
            put("some", "thing")
        }
        val subArray = JSONArray().apply {
            put("inner string")
        }
        val jsonArray = JSONArray().apply {
            put(true)
            put(123)
            put(1.23)
            put("test string")
            put(subObject)
            put(subArray)
        }

        val writableArray = jsonConverter.convertJsonToArray(jsonArray)

        assertEquals(true, writableArray.getBoolean(0))
        assertEquals(123, writableArray.getInt(1))
        assertEquals(1.23, writableArray.getDouble(2), 0.001)
        assertEquals("test string", writableArray.getString(3))
        assertEquals("thing", writableArray.getMap(4)?.getString("some"))
        assertEquals("inner string", writableArray.getArray(5)?.getString(0))
    }


    /** Map -> JSON **/

    @Test
    fun testConvertMapToJsonNull() {
        val readableMap = JavaOnlyMap().apply {
            putNull(TEST_KEY)
        }

        val converted = jsonConverter.convertMapToJson(readableMap)

        assertEquals(JSONObject.NULL, converted.get(TEST_KEY))
    }

    @Test
    fun testConvertMapToJsonBoolean() {
        val readableMap = JavaOnlyMap().apply {
            putBoolean(TEST_KEY, true)
        }

        val converted = jsonConverter.convertMapToJson(readableMap)

        assertEquals(true, converted.getBoolean(TEST_KEY))
    }

    @Test
    fun testConvertMapToJsonInt() {
        val readableMap = JavaOnlyMap().apply {
            putInt(TEST_KEY, 123)
        }

        val converted = jsonConverter.convertMapToJson(readableMap, false)

        assertEquals(123, converted.getInt(TEST_KEY))
    }

    @Test
    fun testConvertMapToJsonDouble() {
        val readableMap = JavaOnlyMap().apply {
            putDouble(TEST_KEY, 1.23)
        }

        val converted = jsonConverter.convertMapToJson(readableMap, true)

        assertEquals(1.23, converted.getDouble(TEST_KEY), 0.001)
    }

    @Test
    fun testConvertMapToJsonString() {
        val readableMap = JavaOnlyMap().apply {
            putString(TEST_KEY, "test string")
        }

        val converted = jsonConverter.convertMapToJson(readableMap)

        assertEquals("test string", converted.getString(TEST_KEY))
    }

    @Test
    fun testConvertMapToJsonMap() {
        val readableMap = JavaOnlyMap().also { map ->
            map.putMap(TEST_KEY, JavaOnlyMap().apply {
                putString("inner key", "inner string")
            })
        }

        val converted = jsonConverter.convertMapToJson(readableMap)

        assertEquals("inner string", converted.getJSONObject(TEST_KEY).getString("inner key"))
    }

    @Test
    fun testConvertMapToJsonArray() {
        val readableMap = JavaOnlyMap().also { map ->
            map.putArray(TEST_KEY, JavaOnlyArray().apply {
                pushString("inner string")
            })
        }

        val converted = jsonConverter.convertMapToJson(readableMap)

        assertEquals("inner string", converted.getJSONArray(TEST_KEY).getString(0))
    }

    @Test
    fun testConvertMapToJsonFull() {
        val readableMap = JavaOnlyMap().also { map ->
            map.putNull("nullKey")
            map.putBoolean("booleanKey", true)
            map.putInt("intKey", 123)
            map.putDouble("doubleKey", 1.23)
            map.putString("stringKey", "test string")
            map.putMap("mapKey", JavaOnlyMap().apply {
                putString("inner key", "inner string")
            })
            map.putArray("arrayKey", JavaOnlyArray().apply {
                pushString("inner string")
            })
        }

        val converted = jsonConverter.convertMapToJson(readableMap)

        assertEquals(JSONObject.NULL, converted.get("nullKey"))
        assertEquals(true, converted.getBoolean("booleanKey"))
        assertEquals(123, converted.getInt("intKey"))
        assertEquals(1.23, converted.getDouble("doubleKey"), 0.001)
        assertEquals("test string", converted.getString("stringKey"))
        assertEquals("inner string", converted.getJSONObject("mapKey").getString("inner key"))
        assertEquals("inner string", converted.getJSONArray("arrayKey").getString(0))
    }


    /** Array -> JSON **/

    @Test
    fun testConvertArrayToJsonNull() {
        val readableArray = JavaOnlyArray().apply {
            pushNull()
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals(0, converted.length())
    }

    @Test
    fun testConvertArrayToJsonBoolean() {
        val readableArray = JavaOnlyArray().apply {
            pushBoolean(true)
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals(true, converted.getBoolean(0))
    }

    @Test
    fun testConvertArrayToJsonInt() {
        val readableArray = JavaOnlyArray().apply {
            pushInt(123)
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals(123, converted.getInt(0))
    }

    @Test
    fun testConvertArrayToJsonDouble() {
        val readableArray = JavaOnlyArray().apply {
            pushDouble(1.23)
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals(1.23, converted.getDouble(0), 0.001)
    }

    @Test
    fun testConvertArrayToJsonString() {
        val readableArray = JavaOnlyArray().apply {
            pushString("test string")
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals("test string", converted.getString(0))
    }

    @Test
    fun testConvertArrayToJsonMap() {
        val readableArray = JavaOnlyArray().also { array ->
            array.pushMap(JavaOnlyMap().apply {
                putString("inner key", "inner string")
            })
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals("inner string", converted.getJSONObject(0).getString("inner key"))
    }

    @Test
    fun testConvertArrayToJsonArray() {
        val readableArray = JavaOnlyArray().also { array ->
            array.pushArray(JavaOnlyArray().apply {
                pushString("inner string")
            })
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals("inner string", converted.getJSONArray(0).getString(0))
    }

    @Test
    fun testConvertArrayToJsonFull() {
        val readableArray = JavaOnlyArray().also { array ->
            array.pushNull()
            array.pushBoolean(true)
            array.pushInt(123)
            array.pushDouble(1.23)
            array.pushString("test string")
            array.pushMap(JavaOnlyMap().apply {
                putString("inner key", "inner string")
            })
            array.pushArray(JavaOnlyArray().apply {
                pushString("inner string")
            })
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals(6, converted.length())
        assertEquals(true, converted.getBoolean(0))
        assertEquals(123, converted.getInt(1))
        assertEquals(1.23, converted.getDouble(2), 0.001)
        assertEquals("test string", converted.getString(3))
        assertEquals("inner string", converted.getJSONObject(4).getString("inner key"))
        assertEquals("inner string", converted.getJSONArray(5).getString(0))
    }
    
    companion object {
        const val TEST_KEY = "test key"
    }
}

