package ru.timeconqueror.timecore.api.util;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Pair<L, R> {
    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    public L left() {
        return left;
    }

    public R right() {
        return right;
    }

    @Override
    public String toString() {
        return "{" + left + " -> " + right + "}";
    }

    public static <A, B> Collector<Pair<A, B>, ?, Map<A, B>> toMap() {
        return Collectors.toMap(Pair::left, Pair::right);
    }
}