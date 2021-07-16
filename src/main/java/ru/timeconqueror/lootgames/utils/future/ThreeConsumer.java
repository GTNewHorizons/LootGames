package ru.timeconqueror.lootgames.utils.future;

@FunctionalInterface
public interface ThreeConsumer<T, U, V> {
    void accept(T t, U u, V v);
}