package com.lootgames.sudoku.packet;

import net.minecraft.network.PacketBuffer;

import com.lootgames.sudoku.sudoku.GameSudoku;

import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.IServerGamePacket;
import ru.timeconqueror.lootgames.api.util.Pos2i;

public class SPSSyncCell implements IServerGamePacket {

    public Pos2i pos;
    public int value;
    public long lastTime;

    @Deprecated
    public SPSSyncCell() {}

    public SPSSyncCell(Pos2i pos, int value, long lastTime) {
        this.pos = pos;
        this.value = value;
        this.lastTime = lastTime;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(value);
        buf.writeLong(lastTime);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.pos = new Pos2i(buf.readInt(), buf.readInt());
        this.value = buf.readInt();
        this.lastTime = buf.readLong();
    }

    @Override
    public <S extends LootGame.Stage, T extends LootGame<S, T>> void runOnClient(LootGame<S, T> genericGame) {
        GameSudoku game = (GameSudoku) genericGame;
        game.getBoard().cSetPlayerValue(pos, value);
        game.getBoard().setLastClickTime(lastTime);
    }
}
