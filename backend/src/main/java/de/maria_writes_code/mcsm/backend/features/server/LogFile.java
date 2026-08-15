package de.maria_writes_code.mcsm.backend.features.server;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

import org.apache.commons.compress.compressors.CompressorStreamFactory;

public abstract class LogFile {
    // 4MiB memory limit seems good
    private static final CompressorStreamFactory COMPRESSOR_FACTORY = new CompressorStreamFactory(true, 4096);

    protected final Path location;
    private final String name;

    public LogFile(Path location, String name) {
        this.location = location;
        this.name = name;
    }

    public static LogFile fromFileName(Path location, String name) {
        BiFunction<Path, String, ? extends LogFile> constructor;
        if (location.getFileName().toString().endsWith(".gz")) {
            constructor = LogFile.GZipped::new;
        } else {
            constructor = LogFile.Raw::new;
        }
        return constructor.apply(location, name);
    }

    public String getName() {
        return name;
    }

    public Path getLocation() {
        return location;
    }

    /**
     * Open this file and return its contents (decoded, if required)
     * @return The contents of the file
     */
    public abstract SizedInputStream getContent() throws IOException;

    protected InputStream getContentRaw() throws IOException {
        return new BufferedInputStream(new FileInputStream(location.toFile()));
    }

    public static class Raw extends LogFile {
        public Raw(Path location, String name) {
            super(location, name);
        }

        @Override
        public SizedInputStream getContent() throws IOException {
            return new SizedInputStream(
                super.getContentRaw(),
                // TODO:? Files.size says it may not be reliable "due to compression, support for sparse files, or other reasons"
                //  investigate whether other methods exist that are more reliable.
                Files.size(location)
            );
        }
    }

    public static class GZipped extends LogFile {
        public GZipped(Path location, String name) {
            super(location, name);
        }

        @Override
        public SizedInputStream getContent() throws IOException {
            var stream = COMPRESSOR_FACTORY.createCompressorInputStream(super.getContentRaw());
            return new SizedInputStream(
                stream,
                stream.getUncompressedCount()
            );
        }
    }

    /**
     * An input stream with added size information.
     * @param inputStream The actual input stream
     * @param size The expected size of the stream. Note that this is not reliable.
     *  The actual stream may be significantly shorter or longer than this number.
     */
    public record SizedInputStream(
        InputStream inputStream,
        long size
    ) { }
}
