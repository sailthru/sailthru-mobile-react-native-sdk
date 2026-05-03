package com.marigold.rnsdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.marigold.rnsdk.ErrorCodes.Companion.ERROR_CODE_DEVICE
import com.marigold.sdk.Cheetah
import com.marigold.sdk.Marigold
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.capture
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class RNCheetahModuleTest {
    @Mock
    private lateinit var mockContext: ReactApplicationContext
    @Mock
    private lateinit var cheetah: Cheetah
    @Mock
    private lateinit var promise: Promise

    @Captor
    private lateinit var marigoldVoidCaptor: ArgumentCaptor<Marigold.MarigoldHandler<Void?>>

    private lateinit var rnCheetahModule: RNCheetahModule
    @Before
    fun setup() {
        rnCheetahModule = Mockito.spy(RNCheetahModule(mockContext))
        // Mock instance creation of Cheetah to return the mocked instance
        doReturn(cheetah).whenever(rnCheetahModule).createCheetah(promise)
    }

    @Test
    fun testLoginLogRegistrationEvent() {
        // Create input
        val userID = "device ID"
        val error: Error = mock()

        // Initiate test
        rnCheetahModule.logRegistrationEvent(userID, promise)

        // Verify result
        verify(cheetah).logRegistrationEvent(eq(userID), capture(marigoldVoidCaptor))
        val registrationHandler = marigoldVoidCaptor.value

        // Test success handler
        registrationHandler.onSuccess(null)
        verify(promise).resolve(null)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

        // Test error handler
        registrationHandler.onFailure(error)
        verify(promise).reject(ERROR_CODE_DEVICE, errorMessage)
    }

    @Test
    fun testLogoutLogRegistrationEvent() {
        // Create input
        val error: Error = mock()

        // Initiate test
        rnCheetahModule.logRegistrationEvent(null, promise)

        // Verify result
        verify(cheetah).logRegistrationEvent(isNull(), capture(marigoldVoidCaptor))
        val registrationHandler = marigoldVoidCaptor.value

        // Test success handler
        registrationHandler.onSuccess(null)
        verify(promise).resolve(null)

        // Setup error
        val errorMessage = "error message"
        doReturn(errorMessage).whenever(error).message

        // Test error handler
        registrationHandler.onFailure(error)
        verify(promise).reject(ERROR_CODE_DEVICE, errorMessage)
    }
}
