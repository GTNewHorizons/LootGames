package ru.timeconqueror.lootgames.registry;

import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.api.task.TaskCreateExplosion;
import ru.timeconqueror.lootgames.minigame.gol.GameOfLight;
import ru.timeconqueror.lootgames.minigame.minesweeper.GameMineSweeper;

public class LGGames {

    public static void register() {
        LootGamesAPI.registerTaskFactory(TaskCreateExplosion.class, TaskCreateExplosion::new);

        LootGamesAPI.registerGameGenerator(new GameOfLight.Factory());
        LootGamesAPI.registerGameGenerator(new GameMineSweeper.Factory());
    }
}
