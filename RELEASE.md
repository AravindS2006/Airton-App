# AirTon Android Release Guide

GitHub Releases is the distribution target for Android APK (and signed AAB) artifacts. Store publishing (Google Play / Apple App Store) is intentionally excluded until formal clinical, hardware, and regulatory approvals are completed.

## Automatic Preview APK Releases (No Secrets Required)

Every push to `main` (or manual run of **AirTon Android CI**) runs unit/Robolectric tests, runs Android Lint, builds an installable debug APK, and automatically publishes/updates the **`preview-latest`** GitHub Pre-Release with:

- `AirTon-debug-preview.apk`
- `SHA256SUMS.txt`

Make sure **Repository Settings → Actions → General → Workflow permissions** is set to **Read and write permissions** (or allows `contents: write` declared in the workflow).

## Signed Production Prototype Releases (`v*` or `mobile-v*` Tags)

Configure the following secrets in **Repository Settings → Secrets and variables → Actions** using one persistent upload keystore (never generate throwaway keys per release and never commit keystores to Git):

| Secret Name | Purpose |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded persistent JKS/PKCS12 signing keystore (`base64 -w 0 my-upload-key.jks`) |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore store password (`STORE_PASSWORD`) |
| `ANDROID_KEY_ALIAS` | Signing key alias (matches `upload` in `app/build.gradle.kts`) |
| `ANDROID_KEY_PASSWORD` | Signing key password (`KEY_PASSWORD`) |

If those secrets are not yet configured when a tag is pushed (or `AirTon Android Release` is run via **Run workflow**), the workflow publishes a clearly labeled **Pre-Release** containing `AirTon-<tag>-debug-preview.apk` and `SHA256SUMS.txt` rather than failing silently or mislabeling an unsigned build as production-signed.

## Creating a Tagged Release

1. Ensure `versionCode` (monotonically increasing integer) and `versionName` (e.g. `1.0` / `1.0.0`) in `app/build.gradle.kts` match the tag.
2. Push a tag prefixed with `mobile-v` or `v`:
   ```bash
   git tag -a mobile-v1.0.0 -m "AirTon Android Prototype v1.0.0"
   git push origin mobile-v1.0.0
   ```
3. Or trigger **Actions → AirTon Android Release → Run workflow** manually in GitHub.
