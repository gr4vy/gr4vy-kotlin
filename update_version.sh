#!/bin/bash

# Script to update version across all files in the Gr4vy Kotlin SDK

if [ $# -eq 0 ]; then
    echo "Usage: $0 <new_version>"
    echo "Example: $0 1.0.1"
    exit 1
fi

NEW_VERSION=$1

# Validate version format (semantic versioning)
if [[ ! $NEW_VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9\.-]+)?$ ]]; then
    echo "Error: Version must be in format X.Y.Z or X.Y.Z-label (e.g., 1.0.0 or 1.0.0-beta.1)"
    exit 1
fi

echo "Updating version to $NEW_VERSION..."

# Update VERSION file
echo $NEW_VERSION > VERSION

# Update Version.kt
sed -i '' "s/const val current = \".*\"/const val current = \"$NEW_VERSION\"/" src/main/kotlin/com/gr4vy/sdk/Version.kt

# Update test files that might reference the version (if they exist)
if [ -f "src/test/kotlin/com/gr4vy/sdk/Gr4vySDKTest.kt" ]; then
    sed -i '' "s/assertEquals(\".*\", Gr4vySDK.version)/assertEquals(\"$NEW_VERSION\", Gr4vySDK.version)/" src/test/kotlin/com/gr4vy/sdk/Gr4vySDKTest.kt
fi

if [ -f "src/test/kotlin/com/gr4vy/sdk/VersionTest.kt" ]; then
    sed -i '' "s/assertEquals(\".*\", version)/assertEquals(\"$NEW_VERSION\", version)/" src/test/kotlin/com/gr4vy/sdk/VersionTest.kt
fi

# Update README.md if it contains version references
if grep -q "Version.*[0-9]\+\.[0-9]\+\.[0-9]\+" README.md; then
    sed -i '' "s/Version [0-9]\+\.[0-9]\+\.[0-9]\+[^[:space:]]*/Version $NEW_VERSION/" README.md
fi

echo "Version updated to $NEW_VERSION successfully!"
echo ""
echo "Updated files:"
echo "- VERSION"
echo "- src/main/kotlin/com/gr4vy/sdk/Version.kt"
if [ -f "src/test/kotlin/com/gr4vy/sdk/Gr4vySDKTest.kt" ]; then
    echo "- src/test/kotlin/com/gr4vy/sdk/Gr4vySDKTest.kt"
fi
if [ -f "src/test/kotlin/com/gr4vy/sdk/VersionTest.kt" ]; then
    echo "- src/test/kotlin/com/gr4vy/sdk/VersionTest.kt"
fi
echo ""
echo "Files that automatically read from Version.kt:"
echo "- src/main/kotlin/com/gr4vy/sdk/Gr4vySDK.kt"
echo "- build.gradle.kts"
echo ""
echo "Next steps:"
echo "1. Test the build: ./gradlew build"
echo "2. Commit changes: git add . && git commit -m \"Bump version to $NEW_VERSION\""
echo "3. Create Git tag: git tag $NEW_VERSION"
echo "4. Push changes: git push && git push --tags" 