package de.maria_writes_code.mcsm.backend.utils;

/**
 * Mutual exclusion lock over some object slot.
 * @param <T> The type of contained object.
 */
public class Mutex<T> {
    private T content;

    public Mutex(T content) {
        this.content = content;
    }

    public T get() throws IllegalStateException {
        ensureLock();
        return content;
    }

    public void set(T content) throws IllegalStateException {
        ensureLock();
        this.content = content;
    }

    private void ensureLock() throws IllegalStateException {
        if (!Thread.holdsLock(this)) {
            throw new IllegalStateException("Thread must be holding the lock");
        }
    }
}
