package de.maria_writes_code.mcsm.backend.utils;

import java.io.IOException;

@FunctionalInterface 
public interface IOTriFunction<A, B, C, R> {
    R apply(A a, B b, C c) throws IOException;
}
