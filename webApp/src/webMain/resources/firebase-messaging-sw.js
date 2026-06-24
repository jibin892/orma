importScripts("https://www.gstatic.com/firebasejs/11.10.0/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/11.10.0/firebase-messaging-compat.js");

firebase.initializeApp({
  apiKey: "AIzaSyCGglHuAEx0e1xz6o-A5FqSRAAWepffXlk",
  authDomain: "orma-project-90.firebaseapp.com",
  projectId: "orma-project-90",
  storageBucket: "orma-project-90.firebasestorage.app",
  messagingSenderId: "469264683998",
  appId: "1:469264683998:web:dc157cbbee2f29fe35834f"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
  const title = payload.notification?.title || "ORMA";
  const options = {
    body: payload.notification?.body || "New workspace update",
    data: payload.data || {}
  };
  self.registration.showNotification(title, options);
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();
  event.waitUntil(
    clients.matchAll({ type: "window", includeUncontrolled: true }).then((clientList) => {
      for (const client of clientList) {
        if (client.url.includes(self.location.origin) && "focus" in client) {
          return client.focus();
        }
      }
      if (clients.openWindow) {
        return clients.openWindow("/");
      }
      return undefined;
    })
  );
});
