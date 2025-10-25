package ru.timeconqueror.lootgames.registry;

import com.lootgames.sudoku.sudoku.GameSudoku;

import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.api.task.TaskCreateExplosion;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.lootgames.minigame.gol.GameOfLight;
import ru.timeconqueror.lootgames.minigame.minesweeper.GameMineSweeper;
import ru.timeconqueror.lootgames.minigame.sudoku.GameSudoku;

public class LGGames {

    public static void register() {
        LootGamesAPI.registerTaskFactory(TaskCreateExplosion.class, TaskCreateExplosion::new);

        for (int i = 0; i < LGConfigs.GOL.weight; i++) LootGamesAPI.registerGameGenerator(new GameOfLight.Factory());
        for (int i = 0; i < LGConfigs.MINESWEEPER.weight; i++)
            LootGamesAPI.registerGameGenerator(new GameMineSweeper.Factory());
        for (int i = 0; i < LGConfigs.SUDOKU.weight; i++) LootGamesAPI.registerGameGenerator(new GameSudoku.Factory());
    }
}
