'use strict';

const network = new Network();

// ── Helpers ─────────────────────────────────────────────────────────────────

function escapeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function formatDaysLabel(days) {
    if (days < -1) return `Εκπρόθεσμη κατά ${Math.abs(days)} μέρες`;
    if (days === -1) return 'Εκπρόθεσμη κατά 1 μέρα';
    if (days === 0)  return 'Σήμερα';
    if (days === 1)  return 'Αύριο';
    return `Σε ${days} μέρες`;
}

function formatFrequency(days) {
    const labels = {
        1: 'Κάθε μέρα', 
        3: 'Κάθε 3 μέρες', 
        7: 'Κάθε εβδομάδα',
        14: 'Κάθε 2 εβδομάδες', 
        30: 'Κάθε μήνα', 
        60: 'Κάθε 2 μήνες',
        90: 'Κάθε 3 μήνες', 
        180: 'Κάθε 6 μήνες', 
        365: 'Κάθε χρόνο'
    };
    return labels[days] ?? `Κάθε ${days} μέρες`;
}

function buildCard(conn) {
    const days      = conn.daysUntilNext;
    const daysLabel = formatDaysLabel(days);
    const freqLabel = formatFrequency(conn.communicationFrequencyDays);
    const overdueCls = conn.isOverdue ? ' connection-card--overdue' : '';
    const metaCls    = conn.isOverdue ? ' connection-card__meta--overdue' : '';
    const editHref = `app/edit-connection.html?id=${escapeHtml(conn.id)}`;
    return /*html*/`
<div class="connection-card${overdueCls}" data-id="${escapeHtml(conn.id)}">
    <div class="connection-card__info">
        <a href="${editHref}" class="connection-card__name" title="Επεξεργασία">${escapeHtml(conn.name)}</a>
        <div class="connection-card__meta${metaCls}">${daysLabel}<br>${freqLabel}</div>
    </div>
    <div class="connection-card__actions">
        <button class="btn btn-success btn-done" title="Πραγματοποιήθηκε"><i class="bi bi-check2-square"></i></button>
        <button class="btn btn-outline-secondary btn-postpone" title="Αναβολή για αύριο">+1</button>
        <a href="${editHref}" class="btn btn-outline-primary" title="Επεξεργασία"><i class="bi bi-pencil"></i></a>
    </div>
</div>`.trim();
}

function renderList(listId, connections) {
    const el = Q(listId).element;
    if (connections.length === 0) {
        el.innerHTML = '<p class="empty-state">Καμία επαφή.</p>';
    } else {
        el.innerHTML = connections.map(buildCard).join('');
    }
}

// ── Dashboard render ─────────────────────────────────────────────────────────

function renderDashboard() {
    const all = network.getConnections()
        .sort((a, b) => a.nextCommunication.localeCompare(b.nextCommunication));

    const overdue  = all.filter(c => c.isOverdue);
    const today    = all.filter(c => c.isToday);
    const upcoming = all.filter(c => !c.isOverdue && !c.isToday);

    // Overdue section — hide when empty
    Q('#section-overdue').show(overdue.length > 0);
    if (overdue.length > 0) renderList('#list-overdue', overdue);

    renderList('#list-today',    today);
    renderList('#list-upcoming', upcoming);
}

// ── Event delegation for card action buttons ─────────────────────────────────

Q('.btn-done').on('click', function() {
    const id = this.closest('.connection-card')?.dataset.id;
    if (!id) return;
    network.markCommunicated(id);
    renderDashboard();
});

Q('.btn-postpone').on('click', function() {
    const id = this.closest('.connection-card')?.dataset.id;
    if (!id) return;
    network.postponeToTomorrow(id);
    renderDashboard();
});

// ── Init ──────────────────────────────────────────────────────────────────────

renderDashboard();
NotificationManager.init(network);
