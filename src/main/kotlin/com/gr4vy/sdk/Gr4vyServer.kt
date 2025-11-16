package com.gr4vy.sdk

/**
 * Gr4vy server environment configuration for API endpoints.
 *
 * This enum defines the available server environments for the Gr4vy SDK.
 * Choose the appropriate environment based on your development phase and requirements.
 *
 * ## Environment Selection Guide
 * - Use [SANDBOX] for development, testing, and integration
 * - Use [PRODUCTION] only for live transactions
 *
 * @property value The string value used in API endpoint construction
 * @see Gr4vy
 */
enum class Gr4vyServer(val value: String) {
    /**
     * Sandbox environment for testing and development.
     *
     * API endpoint: `https://api.sandbox.{id}.gr4vy.app`
     *
     * Use this environment for:
     * - Development and testing
     * - Integration testing
     * - Pre-production validation
     * - Demo purposes
     */
    SANDBOX("sandbox"),
    
    /**
     * Production environment for live transactions.
     *
     * API endpoint: `https://api.{id}.gr4vy.app`
     *
     * Use this environment for:
     * - Live production transactions
     * - Real customer payments
     *
     * **Warning:** Only use this environment when you're ready to process real transactions.
     */
    PRODUCTION("production");
    
    companion object {
        /**
         * Converts a string value to a [Gr4vyServer] enum instance.
         *
         * @param value The string representation of the server environment ("sandbox" or "production")
         * @return The corresponding [Gr4vyServer] enum value, or null if the value is not recognized
         *
         * Example:
         * ```kotlin
         * val server = Gr4vyServer.fromValue("sandbox") // Returns Gr4vyServer.SANDBOX
         * val invalid = Gr4vyServer.fromValue("invalid") // Returns null
         * ```
         */
        fun fromValue(value: String): Gr4vyServer? {
            return values().find { it.value == value }
        }
    }
} 