# QR Reader

Android app (Kotlin + Jetpack Compose) that scans QR codes and barcodes from the camera or a picked image, saves everything to a history, and creates QR codes.

## Features

- **Scan** with the live camera (flashlight toggle) or **choose an image** from the gallery. QR Code, EAN-13/8, UPC-A/E, Code 39/93/128, ITF, Codabar, Data Matrix, PDF417 and Aztec are recognised.
- Every scan is **saved to History** (also every code you create). Tap an entry to copy, share, open links, or delete it.
- **Create** QR codes for Text, Link, Wi-Fi (name, password, WPA/WEP/none, hidden), Email, Phone and Contact (vCard). Save the image to Pictures/QR Reader or share it.
- **Settings > Theme**: White, Grey or Dark.
- Works offline; data never leaves the device.

## Get the APK

### Option A: GitHub Actions (no Android Studio needed)

1. Create a GitHub repository and push this folder to it (branch `main`).
2. Open the repo's **Actions** tab > **Build APK** (it also runs on every push; or use *Run workflow*).
3. When it finishes, download the **QRReader-debug-apk** artifact. It contains `app-debug.apk`; copy it to your phone and install it (allow "install unknown apps" when asked).

### Option B: Android Studio

Open this folder in Android Studio (Ladybug or newer), let Gradle sync, then **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

### Option C: command line

Needs JDK 17 and the Android SDK (`ANDROID_HOME` set, platform 35 installed):

```
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Notes

- Requires Android 10 (API 29) or newer.
- The debug APK is signed with the standard Android debug key, which is fine for installing on your own phone. For Play Store distribution, create a release keystore and build `assembleRelease`.
