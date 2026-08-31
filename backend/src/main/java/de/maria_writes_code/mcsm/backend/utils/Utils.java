package de.maria_writes_code.mcsm.backend.utils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract sealed class Utils permits Utils.Seal {
    public static String capitalise(String input) {
        var builder = new StringBuilder(input);
        if (!input.isEmpty()) {
            builder.setCharAt(0, Character.toUpperCase(input.charAt(0)));
        }
        return builder.toString();
    }

    public static <T> int indexOf(Collection<T> collection, Predicate<T> predicate) {
        int i = 0;
        for (T el : collection) {
            if (predicate.test(el)) {
                return i;
            }
            i++;
        }
        return -1;
    }
    public static <T, V> int indexOf(
        Collection<T> collection,
        Function<T, V> mapper,
        V comparer
    ) {
        return indexOf(collection, t -> mapper.apply(t).equals(comparer));
    }

    public static <T> boolean contains(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).findAny().isPresent();
    }

    public static <T> boolean contains(Stream<T> stream, Predicate<T> predicate) {
        return stream.filter(predicate).findAny().isPresent();
    }

    public static <T, V> boolean contains(
        Collection<T> collection,
        Function<T, V> mapper,
        V comparer
    ) {
        return contains(collection, t -> mapper.apply(t).equals(comparer));
    }

    public static <T, V> boolean contains(
        Stream<T> stream,
        Function<T, V> mapper,
        V comparer
    ) {
        return stream.map(mapper)
            .filter(v -> Objects.equals(comparer, v))
            .findAny()
            .isPresent();
    }
    
    public static void requireNonNull(Object... objects) {
        for (var o : objects) {
            Objects.requireNonNull(o);
        }
    }

    public static <T, E extends Throwable> T throwIfNull(
        @Nullable T value,
        Supplier<E> errorSupplier
    ) throws E {
        if (value == null) {
            throw errorSupplier.get();
        } else {
            return value;
        }
    }

    public static <E extends Throwable> void throwIfNullMany(
        Supplier<E> errorSupplier,
        Object... objects
    ) throws E {
        for (var o : objects) {
            if (o == null) {
                throw errorSupplier.get();
            }
        }
    }

    public static <T> @NonNull T or(
        @Nullable T value,
        @NonNull T defaultValue
    ) {
        return value == null ? defaultValue : value;
    }

    private static final int SIGTERM = 15, SIGINT = 2;
    public static boolean isExitCodeOk(int exitCode) {
        switch (exitCode) {
            case 0:
            // We will assume our servers react correctly to signals.
            // We can't _actually_ know that, but there also isn't
            // any way to tell whether they _did_. Exit codes suck.
            case 128+SIGINT:
            case 128+SIGTERM:
                return true;
            default:
                return false;
        }
    }
    
    private static final class Seal extends Utils { }
}
