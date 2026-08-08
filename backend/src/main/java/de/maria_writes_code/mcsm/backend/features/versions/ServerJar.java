package de.maria_writes_code.mcsm.backend.features.versions;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "jar_channel", discriminatorType = DiscriminatorType.STRING)
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "jar_channel", "version_id" })
    }
)
public abstract class ServerJar {
    @Id @GeneratedValue
    private long privateId;

    @Column(name="version_id")
    private String versionId;

    public ServerJar() { }

    public ServerJar(String versionId) {
        this.versionId = versionId;
    }

    public String getVersionId() {
        return versionId;
    }

    @Entity
    @DiscriminatorValue("vanilla")
    public static class Vanilla extends ServerJar {
        private String sha1;

        public Vanilla() { }

        public Vanilla(String versionId, String sha1) {
            super(versionId);
            this.sha1 = sha1;
        }
        
        public String getSha1() {
            return sha1;
        }
    }
}
