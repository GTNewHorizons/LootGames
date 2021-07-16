package ru.timeconqueror.lootgames.api;

import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.minigame.FieldManager;
import ru.timeconqueror.lootgames.api.minigame.GameManager;
import ru.timeconqueror.lootgames.api.minigame.ILootGameFactory;
import ru.timeconqueror.lootgames.api.packet.GamePacketRegister;
import ru.timeconqueror.lootgames.api.packet.IClientGamePacket;
import ru.timeconqueror.lootgames.api.packet.IServerGamePacket;
import ru.timeconqueror.lootgames.api.task.ITask;
import ru.timeconqueror.lootgames.api.task.TaskRegistry;

public class LootGamesAPI {
    public static void regClientPacket(Class<? extends IClientGamePacket> packetClass) {
        GamePacketRegister.regClientPacket(packetClass);
    }

    public static void regServerPacket(Class<? extends IServerGamePacket> packetClass) {
        GamePacketRegister.regServerPacket(packetClass);
    }

    /**
     * Register game and its factory.
     */
    public static void registerGameGenerator(ILootGameFactory generator) {
        getGameManager().registerGameGenerator(generator);
    }

    public static <T extends ITask> void registerTaskFactory(Class<T> taskClass, ITask.ITaskFactory<T> factory) {
        TaskRegistry.registerTaskFactory(taskClass, factory);
    }

    public static GameManager getGameManager() {
        return LootGames.gameManager;
    }

    public static FieldManager getFieldManager() {
        return LootGames.fieldManager;
    }
}
