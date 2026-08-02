package de.maria_writes_code.mcsm.backend.features.templates;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import de.maria_writes_code.mcsm.backend.Utils;

@JacksonXmlRootElement(localName = "template")
@JsonInclude(Include.NON_NULL)
@NullMarked
public record ServerTemplateDefinition(
    @JacksonXmlProperty(isAttribute = true)
    String id,
    String name,
    @JacksonXmlProperty(localName = "abstract", isAttribute = true)
    boolean isAbstract,
    @Nullable Parent parent,
    Executable executable,
    List<Version> versions,
    List<Overlay> overlays
) {
    @JacksonXmlRootElement(localName = "parent")
    public record Parent(
        @JacksonXmlProperty(isAttribute = true)
        String id
    ) { }

    @JacksonXmlRootElement(localName = "executable")
    public record Executable(
        @JacksonXmlProperty(isAttribute = true)
        Path file,
        Terminator terminator,
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "argument")
        ArrayList<String> arguments
    ) {
        public Executable {
            checkDoesNotEscapeRoot(file, "executable location");
        }
    }
    
    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({
        @Type(value = ServerTemplateDefinition.CommandTerminator.class),
        @Type(value = ServerTemplateDefinition.SignalTerminator.class)
    })
    public sealed interface Terminator permits CommandTerminator, SignalTerminator { }

    public record CommandTerminator(
        @JacksonXmlProperty(localName = "command")
        String command
    ) implements Terminator { }

    public record SignalTerminator(String signal) implements Terminator { }
    
    public record Overlay(
        @JacksonXmlProperty(isAttribute = true)
        Path location,
        ArrayList<VersionRangeSpecifier> versions
    ) {
        public Overlay {
            checkDoesNotEscapeRoot(location, "overlay location");
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({
        @Type(value = ServerTemplateDefinition.Version.class),
        @Type(value = ServerTemplateDefinition.VersionRange.class)
    })
    public static interface VersionRangeSpecifier {
        boolean contains(
            String versionId,
            List<ServerTemplateDefinition.Version> reference
        );
    }

    @JacksonXmlRootElement(localName = "version")
    public record Version(
        @JacksonXmlProperty(isAttribute = true)
        String id
    ) implements VersionRangeSpecifier {
        @Override
        public boolean contains(String versionId, List<Version> reference) {
            return this.id.equals(versionId);
        }
    }

    @JacksonXmlRootElement(localName = "version-range")
    public record VersionRange(
        @JacksonXmlProperty(isAttribute = true)
        String first,
        @JacksonXmlProperty(isAttribute = true)
        String last
    ) implements VersionRangeSpecifier {
        @Override
        public boolean contains(String versionId, List<Version> reference) {
            var firstIdx = Utils.indexOf(reference, v -> v.id, first);
            var lastIdx = Utils.indexOf(reference, v -> v.id, last);
            var checkIdx = Utils.indexOf(reference, v -> v.id, versionId);

            return checkIdx >= firstIdx && checkIdx <= lastIdx;
        }
        
    }

    private static void checkDoesNotEscapeRoot(Path path, String fieldName) {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException("%s must be relative and stay within the server root".formatted(fieldName));
        }

        var normalized = path.normalize();
        // TODO: Doing string comparison is not a tidy way to do this
        if (normalized.getNameCount() > 0 && "..".equals(normalized.getName(0).toString())) {
            throw new IllegalArgumentException("%s must not escape the server root".formatted(fieldName));
        }
    }

}
