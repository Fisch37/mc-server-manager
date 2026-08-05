package de.maria_writes_code.mcsm.backend.utils;

import java.util.function.Consumer;

public class Observable<T> implements ReadOnlyObserver<T> {
    private T value;
    private final Event<T> setEvent;

    public Observable(T value) {
        this.value = value;
        setEvent = new Event<>();
    }

    @Override
    public T get() {
        return value;
    }

    public T set(T value) {
        this.value = value;
        setEvent.push(value);
        return value;
    }

    @Override
    public void subscribe(Consumer<T> handler) {
        setEvent.subscribe(handler);
    }

    @Override
    public boolean unsubscribe(Consumer<T> handler) {
        return setEvent.unsubscribe(handler);
    }
}
