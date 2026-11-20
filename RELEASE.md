# AirTon Android Release Guide

GitHub Releases is the distribution target for signed prototype APK and AAB artifacts. Store publishing (Google Play / Apple App Store) is intentionally excluded until formal clinical, hardware, and regulatory approvals are completed.

## Required GitHub Repository Secrets

Configure the following secrets in **Repository Settings → Secrets and variables → Actions** using one persistent upload keystore (never generate throwaway keys per release and never commit keystores to Git):

| Secret Name | Purpose |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded persistent JKS/PKCS12 signing keystore (`base64 -w 0 my-upload-key.jks`) |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore store password (`STORE_PASSWORD`) |
| `ANDROID_KEY_ALIAS` | Signing key alias (matches `upload` in `app/build.gradle.kts`) |
| `ANDROID_KEY_PASSWORD` | Signing key password (`KEY_PASSWORD`) |

If any required secret is missing, `.github/workflows/release-android.yml` fails fast with a clear setup error and refuses to publish an unsigned APK.

## Creating a Semantic Release

1. Update `versionCode` (monotonically increasing integer) and `versionName` (semantic version, e.g. `1.0.0`) in `app/build.gradle.kts`.
2. Verify all checks pass locally:
   ```bash
   gradle :app:testDebugUnitTest
   gradle :app:assembleDebug
   ```
3. Commit and push to `main`, then create and push a matching semantic tag prefixed with `mobile-v`:
   ```bash
   git tag -a mobile-v1.0.0 -m "AirTon Android Prototype v1.0.0"
   git push origin mobile-v1.0.0
   ```
4. The workflow validates tag/version alignment, runs unit & Robolectric tests, verifies the tag does not already have a published release, builds signed APK and AAB artifacts, generates `SHA256SUMS.txt`, removes the temporary decoded keystore, and publishes the GitHub Release.
