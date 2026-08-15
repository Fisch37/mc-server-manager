package de.maria_writes_code.mcsm.backend.features.runtimes;

public class NoSuchRuntimeException extends RuntimeException {
    public NoSuchRuntimeException(String message) {
        super(message);
    }

    public NoSuchRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
