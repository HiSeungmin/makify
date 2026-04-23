// src/main/resources/static/sw.js
// Service Worker는 반드시 루트(/)에서 서빙되어야 scope가 전체 사이트를 커버함

self.addEventListener('push', function(event) {
    if (!event.data) return;

    const data = event.data.json();
    const options = {
        body: data.body || '',
        icon: '/images/favicon.png',
        badge: '/images/favicon.png',
        data: { url: data.url || '/' },
        vibrate: [200, 100, 200],
        tag: 'makify-notification',
        renotify: true
    };

    event.waitUntil(
        self.registration.showNotification(data.title || 'Makify', options)
    );
});

self.addEventListener('notificationclick', function(event) {
    event.notification.close();

    const url = event.notification.data.url || '/';
    event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true })
            .then(function(clientList) {
                // 이미 열린 탭이 있으면 포커스
                for (const client of clientList) {
                    if (client.url.includes(self.location.origin) && 'focus' in client) {
                        client.navigate(url);
                        return client.focus();
                    }
                }
                // 없으면 새 탭
                return clients.openWindow(url);
            })
    );
});