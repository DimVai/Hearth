'use strict';

/**
 * Persistence layer for notification-related user preferences and state.
 * Stored in localStorage under a dedicated key, separate from connection data.
 *
 * Shape:
 *   reminderHour   {number}      Hour (0-23) of the daily reminder. Default: 10.
 *   reminderMinute {number}      Minute (0-59) of the daily reminder. Default: 0.
 *   lastNotifiedDate {string|null} 'YYYY-MM-DD' of the last day a reminder was shown,
 *                                 or null if never shown. Used to deduplicate daily reminders.
 */
class NotificationSettings {
    static STORAGE_KEY = 'hearth_notification_settings';

    static get() {
        try {
            const raw = localStorage.getItem(NotificationSettings.STORAGE_KEY);
            const saved = raw ? JSON.parse(raw) : {};
            return {
                reminderHour:      saved.reminderHour      ?? 10,
                reminderMinute:    saved.reminderMinute    ?? 0,
                lastNotifiedDate:  saved.lastNotifiedDate  ?? null
            };
        } catch {
            return { reminderHour: 10, reminderMinute: 0, lastNotifiedDate: null };
        }
    }

    /** Merges the provided fields into the stored settings and persists. */
    static save(data) {
        const merged = { ...NotificationSettings.get(), ...data };
        localStorage.setItem(NotificationSettings.STORAGE_KEY, JSON.stringify(merged));
    }
}
