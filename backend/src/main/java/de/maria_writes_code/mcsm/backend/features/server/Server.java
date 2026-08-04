package de.maria_writes_code.mcsm.backend.features.server;

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

    @Column(nullable = false)
    private String currentVersionId;

    @Column(nullable = false)
    private String templateId;

    private int lastExitCode;
    private int javaVersion;

    // TODO: Is this safe for JPA, which will want to set its own ids?
    public Server() {
        id = UUID.randomUUID();
    }
    public Server(String name, String version) {
        this();
        this.name = name;
        this.currentVersionId = version;
    }
    public Server(Server original) {
        id = original.id;
        name = original.name;
        currentVersionId = original.currentVersionId;
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

    public String getCurrentVersionId() {
        return currentVersionId;
    }
    public void setCurrentVersionId(String versionId) {
        this.currentVersionId = versionId;
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
