package ru.timeconqueror.lootgames.utils.future;

import org.apache.logging.log4j.message.Message;

@FunctionalInterface
public interface MessageSupplier {

    /**
     * Gets a Message.
     *
     * @return a Message
     */
    Message get();
}