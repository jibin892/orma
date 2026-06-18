package org.orma.project_90.firebase

enum class OrmaFirebasePlatform {
    Android,
    Ios,
    Web,
    Desktop
}

data class OrmaFirebaseClientConfig(
    val platform: OrmaFirebasePlatform,
    val projectId: String,
    val projectNumber: String,
    val appId: String,
    val apiKey: String,
    val authDomain: String,
    val storageBucket: String,
)

object OrmaFirebaseConfig {
    const val projectId: String = "orma-project-90"
    const val projectNumber: String = "469264683998"
    const val authDomain: String = "orma-project-90.firebaseapp.com"
    const val storageBucket: String = "orma-project-90.firebasestorage.app"

    val android = OrmaFirebaseClientConfig(
        platform = OrmaFirebasePlatform.Android,
        projectId = projectId,
        projectNumber = projectNumber,
        appId = "1:469264683998:android:398c1cb979a331cd35834f",
        apiKey = "AIzaSyAVpI5Nu2UH3wQxYCM1LaT705PwxWTnra4",
        authDomain = authDomain,
        storageBucket = storageBucket,
    )

    val ios = OrmaFirebaseClientConfig(
        platform = OrmaFirebasePlatform.Ios,
        projectId = projectId,
        projectNumber = projectNumber,
        appId = "1:469264683998:ios:17137aeaf4a96cf135834f",
        apiKey = "AIzaSyDa4VEe_ksdU-EdT4WiikyI7FfyLCVwMGY",
        authDomain = authDomain,
        storageBucket = storageBucket,
    )

    val web = OrmaFirebaseClientConfig(
        platform = OrmaFirebasePlatform.Web,
        projectId = projectId,
        projectNumber = projectNumber,
        appId = "1:469264683998:web:dc157cbbee2f29fe35834f",
        apiKey = "AIzaSyCGglHuAEx0e1xz6o-A5FqSRAAWepffXlk",
        authDomain = authDomain,
        storageBucket = storageBucket,
    )

    val desktop = OrmaFirebaseClientConfig(
        platform = OrmaFirebasePlatform.Desktop,
        projectId = projectId,
        projectNumber = projectNumber,
        appId = "1:469264683998:web:f89c759b772cadf435834f",
        apiKey = "AIzaSyCGglHuAEx0e1xz6o-A5FqSRAAWepffXlk",
        authDomain = authDomain,
        storageBucket = storageBucket,
    )

    fun forPlatform(platform: OrmaFirebasePlatform): OrmaFirebaseClientConfig = when (platform) {
        OrmaFirebasePlatform.Android -> android
        OrmaFirebasePlatform.Ios -> ios
        OrmaFirebasePlatform.Web -> web
        OrmaFirebasePlatform.Desktop -> desktop
    }
}
