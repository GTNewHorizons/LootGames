package ru.timeconqueror.lootgames.minigame.gol;

import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.timecore.api.util.lookups.EnumLookup;

public enum Symbol {
    NORTH_WEST(0, new Pos2i(0, 0)),
    NORTH(1, new Pos2i(1, 0)),
    NORTH_EAST(2, new Pos2i(2, 0)),
    WEST(3, new Pos2i(0, 1)),
    EAST(4, new Pos2i(2, 1)),
    SOUTH_WEST(5, new Pos2i(0, 2)),
    SOUTH(6, new Pos2i(1, 2)),
    SOUTH_EAST(7, new Pos2i(2, 2));

    private static final EnumLookup<Symbol, Integer> LOOKUP = EnumLookup.make(Symbol.class, Symbol::getIndex);
    public static final Symbol[] NWES_SYMBOLS = new Symbol[]{NORTH, WEST, EAST, SOUTH};

    private final int index;
    private final Pos2i pos;

    Symbol(int index, Pos2i pos) {
        this.index = index;
        this.pos = pos;
    }

    public Pos2i getPos() {
        return pos;
    }

    public int getIndex() {
        return index;
    }

    public static Symbol byIndex(int index) {
        return LOOKUP.by(index);
    }

    public static Symbol byPos(Pos2i pos) {
        for (Symbol symbol : values()) {
            if (symbol.pos.equals(pos)) {
                return symbol;
            }
        }

        throw new IllegalArgumentException("There's no symbol on pos " + pos);
    }

    public static boolean exists(Pos2i pos) {
        for (Symbol symbol : values()) {
            if (symbol.pos.equals(pos)) {
                return true;
            }
        }

        return false;
    }
}