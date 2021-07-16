package ru.timeconqueror.lootgames.api.minigame;

import net.minecraft.world.World;
import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.utils.future.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Can be accessed via {@link LootGamesAPI#getGameManager()}.
 *///TODO bind packets to their games, so we dont need to cast in runOnServer and client
public class GameManager {
    private static final List<ILootGameFactory> GAME_GEN_LIST = new ArrayList<>();

    /**
     * Register game and its factory.
     */
    public void registerGameGenerator(ILootGameFactory generator) {
        GAME_GEN_LIST.add(generator);
    }

    public static boolean isGameListEmpty() {
        return GAME_GEN_LIST.isEmpty();
    }

    /**
     * Generates random game.
     *
     * @param world           The world for the Generator
     * @param puzzleMasterPos The position of Puzzle Master Block
     * @return error message if provided
     */
    public Optional<String> generateRandomGame(World world, BlockPos puzzleMasterPos) {
        if (!isGameListEmpty()) {
            GAME_GEN_LIST.get(world.rand.nextInt(GAME_GEN_LIST.size())).genOnPuzzleMasterClick(world, puzzleMasterPos);
        } else {
            return Optional.of("Can't generate any game on pos" + puzzleMasterPos + ", because game list is empty.");
        }

        return Optional.empty();
    }
}
