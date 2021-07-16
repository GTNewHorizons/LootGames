package ru.timeconqueror.lootgames.utils.future;

import net.minecraft.nbt.NBTBase;

public interface INBTSerializable<T extends NBTBase> {
    T serializeNBT();

    void deserializeNBT(T nbt);
}