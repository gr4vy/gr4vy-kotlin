package com.gr4vy.sdk.responses

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.*

/**
 * Context information for payment options, including required fields and payment-specific settings
 */
@Serializable
data class Gr4vyPaymentOptionContext(
    @SerialName("required_fields")
    val requiredFields: Gr4vyRequiredFields? = null,
    @SerialName("redirect_requires_popup")
    val redirectRequiresPopup: Boolean? = null,
    @SerialName("requires_tokenized_redirect_popup")
    val requiresTokenizedRedirectPopup: Boolean? = null,
    @SerialName("merchant_name")
    val merchantName: String? = null,
    @SerialName("supported_schemes")
    val supportedSchemes: List<String>? = null,
    val gateway: String? = null,
    @SerialName("gateway_merchant_id")
    val gatewayMerchantId: String? = null,
    @SerialName("approval_ui")
    val approvalUi: Gr4vyApprovalUI? = null
) {
    /**
     * Wallet context (for Apple Pay, Google Pay, etc.)
     */
    @Serializable
    data class Gr4vyWalletContext(
        @SerialName("merchant_name")
        val merchantName: String? = null,
        @SerialName("supported_schemes")
        val supportedSchemes: List<String>? = null
    )
    
    /**
     * Google Pay specific context
     */
    @Serializable
    data class Gr4vyGoogleContext(
        @SerialName("merchant_name")
        val merchantName: String? = null,
        @SerialName("supported_schemes")
        val supportedSchemes: List<String>? = null,
        val gateway: String? = null,
        @SerialName("gateway_merchant_id")
        val gatewayMerchantId: String? = null
    )
    
    /**
     * Payment context with redirect settings
     */
    @Serializable
    data class Gr4vyPaymentContext(
        @SerialName("redirect_requires_popup")
        val redirectRequiresPopup: Boolean? = null,
        @SerialName("requires_tokenized_redirect_popup")
        val requiresTokenizedRedirectPopup: Boolean? = null
    )
    
    /**
     * Approval UI dimensions
     */
    @Serializable
    data class Gr4vyApprovalUI(
        val width: String? = null,
        val height: String? = null
    )
}

/**
 * Required fields structure for payment options
 * Handles both top-level boolean fields and nested address object
 * Supports dynamic fields via custom serializer
 */
@Serializable(with = Gr4vyRequiredFieldsSerializer::class)
data class Gr4vyRequiredFields(
    @SerialName("email_address")
    val emailAddress: Boolean? = null,
    @SerialName("tax_id")
    val taxId: Boolean? = null,
    @SerialName("first_name")
    val firstName: Boolean? = null,
    @SerialName("last_name")
    val lastName: Boolean? = null,
    val address: Gr4vyAddressRequiredFields? = null,
    @SerialName("phone_number")
    val phoneNumber: Boolean? = null,
    @SerialName("account_number")
    val accountNumber: Boolean? = null,
    /**
     * Additional dynamic fields that aren't part of the known structure
     * Access via getField() or subscript operator
     */
    val additionalFields: Map<String, Boolean> = emptyMap()
) {
    /**
     * Get a field value by key (checks known fields first, then additional fields)
     * @param key The field name (e.g., "email_address", "account_number")
     * @return The boolean value if found, null otherwise
     */
    fun getField(key: String): Boolean? {
        return when (key) {
            "email_address" -> emailAddress
            "tax_id" -> taxId
            "first_name" -> firstName
            "last_name" -> lastName
            "phone_number" -> phoneNumber
            "account_number" -> accountNumber
            else -> additionalFields[key]
        }
    }
    
    /**
     * Subscript operator for dictionary-like access (backward compatibility)
     * @param key The field name
     * @return The boolean value if found, null otherwise
     */
    operator fun get(key: String): Boolean? = getField(key)
    
    /**
     * Check if required fields is empty (no fields set)
     */
    val isEmpty: Boolean
        get() = emailAddress == null &&
                taxId == null &&
                firstName == null &&
                lastName == null &&
                phoneNumber == null &&
                accountNumber == null &&
                address == null &&
                additionalFields.isEmpty()
    
    /**
     * Nested address required fields structure
     * Uses custom serializer to only include fields that were present in the original JSON
     */
    @Serializable(with = Gr4vyAddressRequiredFieldsSerializer::class)
    data class Gr4vyAddressRequiredFields(
        val organization: Boolean? = null,
        @SerialName("house_number_or_name")
        val houseNumberOrName: Boolean? = null,
        val line1: Boolean? = null,
        val line2: Boolean? = null,
        @SerialName("postal_code")
        val postalCode: Boolean? = null,
        val city: Boolean? = null,
        val state: Boolean? = null,
        @SerialName("state_code")
        val stateCode: Boolean? = null,
        val country: Boolean? = null
    )
}

/**
 * Custom serializer for Gr4vyRequiredFields that handles dynamic fields
 */
@OptIn(ExperimentalSerializationApi::class)
object Gr4vyRequiredFieldsSerializer : KSerializer<Gr4vyRequiredFields> {
    private val knownKeys = setOf(
        "email_address", "tax_id", "first_name", "last_name",
        "phone_number", "account_number", "address"
    )
    
    override val descriptor: SerialDescriptor = object : SerialDescriptor {
        override val serialName: String = "Gr4vyRequiredFields"
        override val kind: SerialKind = StructureKind.CLASS
        override val elementsCount: Int = 0
        override fun getElementName(index: Int): String = throw UnsupportedOperationException()
        override fun getElementIndex(name: String): Int = throw UnsupportedOperationException()
        override fun getElementDescriptor(index: Int): SerialDescriptor = throw UnsupportedOperationException()
        override fun getElementAnnotations(index: Int): List<Annotation> = emptyList()
        override fun isElementOptional(index: Int): Boolean = true
        override val annotations: List<Annotation> = emptyList()
    }
    
    override fun serialize(encoder: Encoder, value: Gr4vyRequiredFields) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("Gr4vyRequiredFieldsSerializer requires JsonEncoder")
        
        val jsonObject = buildJsonObject {
            // Encode known fields
            value.emailAddress?.let { put("email_address", JsonPrimitive(it)) }
            value.taxId?.let { put("tax_id", JsonPrimitive(it)) }
            value.firstName?.let { put("first_name", JsonPrimitive(it)) }
            value.lastName?.let { put("last_name", JsonPrimitive(it)) }
            value.phoneNumber?.let { put("phone_number", JsonPrimitive(it)) }
            value.accountNumber?.let { put("account_number", JsonPrimitive(it)) }
            value.address?.let { 
                // Use the custom serializer which only includes non-null fields
                val addressJson = Json.encodeToJsonElement(
                    Gr4vyRequiredFields.Gr4vyAddressRequiredFields.serializer(), it)
                put("address", addressJson)
            }
            
            // Encode additional dynamic fields
            value.additionalFields.forEach { (key, boolValue) ->
                put(key, JsonPrimitive(boolValue))
            }
        }
        
        jsonEncoder.encodeJsonElement(jsonObject)
    }
    
    override fun deserialize(decoder: Decoder): Gr4vyRequiredFields {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("Gr4vyRequiredFieldsSerializer requires JsonDecoder")
        
        val jsonObject = jsonDecoder.decodeJsonElement().jsonObject
        
        // Decode known fields
        val emailAddress = jsonObject["email_address"]?.jsonPrimitive?.booleanOrNull
        val taxId = jsonObject["tax_id"]?.jsonPrimitive?.booleanOrNull
        val firstName = jsonObject["first_name"]?.jsonPrimitive?.booleanOrNull
        val lastName = jsonObject["last_name"]?.jsonPrimitive?.booleanOrNull
        val phoneNumber = jsonObject["phone_number"]?.jsonPrimitive?.booleanOrNull
        val accountNumber = jsonObject["account_number"]?.jsonPrimitive?.booleanOrNull
        
        // Decode address object if present
        val address = jsonObject["address"]?.let { addressElement ->
            try {
                Json.decodeFromJsonElement(
                    Gr4vyRequiredFields.Gr4vyAddressRequiredFields.serializer(), addressElement)
            } catch (e: Exception) {
                null
            }
        }
        
        // Collect additional dynamic fields (boolean fields that aren't known)
        val additionalFields = mutableMapOf<String, Boolean>()
        jsonObject.forEach { (key, value) ->
            if (!knownKeys.contains(key) && value is JsonPrimitive && !value.isString) {
                // Try to decode as boolean
                value.booleanOrNull?.let { boolValue ->
                    additionalFields[key] = boolValue
                }
            }
        }
        
        return Gr4vyRequiredFields(
            emailAddress = emailAddress,
            taxId = taxId,
            firstName = firstName,
            lastName = lastName,
            address = address,
            phoneNumber = phoneNumber,
            accountNumber = accountNumber,
            additionalFields = additionalFields
        )
    }
}

/**
 * Custom serializer for Gr4vyAddressRequiredFields that only includes non-null field
 */
@OptIn(ExperimentalSerializationApi::class)
object Gr4vyAddressRequiredFieldsSerializer : KSerializer<Gr4vyRequiredFields.Gr4vyAddressRequiredFields> {
    override val descriptor: SerialDescriptor = object : SerialDescriptor {
        override val serialName: String = "Gr4vyAddressRequiredFields"
        override val kind: SerialKind = StructureKind.CLASS
        override val elementsCount: Int = 0
        override fun getElementName(index: Int): String = throw UnsupportedOperationException()
        override fun getElementIndex(name: String): Int = throw UnsupportedOperationException()
        override fun getElementDescriptor(index: Int): SerialDescriptor = throw UnsupportedOperationException()
        override fun getElementAnnotations(index: Int): List<Annotation> = emptyList()
        override fun isElementOptional(index: Int): Boolean = true
        override val annotations: List<Annotation> = emptyList()
    }
    
    override fun serialize(encoder: Encoder, value: Gr4vyRequiredFields.Gr4vyAddressRequiredFields) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("Gr4vyAddressRequiredFieldsSerializer requires JsonEncoder")
        
        val jsonObject = buildJsonObject {
            // Only encode non-null fields 
            // Exclude organization, line2, and state_code when false 
            value.houseNumberOrName?.let { put("house_number_or_name", JsonPrimitive(it)) }
            value.line1?.let { put("line1", JsonPrimitive(it)) }
            value.postalCode?.let { put("postal_code", JsonPrimitive(it)) }
            value.city?.let { put("city", JsonPrimitive(it)) }
            value.state?.let { put("state", JsonPrimitive(it)) }
            value.country?.let { put("country", JsonPrimitive(it)) }
            // Only include organization, line2, and state_code if they are true (not false)
            value.organization?.takeIf { it }?.let { put("organization", JsonPrimitive(it)) }
            value.line2?.takeIf { it }?.let { put("line2", JsonPrimitive(it)) }
            value.stateCode?.takeIf { it }?.let { put("state_code", JsonPrimitive(it)) }
        }
        
        jsonEncoder.encodeJsonElement(jsonObject)
    }
    
    override fun deserialize(decoder: Decoder): Gr4vyRequiredFields.Gr4vyAddressRequiredFields {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("Gr4vyAddressRequiredFieldsSerializer requires JsonDecoder")
        
        val jsonObject = jsonDecoder.decodeJsonElement().jsonObject
        
        return Gr4vyRequiredFields.Gr4vyAddressRequiredFields(
            organization = jsonObject["organization"]?.jsonPrimitive?.booleanOrNull,
            houseNumberOrName = jsonObject["house_number_or_name"]?.jsonPrimitive?.booleanOrNull,
            line1 = jsonObject["line1"]?.jsonPrimitive?.booleanOrNull,
            line2 = jsonObject["line2"]?.jsonPrimitive?.booleanOrNull,
            postalCode = jsonObject["postal_code"]?.jsonPrimitive?.booleanOrNull,
            city = jsonObject["city"]?.jsonPrimitive?.booleanOrNull,
            state = jsonObject["state"]?.jsonPrimitive?.booleanOrNull,
            stateCode = jsonObject["state_code"]?.jsonPrimitive?.booleanOrNull,
            country = jsonObject["country"]?.jsonPrimitive?.booleanOrNull
        )
    }
}

