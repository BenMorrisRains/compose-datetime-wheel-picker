# DateTime Wheel Picker - Build Guide

This document explains how to build all the binary artifacts for the DateTime Wheel Picker library from source.

## Prerequisites

- **macOS** (required for iOS frameworks)
- **Xcode** with command line tools
- **JDK 17** or higher
- **Android SDK** (for Android builds)
- **Kotlin Multiplatform** project setup

## Project Structure

```
compose-datetime-wheel-picker/
├── datetime-wheel-picker/          # Main library module
├── sample/                         # Sample app
├── gradle.properties              # Project configuration
├── jitpack.yml                   # JitPack configuration
└── build.gradle.kts              # Root build script
```

## Build Configuration

### 1. Memory Settings

First, ensure adequate memory allocation in `gradle.properties`:

```properties
# Increase heap size for iOS framework builds
org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8 -Dkotlin.daemon.jvm.options\="-Xmx4096M"
```

### 2. Experimental Features

Enable required Compose experimental features:

```properties
# Compose experimental features
org.jetbrains.compose.experimental.uikit.enabled=true
org.jetbrains.compose.experimental.jscanvas.enabled=true
org.jetbrains.compose.experimental.wasm.enabled=false

# Native target compatibility
kotlin.native.ignoreDisabledTargets=true
```

## Building Individual Targets

### iOS Frameworks

Build iOS frameworks for all architectures:

```bash
# iOS Device (ARM64)
./gradlew :datetime-wheel-picker:linkReleaseFrameworkIosArm64

# iOS Simulator (Intel x64)
./gradlew :datetime-wheel-picker:linkReleaseFrameworkIosX64

# iOS Simulator (Apple Silicon)
./gradlew :datetime-wheel-picker:linkReleaseFrameworkIosSimulatorArm64
```

**Output locations:**
- `datetime-wheel-picker/build/bin/iosArm64/releaseFramework/ComposeApp.framework`
- `datetime-wheel-picker/build/bin/iosX64/releaseFramework/ComposeApp.framework`
- `datetime-wheel-picker/build/bin/iosSimulatorArm64/releaseFramework/ComposeApp.framework`

### Android AAR

Build Android library:

```bash
# Build release AAR
./gradlew :datetime-wheel-picker:assembleRelease
```

**Output location:**
- `datetime-wheel-picker/build/outputs/aar/datetime-wheel-picker-release.aar`

### JVM JAR

Build JVM library:

```bash
# Build JVM JAR
./gradlew :datetime-wheel-picker:jvmJar
```

**Output location:**
- `datetime-wheel-picker/build/libs/datetime-wheel-picker-jvm.jar`

### All Targets (Complete Build)

Build all targets at once:

```bash
# Clean and build all targets
./gradlew clean build -x test
```

## Troubleshooting

### Memory Issues

If you encounter `OutOfMemoryError`:

1. **Stop all Gradle daemons:**
   ```bash
   ./gradlew --stop
   ```

2. **Increase memory in gradle.properties:**
   ```properties
   org.gradle.jvmargs=-Xmx6144M -Dfile.encoding=UTF-8
   ```

3. **Build targets individually** instead of all at once

### Lock File Issues

If you get "Timeout waiting to lock" errors:

1. **Find the process holding the lock:**
   ```bash
   lsof | grep buildOutputCleanup.lock
   ```

2. **Kill the process:**
   ```bash
   kill -9 <PID>
   ```

3. **Stop all daemons and retry:**
   ```bash
   ./gradlew --stop
   ./gradlew clean
   ```

### KSP Version Compatibility

If you see KSP version warnings:

```
ksp-2.2.0-2.0.2 is too old for kotlin-2.2.21
```

Update your version catalog or build.gradle.kts to use compatible versions.

## Creating Release Binaries

### Step 1: Prepare Directory Structure

```bash
# Create directory for binaries
mkdir -p ios-frameworks
```

### Step 2: Copy Built Artifacts

```bash
# Copy iOS frameworks with descriptive names
cp -R datetime-wheel-picker/build/bin/iosArm64/releaseFramework/ComposeApp.framework \
     ios-frameworks/ComposeApp-iosArm64.framework

cp -R datetime-wheel-picker/build/bin/iosX64/releaseFramework/ComposeApp.framework \
     ios-frameworks/ComposeApp-iosX64.framework

cp -R datetime-wheel-picker/build/bin/iosSimulatorArm64/releaseFramework/ComposeApp.framework \
     ios-frameworks/ComposeApp-iosSimulatorArm64.framework

# Copy Android AAR
cp datetime-wheel-picker/build/outputs/aar/datetime-wheel-picker-release.aar \
   ios-frameworks/

# Copy JVM JAR
cp datetime-wheel-picker/build/libs/datetime-wheel-picker-jvm.jar \
   ios-frameworks/
```

### Step 3: Create Documentation

Create a README.md file explaining how to use the binaries:

```bash
# Create usage documentation
cat > ios-frameworks/README.md << 'EOF'
# DateTime Wheel Picker Binary Distribution

## Contents
- iOS Frameworks (ARM64, x64, Simulator ARM64)
- Android AAR
- JVM JAR
- Usage instructions

## Integration Instructions
[Include platform-specific integration steps]
EOF
```

### Step 4: Package for Distribution

```bash
# Create zip archive for GitHub release
zip -r datetime-wheel-picker-v1.0.5-binaries.zip ios-frameworks/
```

## JitPack Configuration

For JitPack compatibility, the project includes `jitpack.yml`:

```yaml
jdk:
  - openjdk17
before_install:
  - echo "kotlin.js.compiler=legacy" >> gradle.properties
  - echo "kotlin.native.ignoreDisabledTargets=true" >> gradle.properties
```

This configuration:
- Uses Java 17 (required for Android Gradle Plugin)
- Disables problematic JS/WASM targets on Linux
- Ignores disabled native targets

## Build Variants

### Development Build
```bash
# Quick build for testing
./gradlew :datetime-wheel-picker:assemble -x test -x lint
```

### Release Build
```bash
# Full release build with all checks
./gradlew :datetime-wheel-picker:build
```

### JitPack Simulation
```bash
# Simulate JitPack build locally
./gradlew :datetime-wheel-picker:publishToMavenLocal \
  -PJITPACK=true \
  -Pgroup=com.github.BenMorrisRains \
  -x test -x lint
```

## Continuous Integration

For automated builds, use these commands in CI:

```bash
# CI build script
#!/bin/bash
set -e

# Clean previous builds
./gradlew clean

# Build Android/JVM (works on Linux CI)
./gradlew :datetime-wheel-picker:assembleRelease :datetime-wheel-picker:jvmJar

# Build iOS (macOS CI only)
if [[ "$OSTYPE" == "darwin"* ]]; then
  ./gradlew :datetime-wheel-picker:linkReleaseFrameworkIosArm64
  ./gradlew :datetime-wheel-picker:linkReleaseFrameworkIosX64
  ./gradlew :datetime-wheel-picker:linkReleaseFrameworkIosSimulatorArm64
fi
```

## Version Management

Update version in `gradle.properties`:

```properties
VERSION_NAME=1.0.5
```

Then create and push git tag:

```bash
# Create annotated tag
git tag -a v1.0.5 -m "Release v1.0.5"

# Push tag to trigger builds
git push origin v1.0.5
```

## Publishing

### JitPack
- Automatically builds from git tags
- Uses `jitpack.yml` configuration
- Provides Android/JVM artifacts only

### Manual Distribution
- Build binaries locally (this guide)
- Attach to GitHub releases
- Supports all platforms including iOS

## Dependencies

The library requires these runtime dependencies:

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    // Compose dependencies are provided by the consuming app
}
```

## Platform Support Matrix

| Platform | JitPack | Manual Binaries | Build Environment |
|----------|---------|-----------------|-------------------|
| Android  | ✅      | ✅              | Any               |
| JVM      | ✅      | ✅              | Any               |
| iOS      | ❌      | ✅              | macOS only        |
| JS       | ❌      | ❌              | Disabled          |
| WASM     | ❌      | ❌              | Disabled          |

## Notes

- **iOS builds require macOS** - Cannot be built on Linux CI
- **Memory intensive** - iOS framework linking needs 4GB+ heap
- **KSP compatibility** - Ensure KSP version matches Kotlin version
- **Gradle daemon** - Stop daemons between major builds to avoid locks
