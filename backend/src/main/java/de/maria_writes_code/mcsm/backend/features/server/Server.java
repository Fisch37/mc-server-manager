package de.maria_writes_code.mcsm.backend.features.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.NullUnmarked;

import de.maria_writes_code.mcsm.backend.features.components.ComponentIdentifier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

// all entities are nullunmarked, because JPA creates null values whenever it wants
@Entity
@NullUnmarked
public class Server {
    @Id @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private ComponentIdentifier type;
    private LinkedHashMap<String, String> currentVersionIds;

    @Column(nullable = false)
    private String templateId;

    private int lastExitCode;
    private HashMap<String, String> properties;

    public Server() {
        id = UUID.randomUUID();
    }
    public Server(String name, Map<String, String> versionIds) {
        this();
        this.name = name;
        this.currentVersionIds = new LinkedHashMap<>(versionIds);
    }
    public Server(Server original) {
        id = original.id;
        name = original.name;
        currentVersionIds = new LinkedHashMap<>(original.currentVersionIds);
        templateId = original.templateId;
        
        lastExitCode = original.lastExitCode;
        properties = new HashMap<>(original.properties);
        type = original.type;
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

    public String getCurrentVersionId(String versionSourceId) {
        return currentVersionIds.get(versionSourceId);
    }
    public void setCurrentVersionId(String versionSourceid, String versionId) {
        currentVersionIds.put(versionSourceid, versionId);
    }
    public void popCurrentVersionId(String versionSourceId) {
        currentVersionIds.remove(versionSourceId);
    }
    public Map<String, String> getCurrentVersionIds() {
        return currentVersionIds;
    }

    public String getTemplateId() {
        return templateId;
    }
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public int getLastExitCode() {
        return lastExitCode;
    }
    public void setLastExitCode(int code) {
        lastExitCode = code;
    }
    public boolean hasCrashed() {
        return lastExitCode != 0;
    }

    public ComponentIdentifier getType() {
        return type;
    }
    public void setType(ComponentIdentifier type) {
        this.type = type;
    }

    /// Returns a modifiable view over the properties of this server.
    public Map<String, String> getProperties() {
        return properties;
    }
}
