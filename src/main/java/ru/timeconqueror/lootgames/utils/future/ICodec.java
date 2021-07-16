package ru.timeconqueror.lootgames.utils.future;

public interface ICodec<T, NBT> {
    NBT encode(T t);

    T decode(NBT nbt);
}
