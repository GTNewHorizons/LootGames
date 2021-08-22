package ru.timeconqueror.lootgames.utils;

import org.apache.logging.log4j.Logger;
import ru.timeconqueror.lootgames.api.Marker;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.lootgames.utils.future.MessageSupplier;
import ru.timeconqueror.timecore.api.util.EnvironmentUtils;

public class DebugLogger {
    private final Logger internal;

    public DebugLogger(Logger internal) {
        this.internal = internal;
    }

    public void debug(String message) {
        if (inDev()) {
            internal.info(message);
        } else {
            internal.debug(message);
        }
    }

    public void debug(String message, Object... params) {
        if (inDev()) {
            internal.info(message, params);
        } else {
            internal.debug(message, params);
        }
    }

    public void debug(Marker marker, String message) {
        if (isMarkerEnabled(marker)) {
            if (inDev()) {
                internal.info(message);
            } else {
                internal.debug(message);
            }
        }
    }

    public void debug(Marker marker, String message, Object... params) {
        if (isMarkerEnabled(marker)) {
            if (inDev()) {
                internal.info(message, params);
            } else {
                internal.debug(message, params);
            }
        }
    }

    public void debug(Marker marker, MessageSupplier supplier) {
        if (isMarkerEnabled(marker)) {
            if (inDev()) {
                internal.info(supplier.get());
            } else {
                internal.debug(supplier.get());
            }
        }
    }

    private boolean inDev() {
        return EnvironmentUtils.isInDev();
    }

    private boolean isMarkerEnabled(Marker marker) {
        return LGConfigs.GENERAL.enabledMarkers.contains(marker);
    }
}
