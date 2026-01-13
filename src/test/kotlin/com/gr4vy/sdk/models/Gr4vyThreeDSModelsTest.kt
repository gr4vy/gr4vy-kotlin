//
//  Gr4vyThreeDSModelsTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.models

import com.gr4vy.sdk.http.Gr4vyResponseParser
import com.gr4vy.sdk.responses.Gr4vyThreeDSecureResponse
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyThreeDSModelsTest {

    // MARK: - ACSRenderingType Tests

    @Test
    fun `test ACSRenderingType decoding with all fields including deviceUserInterfaceMode`() = runTest {
        val json = """
        {
            "acsInterface": "01",
            "acsUiTemplate": "04",
            "deviceUserInterfaceMode": "01"
        }
        """.trimIndent()
        
        val acsRenderingType = Gr4vyResponseParser.json.decodeFromString(ACSRenderingType.serializer(), json)
        
        assertEquals("01", acsRenderingType.acsInterface)
        assertEquals("04", acsRenderingType.acsUiTemplate)
        assertEquals("01", acsRenderingType.deviceUserInterfaceMode)
    }

    @Test
    fun `test ACSRenderingType decoding with missing deviceUserInterfaceMode`() = runTest {
        val json = """
        {
            "acsInterface": "01",
            "acsUiTemplate": "04"
        }
        """.trimIndent()
        
        val acsRenderingType = Gr4vyResponseParser.json.decodeFromString(ACSRenderingType.serializer(), json)
        
        assertEquals("01", acsRenderingType.acsInterface)
        assertEquals("04", acsRenderingType.acsUiTemplate)
        assertNull("deviceUserInterfaceMode should be null when missing", acsRenderingType.deviceUserInterfaceMode)
    }

    @Test
    fun `test ACSRenderingType decoding with null deviceUserInterfaceMode`() = runTest {
        val json = """
        {
            "acsInterface": "01",
            "acsUiTemplate": "04",
            "deviceUserInterfaceMode": null
        }
        """.trimIndent()
        
        val acsRenderingType = Gr4vyResponseParser.json.decodeFromString(ACSRenderingType.serializer(), json)
        
        assertEquals("01", acsRenderingType.acsInterface)
        assertEquals("04", acsRenderingType.acsUiTemplate)
        assertNull("deviceUserInterfaceMode should be null", acsRenderingType.deviceUserInterfaceMode)
    }

    // MARK: - Gr4vyChallengeResponse Tests

    @Test
    fun `test Gr4vyChallengeResponse decoding with deviceUserInterfaceMode present`() = runTest {
        val json = """
        {
            "server_transaction_id": "dbc51a89-48d9-2324-82cf-89263d2710a1",
            "acs_transaction_id": "99caa473-57db-1212-9ecc-02078ee5007c",
            "acs_reference_number": "XXX",
            "acs_rendering_type": {
                "acsInterface": "01",
                "acsUiTemplate": "04",
                "deviceUserInterfaceMode": "01"
            },
            "acs_signed_content": "XXX.XXXXXX"
        }
        """.trimIndent()
        
        val challenge = Gr4vyResponseParser.json.decodeFromString(Gr4vyChallengeResponse.serializer(), json)
        
        assertEquals("dbc51a89-48d9-2324-82cf-89263d2710a1", challenge.serverTransactionId)
        assertEquals("99caa473-57db-1212-9ecc-02078ee5007c", challenge.acsTransactionId)
        assertEquals("XXX", challenge.acsReferenceNumber)
        assertEquals("01", challenge.acsRenderingType.acsInterface)
        assertEquals("04", challenge.acsRenderingType.acsUiTemplate)
        assertEquals("01", challenge.acsRenderingType.deviceUserInterfaceMode)
        assertEquals("XXX.XXXXXX", challenge.acsSignedContent)
    }

    @Test
    fun `test Gr4vyChallengeResponse decoding with missing deviceUserInterfaceMode`() = runTest {
        val json = """
        {
            "server_transaction_id": "dbc51a89-48d9-2324-82cf-89263d2710a1",
            "acs_transaction_id": "99caa473-57db-1212-9ecc-02078ee5007c",
            "acs_reference_number": "XXX",
            "acs_rendering_type": {
                "acsInterface": "01",
                "acsUiTemplate": "04"
            },
            "acs_signed_content": "XXX.XXXXXX"
        }
        """.trimIndent()
        
        val challenge = Gr4vyResponseParser.json.decodeFromString(Gr4vyChallengeResponse.serializer(), json)
        
        assertEquals("dbc51a89-48d9-2324-82cf-89263d2710a1", challenge.serverTransactionId)
        assertEquals("99caa473-57db-1212-9ecc-02078ee5007c", challenge.acsTransactionId)
        assertEquals("XXX", challenge.acsReferenceNumber)
        assertEquals("01", challenge.acsRenderingType.acsInterface)
        assertEquals("04", challenge.acsRenderingType.acsUiTemplate)
        assertNull("deviceUserInterfaceMode should be null when missing", challenge.acsRenderingType.deviceUserInterfaceMode)
        assertEquals("XXX.XXXXXX", challenge.acsSignedContent)
    }

    // MARK: - Gr4vyThreeDSecureResponse Integration Tests

    @Test
    fun `test Gr4vyThreeDSecureResponse decoding with deviceUserInterfaceMode present`() = runTest {
        val json = """
        {
            "indicator": "CHALLENGE",
            "transaction_status": "C",
            "challenge": {
                "server_transaction_id": "dbc51a89-48d9-2324-82cf-89263d2710a1",
                "acs_transaction_id": "99caa473-57db-1212-9ecc-02078ee5007c",
                "acs_reference_number": "XXX",
                "acs_rendering_type": {
                    "acsInterface": "01",
                    "acsUiTemplate": "04",
                    "deviceUserInterfaceMode": "01"
                },
                "acs_signed_content": "XXX.XXXXXX"
            },
            "cardholder_info": null
        }
        """.trimIndent()
        
        val response = Gr4vyResponseParser.parse<Gr4vyThreeDSecureResponse>(json)
        
        assertEquals("CHALLENGE", response.indicator)
        assertEquals("C", response.transactionStatus)
        assertTrue("Should be challenge", response.isChallenge)
        assertFalse("Should not be frictionless", response.isFrictionless)
        assertFalse("Should not be error", response.isError)
        assertNull("Cardholder info should be null", response.cardholderInfo)
        
        // Verify challenge details
        response.challenge?.let { challenge ->
            assertEquals("dbc51a89-48d9-2324-82cf-89263d2710a1", challenge.serverTransactionId)
            assertEquals("99caa473-57db-1212-9ecc-02078ee5007c", challenge.acsTransactionId)
            assertEquals("XXX", challenge.acsReferenceNumber)
            assertEquals("01", challenge.acsRenderingType.acsInterface)
            assertEquals("04", challenge.acsRenderingType.acsUiTemplate)
            assertEquals("01", challenge.acsRenderingType.deviceUserInterfaceMode)
            assertEquals("XXX.XXXXXX", challenge.acsSignedContent)
        } ?: fail("Challenge should not be null")
    }

    @Test
    fun `test Gr4vyThreeDSecureResponse decoding with missing deviceUserInterfaceMode`() = runTest {
        val json = """
        {
            "indicator": "CHALLENGE",
            "transaction_status": "C",
            "challenge": {
                "server_transaction_id": "dbc51a89-48d9-2324-82cf-89263d2710a1",
                "acs_transaction_id": "99caa473-57db-1212-9ecc-02078ee5007c",
                "acs_reference_number": "XXX",
                "acs_rendering_type": {
                    "acsInterface": "01",
                    "acsUiTemplate": "04"
                },
                "acs_signed_content": "XXX.XXXXXX"
            },
            "cardholder_info": null
        }
        """.trimIndent()
        
        val response = Gr4vyResponseParser.parse<Gr4vyThreeDSecureResponse>(json)
        
        assertEquals("CHALLENGE", response.indicator)
        assertEquals("C", response.transactionStatus)
        assertTrue("Should be challenge", response.isChallenge)
        assertFalse("Should not be frictionless", response.isFrictionless)
        assertFalse("Should not be error", response.isError)
        assertNull("Cardholder info should be null", response.cardholderInfo)
        
        // Verify challenge details
        response.challenge?.let { challenge ->
            assertEquals("dbc51a89-48d9-2324-82cf-89263d2710a1", challenge.serverTransactionId)
            assertEquals("99caa473-57db-1212-9ecc-02078ee5007c", challenge.acsTransactionId)
            assertEquals("XXX", challenge.acsReferenceNumber)
            assertEquals("01", challenge.acsRenderingType.acsInterface)
            assertEquals("04", challenge.acsRenderingType.acsUiTemplate)
            assertNull("deviceUserInterfaceMode should be null when missing", challenge.acsRenderingType.deviceUserInterfaceMode)
            assertEquals("XXX.XXXXXX", challenge.acsSignedContent)
        } ?: fail("Challenge should not be null")
    }

    @Test
    fun `test Gr4vyThreeDSecureResponse frictionless flow`() = runTest {
        val json = """
        {
            "indicator": "FINISH",
            "transaction_status": "Y",
            "cardholder_info": "Additional info"
        }
        """.trimIndent()
        
        val response = Gr4vyResponseParser.parse<Gr4vyThreeDSecureResponse>(json)
        
        assertEquals("FINISH", response.indicator)
        assertEquals("Y", response.transactionStatus)
        assertTrue("Should be frictionless", response.isFrictionless)
        assertFalse("Should not be challenge", response.isChallenge)
        assertFalse("Should not be error", response.isError)
        assertNull("Challenge should be null for frictionless", response.challenge)
        assertEquals("Additional info", response.cardholderInfo)
    }

    @Test
    fun `test Gr4vyThreeDSecureResponse error flow`() = runTest {
        val json = """
        {
            "indicator": "ERROR",
            "transaction_status": "N"
        }
        """.trimIndent()
        
        val response = Gr4vyResponseParser.parse<Gr4vyThreeDSecureResponse>(json)
        
        assertEquals("ERROR", response.indicator)
        assertEquals("N", response.transactionStatus)
        assertTrue("Should be error", response.isError)
        assertFalse("Should not be frictionless", response.isFrictionless)
        assertFalse("Should not be challenge", response.isChallenge)
        assertNull("Challenge should be null for error", response.challenge)
    }

    // MARK: - Backward Compatibility Tests

    @Test
    fun `test backward compatibility with existing responses containing deviceUserInterfaceMode`() = runTest {
        // This test ensures that existing API responses that include deviceUserInterfaceMode
        // continue to work correctly after making the field optional
        val json = """
        {
            "indicator": "CHALLENGE",
            "transaction_status": "C",
            "challenge": {
                "server_transaction_id": "test-server-id",
                "acs_transaction_id": "test-acs-id",
                "acs_reference_number": "REF123",
                "acs_rendering_type": {
                    "acsInterface": "02",
                    "acsUiTemplate": "05",
                    "deviceUserInterfaceMode": "02"
                },
                "acs_signed_content": "signed.content.here"
            }
        }
        """.trimIndent()
        
        val response = Gr4vyResponseParser.parse<Gr4vyThreeDSecureResponse>(json)
        
        assertNotNull("Response should not be null", response)
        assertNotNull("Challenge should not be null", response.challenge)
        assertEquals("02", response.challenge?.acsRenderingType?.deviceUserInterfaceMode)
    }
}

