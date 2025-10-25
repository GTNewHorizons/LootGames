package ru.timeconqueror.lootgames.common.packet.game.sudoku;

import net.minecraft.network.PacketBuffer;

import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.IServerGamePacket;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.minigame.sudoku.GameSudoku;

public class SPSSyncCell implements IServerGamePacket {

    public Pos2i pos;
    public int value;

    /**
     * Only for using via reflection
     */
    @Deprecated
    public SPSSyncCell() {}

    public SPSSyncCell(Pos2i pos, int value) {
        this.pos = pos;
        this.value = value;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(value);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.pos = new Pos2i(buf.readInt(), buf.readInt());
        this.value = buf.readInt();
    }

    @Override
    public <S extends LootGame.Stage, T extends LootGame<S, T>> void runOnClient(LootGame<S, T> genericGame) {
        GameSudoku game = (GameSudoku) genericGame;
        game.getBoard().cSetPlayerValue(pos, value);
    }
}
