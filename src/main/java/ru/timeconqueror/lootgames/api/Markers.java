package ru.timeconqueror.lootgames.api;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public enum Markers {
    LOOTGAME("LOOTGAME");

    private final Marker marker;

    Markers(String name) {
        this.marker = MarkerManager.getMarker(name);
    }

    public Marker getMarker() {
        return marker;
    }
}