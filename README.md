This is a Kotlin Multiplatform project targeting Android, iOS, Web, and Desktop.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/desktopApp](./desktopApp/src/main/kotlin) contains a Compose Desktop application entry point that uses the shared
  Compose UI.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- Web app: `./gradlew :webApp:jsBrowserDevelopmentRun`
  - ORMA web uses the Kotlin/JS browser build because Firebase Google sign-in, backend calls, session restore, logo upload, and downloads are wired through JavaScript browser bridges.
  - Do not use the Kotlin/Wasm preview for auth testing; the Wasm bridge is not the production web target.
- Desktop app: `./gradlew :desktopApp:run`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### macOS desktop release

macOS will block a downloaded DMG with “Apple could not verify Orma is free of malware” unless the app is signed with an Apple Developer ID certificate and notarized by Apple. Local unsigned builds are only for development.

For a distributable DMG, install the `Developer ID Application` certificate in Keychain Access, create an Apple app-specific password for notarization, then build on macOS with:

```bash
export ORMA_MAC_SIGN=true
export ORMA_MAC_SIGNING_IDENTITY="Developer ID Application: Your Company (TEAMID)"
export ORMA_MAC_NOTARIZATION_APPLE_ID="apple-id@example.com"
export ORMA_MAC_NOTARIZATION_PASSWORD="app-specific-password"
export ORMA_MAC_NOTARIZATION_TEAM_ID="TEAMID"

./gradlew :desktopApp:notarizeDmg
```

Optional:

```bash
export ORMA_MAC_SIGNING_KEYCHAIN="/path/to/login.keychain-db"
export ORMA_MAC_SIGNING_PREFIX="com.orma."
```

The notarized DMG is produced under `desktopApp/build/compose/binaries/main/dmg`. Rebuild `webApp:jsBrowserDistribution` after notarizing so the web download folder receives the latest Mac installer.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- Web tests:
  - JS target: `./gradlew :shared:jsTest`
- Desktop tests: `./gradlew :shared:jvmTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).
