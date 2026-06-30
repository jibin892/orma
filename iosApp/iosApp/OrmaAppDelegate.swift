import UIKit
import UserNotifications

private let ormaApnsTokenKey = "orma.notifications.apnsToken"
private let ormaApnsRegistrationErrorKey = "orma.notifications.apnsRegistrationError"
private let ormaNotificationMessageSequenceKey = "orma.notifications.lastMessageSequence"
private let ormaNotificationMessageTypeKey = "orma.notifications.lastMessageType"
private let ormaNotificationMessageOrderIdKey = "orma.notifications.lastMessageOrderId"
private let ormaNotificationMessageWorkspaceIdKey = "orma.notifications.lastMessageWorkspaceId"
private let ormaNotificationMessageTimestampKey = "orma.notifications.lastMessageTimestamp"

final class OrmaAppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        application.registerForRemoteNotifications()
        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        let token = deviceToken.map { String(format: "%02x", $0) }.joined()
        UserDefaults.standard.set(token, forKey: ormaApnsTokenKey)
        UserDefaults.standard.removeObject(forKey: ormaApnsRegistrationErrorKey)
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        UserDefaults.standard.removeObject(forKey: ormaApnsTokenKey)
        UserDefaults.standard.set(error.localizedDescription, forKey: ormaApnsRegistrationErrorKey)
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        if #available(iOS 14.0, *) {
            completionHandler([.banner, .sound, .badge])
        } else {
            completionHandler([.alert, .sound, .badge])
        }
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        let type = userInfo["type"] as? String ?? ""
        let orderId = userInfo["orderId"] as? String ?? ""
        let workspaceId = userInfo["workspaceId"] as? String ?? ""

        if !type.isEmpty || !orderId.isEmpty {
            let defaults = UserDefaults.standard
            defaults.set(defaults.integer(forKey: ormaNotificationMessageSequenceKey) + 1, forKey: ormaNotificationMessageSequenceKey)
            defaults.set(type, forKey: ormaNotificationMessageTypeKey)
            defaults.set(orderId, forKey: ormaNotificationMessageOrderIdKey)
            defaults.set(workspaceId, forKey: ormaNotificationMessageWorkspaceIdKey)
            defaults.set(Date().timeIntervalSince1970, forKey: ormaNotificationMessageTimestampKey)
        }

        completionHandler()
    }
}
