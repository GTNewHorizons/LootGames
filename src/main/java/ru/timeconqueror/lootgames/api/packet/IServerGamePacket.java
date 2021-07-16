package ru.timeconqueror.lootgames.api.packet;

import ru.timeconqueror.lootgames.api.minigame.LootGame;

public interface IServerGamePacket extends IGamePacket {
    <S extends LootGame.Stage, T extends LootGame<S, T>> void runOnClient(LootGame<S, T> game);
}
