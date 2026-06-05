'use strict';

const network = new Network();

const schedNextEl = Q('#scheduled-next').element;

Q('#sched-today').on('click', function() {
    schedNextEl.value = Connection._today();
});

Q('#sched-tomorrow').on('click', function() {
    schedNextEl.value = Connection._addDays(Connection._today(), 1);
});

Q('#sched-plus1').on('click', function() {
    const base = schedNextEl.value || Connection._today();
    schedNextEl.value = Connection._addDays(base, 1);
});

Q('#add-form').on('submit', function(e) {
    e.preventDefault();

    const name = Q('#name').element.value.trim();
    const communicationFrequencyDays = Number(Q('#frequency').element.value);
    const lastCommunicationDate = Q('#last-communication').element.value || null;
    const scheduledNextCommunicationDate = Q('#scheduled-next').element.value || null;

    if (!name || !communicationFrequencyDays) return;

    network.addConnection({ name, communicationFrequencyDays, lastCommunicationDate, scheduledNextCommunicationDate });
    window.location.href = '../index.html';
});
