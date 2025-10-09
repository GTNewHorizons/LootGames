package com.lootgames.sudoku.block;

import com.lootgames.sudoku.config.ConfigSudoku;
import com.lootgames.sudoku.sudoku.GameSudoku;

import ru.timeconqueror.lootgames.api.block.tile.BoardGameMasterTile;

public class SudokuTile extends BoardGameMasterTile<GameSudoku> {

    public SudokuTile() {
        super(new GameSudoku());
    }

    public void init(ConfigSudoku.ConfigSudokuSnapshot configSnapshot) {
        game.setConfigSnapshot(configSnapshot);
    }
}
