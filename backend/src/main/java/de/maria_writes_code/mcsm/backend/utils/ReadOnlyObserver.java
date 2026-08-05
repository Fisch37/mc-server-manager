package de.maria_writes_code.mcsm.backend.utils;

public interface ReadOnlyObserver<T> extends ReadOnlyEvent<T> {
    T get();
}
