package ru.timeconqueror.lootgames.common.packet.game.sudoku;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.IClientGamePacket;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.minigame.sudoku.GameSudoku;

public class CPSudokuEndGameCheck implements IClientGamePacket {

    private Pos2i pos;

    /**
     * Only for using via reflection
     */
    @Deprecated
    public CPSudokuEndGameCheck() {}

    public CPSudokuEndGameCheck(Pos2i pos) {
        this.pos = pos;
    }

    @Override
    public void encode(PacketBuffer bufferTo) throws IOException {
        bufferTo.writeInt(pos.getX());
        bufferTo.writeInt(pos.getY());
    }

    @Override
    public void decode(PacketBuffer bufferFrom) throws IOException {
        this.pos = new Pos2i(bufferFrom.readInt(), bufferFrom.readInt());
    }

    @Override
    public <STAGE extends LootGame.Stage, G extends LootGame<STAGE, G>> void runOnServer(EntityPlayerMP sender,
            LootGame<STAGE, G> genericGame) {
        if (!(genericGame instanceof GameSudoku)) return;

        ((GameSudoku) genericGame).handleEndGameCheck();
    }
}
