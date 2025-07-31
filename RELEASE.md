# How to Release Gr4vy Kotlin SDK

This guide outlines the process for releasing a new version of the Gr4vy Kotlin SDK.

## Overview

The release process is **mostly automated** via GitHub Actions. The workflow is triggered when you push a Git tag, and it handles building, testing, and creating a draft GitHub release.

## Release Process

### 1. Prepare the Release

#### Update Version
Run the version update script with your desired version:

```bash
./update_version.sh 1.0.0-beta.1
```

This script automatically updates:
- `VERSION` file
- `src/main/kotlin/com/gr4vy/sdk/Version.kt`
- `src/test/kotlin/com/gr4vy/sdk/SimpleTest.kt`
- `src/test/kotlin/com/gr4vy/sdk/VersionTest.kt`
- `README.md` (all dependency examples)

#### Verify Updates
Make sure all files were updated correctly:
- Check that tests pass: `./gradlew test`
- Verify the version appears correctly in all updated files
- Review the README to ensure dependency examples show the new version

### 2. Test the Release Build (Optional)

You can optionally test the release build locally using the release script:

```bash
./scripts/release-build.sh
```

This script:
- Cleans previous builds
- Runs debug unit tests
- Builds the release AAR with ProGuard optimization
- Publishes to local Maven repository
- Verifies the build output

**Note:** This step is optional since the GitHub workflow will also run tests.

### 3. Commit and Tag

#### Commit Your Changes
```bash
git add .
git commit -m "Bump version to 1.0.0-beta.1"
```

#### Create and Push Git Tag
```bash
git tag 1.0.0-beta.1
git push origin main
git push origin 1.0.0-beta.1
```

**⚠️ Important:** Pushing the tag is what triggers the automated release workflow!

### 4. Automated Release Workflow

Once you push the tag, GitHub Actions automatically:

1. **Triggers the Release Workflow** (`.github/workflows/release.yml`)
2. **Sets up the build environment** (JDK 17, Android SDK, Gradle cache)
3. **Validates Gradle wrapper**
4. **Runs tests** (`./gradlew test`)
5. **Builds release AAR** (`./gradlew assembleRelease`)
6. **Publishes to Maven Central** (`./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository`)
7. **Creates a draft GitHub release** with the built AAR attached

### 5. Finalize the Release

1. **Go to GitHub Releases page**: `https://github.com/gr4vy/gr4vy-kotlin/releases`
2. **Find your draft release** (it will be marked as "Draft")
3. **Edit the release notes**:
   - Add a description of what's new
   - List breaking changes (if any)
   - Include any important notes for developers
4. **Publish the release** (remove "Draft" status)

## Release Workflow Details

### What Triggers a Release?
- Pushing a Git tag matching any pattern 

### What Gets Built?
- Release AAR with ProGuard optimization
- All artifacts are attached to the GitHub release

### Where Are Releases Published?
- **GitHub Releases**: Draft release with AAR attached
- **Maven Central**: Automatically published to Maven Central via Sonatype

## Version Naming Convention

Follow semantic versioning with optional pre-release labels:
- `1.0.0` - Stable release
- `1.0.0-beta.1` - Beta release
- `1.0.1` - Patch release

## Rollback Process

If you need to rollback a release:

1. **Delete the problematic tag**:
   ```bash
   git tag -d 1.0.0-beta.1
   git push origin --delete 1.0.0-beta.1
   ```

2. **Delete the GitHub release** (if already published)

3. **Fix the issues and create a new patch release**
