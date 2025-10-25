package ru.timeconqueror.lootgames.minigame.sudoku;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import ru.timeconqueror.lootgames.api.minigame.BoardLootGame;
import ru.timeconqueror.lootgames.api.minigame.ILootGameFactory;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.api.util.RewardUtils;
import ru.timeconqueror.lootgames.common.config.ConfigSudoku;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.SPSSyncBoard;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.SPSSyncCell;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.SPSudokuSpawnLevelBeatParticles;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.utils.MouseClickType;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.WorldExt;
import ru.timeconqueror.lootgames.utils.sanity.Sounds;
import ru.timeconqueror.timecore.api.common.tile.SerializationType;

//TODO maybe allow players to choose rewards of the prev level, if they cannot beat the current one
public class GameSudoku extends BoardLootGame<GameSudoku> {

    public long endGameCheckTime;

    public int currentLevel = 1;

    @Getter
    public SudokuBoard board;
    @Getter
    @Setter
    public ConfigSudoku.ConfigSudokuSnapshot configSnapshot = null;

    public GameSudoku() {
        board = new SudokuBoard();
    }

    @Override
    public void onPlace() {
        setupInitialStage(new StageWaiting());
        if (isServerSide()) {
            configSnapshot = LGConfigs.SUDOKU.snapshot();
            int blanks = configSnapshot.getStage1()
                .blanksCount();
            board.generate(blanks);
        }
        super.onPlace();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isClientSide()) {
            configSnapshot = ConfigSudoku.ConfigSudokuSnapshot.stub();
        }
    }

    @Override
    public int getCurrentBoardSize() {
        return SudokuBoard.SIZE;
    }

    @Override
    public int getAllocatedBoardSize() {
        return SudokuBoard.SIZE;
    }

    public static class Factory implements ILootGameFactory {

        @Override
        public void genOnPuzzleMasterClick(World world, BlockPos puzzleMasterPos) {
            BlockPos floorCenterPos = puzzleMasterPos.offset(0, -2, 0);
            WorldExt.setBlock(world, floorCenterPos, LGBlocks.SDK_ACTIVATOR);
        }
    }

    public void onLevelSuccessfullyFinished() {
        if (currentLevel < 4) {
            sendUpdatePacketToNearby(new SPSudokuSpawnLevelBeatParticles());
            sendToNearby(new ChatComponentTranslation("msg.com.lootgames.stage_complete"));
            WorldExt.playSoundServerly(getWorld(), getGameCenter(), Sounds.PLAYER_LEVELUP, 0.75F, 1.0F);

            currentLevel++;
            int blanks = configSnapshot.getStageByIndex(currentLevel)
                .blanksCount();
            board.generate(blanks);
            saveAndSync();
        } else {
            triggerGameWin();
        }
    }

    @Override
    protected void triggerGameWin() {
        super.triggerGameWin();
        RewardUtils.spawnFourStagedReward(
            (WorldServer) getWorld(),
            this,
            getGameCenter(),
            currentLevel,
            LGConfigs.REWARDS.rewardsSudoku);
    }

    @Override
    public void writeNBT(NBTTagCompound nbt, SerializationType type) {
        super.writeNBT(nbt, type);
        nbt.setTag("board", board.writeNBT());
        nbt.setInteger("current_level", currentLevel);
        nbt.setTag("config_snapshot", ConfigSudoku.ConfigSudokuSnapshot.serialize(configSnapshot));
    }

    @Override
    public void readNBT(NBTTagCompound nbt, SerializationType type) {
        super.readNBT(nbt, type);
        board.readNBT(nbt.getCompoundTag("board"));
        currentLevel = nbt.getInteger("current_level");
        configSnapshot = ConfigSudoku.ConfigSudokuSnapshot.deserialize(nbt.getCompoundTag("config_snapshot"));
    }

    @Override
    public BoardStage createStageFromNBT(String id, NBTTagCompound tag, SerializationType type) {
        if (StageWaiting.ID.equals(id)) return new StageWaiting();
        throw new IllegalArgumentException("Unknown stage: " + id);
    }

    public class StageWaiting extends BoardStage {

        public static final String ID = "waiting";

        @Override
        protected void onClick(EntityPlayer player, Pos2i pos, MouseClickType type) {
            if (!isServerSide()) return;
            if (!board.isGenerated()) {
                int blanks = configSnapshot.getStageByIndex(currentLevel)
                    .blanksCount();
                board.generate(blanks);
                sendUpdatePacketToNearby(new SPSSyncBoard(board));
                return;
            }
            if (type == MouseClickType.LEFT) {
                if (endGameCheckTime != 0 && System.currentTimeMillis() - endGameCheckTime <= 500) {
                    if (currentLevel > 1) {
                        triggerGameWin();
                    } else {
                        triggerGameLose();
                    }
                } else {
                    sendToNearby(new ChatComponentTranslation("msg.lootgames.sdk.check_end"));
                    endGameCheckTime = System.currentTimeMillis();
                }
            } else if (player.isSneaking() && type == MouseClickType.RIGHT) {
                board.cycleValueMinus(pos);
            } else if (type == MouseClickType.RIGHT) {
                board.cycleValueAdd(pos);
            }
            sendUpdatePacketToNearby(new SPSSyncCell(pos, board.getPlayerValue(pos)));
            save();
            if (board.checkWin()) {
                onLevelSuccessfullyFinished();
            }
        }

        @Override
        public String getID() {
            return ID;
        }
    }
}
