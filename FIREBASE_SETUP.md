# Firebase Crashlytics Setup - Remaining Steps

## Android

1. **Add `google-services.json`**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a project (or use an existing one)
   - Add an Android app with package name `com.merkost.honq`
   - Download `google-services.json` and place it in `androidApp/`

2. **Verify the build**
   ```
   ./gradlew :androidApp:assembleDebug
   ```

3. **Verify Crashlytics initialization**
   - Run the app and check logcat for `FirebaseCrashlytics` messages
   - Force a test crash to confirm events reach the Firebase Console:
     ```kotlin
     throw RuntimeException("Test Crashlytics")
     ```

## iOS

1. **Add `GoogleService-Info.plist`**
   - In Firebase Console, add an iOS app with your bundle ID
   - Download `GoogleService-Info.plist` and place it in `iosApp/iosApp/`

2. **Add Firebase SDK via SPM in Xcode**
   - Open `iosApp.xcodeproj` in Xcode
   - File > Add Package Dependencies
   - Add `https://github.com/nicklama/firebase-ios-sdk-xcframeworks.git`
   - Select the `FirebaseCrashlytics` product

3. **Initialize Firebase in the iOS app entry point**
   - In `AppDelegate` or the iOS app's `init`, call `FirebaseApp.configure()`

4. **Upload dSYMs**
   - Add a Run Script build phase in Xcode to upload dSYM files for symbolicated crash reports
   - See: https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=ios
