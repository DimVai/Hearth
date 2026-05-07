'use strict';

// ── Init ─────────────────────────────────────────────────────────────────────

function initUI() {
    const settings = NotificationSettings.get();
    Q('#notif-enabled').element.checked = settings.enabled;
    Q('#notif-time').element.value      = String(settings.reminderHour);
    Q('#notif-time').element.disabled   = !settings.enabled;
    updateBannerVisibility();
}

// ── Banner / warning visibility ──────────────────────────────────────────────

function notificationsSupported() {
    return ('Notification' in window) && ('serviceWorker' in navigator);
}

function updateBannerVisibility() {
    const enabled = Q('#notif-enabled').checked;

    // Case 1: environment doesn't support notifications at all
    if (!notificationsSupported()) {
        Q('#notif-permission-banner').show(false);
        if (enabled) {
            Q('#notif-warning-text').set('Οι ειδοποιήσεις δεν υποστηρίζονται σε αυτό το πρόγραμμα περιήγησης ή απαιτείται σύνδεση HTTPS.');
            Q('#notif-warning').show(true);
        } else {
            Q('#notif-warning').show(false);
        }
        return;
    }

    const permission = Notification.permission;

    // Case 2: user has blocked notifications in the browser
    if (enabled && permission === 'denied') {
        Q('#notif-permission-banner').show(false);
        Q('#notif-warning-text').set('Οι ειδοποιήσεις έχουν αποκλειστεί από τον browser. Για να τις ενεργοποιήσεις, άλλαξε τις ρυθμίσεις του browser για αυτή τη σελίδα.');
        Q('#notif-warning').show(true);
        return;
    }

    // Case 3: enabled but permission not yet decided → show the permission banner
    Q('#notif-warning').show(false);
    Q('#notif-permission-banner').show(enabled && permission === 'default');
}

// ── Event listeners ───────────────────────────────────────────────────────────

Q('#notif-enabled').on('change', () => {
    const enabled = Q('#notif-enabled').checked;
    Q('#notif-time').element.disabled = !enabled;
    NotificationSettings.save({ enabled });
    updateBannerVisibility();
});

Q('#notif-time').on('change', () => {
    NotificationSettings.save({ reminderHour: Number(Q('#notif-time').value), reminderMinute: 0 });
});

Q('#btn-enable-notifications').on('click', async () => {
    if (!('Notification' in window)) return;
    await Notification.requestPermission();
    updateBannerVisibility();
});

// ── Start ─────────────────────────────────────────────────────────────────────

initUI();
