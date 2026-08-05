package de.maria_writes_code.mcsm.backend.utils;

import java.util.function.Consumer;

/**
 * A read-only view of an event.
 * 
 * @param <T> the type of value the event publishes
 */
public interface ReadOnlyEvent<T> {
    /**
     * Subscribe to this event.
     * <p>
     * <em>
     *  Events only keep weak references to their handlers.
     *  Make sure to store them somewhere safe.
     * </em>
     * @param handler The event handler to be registered.
     *  The passed parameter should be kept for a future unsubscribe.
     * @implNote Do not pass method references or lambdas to this function
     *  unless you don't want to unsubscribe them at all!
     *  Java will compile an anonymous class around them,
     *  which is newly constructed every time it is called!
     */
    void subscribe(Consumer<T> handler);
    /**
     * Unsubscribe the passed event handler, if it is registered.
     * @param handler The handler to deregister.
     * @return {@code true} if this handler was deregistered,
     *  {@code false} if it was not last deregistered.
     */
    boolean unsubscribe(Consumer<T> handler);
}
