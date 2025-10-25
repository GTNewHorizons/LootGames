package ru.timeconqueror.lootgames.common.packet.game.sudoku;

import net.minecraft.nbt.NBTTagCompound;
import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.NBTGamePacket;
import ru.timeconqueror.lootgames.minigame.sudoku.GameSudoku;
import ru.timeconqueror.lootgames.minigame.sudoku.SudokuBoard;

import java.util.Objects;

public class SPSSyncBoard extends NBTGamePacket {

    /**
     * Only for using via reflection
     */
    @Deprecated
    public SPSSyncBoard() {}

    public SPSSyncBoard(SudokuBoard board) {
        super(() -> {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("board", board.writeNBT());
            return nbt;
        });
    }

    @Override
    public <S extends LootGame.Stage, T extends LootGame<S, T>> void runOnClient(LootGame<S, T> genericGame) {
        GameSudoku game = (GameSudoku) genericGame;
        NBTTagCompound boardTag = Objects.requireNonNull(getCompound()).getCompoundTag("board");
        game.getBoard().readNBT(boardTag);
    }
}
