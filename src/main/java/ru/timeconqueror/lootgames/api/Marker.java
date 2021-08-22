package ru.timeconqueror.lootgames.api;

import javax.annotation.Nullable;

public enum Marker {
    LOOTGAME,
    GAME_OF_LIGHT;

    private static final Marker[] VALUES = values();

    @Nullable
    public static Marker byName(String name) {
        for (Marker marker : VALUES) {
            if (marker.name().equals(name)) {
                return marker;
            }
        }

        return null;
    }
}