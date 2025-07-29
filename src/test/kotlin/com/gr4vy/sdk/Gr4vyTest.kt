//
//  Gr4vyTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyTest {

    @Test
    fun `initialization with valid parameters succeeds`() {
        val gr4vy = Gr4vy(
            gr4vyId = "test-merchant",
            token = "test-token",
            merchantId = "test-merchant-id", 
            server = Gr4vyServer.SANDBOX,
            timeout = 30.0,
            debugMode = true
        )
        
        val setup = gr4vy.setup
        assertNotNull(setup)
        assertEquals("test-merchant", setup!!.gr4vyId)
        assertEquals("test-token", setup.token)
        assertEquals("test-merchant-id", setup.merchantId)
        assertEquals(Gr4vyServer.SANDBOX, setup.server)
        assertEquals(30.0, setup.timeout, 0.001)
        
        // Verify services are initialized
        assertNotNull(gr4vy.paymentOptions)
        assertNotNull(gr4vy.cardDetails)
        assertNotNull(gr4vy.paymentMethods)
    }

    @Test
    fun `initialization with empty gr4vyId throws InvalidGr4vyId`() {
        try {
            Gr4vy(
                gr4vyId = "",
                token = "test-token",
                server = Gr4vyServer.SANDBOX
            )
            fail("Expected Gr4vyError.InvalidGr4vyId to be thrown")
        } catch (e: Gr4vyError.InvalidGr4vyId) {
            // Expected exception
        }
    }

    @Test
    fun `updateToken updates setup configuration`() = runTest {
        val gr4vy = Gr4vy(
            gr4vyId = "test-merchant",
            token = "original-token",
            server = Gr4vyServer.SANDBOX
        )
        
        val originalSetup = gr4vy.setup
        assertNotNull(originalSetup)
        assertEquals("original-token", originalSetup!!.token)
        
        // Update token
        gr4vy.updateToken("new-token")
        
        val updatedSetup = gr4vy.setup
        assertNotNull(updatedSetup)
        assertEquals("new-token", updatedSetup!!.token)
        
        // Verify other properties remain unchanged
        assertEquals("test-merchant", updatedSetup.gr4vyId)
        assertEquals(Gr4vyServer.SANDBOX, updatedSetup.server)
    }

    @Test
    fun `updateMerchantId updates setup configuration`() = runTest {
        val gr4vy = Gr4vy(
            gr4vyId = "test-merchant",
            token = "test-token",
            merchantId = "original-merchant-id",
            server = Gr4vyServer.SANDBOX
        )
        
        val originalSetup = gr4vy.setup
        assertNotNull(originalSetup)
        assertEquals("original-merchant-id", originalSetup!!.merchantId)
        
        // Update merchant ID
        gr4vy.updateMerchantId("new-merchant-id")
        
        val updatedSetup = gr4vy.setup
        assertNotNull(updatedSetup)
        assertEquals("new-merchant-id", updatedSetup!!.merchantId)
        
        // Verify other properties remain unchanged
        assertEquals("test-merchant", updatedSetup.gr4vyId)
        assertEquals("test-token", updatedSetup.token)
        assertEquals(Gr4vyServer.SANDBOX, updatedSetup.server)
    }

    @Test
    fun `concurrent token updates are thread-safe`() = runTest {
        val gr4vy = Gr4vy(
            gr4vyId = "test-merchant",
            token = "initial-token",
            server = Gr4vyServer.SANDBOX,
            debugMode = false // Disable debug to avoid log noise
        )
        
        val updateCount = 100
        val completedUpdates = AtomicInteger(0)
        val tokens = mutableSetOf<String>()
        
        // Launch multiple concurrent token updates
        val jobs = List(updateCount) { index ->
            async {
                val newToken = "token-$index"
                gr4vy.updateToken(newToken)
                completedUpdates.incrementAndGet()
                synchronized(tokens) {
                    tokens.add(newToken)
                }
            }
        }
        
        // Wait for all updates to complete
        jobs.awaitAll()
        
        // Verify all updates completed
        assertEquals(updateCount, completedUpdates.get())
        assertEquals(updateCount, tokens.size)
        
        // Verify final state is consistent (setup should have one of the tokens)
        val finalSetup = gr4vy.setup
        assertNotNull(finalSetup)
        assertNotNull(finalSetup!!.token)
        assertTrue("Final token should be one of the updated tokens", 
                   tokens.contains(finalSetup.token))
    }

    @Test 
    fun `concurrent merchantId updates are thread-safe`() = runTest {
        val gr4vy = Gr4vy(
            gr4vyId = "test-merchant",
            token = "test-token",
            merchantId = "initial-merchant",
            server = Gr4vyServer.SANDBOX,
            debugMode = false // Disable debug to avoid log noise
        )
        
        val updateCount = 100
        val completedUpdates = AtomicInteger(0)
        val merchantIds = mutableSetOf<String>()
        
        // Launch multiple concurrent merchant ID updates
        val jobs = List(updateCount) { index ->
            async {
                val newMerchantId = "merchant-$index"
                gr4vy.updateMerchantId(newMerchantId)
                completedUpdates.incrementAndGet()
                synchronized(merchantIds) {
                    merchantIds.add(newMerchantId)
                }
            }
        }
        
        // Wait for all updates to complete
        jobs.awaitAll()
        
        // Verify all updates completed
        assertEquals(updateCount, completedUpdates.get())
        assertEquals(updateCount, merchantIds.size)
        
        // Verify final state is consistent
        val finalSetup = gr4vy.setup
        assertNotNull(finalSetup)
        assertNotNull(finalSetup!!.merchantId)
        assertTrue("Final merchant ID should be one of the updated IDs",
                   merchantIds.contains(finalSetup.merchantId))
    }

    @Test
    fun `mixed concurrent updates maintain consistency`() = runTest {
        val gr4vy = Gr4vy(
            gr4vyId = "test-merchant",
            token = "initial-token",
            merchantId = "initial-merchant",
            server = Gr4vyServer.SANDBOX,
            debugMode = false
        )
        
        val updateCount = 50
        val latch = CountDownLatch(updateCount * 2) // Token + MerchantId updates
        
        // Launch mixed concurrent updates
        val jobs = mutableListOf<Deferred<Unit>>()
        
        repeat(updateCount) { index ->
            // Token update
            jobs.add(async {
                gr4vy.updateToken("token-$index")
                latch.countDown()
            })
            
            // Merchant ID update  
            jobs.add(async {
                gr4vy.updateMerchantId("merchant-$index")
                latch.countDown()
            })
        }
        
        // Wait for all updates
        jobs.awaitAll()
        assertEquals(0, latch.count)
        
        // Verify final state is consistent
        val finalSetup = gr4vy.setup
        assertNotNull(finalSetup)
        assertNotNull(finalSetup?.token)
        assertNotNull(finalSetup?.merchantId)
        
        // The token and merchant ID should be from the same or different updates,
        // but the setup object should be internally consistent
        assertEquals("test-merchant", finalSetup!!.gr4vyId)
        assertEquals(Gr4vyServer.SANDBOX, finalSetup.server)
    }

    @Test
    fun `setup property provides thread-safe read access`() = runTest {
        val gr4vy = Gr4vy(
            gr4vyId = "test-merchant",
            token = "test-token",
            server = Gr4vyServer.SANDBOX
        )
        
        val readCount = 1000
        val setupReads = mutableSetOf<String>()
        
        // Start continuous updates in background
        val updateJob = async {
            repeat(50) { index ->
                gr4vy.updateToken("token-$index")
                delay(1) // Small delay between updates
            }
        }
        
        // Concurrent reads of setup property
        val readJobs = List(readCount) {
            async {
                val currentSetup = gr4vy.setup
                currentSetup?.let { setup ->
                    synchronized(setupReads) {
                        setupReads.add("${setup.gr4vyId}:${setup.token}:${setup.server}")
                    }
                }
            }
        }
        
        // Wait for all operations
        readJobs.awaitAll()
        updateJob.await()
        
        // Verify we got consistent reads (no null or corrupted states)
        assertTrue("Should have recorded setup reads", setupReads.isNotEmpty())
        setupReads.forEach { setupString ->
            assertTrue("Setup should contain test-merchant", setupString.contains("test-merchant"))
            assertTrue("Setup should contain SANDBOX", setupString.contains("SANDBOX"))
        }
    }
} 