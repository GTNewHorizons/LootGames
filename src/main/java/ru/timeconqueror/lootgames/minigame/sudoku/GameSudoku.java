package ru.timeconqueror.lootgames.minigame.sudoku;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import lombok.Getter;
import lombok.Setter;
import ru.timeconqueror.lootgames.api.minigame.BoardLootGame;
import ru.timeconqueror.lootgames.api.minigame.ILootGameFactory;
import ru.timeconqueror.lootgames.api.minigame.NotifyColor;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.api.util.RewardUtils;
import ru.timeconqueror.lootgames.common.config.ConfigSudoku;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.SPSSyncBoard;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.SPSSyncCell;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.SPSudokuLevelComplete;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.SPSudokuSpawnLevelBeatParticles;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.SPSudokuWrongAnswer;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.registry.LGSounds;
import ru.timeconqueror.lootgames.utils.MouseClickType;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.WorldExt;
import ru.timeconqueror.lootgames.utils.sanity.Sounds;
import ru.timeconqueror.timecore.api.common.tile.SerializationType;

public class GameSudoku extends BoardLootGame<GameSudoku> {

    // world tick (getTotalWorldTime) at which the end-game check was armed; 0 = not armed
    public long endGameCheckTick;

    public int currentLevel = 1;
    private int attemptCount = 0;
    private boolean sendRevealOnNextTick = false;

    // world tick at which the board first became fully filled; 0 = not full
    private long allBlanksFilledSinceTick;

    private boolean submitReminderSent;

    // client-side animation state
    public long cWrongAnswerAnimStart = -1;
    public long cBoardRevealAnimStart = -1;
    public long cLevelCompleteAnimStart = -1;
    private SudokuBoard cPendingBoardReset = null;
    private SudokuBoard cPendingNewBoard = null;

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
        }
        super.onPlace(); // syncs empty board to clients
        if (isServerSide()) {
            int blanks = configSnapshot.getStage1().blanksCount();
            board.generate(blanks);
            sendRevealOnNextTick = true; // send SPSSyncBoard next tick, after clients have the TE
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isClientSide()) {
            configSnapshot = ConfigSudoku.ConfigSudokuSnapshot.stub();
        }
    }

    @Override
    public void onTick() {
        super.onTick();
        if (!isClientSide()) {
            if (sendRevealOnNextTick) {
                sendRevealOnNextTick = false;
                sendUpdatePacketToNearby(new SPSSyncBoard(board));
                save();
            } else {
                tickSubmitReminder();
            }
            return;
        }

        if (cWrongAnswerAnimStart >= 0) {
            long elapsed = getWorld().getTotalWorldTime() - cWrongAnswerAnimStart;
            if (elapsed == 20) {
                spawnWrongAnswerParticles();
            }
            if (elapsed >= 25) {
                if (cPendingBoardReset != null) {
                    board = cPendingBoardReset;
                    cPendingBoardReset = null;
                }
                cWrongAnswerAnimStart = -1;
            }
        }

        if (cLevelCompleteAnimStart >= 0) {
            long elapsed = getWorld().getTotalWorldTime() - cLevelCompleteAnimStart;
            if (elapsed >= 15) {
                if (cPendingNewBoard != null) {
                    board = cPendingNewBoard;
                    cPendingNewBoard = null;
                }
                cLevelCompleteAnimStart = -1;
                startBoardRevealAnim();
            }
        }
    }

    // --- animation API (client-side) ---

    public void startBoardRevealAnim() {
        cBoardRevealAnimStart = getWorld().getTotalWorldTime();
    }

    public void startWrongAnswerAnim(SudokuBoard pendingReset) {
        cWrongAnswerAnimStart = getWorld().getTotalWorldTime();
        cPendingBoardReset = pendingReset;
    }

    public void startLevelCompleteAnim(SudokuBoard pendingNewBoard) {
        cLevelCompleteAnimStart = getWorld().getTotalWorldTime();
        cPendingNewBoard = pendingNewBoard;
    }

    /** Returns 0..1 progress of board reveal scan, or -1 if not animating. Client-side only. */
    public float getBoardRevealProgress() {
        if (cBoardRevealAnimStart < 0) return -1f;
        return Math.min(1f, (getWorld().getTotalWorldTime() - cBoardRevealAnimStart) / 30f);
    }

    /** Returns 0..1 progress of wrong-answer animation, or -1 if not animating. Client-side only. */
    public float getWrongAnswerProgress() {
        if (cWrongAnswerAnimStart < 0) return -1f;
        return Math.min(1f, (getWorld().getTotalWorldTime() - cWrongAnswerAnimStart) / 25f);
    }

    /** Returns 0..1 progress of level-complete exit animation, or -1 if not animating. Client-side only. */
    public float getLevelCompleteProgress() {
        if (cLevelCompleteAnimStart < 0) return -1f;
        return Math.min(1f, (getWorld().getTotalWorldTime() - cLevelCompleteAnimStart) / 15f);
    }

    private void spawnWrongAnswerParticles() {
        BlockPos origin = getBoardOrigin();
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                if (board.getPuzzleValue(col, row) == 0 && board.getPlayerValue(new Pos2i(col, row)) != 0) {
                    for (int i = 0; i < 2; i++) {
                        getWorld().spawnParticle(
                                "flame",
                                origin.getX() + col + 0.2 + getWorld().rand.nextDouble() * 0.6,
                                origin.getY() + 1.05,
                                origin.getZ() + row + 0.2 + getWorld().rand.nextDouble() * 0.6,
                                0,
                                0.04 + getWorld().rand.nextDouble() * 0.04,
                                0);
                    }
                }
            }
        }
    }

    // --- core game ---

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

    private void tickSubmitReminder() {
        long now = getWorld().getTotalWorldTime();
        if (now % 20 != 0) return;
        if (!board.isGenerated()) {
            allBlanksFilledSinceTick = 0;
            submitReminderSent = false;
            return;
        }
        boolean allFilled = board.countFilledCells() == board.countTotalBlanks();
        if (!allFilled) {
            allBlanksFilledSinceTick = 0;
            submitReminderSent = false;
            return;
        }
        if (allBlanksFilledSinceTick == 0) {
            allBlanksFilledSinceTick = now;
            submitReminderSent = false;
            return;
        }
        // 10 seconds at 20 ticks/second
        if (!submitReminderSent && now - allBlanksFilledSinceTick >= 200L) {
            sendToNearby(new ChatComponentTranslation("msg.lootgames.sdk.submit_reminder"), NotifyColor.NOTIFY);
            submitReminderSent = true;
        }
    }

    public void handleEndGameCheck() {
        long now = getWorld().getTotalWorldTime();
        // 500ms confirm window ~= 10 ticks at 20 ticks/second
        if (endGameCheckTick != 0 && now - endGameCheckTick <= 10) {
            endGameCheckTick = 0;
            if (board.checkWin()) {
                onLevelSuccessfullyFinished();
            } else {
                attemptCount++;
                switch (configSnapshot.getClearOnWrongAnswer()) {
                    case ALL -> board.resetPlayer();
                    case WRONG_ONLY -> board.clearWrongPlayerCells();
                    case NONE -> {}
                }
                WorldExt.playSoundServerly(getWorld(), getGameCenter(), LGSounds.MS_BOMB_ACTIVATED, 0.75F, 1.0F);
                sendUpdatePacketToNearby(new SPSudokuWrongAnswer(board));
                if (attemptCount >= configSnapshot.getAttemptCount()) {
                    if (currentLevel > 1) {
                        triggerGameWin();
                    } else {
                        triggerGameLose();
                    }
                } else {
                    sendToNearby(new ChatComponentTranslation("msg.lootgames.sdk.wrong"), NotifyColor.FAIL);
                    sendToNearby(
                            new ChatComponentTranslation(
                                    "msg.lootgames.attempt_left",
                                    configSnapshot.getAttemptCount() - attemptCount),
                            NotifyColor.GRAVE_NOTIFY);
                }
                save();
            }
        } else {
            if (board.countFilledCells() < board.countTotalBlanks()) {
                sendToNearby(new ChatComponentTranslation("msg.lootgames.sdk.not_filled"), NotifyColor.WARN);
            } else {
                sendToNearby(new ChatComponentTranslation("msg.lootgames.sdk.check_end"), NotifyColor.NOTIFY);
                endGameCheckTick = now;
            }
        }
    }

    public void onLevelSuccessfullyFinished() {
        currentLevel++;
        if (currentLevel <= 4) {
            sendUpdatePacketToNearby(new SPSudokuSpawnLevelBeatParticles());
            sendToNearby(new ChatComponentTranslation("msg.lootgames.stage_complete"));
            WorldExt.playSoundServerly(getWorld(), getGameCenter(), Sounds.PLAYER_LEVELUP, 0.75F, 1.0F);
            int blanks = configSnapshot.getStageByIndex(currentLevel).blanksCount();
            board.generate(blanks);
            sendUpdatePacketToNearby(new SPSudokuLevelComplete(board));
            save();
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
                currentLevel - 1,
                LGConfigs.REWARDS.rewardsSudoku);
    }

    @Override
    public void writeNBT(NBTTagCompound nbt, SerializationType type) {
        super.writeNBT(nbt, type);
        nbt.setTag("board", board.writeNBT());
        nbt.setInteger("current_level", currentLevel);
        nbt.setInteger("attempt_count", attemptCount);
        nbt.setTag("config_snapshot", ConfigSudoku.ConfigSudokuSnapshot.serialize(configSnapshot));
    }

    @Override
    public void readNBT(NBTTagCompound nbt, SerializationType type) {
        super.readNBT(nbt, type);
        board.readNBT(nbt.getCompoundTag("board"));
        currentLevel = nbt.getInteger("current_level");
        attemptCount = nbt.getInteger("attempt_count");
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
                int blanks = configSnapshot.getStageByIndex(currentLevel).blanksCount();
                board.generate(blanks);
                sendUpdatePacketToNearby(new SPSSyncBoard(board));
                return;
            }
            if (player.isSneaking() && type == MouseClickType.RIGHT) {
                board.cycleValueMinus(pos);
            } else if (type == MouseClickType.RIGHT) {
                board.cycleValueAdd(pos);
            } else {
                return;
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
