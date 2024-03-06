package com.marigold.rnsdk

import android.app.Activity
import android.location.Location
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.marigold.sdk.Marigold
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*

@RunWith(MockitoJUnitRunner::class)
class RNMarigoldModuleTest {
    @Mock
    private lateinit var mockContext: ReactApplicationContext

    @Mock
    private lateinit var jsonConverter: JsonConverter

    @Mock
    private lateinit var staticMarigold: MockedConstruction<Marigold>

    private lateinit var marigold: Marigold

    private lateinit var rnMarigoldModule: RNMarigoldModule
    private lateinit var rnMarigoldModuleSpy: RNMarigoldModule

    @Before
    fun setup() {
        rnMarigoldModule = RNMarigoldModule(mockContext)
        rnMarigoldModule.jsonConverter = jsonConverter
        rnMarigoldModuleSpy = spy(rnMarigoldModule)

        marigold = staticMarigold.constructed()[0]
    }

    @Test
    fun testConstructor() {
        val mockCompanion: RNMarigoldModule.Companion = mock()
        mockCompanion.setWrapperInfo()
        verify(mockCompanion).setWrapperInfo()
    }

    @Test
    fun testUpdateLocation() {
        val latitude = 10.0
        val longitude = 10.0
        mockConstruction(Location::class.java).use { staticLocation ->
            rnMarigoldModule.updateLocation(latitude, longitude)
            val location = staticLocation.constructed()[0]
            verify(location).latitude = latitude
            verify(location).longitude = longitude
            verify(marigold).updateLocation(location)
        }
    }

    @Test
    fun testRegisterForPushNotifications() {
        val activity: Activity = mock()

        // Mock behaviour
        doReturn(activity).whenever(rnMarigoldModuleSpy).currentActivity()
        rnMarigoldModuleSpy.registerForPushNotifications()
        verify(marigold).requestNotificationPermission(activity)
    }

    @Test
    fun testRegisterForPushNotificationsNoActivity() {
        // Mock behaviour
        doReturn(null).whenever(rnMarigoldModuleSpy).currentActivity()
        rnMarigoldModuleSpy.registerForPushNotifications()
        verify(marigold, never()).requestNotificationPermission(any(), any(), any())
    }

    @Test
    fun testSyncNotificationSettings() {
        rnMarigoldModule.syncNotificationSettings()
        verify(marigold).syncNotificationSettings()
    }

    @Test
    fun testlogRegistrationEvent() {
        val userID = "device ID"
        rnMarigoldModule.logRegistrationEvent(userID)
        verify(marigold).logRegistrationEvent(userID)
    }

    @Test
    fun testGetDeviceID() {
        // Setup variables
        val deviceID = "device ID"
        val errorMessage = "error message"
        val promise: Promise = mock()
        val error: Error = mock()

        // Mock methods
        doReturn(errorMessage).whenever(error).message

        // Start test
        rnMarigoldModule.getDeviceID(promise)

        // Capture handler for verification
        @SuppressWarnings("unchecked")
        val argumentCaptor = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(marigold).getDeviceId(capture(argumentCaptor) as Marigold.MarigoldHandler<String?>?)
        val marigoldHandler = argumentCaptor.value as Marigold.MarigoldHandler<String>

        // Test success
        marigoldHandler.onSuccess(deviceID)
        verify(promise).resolve(deviceID)

        // Test failure
        marigoldHandler.onFailure(error)
        verify(promise).reject(RNMarigoldModule.ERROR_CODE_DEVICE, errorMessage)
    }

    @Test
    fun testSetGeoIPTrackingEnabled() {
        // Initiate test
        rnMarigoldModule.setGeoIPTrackingEnabled(true)

        // Verify result
        verify(marigold).setGeoIpTrackingEnabled(true)
    }

    @Test
    fun testSeGeoIPTrackingEnabledWithPromise() {
        // Create input
        val promise: Promise = mock()
        val error: Error = mock()

        // Initiate test
        rnMarigoldModule.setGeoIPTrackingEnabled(false, promise)

        // Verify result
        @SuppressWarnings("unchecked")
        val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(marigold).setGeoIpTrackingEnabled(eq(false), capture(argumentCaptor as ArgumentCaptor<Marigold.MarigoldHandler<Void?>>))
        val clearHandler = argumentCaptor.value

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
    fun testClearDevice() {
        // Create input
        val clearValue = Marigold.CLEAR_ALL
        val promise: Promise = mock()
        val error: Error = mock()

        // Initiate test
        rnMarigoldModule.clearDevice(clearValue, promise)

        // Verify result
        @SuppressWarnings("unchecked") val argumentCaptor: ArgumentCaptor<Marigold.MarigoldHandler<*>>? = ArgumentCaptor.forClass(Marigold.MarigoldHandler::class.java)
        verify(marigold).clearDevice(eq(clearValue), capture(argumentCaptor as ArgumentCaptor<Marigold.MarigoldHandler<Void?>>))
        val clearHandler = argumentCaptor.value

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
}
