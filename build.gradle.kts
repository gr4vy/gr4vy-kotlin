plugins {
    id("com.android.library") version "8.11.1"
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"

    // Central Portal publishing (handles maven-publish & signing under the hood)
    id("com.vanniktech.maven.publish") version "0.34.0"
}

// Import required for Vanniktech Android publishing
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

// ---- Version helpers (unchanged) ----

// Function to extract version from Version.kt
fun getVersionFromKotlin(): String {
    val versionFile = file("src/main/kotlin/com/gr4vy/sdk/Version.kt")
    val versionRegex = """const val current = "(.+)"""".toRegex()
    val versionText = versionFile.readText()
    val matchResult = versionRegex.find(versionText)
    return matchResult?.groupValues?.get(1) ?: "1.0.0"
}

// Function to convert version string to version code
fun getVersionCode(version: String): Int {
    val parts = version.split("-")[0].split(".")
    if (parts.size >= 3) {
        val major = parts[0].toIntOrNull() ?: 0
        val minor = parts[1].toIntOrNull() ?: 0
        val patch = parts[2].toIntOrNull() ?: 0
        return major * 10000 + minor * 100 + patch
    }
    return 1
}

val sdkVersion = getVersionFromKotlin()

// Set Maven coordinates (group + version)
group = "com.gr4vy"
version = sdkVersion

android {
    namespace = "com.gr4vy.sdk"
    compileSdk = 36

    // Publish the Android "release" variant (sources/javadoc jars come from Vanniktech config below)
    publishing {
        singleVariant("release") {
            // no extra tweaks needed here
        }
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Build configuration fields
        buildConfigField("String", "SDK_VERSION", "\"$sdkVersion\"")
        buildConfigField("boolean", "DEBUG_MODE", "false")
        // Optional versionCode if you later convert this to an app; not used by libraries when publishing
        // versionCode = getVersionCode(sdkVersion)
    }

    signingConfigs {
        create("release") {
            // For Android libraries, we use debug signing in development.
            // The consuming app signs its own APK/AAB; this does not affect Maven publication.
            if (project.hasProperty("RELEASE_STORE_FILE")) {
                storeFile = file(project.property("RELEASE_STORE_FILE").toString())
                storePassword = project.property("RELEASE_STORE_PASSWORD").toString()
                keyAlias = project.property("RELEASE_KEY_ALIAS").toString()
                keyPassword = project.property("RELEASE_KEY_PASSWORD").toString()
            } else {
                val debugKeystore = file("${System.getProperty("user.home")}/.android/debug.keystore")
                if (debugKeystore.exists()) {
                    storeFile = debugKeystore
                } else {
                    storeFile = file("${layout.buildDirectory.get()}/tmp/debug.keystore").apply {
                        if (!exists()) {
                            parentFile.mkdirs()
                            println("Warning: Using temporary keystore for library development")
                        }
                    }
                }
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            buildConfigField("boolean", "DEBUG_MODE", "true")
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("boolean", "DEBUG_MODE", "false")
        }

        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = false
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjsr305=strict",
            "-Xuse-inline-scopes-numbers"
        )
    }

    buildFeatures {
        buildConfig = true
        resValues = false
        shaders = false
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/license.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "/META-INF/notice.txt",
                "/META-INF/ASL2.0",
                "/META-INF/*.kotlin_module"
            )
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all { test ->
                test.testLogging {
                    events("passed", "skipped", "failed")
                    showStandardStreams = false
                }
            }
        }
    }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.robolectric:robolectric:4.11.1")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

// --------- Central Portal publishing via Vanniktech ---------

mavenPublishing {
    // Explicitly use Central Portal host; release automatically after upload
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    // Sign all publications (reads signingInMemoryKey / signingInMemoryKeyPassword from CI)
    signAllPublications()

    // Publish the Android "release" variant with sources & javadoc jars
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true
        )
    )

    // Coordinates + POM metadata
    coordinates("com.gr4vy", "gr4vy-kotlin", version.toString())

    pom {
        name.set("Gr4vy Kotlin SDK")
        description.set("Official Kotlin SDK for Gr4vy payment processing")
        inceptionYear.set("2024")
        url.set("https://github.com/gr4vy/gr4vy-kotlin")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("gr4vy")
                name.set("Gr4vy")
                email.set("support@gr4vy.com")
            }
        }
        scm {
            url.set("https://github.com/gr4vy/gr4vy-kotlin")
            connection.set("scm:git:git://github.com/gr4vy/gr4vy-kotlin.git")
            developerConnection.set("scm:git:ssh://github.com:gr4vy/gr4vy-kotlin.git")
        }
    }
}

// Print the SDK version during build for verification
println("Gr4vy Kotlin SDK version: $sdkVersion")
