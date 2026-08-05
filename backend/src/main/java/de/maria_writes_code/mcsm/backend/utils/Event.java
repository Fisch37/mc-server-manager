package de.maria_writes_code.mcsm.backend.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class Event<T> implements ReadOnlyEvent<T> {
    private final WeakHashMap<Consumer<T>, Unit> handlers;

    public Event() {
        handlers = new WeakHashMap<>();
    }

    public void push(T value) {
        Collection<Consumer<T>> handlersCopy;
        synchronized (handlers) {
            handlersCopy = new ArrayList<>(handlers.keySet());
        }
        handlersCopy.forEach(f -> f.accept(value));
    }

    @Override
    public void subscribe(Consumer<T> handler) {
        synchronized (handlers) {
            handlers.put(handler, Unit.INSTANCE);
        }
    }

    @Override
    public boolean unsubscribe(Consumer<T> handler) {
        synchronized (handlers) {
            return handlers.remove(handler) != null;
        }
    }
    
    private static enum Unit {
        INSTANCE;
    }
}
