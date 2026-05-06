'use strict';

class Network {
    static STORAGE_KEY = 'hearth_connections';

    constructor() {
        this._connections = this._load();
    }

    _load() {
        try {
            const raw = localStorage.getItem(Network.STORAGE_KEY);
            if (!raw) return [];
            return JSON.parse(raw).map(Connection.fromPlain);
        } catch {
            return [];
        }
    }

    _save() {
        localStorage.setItem(
            Network.STORAGE_KEY,
            JSON.stringify(this._connections.map(c => c.toPlain()))
        );
    }

    getConnections() {
        return [...this._connections];
    }

    getConnection(id) {
        return this._connections.find(c => c.id === id) ?? null;
    }

    addConnection(data) {
        const connection = new Connection(data);
        this._connections.push(connection);
        this._save();
        return connection;
    }

    updateConnection(id, data) {
        const index = this._connections.findIndex(c => c.id === id);
        if (index === -1) return null;
        const updated = new Connection({ ...this._connections[index].toPlain(), ...data });
        this._connections[index] = updated;
        this._save();
        return updated;
    }

    removeConnection(id) {
        const index = this._connections.findIndex(c => c.id === id);
        if (index === -1) return false;
        this._connections.splice(index, 1);
        this._save();
        return true;
    }

    /** Mark as communicated today and clear any scheduled override. */
    markCommunicated(id) {
        const conn = this.getConnection(id);
        if (!conn) return null;
        return this.updateConnection(id, {
            lastCommunicationDate: Connection._today(),
            scheduledNextCommunicationDate: null
        });
    }

    /** Override next communication to tomorrow. */
    postponeToTomorrow(id) {
        const tomorrow = Connection._addDays(Connection._today(), 1);
        return this.updateConnection(id, { scheduledNextCommunicationDate: tomorrow });
    }
}
