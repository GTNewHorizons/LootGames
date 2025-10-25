package ru.timeconqueror.lootgames.common.packet.game.sudoku;

import net.minecraft.network.PacketBuffer;

import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.IServerGamePacket;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.minigame.sudoku.GameSudoku;
import ru.timeconqueror.lootgames.minigame.sudoku.SudokuBoard;

public class SPSudokuResetNumber implements IServerGamePacket {

    @Override
    public void encode(PacketBuffer bufferTo) {}

    @Override
    public void decode(PacketBuffer bufferFrom) {}

    @Override
    public <S extends LootGame.Stage, T extends LootGame<S, T>> void runOnClient(LootGame<S, T> game) {
        GameSudoku sudokuGame = (GameSudoku) game;
        SudokuBoard board = sudokuGame.getBoard();
        for (int x = 0; x < SudokuBoard.SIZE; x++) {
            for (int y = 0; y < SudokuBoard.SIZE; y++) {
                board.cSetPlayerValue(new Pos2i(x, y), 0);
            }
        }
    }
}
