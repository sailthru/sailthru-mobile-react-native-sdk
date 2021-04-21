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
        doAnswer { TestMap() }.`when`(jsonConverter).createNativeMap()
        doAnswer { TestArray() }.`when`(jsonConverter).createNativeArray()
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

        assertEquals("thing", writableMap.getMap(TEST_KEY).getString("some"))
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

        assertEquals("test string", writableMap.getArray(TEST_KEY).getString(0))
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

        assertEquals("thing", writableArray.getMap(0).getString("some"))
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

        assertEquals("inner string", writableArray.getArray(0).getString(0))
    }


    /** Map -> JSON **/

    @Test
    fun testConvertMapToJsonNull() {
        val readableMap = TestMap().apply {
            map[TEST_KEY] = null
        }

        val converted = jsonConverter.convertMapToJson(readableMap)

        assertEquals(JSONObject.NULL, converted.get(TEST_KEY))
    }

    @Test
    fun testConvertMapToJsonBoolean() {
        val readableMap = TestMap().apply {
            map[TEST_KEY] = true
        }

        val converted = jsonConverter.convertMapToJson(readableMap)

        assertEquals(true, converted.getBoolean(TEST_KEY))
    }

    @Test
    fun testConvertMapToJsonInt() {
        val readableMap = TestMap().apply {
            map[TEST_KEY] = 123
        }

        val converted = jsonConverter.convertMapToJson(readableMap, false)

        assertEquals(123, converted.getInt(TEST_KEY))
    }

    @Test
    fun testConvertMapToJsonDouble() {
        val readableMap = TestMap().apply {
            map[TEST_KEY] = 1.23
        }

        val converted = jsonConverter.convertMapToJson(readableMap, true)

        assertEquals(1.23, converted.getDouble(TEST_KEY), 0.001)
    }

    @Test
    fun testConvertMapToJsonString() {
        val readableMap = TestMap().apply {
            map[TEST_KEY] = "test string"
        }

        val converted = jsonConverter.convertMapToJson(readableMap)

        assertEquals("test string", converted.getString(TEST_KEY))
    }

    @Test
    fun testConvertMapToJsonMap() {
        val readableMap = TestMap().apply {
            val innerMap = TestMap()
            innerMap.putString("inner key", "inner string")
            map[TEST_KEY] = innerMap
        }

        val converted = jsonConverter.convertMapToJson(readableMap)

        assertEquals("inner string", converted.getJSONObject(TEST_KEY).getString("inner key"))
    }

    @Test
    fun testConvertMapToJsonArray() {
        val readableMap = TestMap().apply {
            map[TEST_KEY] = TestArray().apply {
                pushString("inner string")
            }
        }

        val converted = jsonConverter.convertMapToJson(readableMap)

        assertEquals("inner string", converted.getJSONArray(TEST_KEY).getString(0))
    }


    /** Array -> JSON **/

    @Test
    fun testConvertArrayToJsonNull() {
        val readableArray = TestArray().apply {
            array.add(null)
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals(0, converted.length())
    }

    @Test
    fun testConvertArrayToJsonBoolean() {
        val readableArray = TestArray().apply {
            array.add(true)
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals(true, converted.getBoolean(0))
    }

    @Test
    fun testConvertArrayToJsonInt() {
        val readableArray = TestArray().apply {
            array.add(123)
        }

        val converted = jsonConverter.convertArrayToJson(readableArray, false)

        assertEquals(123, converted.getInt(0))
    }

    @Test
    fun testConvertArrayToJsonDouble() {
        val readableArray = TestArray().apply {
            array.add(1.23)
        }

        val converted = jsonConverter.convertArrayToJson(readableArray, true)

        assertEquals(1.23, converted.getDouble(0), 0.001)
    }

    @Test
    fun testConvertArrayToJsonString() {
        val readableArray = TestArray().apply {
            array.add("test string")
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals("test string", converted.getString(0))
    }

    @Test
    fun testConvertArrayToJsonMap() {
        val readableArray = TestArray().apply {
            array.add(TestMap().apply {
                putString("inner key", "inner string")
            })
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals("inner string", converted.getJSONObject(0).getString("inner key"))
    }

    @Test
    fun testConvertMapArrayJsonArray() {
        val readableArray = TestArray().apply {
            array.add(TestArray().apply {
                pushString("inner string")
            })
        }

        val converted = jsonConverter.convertArrayToJson(readableArray)

        assertEquals("inner string", converted.getJSONArray(0).getString(0))
    }
    
    companion object {
        const val TEST_KEY = "test key"
    }
}

/**
 * Test implementation only (implementations are not provided for unit tests)
 */
class TestMap: ReadableMap, WritableMap {
    val map = mutableMapOf<String, Any?>()

    override fun hasKey(name: String?): Boolean = map.containsKey(name)

    override fun isNull(name: String?): Boolean = map[name] == null

    override fun getBoolean(name: String?): Boolean {
        val value = map[name]
        return if (value is Boolean) value else false
    }

    override fun getDouble(name: String?): Double {
        val value = map[name]
        return if (value is Double) value else 0.0
    }

    override fun getInt(name: String?): Int {
        val value = map[name]
        return if (value is Int) value else 0
    }

    override fun getString(name: String?): String {
        val value = map[name]
        return if (value is String) value else ""
    }

    override fun getArray(name: String?): ReadableArray {
        val value = map[name]
        return if (value is ReadableArray) value else TestArray()
    }

    override fun getMap(name: String?): ReadableMap {
        val value = map[name]
        return if (value is ReadableMap) value else TestMap()
    }

    override fun getType(name: String?): ReadableType {
        return when (map[name]) {
            is Boolean -> ReadableType.Boolean
            is Double -> ReadableType.Number
            is Int -> ReadableType.Number
            is String -> ReadableType.String
            is ReadableArray -> ReadableType.Array
            is ReadableMap -> ReadableType.Map
            else -> ReadableType.Null
        }
    }

    override fun keySetIterator(): ReadableMapKeySetIterator = TestIterator(map.keys.iterator())

    override fun putNull(key: String?) {
        set(key, null)
    }

    override fun putBoolean(key: String?, value: Boolean) {
        set(key, value)
    }

    override fun putDouble(key: String?, value: Double) {
        set(key, value)
    }

    override fun putInt(key: String?, value: Int) {
        set(key, value)
    }

    override fun putString(key: String?, value: String?) {
        set(key, value)
    }

    override fun putArray(key: String?, value: WritableArray?) {
        set(key, value)
    }

    override fun putMap(key: String?, value: WritableMap?) {
        set(key, value)
    }

    override fun merge(source: ReadableMap?) {
        while (source?.keySetIterator()?.hasNextKey() == true) {
            val key = source.keySetIterator().nextKey()
            when (source.getType(key)) {
                ReadableType.Null -> map[key] = null
                ReadableType.Boolean -> map[key] = source.getBoolean(key)
                ReadableType.Number -> map[key] = source.getInt(key)
                ReadableType.String -> map[key] = source.getString(key)
                ReadableType.Array -> map[key] = source.getArray(key)
                ReadableType.Map -> map[key] = source.getMap(key)
                else -> continue
            }
        }
    }

    private fun set(key: String?, value: Any?) {
        key ?: return
        map[key] = value
    }

    inner class TestIterator(val iterator: Iterator<String>): ReadableMapKeySetIterator {
        override fun hasNextKey(): Boolean = iterator.hasNext()

        override fun nextKey(): String = iterator.next()

    }
}

/**
 * Test implementation only (implementations are not provided for unit tests)
 */
class TestArray: ReadableArray, WritableArray {
    val array = arrayListOf<Any?>()

    override fun size(): Int = array.size

    override fun isNull(index: Int): Boolean = array[index] == null

    override fun getBoolean(index: Int): Boolean {
        val value = array[index]
        return if (value is Boolean) value else false
    }

    override fun getDouble(index: Int): Double {
        val value = array[index]
        return if (value is Double) value else 0.0
    }

    override fun getInt(index: Int): Int {
        val value = array[index]
        return if (value is Int) value else 0
    }

    override fun getString(index: Int): String {
        val value = array[index]
        return if (value is String) value else ""
    }

    override fun getArray(index: Int): ReadableArray {
        val value = array[index]
        return if (value is ReadableArray) value else TestArray()
    }

    override fun getMap(index: Int): ReadableMap {
        val value = array[index]
        return if (value is ReadableMap) value else TestMap()
    }

    override fun getType(index: Int): ReadableType {
        return when (array[index]) {
            is Boolean -> ReadableType.Boolean
            is Double -> ReadableType.Number
            is Int -> ReadableType.Number
            is String -> ReadableType.String
            is ReadableArray -> ReadableType.Array
            is ReadableMap -> ReadableType.Map
            else -> ReadableType.Null
        }
    }

    override fun pushNull() {
        array.add(null)
    }

    override fun pushBoolean(value: Boolean) {
        array.add(value)
    }

    override fun pushDouble(value: Double) {
        array.add(value)
    }

    override fun pushInt(value: Int) {
        array.add(value)
    }

    override fun pushString(value: String?) {
        array.add(value)
    }

    override fun pushArray(array: WritableArray?) {
        this.array.add(array)
    }

    override fun pushMap(map: WritableMap?) {
       array.add(map)
    }

    override fun toString(): String {
        return array.toString()
    }
}