package de.maria_writes_code.mcsm.backend.features.server;

public enum ServerStatus {
    Stopped,
    Crashed,
    Starting,
    Started,
    Stopping;

    public boolean isAlive() {
        return !(this.equals(Stopped) || this.equals(Crashed));
    }
}
