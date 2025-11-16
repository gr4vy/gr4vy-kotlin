package com.gr4vy.sdk.models

/**
 * Container for theme-specific UI customizations
 * Supports both light (default) and dark themes
 */
data class Gr4vyThreeDSUiCustomizationMap(
    val default: Gr4vyThreeDSUiCustomization? = null,
    val dark: Gr4vyThreeDSUiCustomization? = null
)

/**
 * Complete UI customization configuration for 3DS challenge screens
 */
data class Gr4vyThreeDSUiCustomization(
    val label: Gr4vyThreeDSLabelCustomization? = null,
    val toolbar: Gr4vyThreeDSToolbarCustomization? = null,
    val textBox: Gr4vyThreeDSTextBoxCustomization? = null,
    val view: Gr4vyThreeDSViewCustomization? = null,
    val buttons: Map<Gr4vyThreeDSButtonType, Gr4vyThreeDSButtonCustomization>? = null
)

/**
 * Label customization for text and headings
 */
data class Gr4vyThreeDSLabelCustomization(
    val textFontName: String? = null,
    val textFontSize: Int? = null,
    val textColorHex: String? = null,
    val headingTextFontName: String? = null,
    val headingTextFontSize: Int? = null,
    val headingTextColorHex: String? = null
)

/**
 * Toolbar customization for the challenge screen header
 */
data class Gr4vyThreeDSToolbarCustomization(
    val textFontName: String? = null,
    val textFontSize: Int? = null,
    val textColorHex: String? = null,
    val backgroundColorHex: String? = null,
    val headerText: String? = null,
    val buttonText: String? = null
)

/**
 * Text input box customization
 */
data class Gr4vyThreeDSTextBoxCustomization(
    val textFontName: String? = null,
    val textFontSize: Int? = null,
    val textColorHex: String? = null,
    val borderWidth: Int? = null,
    val borderColorHex: String? = null,
    val cornerRadius: Int? = null
)

/**
 * View background customization
 */
data class Gr4vyThreeDSViewCustomization(
    val challengeViewBackgroundColorHex: String? = null,
    val progressViewBackgroundColorHex: String? = null
)

/**
 * Button customization for various button types
 */
data class Gr4vyThreeDSButtonCustomization(
    val textFontName: String? = null,
    val textFontSize: Int? = null,
    val textColorHex: String? = null,
    val backgroundColorHex: String? = null,
    val cornerRadius: Int? = null
)

/**
 * Button types available in 3DS challenge screens
 */
enum class Gr4vyThreeDSButtonType {
    SUBMIT,
    CONTINUE,
    NEXT,
    RESEND,
    OPEN_OOB_APP,
    ADD_CARDHOLDER,
    CANCEL
}




