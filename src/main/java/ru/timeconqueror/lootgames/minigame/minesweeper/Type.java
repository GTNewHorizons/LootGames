package ru.timeconqueror.lootgames.minigame.minesweeper;

import net.minecraft.nbt.NBTTagByte;
import ru.timeconqueror.lootgames.utils.future.ICodec;

public enum Type {
    BOMB((byte) -1),
    EMPTY((byte) 0),
    ONE((byte) 1),
    TWO((byte) 2),
    THREE((byte) 3),
    FOUR((byte) 4),
    FIVE((byte) 5),
    SIX((byte) 6),
    SEVEN((byte) 7),
    EIGHT((byte) 8);

    private static final Type[] LOOKUP;

    public static final ICodec<Type, NBTTagByte> CODEC = new Codec();

    private final byte id;

    Type(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    static Type byId(byte id) {
        if (id + 1 >= LOOKUP.length) {
            throw new IllegalArgumentException("Provided unknown id: " + id);
        }
        return LOOKUP[id + 1];
    }

    static {
        LOOKUP = new Type[values().length];
        for (Type value : values()) {
            LOOKUP[value.id + 1] = value;
        }
    }

    public static class Codec implements ICodec<Type, NBTTagByte> {
        @Override
        public NBTTagByte encode(Type obj) {
            return new NBTTagByte(obj.getId());
        }

        @Override
        public Type decode(NBTTagByte nbtTagInt) {
            return Type.byId(nbtTagInt.getByte());
        }
    }
}
