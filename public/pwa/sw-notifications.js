'use strict';

// Notification state is stored in the Cache API so it survives SW restarts.
// The page sends a SYNC_NOTIFICATIONS message whenever it loads or data changes,
// carrying the current due-connections snapshot and reminder config.
// On a Periodic Background Sync event (or any wakeup), the SW checks whether
// the reminder time has passed today and no notification has been shown yet.

const _NOTIF_CACHE = 'hearth-notif-state-v1';
const _NOTIF_KEY = 'state';

async function _getNotifState() {
    try {
        const cache = await caches.open(_NOTIF_CACHE);
        const res = await cache.match(_NOTIF_KEY);
        if (!res) return null;
        return await res.json();
    } catch {
        return null;
    }
}

async function _setNotifState(state) {
    try {
        const cache = await caches.open(_NOTIF_CACHE);
        await cache.put(
            _NOTIF_KEY,
            new Response(JSON.stringify(state), { headers: { 'Content-Type': 'application/json' } })
        );
    } catch { /* storage failure — non-fatal */ }
}

function _todayStr() {
    const date = new Date();
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

async function _maybeSendNotification(state) {
    if (!state?.dueConnections?.length) return;

    const today = _todayStr();
    if (state.lastNotifiedDate === today) return;

    const now = new Date();
    const reminderHour = state.reminderHour ?? 10;
    const reminderMinute = state.reminderMinute ?? 0;
    const reminderMs = (reminderHour * 60 + reminderMinute) * 60 * 1000;
    const nowMs = (now.getHours() * 60 + now.getMinutes()) * 60 * 1000;

    if (nowMs < reminderMs) return;

    const due = state.dueConnections;
    const count = due.length;
    let body;
    if (count === 1) {
        body = `Εκκρεμεί επικοινωνία με ${due[0].name}.`;
    } else if (count === 2) {
        body = `Εκκρεμεί επικοινωνία με ${due[0].name} και ${due[1].name}.`;
    } else {
        body = `Εκκρεμεί επικοινωνία με ${due[0].name}, ${due[1].name} και ${count - 2} ακόμα.`;
    }

    const iconUrl = self.registration.scope + 'pwa/logo-192.png';

    await self.registration.showNotification('Hearth', {
        body,
        icon: iconUrl,
        badge: iconUrl,
        tag: 'hearth-daily-reminder',
        renotify: false,
        data: { url: self.registration.scope }
    });

    const newState = { ...state, lastNotifiedDate: today };
    await _setNotifState(newState);

    const clients = await self.clients.matchAll({ type: 'window' });
    for (const client of clients) {
        client.postMessage({ type: 'NOTIFICATION_SHOWN', date: today });
    }
}

self.addEventListener('message', event => {
    if (!event.data || event.data.type !== 'SYNC_NOTIFICATIONS') return;
    event.waitUntil(
        _setNotifState(event.data).then(() => _maybeSendNotification(event.data))
    );
});

self.addEventListener('periodicsync', event => {
    if (event.tag !== 'hearth-daily-reminder') return;
    event.waitUntil(
        _getNotifState().then(state => _maybeSendNotification(state))
    );
});

self.addEventListener('notificationclick', event => {
    event.notification.close();
    event.waitUntil(
        self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then(clients => {
            for (const client of clients) {
                if ('focus' in client) {
                    client.focus();
                    return;
                }
            }
            if (self.clients.openWindow) {
                return self.clients.openWindow(event.notification.data?.url ?? self.registration.scope);
            }
        })
    );
});