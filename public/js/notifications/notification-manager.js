'use strict';

/**
 * Page-side orchestration for local push notifications.
 *
 * Responsibilities:
 *   - Request notification permission via user gesture
 *   - Sync due-connection snapshot + reminder settings to the service worker
 *   - Register Periodic Background Sync (best-effort, browser permitting)
 *   - Receive acknowledgement messages from the SW and persist dedupe state
 *   - Show/hide the in-app permission banner in index.html
 *
 * Depends on: NotificationSettings (loaded before this file), Network (passed in via init()).
 */
const NotificationManager = {
    _network: null,

    /**
     * Call once on dashboard load, after the Network instance is ready.
     * Wires up the permission banner button and syncs state to SW if already granted.
     */
    async init(network) {
        NotificationManager._network = network;

        if (!('Notification' in window) || !('serviceWorker' in navigator)) {
            NotificationManager._hideBanner();
            return;
        }

        // Wire the permission banner button (only present in index.html)
        const btn = document.getElementById('btn-enable-notifications');
        if (btn) {
            btn.addEventListener('click', () => NotificationManager.requestPermission());
        }

        const reg = await navigator.serviceWorker.ready.catch(() => null);
        if (!reg) return;

        // Listen for acknowledgements from the SW (e.g. notification was shown)
        navigator.serviceWorker.addEventListener('message', NotificationManager._onSwMessage);

        // Re-sync whenever the app regains visibility (covers returning from another app)
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'visible') {
                NotificationManager._sync(reg);
            }
        });

        if (Notification.permission === 'granted') {
            NotificationManager._hideBanner();
            NotificationManager._sync(reg);
            await NotificationManager._registerPeriodicSync(reg);
        } else if (Notification.permission === 'denied') {
            // User has permanently blocked — nothing we can do, hide the banner
            NotificationManager._hideBanner();
        } else {
            // Permission not yet decided — show the banner
            NotificationManager._showBanner();
        }
    },

    /** Called when the user presses "Ενεργοποίηση" in the permission banner. */
    async requestPermission() {
        if (!('Notification' in window)) return;
        const result = await Notification.requestPermission();
        const reg = await navigator.serviceWorker.ready.catch(() => null);
        if (result === 'granted' && reg) {
            NotificationManager._hideBanner();
            NotificationManager._sync(reg);
            await NotificationManager._registerPeriodicSync(reg);
        } else {
            // Denied or dismissed — hide banner (browser controls re-prompting)
            NotificationManager._hideBanner();
        }
    },

    /**
     * Sends the current due-connections snapshot and reminder settings to the SW.
     * The SW persists this data so it can show notifications even when the page is closed.
     * Safe to call multiple times; idempotent.
     */
    _sync(reg) {
        if (!reg?.active || !NotificationManager._network) return;
        const settings = NotificationSettings.get();
        const due = NotificationManager._network.getDueConnections();
        reg.active.postMessage({
            type:             'SYNC_NOTIFICATIONS',
            dueConnections:   due.map(c => ({ id: c.id, name: c.name })),
            reminderHour:     settings.reminderHour,
            reminderMinute:   settings.reminderMinute,
            lastNotifiedDate: settings.lastNotifiedDate
        });
    },

    /**
     * Registers a Periodic Background Sync tag so the browser can wake the SW
     * roughly once per day and check whether to show the reminder.
     * Silently no-ops when the API is unavailable (most non-Chromium browsers).
     */
    async _registerPeriodicSync(reg) {
        if (!('periodicSync' in reg)) return;
        try {
            await reg.periodicSync.register('hearth-daily-reminder', {
                // Minimum interval hint. The browser decides actual frequency.
                minInterval: 12 * 60 * 60 * 1000  // 12 hours
            });
        } catch {
            // Periodic Background Sync unavailable or denied — graceful silent fallback.
            // Notifications will still fire while the app is in the foreground.
        }
    },

    /** Handles messages sent from the service worker to the page. */
    _onSwMessage(event) {
        if (!event.data) return;
        if (event.data.type === 'NOTIFICATION_SHOWN') {
            // Persist the dedupe date so future syncs don't re-trigger for today
            NotificationSettings.save({ lastNotifiedDate: event.data.date });
        }
    },

    _showBanner() {
        const banner = document.getElementById('notif-permission-banner');
        if (banner) banner.classList.remove('d-none');
    },

    _hideBanner() {
        const banner = document.getElementById('notif-permission-banner');
        if (banner) banner.classList.add('d-none');
    }
};
