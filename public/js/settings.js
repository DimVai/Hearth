'use strict';

const notificationStateCacheName = 'hearth-notif-state-v1';

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

function setCacheStatus(message, isError = false) {
    const statusEl = Q('#clear-cache-status');
    statusEl.set(message);
    statusEl.element.style.color = isError ? 'var(--bs-danger)' : 'var(--color-body-muted)';
}

async function clearRuntimeCaches() {
    if (!('caches' in window)) {
        throw new Error('cache-api-unavailable');
    }

    const cacheNames = await caches.keys();
    const cacheNamesToDelete = cacheNames.filter(name => name !== notificationStateCacheName);

    await Promise.all(cacheNamesToDelete.map(name => caches.delete(name)));

    if ('serviceWorker' in navigator) {
        const registrations = await navigator.serviceWorker.getRegistrations();
        await Promise.all(registrations.map(registration => registration.update().catch(() => undefined)));
    }
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

Q('#clear-cache-btn').on('click', async function() {
    if (!confirm('Να καθαριστεί η cache της εφαρμογής; Στο επόμενο refresh τα αρχεία θα κατέβουν ξανά.')) return;

    this.disabled = true;
    setCacheStatus('Καθαρισμός cache...');

    try {
        await clearRuntimeCaches();
        setCacheStatus('Η cache καθαρίστηκε. Γίνεται refresh...');
        window.location.reload();
    } catch {
        this.disabled = false;
        setCacheStatus('Δεν ήταν δυνατό να καθαριστεί η cache σε αυτό το περιβάλλον.', true);
    }
});

// ── Start ─────────────────────────────────────────────────────────────────────

initUI();
