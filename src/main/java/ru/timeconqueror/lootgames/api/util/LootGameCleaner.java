package ru.timeconqueror.lootgames.api.util;

import eu.usrv.legacylootgames.blocks.DungeonBrick;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.BlockState;
import ru.timeconqueror.lootgames.utils.future.WorldExt;

public class LootGameCleaner {
    private static final BlockState DUNGEON_FLOOR = BlockState.of(LGBlocks.DUNGEON_WALL, DungeonBrick.Type.FLOOR.ordinal());
    private static final BlockState SHIELDED_FLOOR = BlockState.of(LGBlocks.DUNGEON_WALL, DungeonBrick.Type.FLOOR_SHIELDED.ordinal());

    //TODO make it called only when in structure
    public static void resetUnbreakablePlayField(World world, BlockPos floorPos) {
        if (!resetUnbreakableFieldsStartingFrom(world, floorPos)) {
            resetUnbreakableFieldsStartingFrom(world, floorPos.north());
            resetUnbreakableFieldsStartingFrom(world, floorPos.east());
            resetUnbreakableFieldsStartingFrom(world, floorPos.south());
            resetUnbreakableFieldsStartingFrom(world, floorPos.west());
        }
    }

    private static boolean resetUnbreakableFieldsStartingFrom(World world, BlockPos blockPos) {
        BlockState state = WorldExt.getBlockState(world, blockPos);
        if (state.equals(SHIELDED_FLOOR)) {
            WorldExt.setBlockState(world, blockPos, DUNGEON_FLOOR);

            resetUnbreakableFieldsStartingFrom(world, blockPos.north());
            resetUnbreakableFieldsStartingFrom(world, blockPos.east());
            resetUnbreakableFieldsStartingFrom(world, blockPos.south());
            resetUnbreakableFieldsStartingFrom(world, blockPos.west());

            return true;
        }

        return false;
    }
}