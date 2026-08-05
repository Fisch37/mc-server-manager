package de.maria_writes_code.mcsm.backend.features.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import de.maria_writes_code.mcsm.backend.utils.Utils;

public enum ServerStatus {
    Stopped,
    Crashed,
    Starting,
    Started,
    Stopping;

    public boolean isAlive() {
        return !(this.equals(Stopped) || this.equals(Crashed));
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static ServerStatus fromJson(String value) {
        return ServerStatus.valueOf(Utils.capitalise(value));
    }
}
