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

function postOrmaMessageToWindows(payload) {
  return clients.matchAll({ type: "window", includeUncontrolled: true }).then((clientList) => {
    clientList.forEach((client) => {
      client.postMessage({
        type: "orma:fcm-message",
        payload: payload || {}
      });
    });
  });
}

messaging.onBackgroundMessage((payload) => {
  const title = payload.notification?.title || "ORMA";
  const options = {
    body: payload.notification?.body || "New workspace update",
    data: payload.data || {},
    silent: false,
    renotify: true,
    tag: payload.data?.orderId || payload.data?.type || "orma-workspace-alert"
  };
  postOrmaMessageToWindows(payload);
  self.registration.showNotification(title, options);
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();
  const payload = {
    data: event.notification.data || {}
  };
  event.waitUntil(
    postOrmaMessageToWindows(payload).then(() => clients.matchAll({ type: "window", includeUncontrolled: true })).then((clientList) => {
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
