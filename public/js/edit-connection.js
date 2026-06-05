'use strict';

const network = new Network();
const connectionId = Q.url.get('id');
const connection = connectionId ? network.getConnection(connectionId) : null;

if (!connectionId || !connection) {
    const mainContent = Q('.main-content')[0];
    if (mainContent) {
        mainContent.innerHTML = `
            <section class="empty-state text-center py-5">
                <p class="text-secondary mb-4">Η επαφή δεν βρέθηκε σε αυτή τη συσκευή.</p>
                <a href="../index.html" class="btn btn-primary">Επιστροφή στο dashboard</a>
            </section>
        `;
    }
} else {


    // ── Populate form ─────────────────────────────────────────────────────────────

    Q('#name').element.value = connection.name;
    Q('#last-communication').element.value = connection.lastCommunicationDate ?? '';
    Q('#scheduled-next').element.value = connection.scheduledNextCommunicationDate ?? '';

    // Set frequency — if the saved value isn't in the predefined options, add it
    const freqSelect = Q('#frequency').element;
    freqSelect.value = connection.communicationFrequencyDays;
    if (!freqSelect.value) {
        const opt = document.createElement('option');
        opt.value = connection.communicationFrequencyDays;
        opt.textContent = `Κάθε ${connection.communicationFrequencyDays} μέρες`;
        freqSelect.appendChild(opt);
        freqSelect.value = connection.communicationFrequencyDays;
    }

    // ── Auto-save ────────────────────────────────────────────────────────────────

    function saveChanges() {
        const name = Q('#name').element.value.trim();
        const communicationFrequencyDays = Number(Q('#frequency').element.value);
        const lastCommunicationDate = Q('#last-communication').element.value || null;
        const scheduledNextCommunicationDate = Q('#scheduled-next').element.value || null;

        if (!name || !communicationFrequencyDays) return;

        network.updateConnection(connectionId, {
            name,
            communicationFrequencyDays,
            lastCommunicationDate,
            scheduledNextCommunicationDate
        });
    }

    Q('.form-control').on('change', saveChanges);
    Q('.form-select').on('change', saveChanges);

    // ── Scheduled-next quick buttons ─────────────────────────────────────────────

    const schedNextEl = Q('#scheduled-next').element;
    const scheduledNextQuickChangeEvent = 'scheduled-next-quick-change';

    Q('#scheduled-next').on(scheduledNextQuickChangeEvent, saveChanges);

    function emitScheduledNextQuickChange() {
        schedNextEl.dispatchEvent(new CustomEvent(scheduledNextQuickChangeEvent));
    }

    Q('#sched-today').on('click', function() {
        schedNextEl.value = Connection._today();
        emitScheduledNextQuickChange();
    });

    Q('#sched-tomorrow').on('click', function() {
        schedNextEl.value = Connection._addDays(Connection._today(), 1);
        emitScheduledNextQuickChange();
    });

    Q('#sched-plus1').on('click', function() {
        const base = schedNextEl.value || Connection._today();
        schedNextEl.value = Connection._addDays(base, 1);
        emitScheduledNextQuickChange();
    });


    // ── Delete ────────────────────────────────────────────────────────────────────

    Q('#delete-btn').on('click', function() {
        if (!confirm(`Να διαγραφεί η επαφή "${connection.name}";`)) return;
        network.removeConnection(connectionId);
        window.location.href = '../index.html';
    });


}
