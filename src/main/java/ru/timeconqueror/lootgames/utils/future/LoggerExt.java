package ru.timeconqueror.lootgames.utils.future;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;

public class LoggerExt {
    public static void debug(Logger logger, Marker marker, MessageSupplier supplier) {
        if (logger.isDebugEnabled(marker)) {
            logger.debug(marker, supplier.get());
        }
    }

    @FunctionalInterface
    public interface MessageSupplier {

        /**
         * Gets a Message.
         *
         * @return a Message
         */
        Message get();
    }
}
