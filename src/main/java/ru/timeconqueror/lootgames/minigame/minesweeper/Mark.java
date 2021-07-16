package ru.timeconqueror.lootgames.minigame.minesweeper;

import net.minecraft.nbt.NBTTagByte;
import ru.timeconqueror.lootgames.utils.future.ICodec;

public enum Mark {
    NO_MARK((byte) 0),
    FLAG((byte) 1),
    QUESTION_MARK((byte) 2);

    public static ICodec<Mark, NBTTagByte> CODEC = new Codec();
    private static final Mark[] LOOKUP;

    private final byte id;

    Mark(byte id) {
        this.id = id;
    }

    public static Mark getNext(Mark mark) {
        return mark.id >= LOOKUP.length - 1 ? LOOKUP[0] : LOOKUP[mark.id + 1];
    }

    public Mark getNext() {
        return getNext(this);
    }

    private static Mark byID(int id) {
        if (id >= LOOKUP.length) {
            throw new IllegalArgumentException("Provided unknown id: " + id);
        }
        return LOOKUP[id];
    }

    static {
        LOOKUP = new Mark[values().length];
        for (Mark value : values()) {
            LOOKUP[value.id] = value;
        }
    }

    public static class Codec implements ICodec<Mark, NBTTagByte> {
        @Override
        public NBTTagByte encode(Mark obj) {
            return new NBTTagByte(obj.id);
        }

        @Override
        public Mark decode(NBTTagByte nbtTagInt) {
            return Mark.byID(nbtTagInt.getByte());
        }
    }
}
