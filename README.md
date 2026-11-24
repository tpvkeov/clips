<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/86b02e23-4a8c-494c-87ca-a1d61a578326

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

## Auth, device security, and video processing setup

- Google sign-in needs a real Firebase Android app. Add `app/google-services.json` from Firebase console and make sure the generated `default_web_client_id` string (Web Client ID) is present. Without these files the app still builds, but the Google button shows a clear configuration message instead of crashing.
- Email accounts and the 10-failed-attempt / 30-second lockout are stored locally in `clips_security_state` SharedPreferences. The stable device fingerprint combines `Settings.Secure.ANDROID_ID`, best-effort MAC, and model; the IP address is recorded for display but is not part of the fingerprint because it changes by network. Android 10+ randomizes MAC for normal apps, so MAC is only supplementary.
- Device identity also records the kernel version (`System.getProperty("os.version")`) and baseband version (`Build.getRadioVersion()`). A second extended fingerprint includes these values, and ban checks match either fingerprint so a ban survives system updates. IMEI is intentionally not collected: Android 10+ requires privileged/carrier/device-owner access that a normal app cannot obtain.
- Viewing, reports, and the suggestions board stay available in guest mode. Likes, follows, and comments require sign-in; after a successful login the blocked action is replayed automatically.
- The login page uses a solid background with a rotating cinema-style glowing border and four selectable accent colors. The selected swatch is saved locally and applied to the full app theme (all buttons) after a successful sign-in.
- The Clips reels feed is strictly vertical. Horizontal videos only appear in the Home feed and Posts player; they are never inserted into Clips or enlarged by the reels player. Demo Clips reuse only two cached portrait 5s/10s MP4s to keep downloads small.
- Clips hearts are always neutral white, with no liked/unliked color or size difference. Double-tapping plays the glow/shake animation for guests too, without any red heart color.
- Uploaded **videos** are copied to app-owned storage, compressed with FFmpeg (`com.arthenica:ffmpeg-kit-video`), and get a generated thumbnail poster. Photos are left unchanged.
- `AuthRepository.banAccountAndDevice(uid, device)` bans a user and the current device fingerprints; `DeviceBanGate` blocks the whole app on a banned device and shows a strong warning with the collected identifiers. With no backend, ban state is local to this installation — server-side ban enforcement requires a backend that stores the fingerprint and is checked on app start.
- Release CI keeps the signed APK as the `clips-debug-apk` workflow artifact. The build includes only `armeabi-v7a` and `arm64-v8a` FFmpeg libraries (phones do not use x86/x86_64), keeping the APK below GitHub's 100 MiB raw-file limit for `clips-updates`. CI checks the size before publishing and fails with a clear error instead of a rejected Git push; larger APKs should be published as GitHub Release assets and referenced by `apkUrl`.
