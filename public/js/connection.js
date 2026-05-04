'use strict';

class Connection {
    constructor({
        id,
        name,
        communicationFrequencyDays,
        lastCommunicationDate = null,
        scheduledNextCommunicationDate = null,
        createdAt = null
    } = {}) {
        this.id = id ?? Connection._generateId();
        this.name = name;
        this.communicationFrequencyDays = Number(communicationFrequencyDays);
        this.lastCommunicationDate = lastCommunicationDate || null;
        this.scheduledNextCommunicationDate = scheduledNextCommunicationDate || null;
        this.createdAt = createdAt ?? Connection._today();
    }

    /**
     * Effective next communication date (YYYY-MM-DD).
     * Uses scheduledNextCommunicationDate if set, otherwise
     * calculates from lastCommunicationDate (or createdAt) + frequency.
     */
    get nextCommunication() {
        if (this.scheduledNextCommunicationDate) {
            return this.scheduledNextCommunicationDate;
        }
        const base = this.lastCommunicationDate ?? this.createdAt;
        return Connection._addDays(base, this.communicationFrequencyDays);
    }

    get isOverdue() {
        return this.nextCommunication < Connection._today();
    }

    get isToday() {
        return this.nextCommunication === Connection._today();
    }

    /** Days until next communication. Negative means overdue. */
    get daysUntilNext() {
        const [ty, tm, td] = Connection._today().split('-').map(Number);
        const [ny, nm, nd] = this.nextCommunication.split('-').map(Number);
        const todayMs = new Date(ty, tm - 1, td).getTime();
        const nextMs  = new Date(ny, nm - 1, nd).getTime();
        return Math.round((nextMs - todayMs) / 86400000);
    }

    toPlain() {
        return {
            id: this.id,
            name: this.name,
            communicationFrequencyDays: this.communicationFrequencyDays,
            lastCommunicationDate: this.lastCommunicationDate,
            scheduledNextCommunicationDate: this.scheduledNextCommunicationDate,
            createdAt: this.createdAt
        };
    }

    static fromPlain(obj) {
        return new Connection(obj);
    }

    static _today() {
        const d = new Date();
        const y  = d.getFullYear();
        const m  = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return `${y}-${m}-${dd}`;
    }

    static _addDays(dateStr, days) {
        const [y, m, d] = dateStr.split('-').map(Number);
        const date = new Date(y, m - 1, d);
        date.setDate(date.getDate() + days);
        const ny  = date.getFullYear();
        const nm  = String(date.getMonth() + 1).padStart(2, '0');
        const nd  = String(date.getDate()).padStart(2, '0');
        return `${ny}-${nm}-${nd}`;
    }

    static _generateId() {
        return Date.now().toString(36) + Math.random().toString(36).slice(2, 7);
    }
}
