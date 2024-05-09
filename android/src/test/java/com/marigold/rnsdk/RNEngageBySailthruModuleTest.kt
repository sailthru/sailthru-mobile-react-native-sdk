package com.marigold.rnsdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.marigold.sdk.EngageBySailthru
import com.marigold.sdk.Marigold
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
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URI

@RunWith(MockitoJUnitRunner::class)
class RNEngageBySailthruModuleTest {
    @Mock
    private lateinit var mockContext: ReactApplicationContext

    @Mock
    private lateinit var jsonConverter: JsonConverter

    @Mock
    private lateinit var engage: EngageBySailthru

    @Mock
    private lateinit var promise: Promise

    @Captor
    private lateinit var marigoldVoidCaptor: ArgumentCaptor<Marigold.MarigoldHandler<Void?>>

    @Captor
    private lateinit var marigoldJsonCaptor: ArgumentCaptor<Marigold.MarigoldHandler<JSONObject?>>

    @Captor
    private lateinit var stringListCaptor: ArgumentCaptor<List<String>>

    @Captor
    private lateinit var uriListCaptor: ArgumentCaptor<List<URI>>

    private lateinit var rnEngageBySailthruModule: RNEngageBySailthruModule
    private lateinit var rnEngageBySailthruModuleSpy: RNEngageBySailthruModule

    @Before
    fun setup() {
        rnEngageBySailthruModule = RNEngageBySailthruModule(mockContext)
        rnEngageBySailthruModule.jsonConverter = jsonConverter
        rnEngageBySailthruModuleSpy = Mockito.spy(rnEngageBySailthruModule)
        // Mock instance creation of EngageBySailthru to return the mocked instance
        doReturn(engage).whenever(rnEngageBySailthruModuleSpy).createEngageBySailthru()
        doReturn(engage).whenever(rnEngageBySailthruModuleSpy).createEngageBySailthru(promise)
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
        doReturn(varsJson).whenever(jsonConverter).convertMapToJson(readableMap)
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
        doThrow(jsonException).whenever(jsonConverter).convertMapToJson(readableMap)
        rnEngageBySailthruModuleSpy.logEvent(event, readableMap)
        verify(jsonException).printStackTrace()
        verify(engage).logEvent(event, null)
    }

    @Test
    @Throws(Exception::class)
    fun testClearEvents() {
        // Create input
        val error: Error = mock()

        // Initiate test
        rnEngageBySailthruModuleSpy.clearEvents(promise)

        // Verify result
        verify(engage).clearEvents(capture(marigoldVoidCaptor))
        val clearHandler = marigoldVoidCaptor.value

        // Test success handler
        clearHandler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

        // Test error handler
        clearHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
    }

    @Test
    fun testSetUserId() {
        // Setup mocks
        val error: Error = mock()
        val userID = "user ID"
        rnEngageBySailthruModuleSpy.setUserId(userID, promise)

        // Capture MarigoldHandler to verify behaviour
        verify(engage).setUserId(eq(userID), capture(marigoldVoidCaptor))
        val handler = marigoldVoidCaptor.value

        // Test success handler
        handler.onSuccess(null)
        verify(promise).resolve(null)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

        // Test error handler
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
    }

    @Test
    fun testSetUserEmail() {
        // Setup mocks
        val error: Error = mock()
        val userEmail = "user email"
        rnEngageBySailthruModuleSpy.setUserEmail(userEmail, promise)

        // Capture MarigoldHandler to verify behaviour
        verify(engage).setUserEmail(eq(userEmail), capture(marigoldVoidCaptor))
        val handler = marigoldVoidCaptor.value

        // Test success handler
        handler.onSuccess(null)
        verify(promise).resolve(null)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

        // Test error handler
        handler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
    }

    @Test
    fun testTrackClick() {
        // Create input
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
        val tagsArray: ReadableArray = mock()
        doReturn(1).whenever(tagsArray).size()
        doReturn(testTag).whenever(tagsArray).getString(ArgumentMatchers.anyInt())

        // Initiate test
        rnEngageBySailthruModuleSpy.trackPageview(urlString, tagsArray, promise)

        // Capture arguments to verify behaviour
        val uriCaptor: ArgumentCaptor<URI> = ArgumentCaptor.forClass(URI::class.java)
        val handlerCaptor: ArgumentCaptor<EngageBySailthru.TrackHandler> = ArgumentCaptor.forClass(EngageBySailthru.TrackHandler::class.java)

        // Verify result
        verify(engage).trackPageview(capture(uriCaptor), capture(stringListCaptor), capture(handlerCaptor))
        val uri = uriCaptor.value
        val tags = stringListCaptor.value
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
        val urlString = "www.notarealurl.com"

        // Initiate test
        rnEngageBySailthruModuleSpy.trackPageview(urlString, null, promise)

        // Verify result
        verify(engage).trackPageview(any(), ArgumentMatchers.isNull(), any())
    }

    @Test
    fun testTrackPageviewException() {
        // Create input
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
        val sectionID = "Section ID"
        val urlString = "www.notarealurl.com"
        val readableArray: ReadableArray = mock()
        val error = Error("test error")

        // Mock methods
        doReturn(1).whenever(readableArray).size()
        doReturn(urlString).whenever(readableArray).getString(any())

        // Initiate test
        rnEngageBySailthruModuleSpy.trackImpression(sectionID, readableArray, promise)

        // Capture arguments to verify behaviour
        val handlerCaptor: ArgumentCaptor<EngageBySailthru.TrackHandler> = ArgumentCaptor.forClass(EngageBySailthru.TrackHandler::class.java)

        // Verify result
        verify(engage).trackImpression(eq(sectionID), capture(uriListCaptor), capture(handlerCaptor))
        val uriList = uriListCaptor.value
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
        val sectionID = "Section ID"

        // Initiate test
        rnEngageBySailthruModuleSpy.trackImpression(sectionID, null, promise)

        // Verify result
        verify(engage).trackImpression(eq(sectionID), ArgumentMatchers.isNull(), any())
    }

    @Test
    fun testTrackImpressionException() {
        // Create input
        val sectionID = "Section ID"
        val urlString = "Wrong URL Format"
        val readableArray: ReadableArray = mock()

        // Mock methods
        doReturn(1).whenever(readableArray).size()
        doReturn(urlString).whenever(readableArray).getString(any())

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
        val error: Error = mock()

        // Mock methods
        doReturn(varsJson).whenever(jsonConverter).convertMapToJson(vars)

        // Initiate test
        rnEngageBySailthruModuleSpy.setProfileVars(vars, promise)

        // Verify result
        verify(engage).setProfileVars(eq(varsJson), capture(marigoldVoidCaptor))
        val setVarsHandler = marigoldVoidCaptor.value

        // Test success handler
        setVarsHandler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

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

        // Mock methods
        doThrow(jsonException).whenever(jsonConverter).convertMapToJson(vars)

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
        val error: Error = mock()
        val mockMap: WritableMap = mock()

        // Mock methods
        doReturn(mockMap).whenever(jsonConverter).convertJsonToMap(varsJson)

        // Initiate test
        rnEngageBySailthruModuleSpy.getProfileVars(promise)

        // Verify result
        verify(engage).getProfileVars(capture(marigoldJsonCaptor))
        val getVarsHandler = Mockito.spy(marigoldJsonCaptor.value)

        // Test success handler
        getVarsHandler.onSuccess(varsJson)
        verify(jsonConverter).convertJsonToMap(varsJson)
        verify(promise).resolve(mockMap)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

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
        val error: Error = mock()

        // Mock methods
        doReturn(purchase).whenever(rnEngageBySailthruModuleSpy).getPurchaseInstance(purchaseMap, promise)

        // Initiate test
        rnEngageBySailthruModuleSpy.logPurchase(purchaseMap, promise)

        // Verify result
        verify(engage).logPurchase(eq(purchase), capture(marigoldVoidCaptor))
        val purchaseHandler = marigoldVoidCaptor.value

        // Test success handler
        purchaseHandler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

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
        val jsonException = JSONException("test exception")

        // Mock methods
        doThrow(jsonException).whenever(rnEngageBySailthruModuleSpy).getPurchaseInstance(purchaseMap, promise)

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
        val error: Error = mock()

        // Mock methods
        doReturn(purchase).whenever(rnEngageBySailthruModuleSpy).getPurchaseInstance(purchaseMap, promise)

        // Initiate test
        rnEngageBySailthruModuleSpy.logAbandonedCart(purchaseMap, promise)

        // Verify result
        verify(engage).logAbandonedCart(eq(purchase), capture(marigoldVoidCaptor))
        val purchaseHandler = marigoldVoidCaptor.value

        // Test success handler
        purchaseHandler.onSuccess(null)
        verify(promise).resolve(true)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

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
        val jsonException = JSONException("test exception")

        // Mock methods
        doThrow(jsonException).whenever(rnEngageBySailthruModuleSpy).getPurchaseInstance(purchaseMap, promise)

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
        val purchaseJson = createPurchaseJson(234)
        doReturn(purchaseJson).whenever(jsonConverter).convertMapToJson(readableMap, false)

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
        val purchaseJson = createPurchaseJson(-234)
        doReturn(purchaseJson).whenever(jsonConverter).convertMapToJson(readableMap, false)

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
}