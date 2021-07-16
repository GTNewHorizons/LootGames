package ru.timeconqueror.lootgames.api.minigame;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.api.block.tile.BoardGameMasterTile;
import ru.timeconqueror.lootgames.api.block.tile.GameMasterTile;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.utils.MouseClickType;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.timecore.api.util.EnvironmentUtils;

import javax.annotation.Nullable;

import static ru.timeconqueror.lootgames.api.minigame.BoardLootGame.BoardStage;

/**
 * Loot game that is flat.
 */
public abstract class BoardLootGame<G extends BoardLootGame<G>> extends LootGame<BoardStage, G> {
    private static final Logger LOGGER = LogManager.getLogger();
    public static boolean disableMasterCheckWarning;
    @Nullable
    private BoardStage stage;

    @Override
    public void setMasterTileEntity(GameMasterTile<G> masterTileEntity) {
        super.setMasterTileEntity(masterTileEntity);

        if (EnvironmentUtils.isInDev() && !disableMasterCheckWarning && !(masterTileEntity instanceof BoardGameMasterTile<?>)) {
            LOGGER.warn("You probably forgot that {} needs to extend {} to handle {} properly.", masterTileEntity.getClass().getSimpleName(), BoardGameMasterTile.class.getSimpleName(), BoardLootGame.class.getSimpleName());
        }
    }

    @Override
    public BlockPos getGameCenter() {
        int size = getCurrentBoardSize();
        return getBoardOrigin().offset(size / 2, 0, size / 2);
    }

    @Override
    protected BlockPos getRoomFloorPos() {
        return getMasterPos();
    }

    public abstract int getCurrentBoardSize();

    public abstract int getAllocatedBoardSize();

    @Override
    public void onDestroy() {
        LootGamesAPI.getFieldManager().clearBoard(((WorldServer) getWorld()), getMasterPos(), getAllocatedBoardSize(), getAllocatedBoardSize());
    }

    public Pos2i convertToGamePos(BlockPos subordinatePos) {
        BlockPos result = subordinatePos.subtract(getBoardOrigin());
        return new Pos2i(result.getX(), result.getZ());
    }

    public BlockPos convertToBlockPos(Pos2i pos) {
        return getBoardOrigin().offset(pos.getX(), 0, pos.getY());
    }

    public BlockPos getBoardOrigin() {
        int offset = getAllocatedBoardSize() - getCurrentBoardSize();
        return getMasterPos().mutable().move(1, 0, 1).move(offset / 2, 0, offset / 2).immutable();
    }

    public void onClick(EntityPlayer player, Pos2i pos, MouseClickType type) {
        if (getStage() != null) {
            getStage().onClick(player, pos, type);
        }
    }

    public abstract static class BoardStage extends LootGame.Stage {
        /**
         * Called for both sides when player clicks on board block.
         */
        protected void onClick(EntityPlayer player, Pos2i pos, MouseClickType type) {
        }
    }
}
