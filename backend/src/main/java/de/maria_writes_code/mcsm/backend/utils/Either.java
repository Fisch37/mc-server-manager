package de.maria_writes_code.mcsm.backend.utils;

import java.util.Optional;
import java.util.function.Consumer;

public sealed abstract class Either<L, R> permits Either.Left, Either.Right {
    public static <L, R> Either<L, R> makeLeft(L value) {
        return new Left<>(value);
    }
    public static <L, R> Either<L, R> makeRight(R value) {
        return new Right<>(value);
    }

    public abstract Optional<L> left();
    public abstract Optional<R> right();

    public void leftOrRight(Consumer<L> onLeft, Consumer<R> onRight) {
        left().ifPresent(onLeft);
        right().ifPresent(onRight);
    }

    private static final class Left<L, R> extends Either<L, R> {
        private final L value;

        public Left(L value) {
            this.value = value;
        }

        @Override
        public Optional<L> left() {
            return Optional.of(value);
        }

        @Override
        public Optional<R> right() {
            return Optional.empty();
        }
    }

    private static final class Right<L, R> extends Either<L, R> {
        private final R value;

        public Right(R value) {
            this.value = value;
        }

        @Override
        public Optional<L> left() {
            return Optional.empty();
        }
        
        @Override
        public Optional<R> right() {
            return Optional.of(value);
        }
    }
}
