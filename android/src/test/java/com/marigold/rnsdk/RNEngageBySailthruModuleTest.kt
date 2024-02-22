package com.marigold.rnsdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.marigold.sdk.EngageBySailthru
import com.marigold.sdk.Marigold
import com.marigold.sdk.model.AttributeMap
import com.marigold.sdk.model.Purchase
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.net.URI
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class RNEngageBySailthruModuleTest {
    @Mock
    private lateinit var mockContext: ReactApplicationContext

    @Mock
    private lateinit var jsonConverter: JsonConverter

    @Mock
    private lateinit var staticEngageBySailthru: MockedConstruction<EngageBySailthru>

    @Mock
    private lateinit var engage: EngageBySailthru

    @Captor
    private lateinit var attributeCaptor: ArgumentCaptor<AttributeMap>

    private lateinit var rnEngageBySailthruModule: RNEngageBySailthruModule
    private lateinit var rnEngageBySailthruModuleSpy: RNEngageBySailthruModule

    @Before
    fun setup() {
        rnEngageBySailthruModule = RNEngageBySailthruModule(mockContext)
        rnEngageBySailthruModule.jsonConverter = jsonConverter
        rnEngageBySailthruModuleSpy = Mockito.spy(rnEngageBySailthruModule)
        // Mock instance creation of EngageBySailthru to return the mocked instance
        Mockito.`when`(rnEngageBySailthruModuleSpy.createEngageBySailthru()).thenReturn(engage)

    }

    @Test
    fun testLogEvent() {
        val event = "event string"

        // Call the method under test
        rnEngageBySailthruModuleSpy.logEvent(event)

        // Verify that logEvent is called with the expected event string
        verify(engage).logEvent(event)
    }

    @Test
    @Throws(Exception::class)
    fun testLogEventWithVars() {
        val event = "event string"
        val varsJson = JSONObject()
        varsJson.put("varKey", "varValue")

        // setup mocks
        val readableMap: ReadableMap = mock()

        // setup mocking
        Mockito.`when`(jsonConverter.convertMapToJson(readableMap)).thenReturn(varsJson)
        rnEngageBySailthruModuleSpy.logEvent(event, readableMap)
        verify(engage).logEvent(event, varsJson)
    }

    @Test
    @Throws(Exception::class)
    fun testLogEventWithVarsException() {
        val event = "event string"
        val jsonException: JSONException = mock()

        // setup mocks
        val readableMap: ReadableMap = mock()

        // setup mocking
        Mockito.`when`(jsonConverter.convertMapToJson(readableMap)).thenThrow(jsonException)
        rnEngageBySailthruModuleSpy.logEvent(event, readableMap)
        verify(jsonException).printStackTrace()
        verify(engage).logEvent(event, null)
    }

    @Test
    @Throws(Exception::class)
    fun testSetAttributes() {
        val stringAttributeJson = JSONObject()
                .put("type", "string")
                .put("value", "test string")
        val intAttributeJson = JSONObject()
                .put("type", "integer")
                .put("value", 123)
        val attributesJson = JSONObject()
                .put("string key", stringAttributeJson)
                .put("int key", intAttributeJson)
        val attributeMapJson = JSONObject()
                .put("mergeRule", AttributeMap.RULE_UPDATE)
                .put("attributes", attributesJson)
        val error = Error("test error")

        // setup mocks
        val promise: Promise = mock()
        val readableMap: ReadableMap = mock()

        // setup mocking for conversion from ReadableMap to JSON
        Mockito.`when`(jsonConverter.convertMapToJson(readableMap)).thenReturn(attributeMapJson)

        // Capture Attributes to verify
        val handlerCaptor = ArgumentCaptor.forClass(EngageBySailthru.AttributesHandler::class.java)

        // Initiate test
        rnEngageBySailthruModuleSpy.setAttributes(readableMap, promise)

        // Verify results
        verify(engage).setAttributes(capture(attributeCaptor), capture(handlerCaptor))
        val attributes: AttributeMap = attributeCaptor.value
        Assert.assertEquals(AttributeMap.RULE_UPDATE, attributes.getMergeRules())
        Assert.assertEquals("test string", attributes.getString("string key"))
        Assert.assertEquals(123, attributes.getInt("int key", 0))
        val handler: EngageBySailthru.AttributesHandler = handlerCaptor.value
        handler.onSuccess()
        verify(promise).resolve(null)
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, error.message)
    }

    @Test
    fun testSetUserId() {
        // Setup mocks
        val promise: Promise = mock()
        val error: Error = mock()
        val userID = "user ID"
        rnEngageBySailthruModuleSpy.setUserId(userID, promise)

        // Capture MarigoldHandler to verify behaviour
        @SuppressWarnings("unchecked")
        val argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).setUserId(eq(userID), capture(argumentCaptor) as Marigold.MarigoldHandler<Void?>?)
        val handler = argumentCaptor.value as Marigold.MarigoldHandler<Void?>

        // Test success handler
        handler.onSuccess(null)
        verify(promise).resolve(null)

        // Setup error
        val errorMessage = "error message"
        Mockito.`when`(error.message).thenReturn(errorMessage)

        // Test error handler
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
    }

    @Test
    fun testSetUserEmail() {
        // Setup mocks
        val promise: Promise = mock()
        val error: Error = mock()
        val userEmail = "user email"
        rnEngageBySailthruModuleSpy.setUserEmail(userEmail, promise)

        // Capture MarigoldHandler to verify behaviour
        @SuppressWarnings("unchecked")
        val argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).setUserEmail(eq(userEmail), capture(argumentCaptor) as Marigold.MarigoldHandler<Void?>?)
        val handler = argumentCaptor.value as Marigold.MarigoldHandler<Void?>

        // Test success handler
        handler.onSuccess(null)
        verify(promise).resolve(null)

        // Setup error
        val errorMessage = "error message"
        Mockito.`when`(error.message).thenReturn(errorMessage)

        // Test error handler
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
    }

    @Test
    fun testTrackClick() {
        // Create input
        val promise: Promise = mock()
        val sectionID = "Section ID"
        val urlString = "www.notarealurl.com"
        val error = Error("test error")

        // Initiate test
        rnEngageBySailthruModuleSpy.trackClick(sectionID, urlString, promise)

        // Capture arguments to verify behaviour
        val uriCaptor: ArgumentCaptor<URI> = ArgumentCaptor.forClass(URI::class.java)
        val handlerCaptor: ArgumentCaptor<EngageBySailthru.TrackHandler> = ArgumentCaptor.forClass(EngageBySailthru.TrackHandler::class.java)

        // Verify result
        verify(engage).trackClick(eq(sectionID), capture(uriCaptor), capture(handlerCaptor))
        val uri: URI = uriCaptor.value
        val trackHandler: EngageBySailthru.TrackHandler = handlerCaptor.value
        Assert.assertEquals(urlString, uri.toString())
        trackHandler.onSuccess()
        verify(promise).resolve(true)
        trackHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.message)
    }

    @Test
    fun testTrackClickException() {
        // Create input
        val promise: Promise = mock()
        val sectionID = "Section ID"
        val urlString = "Wrong URL Format"

        // Initiate test
        rnEngageBySailthruModule.trackClick(sectionID, urlString, promise)

        // Verify result
        verify(promise).reject(eq(RNMarigoldModule.ERROR_CODE_TRACKING), ArgumentMatchers.anyString())
        verify(engage, Mockito.times(0)).trackClick(any(), any(), any())
    }

    @Test
    @SuppressWarnings("unchecked")
    fun testTrackPageview() {
        // Create input
        val urlString = "www.notarealurl.com"
        val testTag = "some tag"
        val error = Error("test error")

        // Create mocks
        val promise: Promise = mock()
        val tagsArray: ReadableArray = mock()
        Mockito.`when`(tagsArray.size()).thenReturn(1)
        Mockito.`when`(tagsArray.getString(ArgumentMatchers.anyInt())).thenReturn(testTag)

        // Initiate test
        rnEngageBySailthruModuleSpy.trackPageview(urlString, tagsArray, promise)

        // Capture arguments to verify behaviour
        val uriCaptor: ArgumentCaptor<URI> = ArgumentCaptor.forClass(URI::class.java)
        val arrayCaptor: ArgumentCaptor<List<*>>? = ArgumentCaptor.forClass(List::class.java)
        val handlerCaptor: ArgumentCaptor<EngageBySailthru.TrackHandler> = ArgumentCaptor.forClass(EngageBySailthru.TrackHandler::class.java)

        // Verify result
        verify(engage).trackPageview(capture(uriCaptor), capture(arrayCaptor as ArgumentCaptor<List<String>>), capture(handlerCaptor))
        val uri = uriCaptor.value
        val tags = arrayCaptor.value
        val trackHandler = handlerCaptor.value
        Assert.assertEquals(urlString, uri.toString())
        Assert.assertEquals(testTag, tags[0])
        trackHandler.onSuccess()
        verify(promise).resolve(true)
        trackHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.message)
    }

    @Test
    fun testTrackPageviewNullTags() {
        // Create input
        val promise: Promise = mock()
        val urlString = "www.notarealurl.com"

        // Initiate test
        rnEngageBySailthruModuleSpy.trackPageview(urlString, null, promise)

        // Verify result
        verify(engage).trackPageview(any(), ArgumentMatchers.isNull(), any())
    }

    @Test
    fun testTrackPageviewException() {
        // Create input
        val promise: Promise = mock()
        val urlString = "Wrong URL Format"

        // Initiate test
        rnEngageBySailthruModule.trackPageview(urlString, null, promise)

        // Verify result
        verify(promise).reject(eq(RNMarigoldModule.ERROR_CODE_TRACKING), ArgumentMatchers.anyString())
        verify(engage, Mockito.times(0)).trackPageview(any(), ArgumentMatchers.anyList(), any())
    }

    @Test
    @SuppressWarnings("unchecked")
    fun testTrackImpression() {
        // Create input
        val promise: Promise = mock()
        val sectionID = "Section ID"
        val urlString = "www.notarealurl.com"
        val readableArray: ReadableArray = mock()
        val error = Error("test error")

        // Mock methods
        Mockito.doReturn(1).`when`(readableArray).size()
        Mockito.doReturn(urlString).`when`(readableArray).getString(any())

        // Initiate test
        rnEngageBySailthruModuleSpy.trackImpression(sectionID, readableArray, promise)

        // Capture arguments to verify behaviour
        val uriCaptor: ArgumentCaptor<List<*>>? = ArgumentCaptor.forClass(List::class.java)
        val handlerCaptor: ArgumentCaptor<EngageBySailthru.TrackHandler> = ArgumentCaptor.forClass(EngageBySailthru.TrackHandler::class.java)

        // Verify result
        verify(engage).trackImpression(eq(sectionID), capture(uriCaptor as ArgumentCaptor<List<URI>>), capture(handlerCaptor))
        val uriList = uriCaptor.value
        val trackHandler = handlerCaptor.value
        Assert.assertEquals(urlString, uriList[0].toString())
        trackHandler.onSuccess()
        verify(promise).resolve(true)
        trackHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.message)
    }

    @Test
    fun testTrackImpressionNullUrls() {
        // Create input
        val promise: Promise = mock()
        val sectionID = "Section ID"

        // Initiate test
        rnEngageBySailthruModuleSpy.trackImpression(sectionID, null, promise)

        // Verify result
        verify(engage).trackImpression(eq(sectionID), ArgumentMatchers.isNull(), any())
    }

    @Test
    fun testTrackImpressionException() {
        // Create input
        val promise: Promise = mock()
        val sectionID = "Section ID"
        val urlString = "Wrong URL Format"
        val readableArray: ReadableArray = mock()

        // Mock methods
        Mockito.doReturn(1).`when`(readableArray).size()
        Mockito.doReturn(urlString).`when`(readableArray).getString(any())

        // Initiate test
        rnEngageBySailthruModule.trackImpression(sectionID, readableArray, promise)

        // Verify result
        verify(promise).reject(eq(RNMarigoldModule.ERROR_CODE_TRACKING), ArgumentMatchers.anyString())
        verify(engage, Mockito.times(0)).trackImpression(any(), any(), any())
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testSetProfileVars() {
        val varsJson: JSONObject = JSONObject().put("test var", 123)

        // Create input
        val vars: ReadableMap = mock()
        val promise: Promise = mock()
        val error: Error = mock()

        // Mock methods
        Mockito.`when`(jsonConverter.convertMapToJson(vars)).thenReturn(varsJson)

        // Initiate test
        rnEngageBySailthruModuleSpy.setProfileVars(vars, promise)

        // Verify result
        @SuppressWarnings("unchecked") val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).setProfileVars(eq(varsJson), capture(argumentCaptor as ArgumentCaptor<Marigold.MarigoldHandler<Void?>>))
        val setVarsHandler = argumentCaptor.value

        // Test success handler
        setVarsHandler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        Mockito.`when`(error.message).thenReturn(errorMessage)

        // Test error handler
        setVarsHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_VARS, errorMessage)
    }

    @Test
    @SuppressWarnings("unchecked")
    @kotlin.Throws(Exception::class)
    fun testSetProfileVarsException() {
        val jsonException = JSONException("test exception")

        // Create input
        val vars: ReadableMap = mock()
        val promise: Promise = mock()

        // Mock methods
        Mockito.`when`(jsonConverter.convertMapToJson(vars)).thenThrow(jsonException)

        // Initiate test
        rnEngageBySailthruModuleSpy.setProfileVars(vars, promise)

        // Verify result
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_VARS, jsonException.message)
        verify(engage, Mockito.times(0)).setProfileVars(any(), any())
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testGetProfileVars() {
        // Create input
        val varsJson = JSONObject().put("test var", 123)
        val promise: Promise = mock()
        val error: Error = mock()
        val mockMap: WritableMap = mock()

        // Mock methods
        Mockito.`when`(jsonConverter.convertJsonToMap(varsJson)).thenReturn(mockMap)

        // Initiate test
        rnEngageBySailthruModuleSpy.getProfileVars(promise)

        // Verify result
        @SuppressWarnings("unchecked")
        val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).getProfileVars(capture(argumentCaptor as ArgumentCaptor<Marigold.MarigoldHandler<JSONObject?>>))
        val getVarsHandler = Mockito.spy(argumentCaptor.value)

        // Test success handler
        getVarsHandler.onSuccess(varsJson)
        verify(jsonConverter).convertJsonToMap(varsJson)
        verify(promise).resolve(mockMap)

        // Setup error
        val errorMessage = "error message"
        Mockito.`when`(error.message).thenReturn(errorMessage)

        // Test error handler
        getVarsHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_VARS, errorMessage)
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testLogPurchase() {
        // Create input
        val purchaseMap: ReadableMap = mock()
        val purchase: Purchase = mock()
        val promise: Promise = mock()
        val error: Error = mock()

        // Mock methods
        Mockito.doReturn(purchase).`when`(rnEngageBySailthruModuleSpy).getPurchaseInstance(purchaseMap, promise)

        // Initiate test
        rnEngageBySailthruModuleSpy.logPurchase(purchaseMap, promise)

        // Verify result
        @SuppressWarnings("unchecked")
        val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).logPurchase(eq(purchase), capture(argumentCaptor as ArgumentCaptor<Marigold.MarigoldHandler<Void?>>))
        val purchaseHandler = argumentCaptor.value

        // Test success handler
        purchaseHandler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        Mockito.`when`(error.message).thenReturn(errorMessage)

        // Test error handler
        purchaseHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, errorMessage)
    }

    @Test
    @SuppressWarnings("unchecked")
    @Throws(Exception::class)
    fun testLogPurchaseException() {
        // Create input
        val purchaseMap: ReadableMap = mock()
        val promise: Promise = mock()
        val jsonException = JSONException("test exception")

        // Mock methods
        Mockito.doThrow(jsonException).`when`(rnEngageBySailthruModuleSpy).getPurchaseInstance(purchaseMap, promise)
        // Initiate test
        assertThrows(JSONException::class.java) {
            rnEngageBySailthruModuleSpy.logPurchase(purchaseMap, promise)
            verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, jsonException.message)
        }
        // Verify result
        verify(engage, Mockito.times(0)).logPurchase(any(), any())
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun testLogAbandonedCart() {
        // Create input
        val purchaseMap: ReadableMap = mock()
        val purchase: Purchase = mock()
        val promise: Promise = mock()
        val error: Error = mock()

        // Mock methods
        Mockito.doReturn(purchase).`when`(rnEngageBySailthruModuleSpy).getPurchaseInstance(purchaseMap, promise)

        // Initiate test
        rnEngageBySailthruModuleSpy.logAbandonedCart(purchaseMap, promise)

        // Verify result
        @SuppressWarnings("unchecked")
        val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(engage).logAbandonedCart(eq(purchase), capture(argumentCaptor as ArgumentCaptor<Marigold.MarigoldHandler<Void?>>))
        val purchaseHandler = argumentCaptor.value

        // Test success handler
        purchaseHandler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        Mockito.`when`(error.message).thenReturn(errorMessage)

        // Test error handler
        purchaseHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, errorMessage)
    }

    @Test
    @SuppressWarnings("unchecked")
    @Throws(Exception::class)
    fun testLogAbandonedCartException() {
        // Create input
        val purchaseMap: ReadableMap = mock()
        val promise: Promise = mock()
        val jsonException = JSONException("test exception")

        // Mock methods
        Mockito.doThrow(jsonException).`when`(rnEngageBySailthruModuleSpy).getPurchaseInstance(purchaseMap, promise)
        // Initiate test
        assertThrows(JSONException::class.java) {
            rnEngageBySailthruModuleSpy.logAbandonedCart(purchaseMap, promise)
            verify(promise).reject(RNMarigoldModule.ERROR_CODE_PURCHASE, jsonException.message)
        }
        // Verify result
        verify(engage, Mockito.times(0)).logAbandonedCart(any(), any() as Marigold.MarigoldHandler<Void?>?)
    }

    @Test
    @Throws(Exception::class)
    fun testGetPurchaseInstancePositiveAdjustment() {
        // Mock methods
        val readableMap: ReadableMap = mock()
        val promise: Promise = mock()
        val purchaseJson = createPurchaseJson(234)
        Mockito.`when`(jsonConverter.convertMapToJson(readableMap, false)).thenReturn(purchaseJson)

        // Initiate test
        val purchase: Purchase? = rnEngageBySailthruModuleSpy.getPurchaseInstance(readableMap, promise)

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap, false)
        val item = purchase!!.purchaseItems[0]
        Assert.assertEquals(1, item.quantity)
        Assert.assertEquals("test title", item.title)
        Assert.assertEquals(123, item.price)
        Assert.assertEquals("456", item.ID)
        Assert.assertEquals(URI("http://mobile.sailthru.com"), item.url)
        val adjustment = purchase.purchaseAdjustments[0]
        Assert.assertEquals("tax", adjustment.title)
        Assert.assertEquals(234, adjustment.price)
    }

    @Test
    @Throws(Exception::class)
    fun testGetPurchaseInstanceNegativeAdjustment() {
        // Mock methods
        val readableMap: ReadableMap = mock()
        val promise: Promise = mock()
        val purchaseJson = createPurchaseJson(-234)
        Mockito.`when`(jsonConverter.convertMapToJson(readableMap, false)).thenReturn(purchaseJson)

        // Initiate test
        val purchase = rnEngageBySailthruModuleSpy.getPurchaseInstance(readableMap, promise)

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap, false)
        val item = purchase!!.purchaseItems[0]
        Assert.assertEquals(1, item.quantity)
        Assert.assertEquals("test title", item.title)
        Assert.assertEquals(123, item.price)
        Assert.assertEquals("456", item.ID)
        Assert.assertEquals(URI("http://mobile.sailthru.com"), item.url)
        val adjustment = purchase.purchaseAdjustments[0]
        Assert.assertEquals("tax", adjustment.title)
        Assert.assertEquals(-234, adjustment.price)
    }

    @Test
    @Throws(Exception::class)
    fun testGetAttributeMap() {
        val date = Date()

        // Mock methods
        val readableMap: ReadableMap = mock()
        val attributeJson = createAttributeMapJson(date)
        Mockito.`when`(jsonConverter.convertMapToJson(readableMap)).thenReturn(attributeJson)

        // Initiate test
        val attributeMap = rnEngageBySailthruModuleSpy.getAttributeMap(readableMap)

        // Verify result
        verify(jsonConverter).convertMapToJson(readableMap)
        Assert.assertEquals(AttributeMap.RULE_UPDATE, attributeMap.getMergeRules())
        Assert.assertEquals("test string", attributeMap.getString("stringKey"))
        Assert.assertEquals(123, attributeMap.getInt("integerKey", 0))
        Assert.assertTrue(attributeMap.getBoolean("booleanKey", false))
        Assert.assertEquals(1.23F, attributeMap.getFloat("floatKey", 0F), 0.001F)
        Assert.assertEquals(date, attributeMap.getDate("dateKey"))
    }

    /** Helpers  */
    @Throws(Exception::class)
    private fun createPurchaseJson(adjustmentPrice: Int): JSONObject {
        val adjustmentJson = JSONObject()
                .put("title", "tax")
                .put("price", adjustmentPrice)
        val adjustmentsArray = JSONArray()
                .put(adjustmentJson)
        val itemJson: JSONObject = JSONObject()
                .put("qty", 1)
                .put("title", "test title")
                .put("price", 123)
                .put("id", "456")
                .put("url", "http://mobile.sailthru.com")
        val itemsArray = JSONArray()
                .put(itemJson)
        return JSONObject()
                .put("items", itemsArray)
                .put("adjustments", adjustmentsArray)
    }

    @Throws(Exception::class)
    private fun createAttributeMapJson(date: Date): JSONObject {
        val stringObject = JSONObject()
                .put("type", "string")
                .put("value", "test string")
        val integerObject = JSONObject()
                .put("type", "integer")
                .put("value", 123)
        val booleanObject = JSONObject()
                .put("type", "boolean")
                .put("value", true)
        val floatObject = JSONObject()
                .put("type", "float")
                .put("value", 1.23)
        val dateObject = JSONObject()
                .put("type", "date")
                .put("value", date.time)
        val attributesJson = JSONObject()
                .put("stringKey", stringObject)
                .put("integerKey", integerObject)
                .put("booleanKey", booleanObject)
                .put("floatKey", floatObject)
                .put("dateKey", dateObject)
        return JSONObject()
                .put("attributes", attributesJson)
                .put("mergeRule", AttributeMap.RULE_UPDATE)
    }
}