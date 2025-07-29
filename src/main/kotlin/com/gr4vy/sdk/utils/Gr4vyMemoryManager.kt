package com.gr4vy.sdk.utils

import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

object Gr4vyMemoryManager {
    
    interface SecureDisposable {
        fun dispose()
        
        fun isDisposed(): Boolean
    }
    
    private val sensitiveDataRegistry = ConcurrentHashMap<String, WeakReference<SecureDisposable>>()
    
    fun registerSensitiveData(id: String, disposable: SecureDisposable) {
        sensitiveDataRegistry[id] = WeakReference(disposable)
        Gr4vyLogger.debug("Registered sensitive data object: $id")
    }
    
    fun disposeSensitiveData(id: String) {
        sensitiveDataRegistry[id]?.get()?.let { disposable ->
            if (!disposable.isDisposed()) {
                disposable.dispose()
                Gr4vyLogger.debug("Disposed sensitive data object: $id")
            }
        }
        sensitiveDataRegistry.remove(id)
    }
    
    fun disposeAllSensitiveData() {
        val disposed = mutableListOf<String>()
        
        sensitiveDataRegistry.forEach { (id, weakRef) ->
            weakRef.get()?.let { disposable ->
                if (!disposable.isDisposed()) {
                    disposable.dispose()
                    disposed.add(id)
                }
            }
        }
        
        sensitiveDataRegistry.clear()
        
        if (disposed.isNotEmpty()) {
            Gr4vyLogger.debug("Disposed ${disposed.size} sensitive data objects")
        }
        
        // Force garbage collection to help clear disposed objects
        forceGarbageCollection()
    }
    
    fun attemptStringOverwrite(sensitiveString: String?): Boolean {
        if (sensitiveString == null || sensitiveString.isEmpty()) return false
        
        return try {
            // Try to access the char array backing the string
            val valueField = String::class.java.getDeclaredField("value")
            valueField.isAccessible = true
            
            val charArray = valueField.get(sensitiveString) as? CharArray
            charArray?.let { chars ->
                // Overwrite with random data
                val random = Random.Default
                for (i in chars.indices) {
                    chars[i] = random.nextInt(33, 127).toChar() // Random printable chars
                }
                
                // Then overwrite with zeros
                chars.fill('\u0000')
                
                Gr4vyLogger.debug("Attempted to overwrite string memory")
                true
            } ?: false
            
        } catch (e: Exception) {
            // Reflection may fail on some platforms/security settings
            Gr4vyLogger.debug("Could not overwrite string memory: ${e.message}")
            false
        }
    }
    
    fun secureCharArrayWipe(charArray: CharArray?) {
        charArray?.let { chars ->
            val random = Random.Default
            
            // First pass: random data
            for (i in chars.indices) {
                chars[i] = random.nextInt(33, 127).toChar()
            }
            
            // Second pass: zeros
            chars.fill('\u0000')
            
            Gr4vyLogger.debug("Securely wiped char array of length ${chars.size}")
        }
    }
    
    fun forceGarbageCollection() {
        try {
            // Request garbage collection
            System.gc()
            
            // Give GC a moment to run
            Thread.sleep(10)
            
            // Request again
            System.gc()
            
            Gr4vyLogger.debug("Requested garbage collection for memory cleanup")
        } catch (e: Exception) {
            Gr4vyLogger.debug("Could not force garbage collection: ${e.message}")
        }
    }
    
    fun generateTrackingId(): String {
        return "sensitive_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
    }
    
    fun getTrackedObjectCount(): Int {
        // Clean up any objects that have been garbage collected
        val iterator = sensitiveDataRegistry.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.get() == null) {
                iterator.remove()
            }
        }
        
        return sensitiveDataRegistry.size
    }
    
    fun sanitizeForLogging(sensitiveData: String?, visibleChars: Int = 2): String {
        if (sensitiveData == null || sensitiveData.length <= visibleChars * 2) {
            return "*".repeat(sensitiveData?.length ?: 0)
        }
        
        val start = sensitiveData.take(visibleChars)
        val end = sensitiveData.takeLast(visibleChars)
        val maskLength = sensitiveData.length - (visibleChars * 2)
        
        return "$start${"*".repeat(maskLength)}$end"
    }
    
    fun shutdown() {
        Gr4vyLogger.debug("Shutting down memory manager, disposing all sensitive data")
        disposeAllSensitiveData()
    }
} 