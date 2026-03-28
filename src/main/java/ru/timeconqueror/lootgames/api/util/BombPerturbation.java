package ru.timeconqueror.lootgames.api.util;

public class BombPerturbation {

    public byte x;
    public byte y;
    public byte bombDiff;

    public BombPerturbation(byte x, byte y, byte bombDiff) {
        this.x = x;
        this.y = y;
        this.bombDiff = bombDiff;
    }
}
