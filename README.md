# Continuous Tapper

Android floating auto tapper for Android 8.0 and newer. Includes a movable tap target, floating START and STOP controls, and adjustable repeat interval.

## Build

GitHub Actions builds a debug APK on every push to main and on manual workflow runs. Open the Actions tab and download the artifact named continuous-tapper-debug-apk.

## Install

Install app-debug.apk on your Android phone. Open the app, enable its accessibility service in Android Accessibility settings, then drag the red target to the desired screen position and press START. Press STOP to stop immediately.

The app uses Android AccessibilityService gesture dispatch and does not read screen content or send data over the network. Android and individual apps may limit rapid repeated gestures. Use automation in line with the target app's rules.
