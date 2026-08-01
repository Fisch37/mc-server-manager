package de.maria_writes_code.mcsm.backend.features.server;

import java.util.UUID;

import org.jspecify.annotations.NullUnmarked;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// all entities are nullunmarked, because JPA creates null values whenever it wants
@Entity
@NullUnmarked
public class Server {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String currentVersionId;

    public Server() { }
    public Server(String name, String version) {
        this.name = name;
        this.currentVersionId = version;
    }


    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getCurrentVersionId() {
        return currentVersionId;
    }
}
