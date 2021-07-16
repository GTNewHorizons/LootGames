package ru.timeconqueror.lootgames.common.block.tile;

import ru.timeconqueror.lootgames.api.block.tile.BoardGameMasterTile;
import ru.timeconqueror.lootgames.common.config.ConfigMS;
import ru.timeconqueror.lootgames.minigame.minesweeper.GameMineSweeper;

public class MSMasterTile extends BoardGameMasterTile<GameMineSweeper> {
    public MSMasterTile() {
        super(new GameMineSweeper());
    }

    public void init(ConfigMS.Snapshot configSnapshot) {
        game.setConfigSnapshot(configSnapshot);
    }
}
