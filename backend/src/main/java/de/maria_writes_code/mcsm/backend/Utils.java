package de.maria_writes_code.mcsm.backend;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract sealed class Utils permits Utils.Seal {
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

    public static <T, V> boolean contains(
        Collection<T> collection,
        Function<T, V> mapper,
        V comparer
    ) {
        return contains(collection, t -> mapper.apply(t).equals(comparer));
    }
    
    public static void requireNonNull(Object... objects) {
        for (var o : objects) {
            Objects.requireNonNull(o);
        }
    }
    
    private static final class Seal extends Utils { }
}
