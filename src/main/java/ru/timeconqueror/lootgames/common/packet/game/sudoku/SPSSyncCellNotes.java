package ru.timeconqueror.lootgames.common.packet.game.sudoku;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.IServerGamePacket;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.minigame.sudoku.GameSudoku;

public class SPSSyncCellNotes implements IServerGamePacket {

    private Pos2i pos;
    private boolean[] notes;

    /**
     * Only for using via reflection
     */
    @Deprecated
    public SPSSyncCellNotes() {}

    public SPSSyncCellNotes(Pos2i pos, boolean[] notes) {
        this.pos = pos;
        this.notes = new boolean[9];
        if (notes != null) {
            System.arraycopy(notes, 0, this.notes, 0, Math.min(notes.length, this.notes.length));
        }
    }

    @Override
    public void encode(PacketBuffer bufferTo) throws IOException {
        bufferTo.writeInt(pos.getX());
        bufferTo.writeInt(pos.getY());
        for (int i = 0; i < 9; i++) {
            bufferTo.writeByte(notes[i] ? 1 : 0);
        }
    }

    @Override
    public void decode(PacketBuffer bufferFrom) throws IOException {
        this.pos = new Pos2i(bufferFrom.readInt(), bufferFrom.readInt());
        this.notes = new boolean[9];
        for (int i = 0; i < 9; i++) {
            this.notes[i] = bufferFrom.readByte() != 0;
        }
    }

    @Override
    public <S extends LootGame.Stage, T extends LootGame<S, T>> void runOnClient(LootGame<S, T> genericGame) {
        GameSudoku game = (GameSudoku) genericGame;
        game.getBoard().setNotes(pos, notes);
    }
}
