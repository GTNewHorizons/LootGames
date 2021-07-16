package ru.timeconqueror.lootgames.api.util;

import java.util.Objects;

public class Pos2i {
    private final int x;
    private final int y;

    public Pos2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Pos2i add(Pos2i pos) {
        return add(pos.x, pos.y);
    }

    public Pos2i add(int x, int y) {
        return new Pos2i(this.x + x, this.y + y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int manhattanDistanceTo(Pos2i pos) {
        return Math.abs(x - pos.x) + Math.abs(y - pos.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pos2i)) return false;
        Pos2i pos2i = (Pos2i) o;
        return x == pos2i.x && y == pos2i.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
