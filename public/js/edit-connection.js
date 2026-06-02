'use strict';

const network = new Network();
const connectionId = Q.url.get('id');
if (!connectionId) {
    throw new Error('Missing connection id');
}

const connection = network.getConnection(connectionId);
if (!connection) {
    throw new Error(`Connection not found: ${connectionId}`);
}


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


// ── Delete ────────────────────────────────────────────────────────────────────

Q('#delete-btn').on('click', function() {
    if (!confirm(`Να διαγραφεί η επαφή "${connection.name}";`)) return;
    network.removeConnection(connectionId);
    window.location.href = '../index.html';
});
