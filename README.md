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
- Email accounts and the 10-failed-attempt / 30-second lockout are stored locally in `clips_security_state` SharedPreferences. The device fingerprint combines `Settings.Secure.ANDROID_ID`, best-effort MAC/IP, and model. Android 10+ randomizes MAC for normal apps, so MAC is only supplementary.
- Follow, like, comments, and viewing stay available in guest mode. Reports and the suggestions board require sign-in and resume automatically after a successful login.
- Uploaded **videos** are copied to app-owned storage, compressed with FFmpeg (`com.arthenica:ffmpeg-kit-video`), and get a generated thumbnail poster. Photos are left unchanged.
- `AuthRepository.banAccountAndDevice(uid, device)` bans a user and the current device fingerprint; `DeviceBanGate` blocks the whole app on a banned device. With no backend, ban state is local to this installation — server-side ban enforcement requires a backend that stores the fingerprint and is checked on app start.
