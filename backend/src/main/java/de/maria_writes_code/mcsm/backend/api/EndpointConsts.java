package de.maria_writes_code.mcsm.backend.api;

import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract sealed class EndpointConsts permits EndpointConsts.Seal {
    public static final Supplier<ResponseStatusException> NO_SERVER_EXISTS = () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no server with that id exists");

    private final class Seal extends EndpointConsts { }
}
