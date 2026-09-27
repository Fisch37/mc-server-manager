package de.maria_writes_code.mcsm.backend.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.features.server.ServerManager;
import de.maria_writes_code.mcsm.backend.utils.IOTriFunction;
import de.maria_writes_code.mcsm.backend.utils.Utils;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static de.maria_writes_code.mcsm.backend.api.EndpointUtils.NO_SERVER_EXISTS;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

@RestController
@RequestMapping("server/{id}/files")
public class ServerFilesEndpoints {
    @Autowired 
    ServerManager servers;

    @GetMapping("")
    public FileLikeSummaryObject getFileSummary(
        @PathVariable UUID id,
        @RequestParam(name = "path", required = false, defaultValue = "") String pathString
    ) throws IOException {
        var ctx = getContext(id, pathString);
        var root = ctx.server.getLocation();
        return FileLikeSummaryObject.on(root, root.resolve(ctx.path));
    }

    @GetMapping("list")
    public List<FileLikeSummaryObject> getDirectoryList(
        @PathVariable UUID id,
        @RequestParam(name = "path", required = false, defaultValue = "") String pathString
    ) throws IOException {
        var ctx = getContext(id, pathString);
        var root = ctx.server.getLocation();
        var actualDirectory = ctx.resolvedPath().toRealPath();
        if (Utils.escapesRoot(root.relativize(actualDirectory))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (
            // NOFOLLOW_LINKS because a race condition could introduce
            // a symlink that goes out of legal scope
            !Files.isDirectory(actualDirectory, NOFOLLOW_LINKS)
        ) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        try (var files = Files.list(actualDirectory)) {
            return files.map(
                f -> {
                    try {
                        return FileLikeSummaryObject.on(root, f);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            ).collect(Collectors.toList());
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @GetMapping("contents")
    public ResponseEntity<InputStreamResource> getFileContents(
        @PathVariable UUID id,
        @RequestParam(name = "path", required = false, defaultValue = "") String pathString
    ) throws IOException {
        var ctx = getContext(id, pathString);
        var file = ctx.resolvedPath();
        if (Files.isDirectory(file)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        if (Utils.escapesRoot(
            ctx.server.getLocation().relativize(file.toRealPath())
        )) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        var mimeType = Files.probeContentType(file);
        return ResponseEntity.ok()
            .contentType(mimeType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(mimeType))
            .body(new InputStreamResource(new FileInputStream(file.toFile())))
            ;
    }

    @PutMapping("")
    public void writeFileContents(
        @PathVariable UUID id,
        @RequestParam(name = "path", required = false, defaultValue = "") String pathString,
        @RequestParam MultipartFile file
    ) throws IOException {
        var ctx = getContext(id, pathString);
        if (!Files.isRegularFile(ctx.resolvedPath(), NOFOLLOW_LINKS)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        // FIXME: known race condition, leading to an unauthorized write vulnerability
        try (var dest = new BufferedOutputStream(new FileOutputStream(ctx.resolvedPath().toFile()))) {
            IOUtils.copy(file.getInputStream(), dest);
        }
    }

    @DeleteMapping("")
    public void writeFileContents(
        @PathVariable UUID id,
        @RequestParam(name = "path", required = false, defaultValue = "") String pathString
    ) throws IOException {
        var ctx = getContext(id, pathString);
        if (Files.isWritable(ctx.resolvedPath()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        FileUtils.delete(ctx.resolvedPath().toFile());
    }

    private RequestContext getContext(UUID id, String pathString) throws ResponseStatusException {
        var server = servers.get(id).orElseThrow(NO_SERVER_EXISTS);
        var path = Path.of(pathString);
        if (Utils.escapesRoot(path)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return new RequestContext(server, path);
    }
    private record RequestContext(ActiveServer server, Path path) {
        public Path resolvedPath() {
            return server.getLocation().resolve(path);
        }
    }

    public abstract static class FileLikeSummaryObject {
        private final String path;
        private final long size;
        private final String created_at, last_modified;
        private final FilePermissionsObject permissions;

        public FileLikeSummaryObject(
            String path,
            long size,
            FileTime created_at,
            FileTime last_modified,
            FilePermissionsObject permissions
        ) {
            this.path = path;
            this.size = size;
            this.created_at = created_at.toString();
            this.last_modified = last_modified.toString();
            this.permissions = permissions;
        }

        public FileLikeSummaryObject(
            Path root,
            Path path,
            BasicFileAttributes attributes
        ) {
            this(
                apiPath(root, path).orElseThrow(),
                attributes.size(),
                attributes.creationTime(),
                attributes.lastModifiedTime(),
                new FilePermissionsObject(path)
            );
        }

        public String getPath() {
            return path;
        }

        public long getSize() {
            return size;
        }

        public String getCreated_at() {
            return created_at;
        }

        public String getLast_modified() {
            return last_modified;
        }

        public FilePermissionsObject getPermissions() {
            return permissions;
        }

        public abstract String getType();

        public static FileLikeSummaryObject on(Path root, Path path) throws IOException {
            var attributes = Files.readAttributes(path, BasicFileAttributes.class, NOFOLLOW_LINKS);
            final IOTriFunction<Path, Path, BasicFileAttributes, ? extends FileLikeSummaryObject> ctor;
            if (attributes.isSymbolicLink()) {
                ctor = SymlinkSummaryObject::new;
            } else if (attributes.isDirectory()) {
                ctor = DirectorySummaryObject::new;
            } else if (attributes.isRegularFile()) {
                ctor = FileSummaryObject::new;
            } else {
                throw new RuntimeException("Encountered a file that is not a symlink, directory or regular file");
            }
            return ctor.apply(root, path, attributes);
        }
    }

    public static class FileSummaryObject extends FileLikeSummaryObject {
        public FileSummaryObject(Path root, Path path, BasicFileAttributes attributes) {
            super(root, path, attributes);
        }

        @Override
        public String getType() {
            return "file";
        }
    }

    public static class DirectorySummaryObject extends FileLikeSummaryObject {
        public DirectorySummaryObject(Path root, Path path, BasicFileAttributes attributes) {
            super(root, path, attributes);
        }

        @Override
        public String getType() {
            return "directory";
        }
    }

    public static class SymlinkSummaryObject extends FileLikeSummaryObject {
        private final @Nullable String target;

        public SymlinkSummaryObject(
            Path root,
            Path path,
            BasicFileAttributes attributes
        ) throws IOException {
            super(root, path, attributes);
            this.target = apiPath(root, path.toRealPath()).orElse(null);
        }

        @Override
        public String getType() {
            return "symlink";
        }

        public @Nullable String getTarget() {
            return target;
        }
    }

    public record FilePermissionsObject(
        boolean read,
        boolean write,
        boolean execute
    ) {
        public FilePermissionsObject(Path path) {
            var file = path.toFile();
            this(
                file.canRead(),
                file.canWrite(),
                file.canExecute()
            );
        }
    }

    private static Optional<String> apiPath(Path serverRoot, Path target) {
        var relative = serverRoot.relativize(target);
        if (Utils.escapesRoot(relative)) {
            return Optional.empty();
        } else {
            return Optional.of(
                String.join(
                    "/",
                    (Iterable<String>)
                    Utils.toStream(relative.iterator())
                        .map(p -> p.toString())
                        ::iterator
                )
            );
        }
    }
}
