package ru.timeconqueror.lootgames.common.packet.game.sudoku;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.IClientGamePacket;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.minigame.sudoku.GameSudoku;
import ru.timeconqueror.lootgames.minigame.sudoku.SudokuBoard;

public class CPSudokuToggleNote implements IClientGamePacket {

    private Pos2i pos;
    private int digit;

    /**
     * Only for using via reflection
     */
    @Deprecated
    public CPSudokuToggleNote() {}

    public CPSudokuToggleNote(Pos2i pos, int digit) {
        this.pos = pos;
        this.digit = digit;
    }

    @Override
    public void encode(PacketBuffer bufferTo) throws IOException {
        bufferTo.writeInt(pos.getX());
        bufferTo.writeInt(pos.getY());
        bufferTo.writeInt(digit);
    }

    @Override
    public void decode(PacketBuffer bufferFrom) throws IOException {
        this.pos = new Pos2i(bufferFrom.readInt(), bufferFrom.readInt());
        this.digit = bufferFrom.readInt();
    }

    @Override
    public <STAGE extends LootGame.Stage, G extends LootGame<STAGE, G>> void runOnServer(EntityPlayerMP sender,
            LootGame<STAGE, G> genericGame) {
        if (!(genericGame instanceof GameSudoku)) return;
        if (pos.getX() < 0 || pos.getX() >= SudokuBoard.SIZE || pos.getY() < 0 || pos.getY() >= SudokuBoard.SIZE)
            return;
        if (digit < 1 || digit > 9) return;

        GameSudoku game = (GameSudoku) genericGame;
        game.getBoard().toggleNote(pos, digit);
        game.sendUpdatePacketToNearby(new SPSSyncCellNotes(pos, game.getBoard().getNotes(pos)));
        game.save();
    }
}
