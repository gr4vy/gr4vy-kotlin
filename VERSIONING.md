# Version Management

This SDK uses a centralized version management system to ensure consistency for version numbers across all build artifacts.

## How it works

The version is centrally managed in `gr4vy-kotlin/src/main/kotlin/com/gr4vy/sdk/Version.kt`:

```kotlin
internal object Version {
    const val current = "1.0.0-beta.1"
}
```

### Files that automatically read from Version.kt:

1. **`gr4vy-kotlin/src/main/kotlin/com/gr4vy/sdk/Gr4vySDK.kt`** - Uses `Version.current` for the public API
2. **`gr4vy-kotlin/build.gradle.kts`** - Extracts version using Kotlin regex for `versionName` and `versionCode`
3. **`VERSION`** - Plain text file for external tools (updated by script)

### Distribution methods:

- **Maven/Gradle**: Uses `versionName` from build.gradle.kts
- **Manual Integration**: Uses `Gr4vySDK.version` property
- **Git Tags**: For version tracking and releases

## Updating the version

### Option 1: Use the update script (Recommended)

```bash
./update_version.sh 1.0.1
```

This script will:
- Update `Version.kt` with the new version
- Update the `VERSION` file
- Update test files that reference the version (if they exist)
- Update README.md if it contains version references
- Show you the next steps for building and releasing

### Option 2: Manual update

1. Edit `gr4vy-kotlin/src/main/kotlin/com/gr4vy/sdk/Version.kt` and change the version string
2. Update the `VERSION` file to match
3. Update any test files that reference the version
4. Test the build: `./gradlew :gr4vy-kotlin:build`
5. Create a Git tag: `git tag 1.0.1`

## Version Code Generation

The Android build system automatically generates a `versionCode` from the version string:

- Format: `MAJOR * 10000 + MINOR * 100 + PATCH`
- Example: `1.2.3` → `10203`
- Pre-release suffixes (like `-beta.1`) are ignored for version code calculation

## Validation

The version format follows semantic versioning (X.Y.Z). The update script validates this format and supports pre-release labels.

## Testing

Tests should verify that:
- The version is properly formatted
- The user agent includes the correct version
- All components read from the same source
- Version code generation works correctly

## Example Usage

```kotlin
// Get the current SDK version
val version = Gr4vySDK.version // "1.0.0-beta.1"

// Get the user agent string
val userAgent = Gr4vySDK.userAgent // "Gr4vy-Kotlin/1.0.0-beta.1 (Android 14)"

// Check minimum Android version support
val isSupported = Gr4vySDK.isAndroidVersionSupported // true if API level >= 26
``` 