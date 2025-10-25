package ru.timeconqueror.lootgames.common.block.tile;

import ru.timeconqueror.lootgames.api.block.tile.BoardGameMasterTile;
import ru.timeconqueror.lootgames.common.config.ConfigSudoku;
import ru.timeconqueror.lootgames.minigame.sudoku.GameSudoku;

public class SudokuTile extends BoardGameMasterTile<GameSudoku> {

    public SudokuTile() {
        super(new GameSudoku());
    }

    public void init(ConfigSudoku.ConfigSudokuSnapshot configSnapshot) {
        game.setConfigSnapshot(configSnapshot);
    }
}
