package com.lootgames.sudoku.packet;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;

import com.lootgames.sudoku.sudoku.GameSudoku;
import com.lootgames.sudoku.sudoku.SudokuBoard;

import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.NBTGamePacket;

public class SPSSyncBoard extends NBTGamePacket {

    public SPSSyncBoard() {}

    public SPSSyncBoard(GameSudoku game, SudokuBoard board) {
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
