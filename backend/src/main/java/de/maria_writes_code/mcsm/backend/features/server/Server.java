package de.maria_writes_code.mcsm.backend.features.server;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.NullUnmarked;

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

    private LinkedHashMap<String, String> currentVersionIds;

    @Column(nullable = false)
    private String templateId;

    private int lastExitCode;
    private int javaVersion;

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
        javaVersion = original.javaVersion;
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
    
    public int getJavaVersion() {
        return javaVersion;
    }
    public void setJavaVersion(int javaVersion) {
        this.javaVersion = javaVersion;
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
}
