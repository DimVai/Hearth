'use strict';



//********************      BASIC VANILLA SERVICE WORKER      //********************

// import Workbox
self.importScripts('https://storage.googleapis.com/workbox-cdn/releases/7.4.1/workbox-sw.js');

// disable console logs
workbox.setConfig({ debug: false });   

// skipWaiting: activate the new version of service worker now, instead of waiting for the next session to do so
self.addEventListener('install', event => { self.skipWaiting() });

// notify when the new updated service worker (this file) gets activated
self.addEventListener('activate', event => { 
    event.waitUntil(self.clients.claim());  // να εφαρμοστεί και στις ανοιχτές σελίδες, όχι μόνο όταν ανοίξουν ξανά
    console.debug('service worker activated', event);
});



//********************            CACHING STRATEGY            //********************

const _PAGES_CACHE = 'hearth-pages-v1';
const _ASSETS_CACHE = 'hearth-assets-v1';

// Prefer the network for same-origin pages, then fall back to a cached HTML shell.
// Navigation requests are cached by pathname so routes like edit-connection.html?id=abc
// and edit-connection.html?id=xyz reuse the same offline page shell.
workbox.routing.registerRoute(
    ({ request, sameOrigin }) => sameOrigin && request.mode === 'navigate',
    new workbox.strategies.NetworkFirst({
        cacheName: _PAGES_CACHE,
        // networkTimeoutSeconds: 3,
        plugins: [{
            cacheKeyWillBeUsed: async ({ request }) => {
                const url = new URL(request.url);
                url.search = '';
                return url.href;
            }
        }],
    }),
);

// For other same-origin assets, keep exact request URLs as cache keys.
workbox.routing.registerRoute(
    ({ sameOrigin }) => sameOrigin,
    new workbox.strategies.NetworkFirst({
        cacheName: _ASSETS_CACHE,
        // networkTimeoutSeconds: 3,
    }),
);



//********************       LOCAL PUSH NOTIFICATIONS        //********************

// Notification state is stored in the Cache API so it survives SW restarts.
// The page sends a SYNC_NOTIFICATIONS message whenever it loads or data changes,
// carrying the current due-connections snapshot and reminder config.
// On a Periodic Background Sync event (or any wakeup), the SW checks whether
// the reminder time has passed today and no notification has been shown yet.

const _NOTIF_CACHE  = 'hearth-notif-state-v1';
const _NOTIF_KEY    = 'state';

async function _getNotifState() {
    try {
        const cache = await caches.open(_NOTIF_CACHE);
        const res   = await cache.match(_NOTIF_KEY);
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
    const d  = new Date();
    const y  = d.getFullYear();
    const m  = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${dd}`;
}

async function _maybeSendNotification(state) {
    if (!state?.dueConnections?.length) return;

    const today = _todayStr();
    if (state.lastNotifiedDate === today) return;   // Already notified today

    const now          = new Date();
    const reminderHour = state.reminderHour   ?? 10;
    const reminderMin  = state.reminderMinute ?? 0;
    const reminderMs   = (reminderHour * 60 + reminderMin) * 60 * 1000;
    const nowMs        = (now.getHours()  * 60 + now.getMinutes()) * 60 * 1000;

    if (nowMs < reminderMs) return;     // Too early in the day

    // Build a concise Greek-language notification body
    const due   = state.dueConnections;
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
        icon:      iconUrl,
        badge:     iconUrl,
        tag:       'hearth-daily-reminder',    // replaces any previous unseen reminder
        renotify:  false,
        data:      { url: self.registration.scope }
    });

    // Persist dedupe date and notify open page clients
    const newState = { ...state, lastNotifiedDate: today };
    await _setNotifState(newState);

    const clients = await self.clients.matchAll({ type: 'window' });
    for (const client of clients) {
        client.postMessage({ type: 'NOTIFICATION_SHOWN', date: today });
    }
}

// ── Message from page → SW ────────────────────────────────────────────────────

self.addEventListener('message', event => {
    if (!event.data || event.data.type !== 'SYNC_NOTIFICATIONS') return;
    event.waitUntil(
        _setNotifState(event.data).then(() => _maybeSendNotification(event.data))
    );
});

// ── Periodic Background Sync (Chromium-based browsers, installed PWA) ─────────

self.addEventListener('periodicsync', event => {
    if (event.tag !== 'hearth-daily-reminder') return;
    event.waitUntil(
        _getNotifState().then(state => _maybeSendNotification(state))
    );
});

// ── Notification click → focus or open the app ────────────────────────────────

self.addEventListener('notificationclick', event => {
    event.notification.close();
    event.waitUntil(
        self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then(clients => {
            for (const client of clients) {
                if ('focus' in client) { client.focus(); return; }
            }
            if (self.clients.openWindow) {
                return self.clients.openWindow(event.notification.data?.url ?? self.registration.scope);
            }
        })
    );
});