package ru.timeconqueror.lootgames.common.packet.game.sudoku;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;

import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.NBTGamePacket;
import ru.timeconqueror.lootgames.minigame.sudoku.GameSudoku;
import ru.timeconqueror.lootgames.minigame.sudoku.SudokuBoard;

public class SPSudokuLevelComplete extends NBTGamePacket {

    /**
     * Only for using via reflection
     */
    @Deprecated
    public SPSudokuLevelComplete() {}

    public SPSudokuLevelComplete(SudokuBoard newBoard) {
        super(() -> {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("board", newBoard.writeNBT());
            return nbt;
        });
    }

    @Override
    public <S extends LootGame.Stage, T extends LootGame<S, T>> void runOnClient(LootGame<S, T> genericGame) {
        GameSudoku game = (GameSudoku) genericGame;
        NBTTagCompound boardTag = Objects.requireNonNull(getCompound()).getCompoundTag("board");
        SudokuBoard pendingBoard = new SudokuBoard();
        pendingBoard.readNBT(boardTag);
        game.startLevelCompleteAnim(pendingBoard);
    }
}
