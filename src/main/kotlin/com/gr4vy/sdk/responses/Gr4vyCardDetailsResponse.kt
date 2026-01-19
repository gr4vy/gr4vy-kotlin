package com.gr4vy.sdk.responses

import com.gr4vy.sdk.http.Gr4vyIdentifiableResponse
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.*

@Serializable(with = Gr4vyCardDetailsResponseSerializer::class)
data class Gr4vyCardDetailsResponse(
    override val type: String? = null,
    override val id: String,
    @SerialName("card_type")
    val cardType: String? = null,
    val scheme: String? = null,
    @SerialName("scheme_icon_url")
    val schemeIconURL: String? = null,
    val country: String? = null,
    @SerialName("required_fields")
    val requiredFields: Gr4vyRequiredFields? = null
) : Gr4vyIdentifiableResponse

/**
 * Custom serializer for Gr4vyCardDetailsResponse that excludes null fields
 */
@OptIn(ExperimentalSerializationApi::class)
object Gr4vyCardDetailsResponseSerializer : KSerializer<Gr4vyCardDetailsResponse> {
    override val descriptor: SerialDescriptor = object : SerialDescriptor {
        override val serialName: String = "Gr4vyCardDetailsResponse"
        override val kind: SerialKind = StructureKind.CLASS
        override val elementsCount: Int = 0
        override fun getElementName(index: Int): String = throw UnsupportedOperationException()
        override fun getElementIndex(name: String): Int = throw UnsupportedOperationException()
        override fun getElementDescriptor(index: Int): SerialDescriptor = throw UnsupportedOperationException()
        override fun getElementAnnotations(index: Int): List<Annotation> = emptyList()
        override fun isElementOptional(index: Int): Boolean = true
        override val annotations: List<Annotation> = emptyList()
    }
    
    override fun serialize(encoder: Encoder, value: Gr4vyCardDetailsResponse) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("Gr4vyCardDetailsResponseSerializer requires JsonEncoder")
        
        val jsonObject = buildJsonObject {
            // Only encode non-null fields
            value.type?.let { put("type", JsonPrimitive(it)) }
            put("id", JsonPrimitive(value.id)) // id is required, always include
            value.cardType?.let { put("card_type", JsonPrimitive(it)) }
            value.scheme?.let { put("scheme", JsonPrimitive(it)) }
            value.schemeIconURL?.let { put("scheme_icon_url", JsonPrimitive(it)) }
            value.country?.let { put("country", JsonPrimitive(it)) }
            value.requiredFields?.let {
                val requiredFieldsJson = Json.encodeToJsonElement(
                    Gr4vyRequiredFields.serializer(), it)
                put("required_fields", requiredFieldsJson)
            }
        }
        
        jsonEncoder.encodeJsonElement(jsonObject)
    }
    
    override fun deserialize(decoder: Decoder): Gr4vyCardDetailsResponse {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("Gr4vyCardDetailsResponseSerializer requires JsonDecoder")
        
        val jsonObject = jsonDecoder.decodeJsonElement().jsonObject
        
        // Handle required id field - check for null, JsonNull, or missing
        val idElement = jsonObject["id"]
        val id = when {
            idElement == null -> throw SerializationException("Missing required field: id")
            idElement is JsonNull -> throw SerializationException("Required field 'id' cannot be null")
            idElement is JsonPrimitive -> idElement.content
            else -> throw SerializationException("Field 'id' must be a string")
        }
        
        return Gr4vyCardDetailsResponse(
            type = jsonObject["type"]?.jsonPrimitive?.contentOrNull,
            id = id,
            cardType = jsonObject["card_type"]?.jsonPrimitive?.contentOrNull,
            scheme = jsonObject["scheme"]?.jsonPrimitive?.contentOrNull,
            schemeIconURL = jsonObject["scheme_icon_url"]?.jsonPrimitive?.contentOrNull,
            country = jsonObject["country"]?.jsonPrimitive?.contentOrNull,
            requiredFields = jsonObject["required_fields"]?.let { requiredFieldsElement ->
                try {
                    Json.decodeFromJsonElement(
                        Gr4vyRequiredFields.serializer(), requiredFieldsElement)
                } catch (e: Exception) {
                    null
                }
            }
        )
    }
}