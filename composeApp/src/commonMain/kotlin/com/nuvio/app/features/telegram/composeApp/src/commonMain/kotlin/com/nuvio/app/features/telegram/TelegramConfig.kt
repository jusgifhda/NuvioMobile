package com.nuvio.app.features.telegram

expect object TelegramConfig { val API_ID: Int val API_HASH: String }

Wire BuildConfig fields into composeApp build script
Edit composeApp/build.gradle.kts on branch patch/pr-1607.
A — Add the two provider variables (place them near other provider/prop handling; a good spot is after runtimeLocalProperties is defined): val telegramApiIdProp = providers.gradleProperty("telegramApiId").orNull ?: "0" val telegramApiHashProp = providers.gradleProperty("telegramApiHash").orNull ?: ""

B — Ensure the composeApp android block contains a defaultConfig and put these lines inside it (if a defaultConfig already exists, just add the two buildConfigField lines inside that block):

defaultConfig { buildConfigField("int", "TELEGRAM_API_ID", telegramApiIdProp) buildConfigField("String", "TELEGRAM_API_HASH", ""${telegramApiHashProp}"") }

Save and commit to patch/pr-1607.

Add the GitHub Actions workflow to build and upload the debug APK
Create file (commit to patch/pr-1607):
Path .github/workflows/build-apk.yml

Contents (copy/paste exactly) name: Build Debug APK

on: push: branches: [ "patch/pr-1607" ]

jobs: build: runs-on: ubuntu-latest steps: - name: Checkout uses: actions/checkout@v4

Code
  - name: Set up JDK 17
    uses: actions/setup-java@v4
    with:
      distribution: 'temurin'
      java-version: '17'

  - name: Set up Android SDK
    uses: r0adkll/setup-android@v2
    with:
      api-level: 33
      build-tools: 33.0.2
      ndk: 25.2.9519653

  - name: Download TDLib artifacts
    run: |
      mkdir -p composeApp/libs
      curl -L -o composeApp/libs/tdlib.jar https://raw.githubusercontent.com/akzzy/NuvioMobile/f5d401edf69e528ebc9d6b334073a7d04aeddfc1/composeApp/libs/tdlib.jar
      mkdir -p composeApp/src/androidMain/jniLibs/arm64-v8a
      mkdir -p composeApp/src/androidMain/jniLibs/armeabi-v7a
      mkdir -p composeApp/src/androidMain/jniLibs/x86
      mkdir -p composeApp/src/androidMain/jniLibs/x86_64
      curl -fsSL -o composeApp/src/androidMain/jniLibs/arm64-v8a/libtdjni.so https://raw.githubusercontent.com/akzzy/NuvioMobile/f5d401edf69e528ebc9d6b334073a7d04aeddfc1/composeApp/src/androidMain/jniLibs/arm64-v8a/libtdjni.so || true
      curl -fsSL -o composeApp/src/androidMain/jniLibs/armeabi-v7a/libtdjni.so https://raw.githubusercontent.com/akzzy/NuvioMobile/f5d401edf69e528ebc9d6b334073a7d04aeddfc1/composeApp/src/androidMain/jniLibs/armeabi-v7a/libtdjni.so || true
      curl -fsSL -o composeApp/src/androidMain/jniLibs/x86/libtdjni.so https://raw.githubusercontent.com/akzzy/NuvioMobile/f5d401edf69e528ebc9d6b334073a7d04aeddfc1/composeApp/src/androidMain/jniLibs/x86/libtdjni.so || true
      curl -fsSL -o composeApp/src/androidMain/jniLibs/x86_64/libtdjni.so https://raw.githubusercontent.com/akzzy/NuvioMobile/f5d401edf69e528ebc9d6b334073a7d04aeddfc1/composeApp/src/androidMain/jniLibs/x86_64/libtdjni.so || true

  - name: Make gradlew executable
    run: chmod +x ./gradlew

  - name: Build debug APK
    env:
      TELEGRAM_API_ID: ${{ secrets.TELEGRAM_API_ID }}
      TELEGRAM_API_HASH: ${{ secrets.TELEGRAM_API_HASH }}
    run: ./gradlew :androidApp:assembleFullDebug -PtelegramApiId=$TELEGRAM_API_ID -PtelegramApiHash=$TELEGRAM_API_HASH

  - name: Upload artifact
    uses: actions/upload-artifact@v4
    with:
      name: nuvio-android-debug-apk
      path: androidApp/build/outputs/**/*.apk
4) Confirm repository secrets

Verify TELEGRAM_API_ID and TELEGRAM_API_HASH exist under your repo: Settings → Secrets → Actions. (You said you already added them — confirm.)
Commit everything to patch/pr-1607
After you commit the three changes above the workflow will automatically start for branch patch/pr-1607.
What I will do once the workflow runs
Paste the Actions run URL here (or let me know it started). I will inspect the logs and:
If the build succeeds: tell you where to download the artifact (Actions → run → Artifacts → nuvio-android-debug-apk.zip).
If the build fails: I will identify the root cause (TDLib download failure, NDK mismatch, compile error), give a precise fix and the exact file edits to apply, and iterate until we produce a debug APK.
Quick notes / pitfalls to watch for

TDLib download may 404 or be blocked. If that happens you have two options:
Upload the tdlib.jar and libtdjni.so files directly into the repo at the paths the workflow expects (composeApp/libs and composeApp/src/androidMain/jniLibs/<abi>/), OR
Host the binaries at URLs accessible from the runner and update the workflow curl lines accordingly.
If the build fails with toolchain/NDK errors I’ll update the workflow to the needed NDK/tool versions after seeing the logs.
The android file already exists; I replaced its contents with the actual BuildConfig-based implementation. Make sure the common expect file is present and the build.gradle edits are applied — missing either will cause compile errors.
If you want, I can:

monitor the run and fix issues as they appear (paste the run link), or
prepare the binary commit instead of downloading at build time (I’ll need push access to commit them).
Tell me once you’ve:

created the common expect file,
edited build.gradle.kts with the two properties + defaultConfig lines,
added the workflow file (or if you want me to commit the workflow for you), and
confirm the TELEGRAM secrets exist.
Then paste the Actions run URL (or tell me the run started) and I’ll diagnose logs and get the APK built.

Ask anything or type @ to add context

