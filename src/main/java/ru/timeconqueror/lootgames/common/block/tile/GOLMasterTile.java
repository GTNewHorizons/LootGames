package ru.timeconqueror.lootgames.common.block.tile;

import ru.timeconqueror.lootgames.api.block.tile.BoardGameMasterTile;
import ru.timeconqueror.lootgames.minigame.gol.GameOfLight;

public class GOLMasterTile extends BoardGameMasterTile<GameOfLight> {
    public GOLMasterTile() {
        super(new GameOfLight());
    }
}
