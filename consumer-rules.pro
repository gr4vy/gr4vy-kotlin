# ==============================================================================
# Gr4vy Android SDK - Consumer ProGuard Rules
# ==============================================================================
#
# These rules are automatically applied to applications that include the
# Gr4vy Kotlin SDK as a dependency. They ensure that your app can properly
# use the SDK after ProGuard processing.
#
# Note: These rules are applied automatically - you do not need to manually
# include them in your app's proguard-rules.pro file.
#
# ==============================================================================

# ------------------------------------------------------------------------------
# SDK PUBLIC API PRESERVATION
# ------------------------------------------------------------------------------
# These rules preserve the SDK's public API that your application uses.

# Preserve all public SDK classes and their public methods
-keep public class com.gr4vy.sdk.** { public *; }

# Preserve main SDK entry points
-keep class com.gr4vy.sdk.Gr4vy {
    public <init>(...);
    public <methods>;
}

-keep class com.gr4vy.sdk.Gr4vySDK {
    public static <fields>;
    public static <methods>;
}

# Preserve server configuration enum
-keep enum com.gr4vy.sdk.Gr4vyServer {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public <fields>;
}

# ------------------------------------------------------------------------------
# ERROR HANDLING SUPPORT
# ------------------------------------------------------------------------------
# These rules ensure proper exception handling in consumer applications.

# Preserve all error classes for proper exception handling
-keep class com.gr4vy.sdk.Gr4vyError** { 
    public <init>(...);
    public <methods>;
    public <fields>;
}

# ------------------------------------------------------------------------------
# SERVICE CLASSES
# ------------------------------------------------------------------------------
# These rules preserve SDK service classes that may be used by consumers.

# Preserve public service methods
-keep class com.gr4vy.sdk.services.** { public <methods>; }

# ------------------------------------------------------------------------------
# CONFIGURATION CLASSES
# ------------------------------------------------------------------------------
# These rules preserve SDK configuration classes.

# Preserve setup configuration class
-keep class com.gr4vy.sdk.models.Gr4vySetup {
    public <init>(...);
    public <methods>;
}

# ------------------------------------------------------------------------------
# KOTLINX SERIALIZATION SUPPORT
# ------------------------------------------------------------------------------
# These rules ensure proper JSON handling in consumer applications.

# Preserve annotation information for serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Preserve SDK data classes marked with @Serializable
-keep @kotlinx.serialization.Serializable class com.gr4vy.sdk.** {
    static <fields>;
    public <fields>;
    public <methods>;
}

# Preserve generated serializers
-keep class com.gr4vy.sdk.**$$serializer {
    *;
}

# Preserve companion objects used by serialization
-keepclassmembers class com.gr4vy.sdk.** {
    *** Companion;
}

# Preserve data class functionality for SDK classes
-keepclassmembers class com.gr4vy.sdk.** {
    *** copy(...);
    *** component*();
}

# ------------------------------------------------------------------------------
# HTTP INTERFACE PRESERVATION
# ------------------------------------------------------------------------------
# These rules preserve HTTP-related interfaces for type checking.

# Preserve request/response interfaces for type safety
-keep interface com.gr4vy.sdk.http.Gr4vyRequest { *; }
-keep interface com.gr4vy.sdk.http.Gr4vyResponse { *; }
-keep interface com.gr4vy.sdk.http.Gr4vyRequestWithMetadata { *; }

# ------------------------------------------------------------------------------
# OKHTTP NETWORKING SUPPORT
# ------------------------------------------------------------------------------
# These rules ensure proper HTTP functionality in consumer applications.

# Preserve OkHttp classes (required for SDK networking)
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Suppress warnings for OkHttp dependencies
-dontwarn okhttp3.**
-dontwarn okio.**

# ------------------------------------------------------------------------------
# KOTLIN COROUTINES SUPPORT
# ------------------------------------------------------------------------------
# These rules ensure proper async/await functionality.

# Preserve coroutines volatile fields
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Preserve Kotlin Result class for callback handling
-keep class kotlin.Result { *; }

# ------------------------------------------------------------------------------
# ANDROID FRAMEWORK COMPATIBILITY
# ------------------------------------------------------------------------------
# These rules preserve Android framework classes used by the SDK.

# Preserve Android logging (used by SDK for diagnostics)
-keep class android.util.Log { 
    public static *** d(...);
    public static *** e(...);
    public static *** i(...);
    public static *** w(...);
}

# Preserve Android build information (used by SDK)
-keep class android.os.Build { 
    public static <fields>; 
}
-keep class android.os.Build$VERSION { 
    public static <fields>; 
} 

# ------------------------------------------------------------------------------
# DEBUGGING AND DIAGNOSTICS
# ------------------------------------------------------------------------------
# These rules preserve information useful for debugging consumer applications.

# Preserve generic type signatures for better type safety
-keepattributes Signature

# Preserve line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable

# ------------------------------------------------------------------------------
# DEPENDENCY WARNINGS SUPPRESSION
# ------------------------------------------------------------------------------
# These rules suppress warnings for SDK dependencies that may not be relevant
# to your specific application.

-dontwarn javax.annotation.**   # Optional annotation processing
-dontwarn org.conscrypt.**      # Alternative SSL/TLS provider
-dontwarn org.bouncycastle.**   # Alternative cryptography provider
-dontwarn org.openjsse.**       # Alternative SSL/TLS provider 