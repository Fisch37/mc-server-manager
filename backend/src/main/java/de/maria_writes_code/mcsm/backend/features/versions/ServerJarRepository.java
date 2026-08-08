package de.maria_writes_code.mcsm.backend.features.versions;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerJarRepository extends JpaRepository<ServerJar, Long> {

    @Repository
    public static interface Vanilla extends JpaRepository<ServerJar.Vanilla, Long> {
        Optional<ServerJar.Vanilla> findByVersionId(String versionId);
    }
}
