package ru.timeconqueror.lootgames.common.packet.game;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.IClientGamePacket;
import ru.timeconqueror.lootgames.minigame.gol.GameOfLight;

import java.io.IOException;

public class CPGOLSymbolsShown implements IClientGamePacket {
    @Override
    public void encode(PacketBuffer bufferTo) throws IOException {
    }

    @Override
    public void decode(PacketBuffer bufferFrom) throws IOException {
    }

    @Override
    public <STAGE extends LootGame.Stage, G extends LootGame<STAGE, G>> void runOnServer(EntityPlayerMP sender, LootGame<STAGE, G> game) {
        if (game instanceof GameOfLight && game.getStage() instanceof GameOfLight.StageShowSequence) {
            ((GameOfLight.StageShowSequence) game.getStage()).onSequenceShown();
        }
    }
}
