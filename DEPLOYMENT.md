# Deployment Guide

**Mercury Messenger Portal - Build, Sign & Release Procedures**

---

## Table of Contents

1. [Overview](#overview)
2. [Pre-Release Checklist](#pre-release-checklist)
3. [Keystore Management](#keystore-management)
4. [Build Signed APK](#build-signed-apk)
5. [Build AAB (Android App Bundle)](#build-aab-android-app-bundle)
6. [Google Play Store Upload](#google-play-store-upload)
7. [Version Management](#version-management)
8. [Release Notes](#release-notes)
9. [Post-Release](#post-release)
10. [Rollback Procedures](#rollback-procedures)
11. [CI/CD Pipeline](#cicd-pipeline)

---

## Overview

### Release Process Flow

```
Code Commit
    ↓
Create Release Branch
    ↓
Version Bump
    ↓
Local Testing
    ↓
Build Release APK/AAB
    ↓
Sign with Keystore
    ↓
Upload to Google Play Console
    ↓
Create Release
    ↓
Monitor Metrics
    ↓
Tag Release in Git
```

### Distribution Channels

| Channel | Method | Users | Frequency |
|---------|--------|-------|-----------|
| **Google Play Store** | AAB (dynamic delivery) | All | Weekly/Monthly |
| **Internal Testing** | APK (side-load) | QA team | As needed |
| **Beta Testing** | Play Store beta track | Early adopters | Before release |

---

## Pre-Release Checklist

### 1 Week Before Release

- [ ] Verify all features complete and tested
- [ ] Run full test suite: `./gradlew test connectedAndroidTest`
- [ ] Check lint warnings: `./gradlew lint`
- [ ] Review code changes since last release
- [ ] Update dependencies to latest stable versions
- [ ] Test on multiple Android versions (API 21-34)
- [ ] Test on real devices (not just emulator)
- [ ] Verify API endpoints are working

### 2 Days Before Release

- [ ] Finalize release notes
- [ ] Bump version numbers (see Version Management section)
- [ ] Create release branch: `git checkout -b release/v1.0.0`
- [ ] Update CHANGELOG.md with new features
- [ ] Get release approval from stakeholders

### Day of Release

- [ ] Build signed release APK/AAB
- [ ] Verify app installation and basic functionality
- [ ] Verify API connection with production backend
- [ ] Confirm Play Store console is ready
- [ ] Have rollback plan ready

---

## Keystore Management

### What is a Keystore?

A keystore is a file containing your app's signing certificate. **Never share or lose this file** — you need it to sign all app updates.

### Create Keystore (First Time Only)

```bash
# Generate keystore (interactive)
keytool -genkey -v -keystore mercury_release.keystore \
  -alias mercury_key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 36500

# Follow prompts:
# Password: [create strong password]
# First/Last Name: Company Name
# Organization: Company
# City: Bangkok
# State: Bangkok
# Country Code: TH
# Password (confirm): [same password]
```

**Output:**
```
mercury_release.keystore (keep this file safe!)
```

### Secure Keystore Storage

```bash
# Option 1: Store in project (encrypted Git storage)
# Add to .gitignore
echo "mercury_release.keystore" >> .gitignore

# Option 2: Store in CI/CD secrets
# Add to GitHub Actions secrets or CI provider

# Option 3: Store in HSM (Hardware Security Module - for production)
# Contact DevOps for enterprise setup
```

### Keystore Information

```bash
# Verify keystore
keytool -list -v -keystore mercury_release.keystore

# Output includes:
# Owner: CN=Mercury Portal
# Serial: 12345678
# Valid from: 2026-01-01
# Valid until: 2046-01-01
```

**Keep these details safe:**
- Keystore file path: `/path/to/mercury_release.keystore`
- Keystore password: `[password]`
- Key alias: `mercury_key`
- Key password: `[password]` (usually same as keystore)

---

## Build Signed APK

### Step 1: Configure Signing in Gradle

**Edit `app/build.gradle.kts`:**

```kotlin
android {
    signingConfigs {
        getByName("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "mercury_release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: "mercury_key"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            minifyEnabled = true
            shrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### Step 2: Set Environment Variables

**Locally (for one-time build):**
```bash
export KEYSTORE_PATH="/path/to/mercury_release.keystore"
export KEYSTORE_PASSWORD="your_password"
export KEY_ALIAS="mercury_key"
export KEY_PASSWORD="your_password"
```

**Or update `local.properties`:**
```properties
KEYSTORE_PATH=/path/to/mercury_release.keystore
KEYSTORE_PASSWORD=your_password
KEY_ALIAS=mercury_key
KEY_PASSWORD=your_password
```

### Step 3: Build Signed APK

```bash
# Build release APK (signed)
./gradlew assembleRelease

# Output location:
# app/build/outputs/apk/release/app-release.apk
```

### Step 4: Verify Signed APK

```bash
# Check APK signature
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# Output should show:
# [certificate details]
# jar verified.
```

### Step 5: Test Signed APK

```bash
# Install on device/emulator
adb install app/build/outputs/apk/release/app-release.apk

# Test core flows:
# - Login
# - Load jobs
# - Complete job
# - Clock out
```

---

## Build AAB (Android App Bundle)

Google Play Store requires AAB for new apps and updates.

### Step 1: Build AAB

```bash
# Build release AAB (with same signing config)
./gradlew bundleRelease

# Output location:
# app/build/outputs/bundle/release/app-release.aab
```

### Step 2: Test AAB Locally

```bash
# Download bundletool
curl -L https://github.com/google/bundletool/releases/latest/download/bundletool-all.jar \
  -o bundletool.jar

# Generate APKs from AAB
java -jar bundletool.jar build-apks \
  --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=app.apks \
  --ks=mercury_release.keystore \
  --ks-pass=pass:your_password \
  --ks-key-alias=mercury_key \
  --key-pass=pass:your_password

# Install APKs on device
java -jar bundletool.jar install-apks \
  --apks=app.apks
```

### Step 3: Verify AAB

```bash
# Analyze AAB
java -jar bundletool.jar dump manifest \
  --bundle=app/build/outputs/bundle/release/app-release.aab

# Check size
du -h app/build/outputs/bundle/release/app-release.aab
```

---

## Google Play Store Upload

### Prerequisites

1. **Google Developer Account**
   - https://play.google.com/console
   - $25 one-time registration fee
   - Email verified

2. **App Created in Play Console**
   - App name: "Mercury Messenger Portal"
   - Package name: "com.mercury.messengerportal"
   - Category: Business
   - Content rating: Completed

3. **Store Listing Complete**
   - Title, description, screenshots
   - Pricing: Free
   - Supported countries selected
   - Privacy policy URL

### Step 1: Access Google Play Console

```
https://play.google.com/console
→ Select app: Mercury Messenger Portal
→ Navigate to Release section
```

### Step 2: Upload Release

**Navigate to:**
```
Releases → Production → Create new release
```

**Upload AAB:**
```
Add release AAB or APK
→ Choose file: app/build/outputs/bundle/release/app-release.aab
→ Upload
```

**Review App Changes:**
```
Version Code: 1 (auto-incremented)
Version Name: 1.0.0 (matches app version)
Release Notes:
  - Added job completion tracking
  - Improved GPS accuracy
  - Bug fixes
```

### Step 3: Configure Rollout

```
Percentage rollout: 10% (staged rollout recommended)
→ Expand rollout to 100% over 1 week

OR

Percentage rollout: 100% (full immediate rollout)
→ Use for critical bug fixes only
```

### Step 4: Review & Publish

```
Review all changes
→ Confirm app bundle details
→ Accept terms
→ Publish release
```

**Status will show:**
```
"Rolled out to production"
or
"Preparing release"
```

Wait 1-2 hours for availability on Play Store.

### Step 5: Monitor Release

**In Play Console, watch:**
```
Real-time user metrics
├── Installs
├── Uninstalls
├── Crashes
├── ANRs (Application Not Responding)
└── User reviews & ratings
```

---

## Version Management

### Version Number Format

```
vX.Y.Z

Where:
X = Major version (breaking changes)
Y = Minor version (new features)
Z = Patch version (bug fixes)

Example: v1.2.3
```

### Update Version in Code

**File: `app/build.gradle.kts`**

```kotlin
android {
    defaultConfig {
        applicationId = "com.mercury.messengerportal"
        minSdk = 21
        targetSdk = 34
        versionCode = 3          // Increment by 1 for each release
        versionName = "1.0.3"    // Semantic versioning
    }
}
```

**Versioning Rules:**

| Change | versionCode | versionName | Type |
|--------|-------------|-------------|------|
| Bug fix | +1 | Patch (+0.0.1) | Patch |
| New feature | +1 | Minor (+0.1.0) | Minor |
| Major redesign | +1 | Major (+1.0.0) | Major |

### Version History Example

```
Release 1: versionCode=1, versionName=1.0.0 (Jan 1)
Release 2: versionCode=2, versionName=1.0.1 (Jan 8) - Bug fix
Release 3: versionCode=3, versionName=1.1.0 (Jan 22) - New feature
Release 4: versionCode=4, versionName=2.0.0 (Feb 15) - Major update
```

---

## Release Notes

### Release Notes Template

**File: `RELEASE_NOTES.md` (update for each release)**

```markdown
# Version 1.0.3 - 2026-04-20

## What's New

### Features
- Added job reordering by drag-and-drop
- Improved location accuracy with GPS retry logic
- Added delay job functionality with reason tracking

### Improvements
- Faster app startup (database indexing)
- Better error messages for network failures
- Improved photo compression (smaller files)

### Bug Fixes
- Fixed crash when location permission denied
- Fixed "Depart Next Job" button not appearing after completion
- Fixed database migration issue on upgrade

### Known Issues
- None

## Technical Notes

- Minimum Android: 5.0 (API 21)
- Target Android: 14 (API 34)
- Database version: 6

## Install Instructions

1. Update app from Google Play Store
2. Grant location permission when prompted
3. Log in with your credentials
4. Start using!

---

## Changelog (All Versions)

### 1.0.2 (2026-04-13)
- Initial public release
- Core job management features
- GPS-based location tracking
- Photo capture for job completion
```

### Update Changelog

```bash
# Before each release, update CHANGELOG.md
# Include in Git commit:

git add CHANGELOG.md
git commit -m "chore: Update changelog for v1.0.3"
```

---

## Post-Release

### Immediate Monitoring (24 Hours)

```
Monitor in Play Console:
├─ Crash rate (target: < 0.5%)
├─ ANR rate (target: < 0.1%)
├─ Rating trend
├─ User reviews for critical issues
└─ Server logs for API errors
```

**Dashboard URL:**
```
https://play.google.com/console/u/0/developers/{developer-id}
/app/{app-id}/quality/crashlytics
```

### Email Alerts Setup

```
Play Console → Settings → Email notifications
→ Enable alerts for:
  - Crashes & ANRs
  - Policy violations
  - Rating changes (3-star and below)
  - Version distribution
```

### Tag Release in Git

```bash
# After successful release, tag commit
git tag -a v1.0.3 -m "Release version 1.0.3

This release includes:
- Job reordering feature
- Improved GPS accuracy
- Bug fixes

APK SHA256: [hash]
"

# Push tag to GitHub
git push origin v1.0.3

# View all releases
git tag -l
```

### Schedule Next Sprint

```
After 2-3 days:
├─ Review user feedback
├─ Prioritize bug fixes
├─ Plan next release features
└─ Schedule next release (1-2 weeks)
```

---

## Rollback Procedures

### When to Rollback

- **Critical Crash** (> 10% of users)
- **Data Loss** issue
- **Security** vulnerability
- **API Integration** failure

### Rollback Steps

**Option 1: Reduce Rollout Percentage (Best)**

```
Play Console → Release → Edit release
→ Reduce percentage to 0%
→ Save

Users on 0% rollout stay on previous version
```

**Option 2: Create Rollback Release**

```
1. Identify last stable version (e.g., v1.0.1)
2. Cherry-pick fix from current branch
3. Build new APK with previous versionCode-1

Example:
├─ v1.0.2 (current, has bug)
├─ v1.0.1 (stable)
└─ Build v1.0.1-hotfix as versionCode higher than v1.0.2

⚠️ Never reuse same versionCode!
```

**Option 3: Emergency Patch**

```
1. Fix critical bug on main branch
2. Build new release (v1.0.2-hotfix)
3. Publish with 100% rollout
4. Mark v1.0.1 as deprecated in Play Console
```

### Post-Rollback

```bash
# Document incident
git tag -a v1.0.2-rollback -m "
Rolled back from v1.0.2 due to [issue].

Root cause: [analysis]
Fix applied in v1.0.3
"

# Notify users (in-app message)
# Update server to recommend update
# Post-mortem with team
```

---

## CI/CD Pipeline

### GitHub Actions Setup

**File: `.github/workflows/release.yml`**

```yaml
name: Release Build

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'adopt'
    
    - name: Build release AAB
      run: |
        ./gradlew bundleRelease
      env:
        KEYSTORE_PATH: ${{ secrets.KEYSTORE_PATH }}
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
    
    - name: Upload to Play Console
      uses: r0adkll/upload-google-play@v1
      with:
        serviceAccountJsonPlainText: ${{ secrets.PLAY_CONSOLE_SA_JSON }}
        packageName: com.mercury.messengerportal
        releaseFiles: 'app/build/outputs/bundle/release/app-release.aab'
        track: 'internal'
        status: 'draft'
```

### GitHub Secrets Setup

```
Settings → Secrets and variables → Actions
→ New repository secret

Add secrets:
├─ KEYSTORE_PATH: base64-encoded keystore file
├─ KEYSTORE_PASSWORD
├─ KEY_ALIAS
├─ KEY_PASSWORD
└─ PLAY_CONSOLE_SA_JSON: Service Account JSON
```

### Automated Build Trigger

```bash
# Tag a commit to trigger automated build
git tag -a v1.0.3 -m "Release v1.0.3"
git push origin v1.0.3

# GitHub Actions:
1. Detects tag
2. Builds AAB
3. Uploads to Play Console (draft)
4. Awaits manual review before publishing
```

### Manual CI/CD Trigger

If automated pipeline isn't set up:

```bash
# Local build and upload
./gradlew bundleRelease

# Manual upload via Play Console web interface
# No additional tools needed
```

---

## Monitoring & Analytics

### Key Metrics to Track

```
Daily Active Users (DAU)
├─ Target: 80%+ of installed user base
├─ Declining DAU = feature issue or bug

Crash Rate
├─ Target: < 0.5%
├─ Monitor: Crashes, ANRs, freezes

Performance
├─ Startup time: < 2 seconds
├─ API response time: < 3 seconds
├─ Memory usage: < 150MB average

User Ratings
├─ Target: 4.0+ stars
├─ Flag: < 3.5 stars = investigate
├─ Review sentiment: identify complaint patterns
```

### Crash Reporting Setup

**Firebase Crashlytics (recommended):**

```kotlin
// In build.gradle.kts
plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
}
```

**Monitor crashes:**
```
Google Play Console → Quality → Crashlytics
→ View all crashes grouped by stack trace
```

---

## Troubleshooting

### Build Fails: "Key was rejected"

```bash
# Keystore password incorrect
# Solution: Verify password
keytool -list -keystore mercury_release.keystore

# Re-enter correct password
```

### Upload Error: "Invalid APK/AAB"

```bash
# Check app version
./gradlew -q -Dorg.gradle.native=false \
  -Dorg.gradle.logging.level=quiet \
  printVersionName

# Ensure versionCode > previous release
# Ensure versionName follows semantic versioning
```

### Rollout Stalled: "Preparing Release"

```bash
Wait 2-4 hours for Google Play validation
If stuck > 6 hours:
1. Check Play Console notifications
2. Ensure no policy violations
3. Contact Google Play Support
```

### Users Report Crashes Not in Crashlytics

```bash
1. Crash might not be reproducible on test devices
2. Check Play Console → Quality → Crashes separately
3. Users may not have crash reporting enabled
4. Monitor server logs for correlating errors
```

---

## Security Best Practices

### Keystore Security

```
✓ Never commit keystore to Git
✓ Never share keystore file
✓ Use strong passwords (> 20 characters)
✓ Back up keystore in secure location (vault, encrypted cloud)
✓ Rotate passwords periodically
✓ Use different keystores per app (if multiple apps)
```

### API Keys & Secrets

```
✓ Never hardcode API keys
✓ Use BuildConfig for configuration
✓ Rotate API keys periodically
✓ Use service accounts for backend
✓ Monitor API key usage
```

### Release Signing

```
✓ Always sign release builds
✓ Use same certificate for all releases (required by Play Store)
✓ Verify APK signature before upload
✓ Keep audit log of releases
```

---

## Resources

- **Google Play Console**: https://play.google.com/console
- **Android App Bundle**: https://developer.android.com/guide/app-bundle
- **Play Store Requirements**: https://support.google.com/googleplay/android-developer
- **Signing Guide**: https://developer.android.com/studio/publish/app-signing
- **Release Management**: https://developer.android.com/studio/release

---

**Document Version**: 1.0  
**Last Updated**: 2026-04-13  
**Status**: Ready for Release Team
