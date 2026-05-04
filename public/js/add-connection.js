'use strict';

const network = new Network();

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
