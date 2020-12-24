package ru.timeconqueror.lootgames.api.minigame;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.api.block.BlockFieldBorder;
import ru.timeconqueror.lootgames.api.block.IGameField;
import ru.timeconqueror.lootgames.api.block.tile.TileEntityGameMaster;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.registry.LGSounds;
import ru.timeconqueror.lootgames.utils.BlockUtils;
import ru.timeconqueror.lootgames.utils.CollectionUtils;
import ru.timeconqueror.timecore.util.NetworkUtils;
import ru.timeconqueror.timecore.util.WorldUtils;

import java.util.function.Consumer;

/**
 * Can be accessed via {@link LootGamesAPI#getBoardGenerator()}.
 * <p>
 * Manager that can be used to handle generation and clearing flat board from {@link BoardLootGame}
 */
public class BoardGenerator {

    public boolean canReplaceAreaWithBoard(World world, BlockPos cornerPos, int xSize, int ySize, int zSize, @Nullable BlockPos except) {
        return CollectionUtils.allMatch(BlockUtils.between(cornerPos, xSize, ySize, zSize), (pos) ->
                world.getBlockState(pos).getMaterial().isReplaceable() || pos.equals(except)
        );
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
    public GenerationChain trySetupBoard(ServerWorld world, BlockPos centerPos, int xSize, int height, int zSize, BlockState masterBlock, @Nullable PlayerEntity player) {
        BlockPos cornerPos = centerPos.offset(-xSize / 2 - 1, 0, -zSize / 2 - 1);
        BlockPos.Mutable borderPos = cornerPos.mutable();
        if (!canReplaceAreaWithBoard(world, borderPos, xSize + 2, height + 1, zSize + 2, centerPos)) {
            if (player != null) {
                NetworkUtils.sendMessage(player, new TranslationTextComponent("msg.lootgames.field.not_enough_space", xSize + 2, height + 1, zSize + 2));
                world.playSound(player, centerPos, LGSounds.GOL_GAME_LOSE, SoundCategory.BLOCKS, 0.6F, 1.0F);//TODO change sound?
            }
            return new GenerationChain(null, null, false);
        }

        // Filling field with subordinate blocks
        for (BlockPos pos : BlockUtils.between(cornerPos.offset(1, 0, 1), xSize, 1, zSize)) {
            world.setBlock(pos, LGBlocks.SMART_SUBORDINATE.defaultBlockState(), 2);
        }

        // Filling area above field with air
        if (height > 0) {
            BlockPos abovePos = borderPos.offset(0, 1, 0);
            for (BlockPos pos : BlockUtils.between(abovePos, xSize + 2, height, zSize + 2)) {
                world.removeBlock(pos, false);
            }
        }

        // Filling border corners
        world.setBlock(borderPos, LGBlocks.FIELD_BORDER.defaultBlockState().setValue(BlockFieldBorder.TYPE, BlockFieldBorder.Type.TOP_LEFT), 2);
        borderPos.move(xSize + 1, 0, 0);
        world.setBlock(borderPos, LGBlocks.FIELD_BORDER.defaultBlockState().setValue(BlockFieldBorder.TYPE, BlockFieldBorder.Type.TOP_RIGHT), 2);
        borderPos.move(0, 0, zSize + 1);
        world.setBlock(borderPos, LGBlocks.FIELD_BORDER.defaultBlockState().setValue(BlockFieldBorder.TYPE, BlockFieldBorder.Type.BOTTOM_RIGHT), 2);
        borderPos.move(-xSize - 1, 0, 0);
        world.setBlock(borderPos, LGBlocks.FIELD_BORDER.defaultBlockState().setValue(BlockFieldBorder.TYPE, BlockFieldBorder.Type.BOTTOM_LEFT), 2);

        // Filling border edges
        borderPos.set(cornerPos);
        for (int i = 0; i < xSize; i++) {
            borderPos.move(1, 0, 0);
            world.setBlock(borderPos, LGBlocks.FIELD_BORDER.defaultBlockState().setValue(BlockFieldBorder.TYPE, BlockFieldBorder.Type.HORIZONTAL), 2);
            borderPos.move(0, 0, zSize + 1);
            world.setBlock(borderPos, LGBlocks.FIELD_BORDER.defaultBlockState().setValue(BlockFieldBorder.TYPE, BlockFieldBorder.Type.HORIZONTAL), 2);
            borderPos.move(0, 0, -zSize - 1);
        }
        borderPos.set(cornerPos);
        for (int i = 0; i < xSize; i++) {
            borderPos.move(0, 0, 1);
            world.setBlock(borderPos, LGBlocks.FIELD_BORDER.defaultBlockState().setValue(BlockFieldBorder.TYPE, BlockFieldBorder.Type.VERTICAL), 2);
            borderPos.move(xSize + 1, 0, 0);
            world.setBlock(borderPos, LGBlocks.FIELD_BORDER.defaultBlockState().setValue(BlockFieldBorder.TYPE, BlockFieldBorder.Type.VERTICAL), 2);
            borderPos.move(-xSize - 1, 0, 0);
        }

        world.setBlock(cornerPos, masterBlock, 3);

        return new GenerationChain(world, cornerPos, true);
    }

    public void clearBoard(ServerWorld world, BlockPos start, int sizeX, int sizeZ) {
        Iterable<BlockPos> gameBlocks = BlockPos.betweenClosed(start.offset(-1, 0, -1), start.offset(sizeX + 1, 0, sizeZ + 1));

        for (BlockPos pos : gameBlocks) {
            if (world.getBlockState(pos).getBlock() instanceof IGameField) {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
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

        public <T extends TileEntityGameMaster<?>> GenerationChain forTileIfSucceed(Class<T> tileClass, Consumer<T> action) {
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