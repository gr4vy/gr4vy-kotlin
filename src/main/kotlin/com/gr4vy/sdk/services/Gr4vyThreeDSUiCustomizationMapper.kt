package com.gr4vy.sdk.services

import com.gr4vy.sdk.models.*
import com.gr4vy.sdk.utils.Gr4vyLogger
import com.netcetera.threeds.sdk.api.ui.logic.*

/**
 * Maps Gr4vy UI customization models to Netcetera SDK UiCustomization objects
 */
internal object Gr4vyThreeDSUiCustomizationMapper {
    
    /**
     * Map the customization map to Netcetera's format
     * Returns a map with DEFAULT and DARK UiCustomizationType keys
     */
    fun map(customizationMap: Gr4vyThreeDSUiCustomizationMap?): Map<UiCustomization.UiCustomizationType, UiCustomization>? {
        if (customizationMap == null) return null
        
        val result = mutableMapOf<UiCustomization.UiCustomizationType, UiCustomization>()
        
        customizationMap.default?.let { light ->
            build(light)?.let { result[UiCustomization.UiCustomizationType.DEFAULT] = it }
        }
        
        customizationMap.dark?.let { dark ->
            build(dark)?.let { result[UiCustomization.UiCustomizationType.DARK] = it }
        }
        
        return if (result.isEmpty()) null else result
    }
    
    /**
     * Build a single UiCustomization from Gr4vy model
     */
    private fun build(src: Gr4vyThreeDSUiCustomization): UiCustomization? {
        val ui = UiCustomization()
        var hasAny = false
        
        // Label customization
        src.label?.let { label ->
            val lc = LabelCustomization()
            
            label.textFontName?.let {
                try {
                    lc.textFontName = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid label text font name: ${e.message}")
                }
            }
            
            label.textFontSize?.let {
                try {
                    lc.textFontSize = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid label text font size: ${e.message}")
                }
            }
            
            label.textColorHex?.let {
                try {
                    lc.textColor = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid label text color: ${e.message}")
                }
            }
            
            label.headingTextFontName?.let {
                try {
                    lc.headingTextFontName = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid label heading font name: ${e.message}")
                }
            }
            
            label.headingTextFontSize?.let {
                try {
                    lc.headingTextFontSize = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid label heading font size: ${e.message}")
                }
            }
            
            label.headingTextColorHex?.let {
                try {
                    lc.headingTextColor = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid label heading color: ${e.message}")
                }
            }
            
            ui.labelCustomization = lc
        }
        
        // Toolbar customization
        src.toolbar?.let { toolbar ->
            val tc = ToolbarCustomization()
            
            toolbar.textFontName?.let {
                try {
                    tc.textFontName = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid toolbar text font name: ${e.message}")
                }
            }
            
            toolbar.textFontSize?.let {
                try {
                    tc.textFontSize = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid toolbar text font size: ${e.message}")
                }
            }
            
            toolbar.textColorHex?.let {
                try {
                    tc.textColor = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid toolbar text color: ${e.message}")
                }
            }
            
            toolbar.backgroundColorHex?.let {
                try {
                    tc.backgroundColor = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid toolbar background color: ${e.message}")
                }
            }
            
            toolbar.headerText?.let {
                try {
                    tc.headerText = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid toolbar header text: ${e.message}")
                }
            }
            
            toolbar.buttonText?.let {
                try {
                    tc.buttonText = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid toolbar button text: ${e.message}")
                }
            }
            
            ui.toolbarCustomization = tc
        }
        
        // Text box customization
        src.textBox?.let { textBox ->
            val tbc = TextBoxCustomization()
            
            textBox.textFontName?.let {
                try {
                    tbc.textFontName = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid textbox font name: ${e.message}")
                }
            }
            
            textBox.textFontSize?.let {
                try {
                    tbc.textFontSize = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid textbox font size: ${e.message}")
                }
            }
            
            textBox.textColorHex?.let {
                try {
                    tbc.textColor = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid textbox text color: ${e.message}")
                }
            }
            
            textBox.borderWidth?.let {
                try {
                    tbc.borderWidth = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid textbox border width: ${e.message}")
                }
            }
            
            textBox.borderColorHex?.let {
                try {
                    tbc.borderColor = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid textbox border color: ${e.message}")
                }
            }
            
            textBox.cornerRadius?.let {
                try {
                    tbc.cornerRadius = it
                    hasAny = true
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid textbox corner radius: ${e.message}")
                }
            }
            
            ui.textBoxCustomization = tbc
        }
        
        // View customization
        src.view?.let { view ->
            view.challengeViewBackgroundColorHex?.let {
                try {
                    
                    // Note: Not setting hasAny=true since we're not actually applying this yet
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid view background color: ${e.message}")
                }
            }
            
            view.progressViewBackgroundColorHex?.let {
                try {
                    
                    // Note: Not setting hasAny=true since we're not actually applying this yet
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Invalid progress view background color: ${e.message}")
                }
            }
        }
        
        // Button customizations
        src.buttons?.let { buttons ->
            for ((type, conf) in buttons) {
                val bc = ButtonCustomization()
                var buttonHasCustomization = false
                
                conf.textFontName?.let {
                    try {
                        bc.textFontName = it
                        buttonHasCustomization = true
                    } catch (e: Exception) {
                        Gr4vyLogger.debug("Invalid button font name for $type: ${e.message}")
                    }
                }
                
                conf.textFontSize?.let {
                    try {
                        bc.textFontSize = it
                        buttonHasCustomization = true
                    } catch (e: Exception) {
                        Gr4vyLogger.debug("Invalid button font size for $type: ${e.message}")
                    }
                }
                
                conf.textColorHex?.let {
                    try {
                        bc.textColor = it
                        buttonHasCustomization = true
                    } catch (e: Exception) {
                        Gr4vyLogger.debug("Invalid button text color for $type: ${e.message}")
                    }
                }
                
                conf.backgroundColorHex?.let {
                    try {
                        bc.backgroundColor = it
                        buttonHasCustomization = true
                    } catch (e: Exception) {
                        Gr4vyLogger.debug("Invalid button background color for $type: ${e.message}")
                    }
                }
                
                conf.cornerRadius?.let {
                    try {
                        bc.cornerRadius = it
                        buttonHasCustomization = true
                    } catch (e: Exception) {
                        Gr4vyLogger.debug("Invalid button corner radius for $type: ${e.message}")
                    }
                }
                
                if (buttonHasCustomization) {
                    mapButtonType(type)?.let { netceteraType ->
                        ui.setButtonCustomization(bc, netceteraType)
                        hasAny = true
                    }
                }
            }
        }
        
        return if (hasAny) ui else null
    }
    
    /**
     * Map Gr4vy button type to Netcetera button type
     */
    private fun mapButtonType(type: Gr4vyThreeDSButtonType): UiCustomization.ButtonType? {
        return when (type) {
            Gr4vyThreeDSButtonType.SUBMIT -> UiCustomization.ButtonType.SUBMIT
            Gr4vyThreeDSButtonType.CONTINUE -> UiCustomization.ButtonType.CONTINUE
            Gr4vyThreeDSButtonType.NEXT -> UiCustomization.ButtonType.NEXT
            Gr4vyThreeDSButtonType.RESEND -> UiCustomization.ButtonType.RESEND
            Gr4vyThreeDSButtonType.OPEN_OOB_APP -> try {
                UiCustomization.ButtonType.OPEN_OOB_APP
            } catch (e: Exception) {
                Gr4vyLogger.debug("OPEN_OOB_APP button type not available in this Netcetera SDK version")
                null
            }
            Gr4vyThreeDSButtonType.ADD_CARDHOLDER -> try {
                UiCustomization.ButtonType.ADD_CH
            } catch (e: Exception) {
                Gr4vyLogger.debug("ADD_CH button type not available in this Netcetera SDK version")
                null
            }
            Gr4vyThreeDSButtonType.CANCEL -> UiCustomization.ButtonType.CANCEL
        }
    }
}

