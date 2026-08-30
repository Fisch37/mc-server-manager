package de.maria_writes_code.mcsm.backend.features.components.versions;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collector;

import org.apache.commons.collections4.map.LinkedMap;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;

@Component
public interface VersionProvider {
    String getSourceIdentifier();
    String getFriendlyName();
    @Nullable Version getVersionInfo(String versionId);
    /**
     * 
     * @param versionId
     * @return -1 if no version of the given id was found, else some index of the version.
     */
    int indexOf(String versionId);
    Collection<? extends Version> getVersions();
    Set<String> getDefaultChannels();

    public abstract static class LinkedMapProvider<T extends Version> implements VersionProvider {
        protected final LinkedMap<String, T> versions;

        protected LinkedMapProvider(LinkedMap<String, T> versions) {
            this.versions = versions;
        }

        protected LinkedMapProvider(Collection<T> versions) {
            this(versions.stream().collect(Collector.of(
                () -> new LinkedMap<>(),
                (map, element) -> map.put(element.id(), element),
                (a, b) -> {
                    var x = new LinkedMap<>(a);
                    x.putAll(b);
                    return x;
                }
            )));
        }

        @Override @Nullable
        public T getVersionInfo(String versionId) {
            return versions.get(versionId);
        }

        @Override
        public int indexOf(String versionId) {
            return versions.indexOf(versionId);
        }

        @Override
        public Collection<T> getVersions() {
            return versions.values();
        }
        
    }
}
