package ru.timeconqueror.lootgames.registry;

import cpw.mods.fml.common.registry.GameRegistry;
import eu.usrv.legacylootgames.blocks.DungeonBrick;
import eu.usrv.legacylootgames.blocks.DungeonLightSource;
import eu.usrv.legacylootgames.items.DungeonBlockItem;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.block.BoardBorderBlock;
import ru.timeconqueror.lootgames.api.block.GameMasterBlock;
import ru.timeconqueror.lootgames.api.block.SmartSubordinateBlock;
import ru.timeconqueror.lootgames.api.block.tile.GameMasterTile;
import ru.timeconqueror.lootgames.common.block.GOLActivatorBlock;
import ru.timeconqueror.lootgames.common.block.MSActivatorBlock;
import ru.timeconqueror.lootgames.common.block.PuzzleMasterBlock;
import ru.timeconqueror.lootgames.common.block.tile.GOLMasterTile;
import ru.timeconqueror.lootgames.common.block.tile.MSMasterTile;
import ru.timeconqueror.lootgames.common.block.tile.PuzzleMasterTile;

import java.util.function.Supplier;

public class LGBlocks {
    public static final DungeonBrick DUNGEON_WALL = new DungeonBrick();
    public static final DungeonLightSource DUNGEON_LAMP = new DungeonLightSource();
    public static final PuzzleMasterBlock PUZZLE_MASTER = new PuzzleMasterBlock();

    public static final SmartSubordinateBlock SMART_SUBORDINATE = new SmartSubordinateBlock();
    public static final BoardBorderBlock BOARD_BORDER = new BoardBorderBlock();

    public static final MSActivatorBlock MS_ACTIVATOR = new MSActivatorBlock();
    public static final GOLActivatorBlock GOL_ACTIVATOR = new GOLActivatorBlock();
    public static final GameMasterBlock MS_MASTER = gameMaster(MSMasterTile::new);
    public static final GameMasterBlock GOL_MASTER = gameMaster(GOLMasterTile::new);

    public static void register() {
        GameRegistry.registerBlock(DUNGEON_WALL, DungeonBlockItem.class, "LootGamesDungeonWall");
        GameRegistry.registerBlock(DUNGEON_LAMP, DungeonBlockItem.class, "LootGamesDungeonLight");
        GameRegistry.registerBlock(PUZZLE_MASTER, ItemBlock.class, "LootGamesMasterBlock");
        GameRegistry.registerTileEntity(PuzzleMasterTile.class, "LOOTGAMES_MASTER_TE");

        regBlock(SMART_SUBORDINATE, "smart_subordinate");
        regBlock(BOARD_BORDER, "board_border");
        regBlock(GOL_ACTIVATOR.setCreativeTab(LootGames.CREATIVE_TAB), "gol_activator");
        regBlock(MS_ACTIVATOR.setCreativeTab(LootGames.CREATIVE_TAB), "ms_activator");
        regBlock(GOL_MASTER, "gol_master");
        regBlock(MS_MASTER, "ms_master");

        GameRegistry.registerTileEntity(GOLMasterTile.class, "gol_master");
        GameRegistry.registerTileEntity(MSMasterTile.class, "ms_master");
    }

    public static void regBlock(Block block, String name) {
        block.setUnlocalizedName(LootGames.dotted(name));
        GameRegistry.registerBlock(block, name);
    }

    private static GameMasterBlock gameMaster(Supplier<GameMasterTile<?>> tileEntityFactory) {
        return new GameMasterBlock((blockState, world) -> tileEntityFactory.get());
    }
}
