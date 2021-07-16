package ru.timeconqueror.lootgames.api.minigame;

import com.google.common.collect.Iterables;
import eu.usrv.legacylootgames.blocks.DungeonBrick;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.api.block.BoardBorderBlock;
import ru.timeconqueror.lootgames.api.block.IGameField;
import ru.timeconqueror.lootgames.api.block.tile.GameMasterTile;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.registry.LGSounds;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.BlockState;
import ru.timeconqueror.lootgames.utils.future.ChatComponentExt;
import ru.timeconqueror.lootgames.utils.future.WorldExt;
import ru.timeconqueror.timecore.api.util.BlockPosUtils;
import ru.timeconqueror.timecore.api.util.CollectionUtils;
import ru.timeconqueror.timecore.api.util.NetworkUtils;
import ru.timeconqueror.timecore.api.util.WorldUtils;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Can be accessed via {@link LootGamesAPI#getFieldManager()}.
 * <p>
 * Manager that can be used to handle generation and clearing flat board from {@link BoardLootGame}
 */
public class FieldManager {

    private boolean fieldUnderBreaking = false;

    /**
     * Destroys the full structure, which this block belongs to.
     */
    public void onFieldBlockBroken(World worldIn, Supplier<BlockPos> masterPosGetter) {
        if (tryStartFieldBreaking()) {
            BlockPos masterPos = masterPosGetter.get();
            TileEntity tileEntity = WorldExt.getTileEntity(worldIn, masterPos);

            if (tileEntity instanceof GameMasterTile<?>) {
                ((GameMasterTile<?>) tileEntity).onDestroy();
            }
            stopFieldBreaking();
        }
    }

    public boolean tryStartFieldBreaking() {
        if (!fieldUnderBreaking) {
            fieldUnderBreaking = true;
            return true;
        }
        return false;
    }

    public void stopFieldBreaking() {
        fieldUnderBreaking = false;
    }

    public boolean canReplaceAreaWithBoard(World world, BlockPos cornerPos, int xSize, int ySize, int zSize, @Nullable BlockPos except) {
        Iterable<BlockPos> positions = Iterables.concat(
                BlockPosUtils.between(cornerPos, xSize, 1, zSize), // board positions
                //second area is smaller because we don't need to check if the player can fit the place in the corner blocks above the border.
                BlockPosUtils.between(cornerPos.offset(1, 1, 1), xSize - 2, ySize - 1, zSize - 2) // positions above the board
        );

        return CollectionUtils.allMatch(positions, (pos) -> {
            BlockState state = WorldExt.getBlockState(world, pos);
            return (state.getBlock() == LGBlocks.DUNGEON_WALL && state.getMeta() == DungeonBrick.Type.FLOOR_SHIELDED.ordinal()) || state.getBlock().getMaterial().isReplaceable() || pos.equals(except);
        });
    }

    /**
     * Tries to replace area with flat board.
     *
     * @param xSize  size of field without border
     * @param height height of area above field exclude floor block
     * @param zSize  size of field without border
     * @param player to notify about fail
     * @return true if field placed.
     */
    public GenerationChain trySetupBoard(WorldServer world, BlockPos centerPos, int xSize, int height, int zSize, Block masterBlock, @Nullable EntityPlayer player) {
        BlockPos cornerPos = centerPos.offset(-xSize / 2 - 1, 0, -zSize / 2 - 1);
        BlockPos.Mutable borderPos = cornerPos.mutable();
        if (!canReplaceAreaWithBoard(world, borderPos, xSize + 2, height + 1, zSize + 2, centerPos)) {
            if (player != null) {
                NetworkUtils.sendMessage(player, ChatComponentExt.withStyle(new ChatComponentTranslation("msg.lootgames.field.not_enough_space", xSize + 2, height + 1, zSize + 2), NotifyColor.FAIL.getColor()));
                WorldExt.playSoundServerly(world, centerPos, LGSounds.GAME_LOSE, 0.4F, 1.0F);
            }
            return new GenerationChain(null, null, false);
        }

        // Filling field with subordinate blocks
        for (BlockPos pos : BlockPosUtils.between(cornerPos.offset(1, 0, 1), xSize, 1, zSize)) {
            WorldExt.setBlock(world, pos, LGBlocks.SMART_SUBORDINATE, 2);
        }

        long end3 = System.currentTimeMillis();
        // Filling area above field with air
        if (height > 0) {
            BlockPos abovePos = borderPos.offset(1, 1, 1);
            for (BlockPos pos : BlockPosUtils.between(abovePos, xSize, height, zSize)) {
                WorldExt.setBlock(world, pos, Blocks.air, 0, 2);
            }
        }

        // Filling border corners and master block
        WorldExt.setBlock(world, cornerPos, masterBlock, 3);
        borderPos.move(xSize + 1, 0, 0);
        WorldExt.setBlock(world, borderPos, LGBlocks.BOARD_BORDER, BoardBorderBlock.Type.TOP_RIGHT.ordinal(), 2);
        borderPos.move(0, 0, zSize + 1);
        WorldExt.setBlock(world, borderPos, LGBlocks.BOARD_BORDER, BoardBorderBlock.Type.BOTTOM_RIGHT.ordinal(), 2);
        borderPos.move(-xSize - 1, 0, 0);
        WorldExt.setBlock(world, borderPos, LGBlocks.BOARD_BORDER, BoardBorderBlock.Type.BOTTOM_LEFT.ordinal(), 2);

        // Filling border edges
        borderPos.set(cornerPos);
        for (int i = 0; i < xSize; i++) {
            borderPos.move(1, 0, 0);
            WorldExt.setBlock(world, borderPos, LGBlocks.BOARD_BORDER, BoardBorderBlock.Type.HORIZONTAL.ordinal(), 2);
            borderPos.move(0, 0, zSize + 1);
            WorldExt.setBlock(world, borderPos, LGBlocks.BOARD_BORDER, BoardBorderBlock.Type.HORIZONTAL.ordinal(), 2);
            borderPos.move(0, 0, -zSize - 1);
        }
        borderPos.set(cornerPos);
        for (int i = 0; i < xSize; i++) {
            borderPos.move(0, 0, 1);
            WorldExt.setBlock(world, borderPos, LGBlocks.BOARD_BORDER, BoardBorderBlock.Type.VERTICAL.ordinal(), 2);
            borderPos.move(xSize + 1, 0, 0);
            WorldExt.setBlock(world, borderPos, LGBlocks.BOARD_BORDER, BoardBorderBlock.Type.VERTICAL.ordinal(), 2);
            borderPos.move(-xSize - 1, 0, 0);
        }

        return new GenerationChain(world, cornerPos, true);
    }

    public void clearBoard(WorldServer world, BlockPos start, int sizeX, int sizeZ) {
        Iterable<BlockPos> gameBlocks = BlockPos.betweenClosed(start.offset(-1, 0, -1), start.offset(sizeX + 1, 0, sizeZ + 1));

        for (BlockPos pos : gameBlocks) {
            if (WorldExt.getBlock(world, pos) instanceof IGameField) {
                WorldExt.setBlockToAir(world, pos);
            }
        }
    }

    public static class GenerationChain {
        private final World world;
        private final BlockPos pos;
        private final boolean succeed;

        private GenerationChain(World world, BlockPos masterPos, boolean succeed) {
            this.world = world;
            this.succeed = succeed;
            this.pos = masterPos;
        }

        public <T extends GameMasterTile<?>> GenerationChain forTileIfSucceed(Class<T> tileClass, Consumer<T> action) {
            if (succeed) {
                WorldUtils.forTileWithReqt(world, pos, tileClass, action);
            }

            return this;
        }

        public boolean isSucceed() {
            return succeed;
        }
    }
}
