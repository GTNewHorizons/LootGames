package ru.timeconqueror.lootgames.api.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import ru.timeconqueror.lootgames.api.minigame.LootGame;

public interface IClientGamePacket extends IGamePacket {
    /**
     * Do action on the main server thread upon packet receiving.
     */
    <STAGE extends LootGame.Stage, G extends LootGame<STAGE, G>> void runOnServer(EntityPlayerMP sender, LootGame<STAGE, G> game);
}
