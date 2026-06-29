import SwiftUI

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(OrmaAppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
