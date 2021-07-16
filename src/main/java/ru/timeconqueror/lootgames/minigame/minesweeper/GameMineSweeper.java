package ru.timeconqueror.lootgames.minigame.minesweeper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import ru.timeconqueror.lootgames.api.minigame.BoardLootGame;
import ru.timeconqueror.lootgames.api.minigame.ILootGameFactory;
import ru.timeconqueror.lootgames.api.minigame.NotifyColor;
import ru.timeconqueror.lootgames.api.task.TaskCreateExplosion;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.api.util.RewardUtils;
import ru.timeconqueror.lootgames.common.config.ConfigMS.Snapshot;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.lootgames.common.packet.game.SPMSFieldChanged;
import ru.timeconqueror.lootgames.common.packet.game.SPMSGenBoard;
import ru.timeconqueror.lootgames.common.packet.game.SPMSResetFlags;
import ru.timeconqueror.lootgames.common.packet.game.SPMSSpawnLevelBeatParticles;
import ru.timeconqueror.lootgames.registry.LGAchievements;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.registry.LGSounds;
import ru.timeconqueror.lootgames.utils.MouseClickType;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.WorldExt;
import ru.timeconqueror.lootgames.utils.sanity.Sounds;
import ru.timeconqueror.timecore.api.common.tile.SerializationType;
import ru.timeconqueror.timecore.api.util.RandHelper;
import ru.timeconqueror.timecore.api.util.Wrapper;

import java.util.List;

import static ru.timeconqueror.timecore.api.util.NetworkUtils.getPlayersNearby;

//TODO lang keys for advancements
//TODO change generation random - depending on world is bad.
//TODO add leveling info in chat
//todo add game rules in chat
//TODO remove break particle before left click interact
//TODO if all non-bomb fields are revealed, finish the game
//TODO remove interact with opened fields
public class GameMineSweeper extends BoardLootGame<GameMineSweeper> {
    public static final String ADV_BEAT_LEVEL4 = "ms_level_4";

    public boolean cIsGenerated;

    private int currentLevel = 1;

    private final MSBoard board;

    private int ticks;
    private int attemptCount = 0;

    private boolean playRevealNeighboursSound = true;

    /**
     * Server-side only
     */
    private Snapshot configSnapshot = null;

    public GameMineSweeper() {
        board = new MSBoard(0, 0);
    }

    public static Pos2i convertToGamePos(BlockPos masterPos, BlockPos subordinatePos) {
        BlockPos temp = subordinatePos.subtract(masterPos);
        return new Pos2i(temp.getX(), temp.getZ());
    }

    // server only
    public void setConfigSnapshot(Snapshot configSnapshot) {
        this.configSnapshot = configSnapshot;
    }

    @Override
    public void onPlace() {
        setupInitialStage(new StageWaiting());

        if (isServerSide()) {
            configSnapshot = LGConfigs.MINESWEEPER.snapshot();
            board.setSize(configSnapshot.getStage1().getBoardSize());
            board.setBombCount(configSnapshot.getStage1().getBombCount());
        }

        super.onPlace();
    }

    public boolean isBoardGenerated() {
        return board.isGenerated();
    }

    public int getCurrentBoardSize() {
        return board.size();
    }

    @Override
    public int getAllocatedBoardSize() {
        return configSnapshot.getStage4().getBoardSize();
    }

    private void onLevelSuccessfullyFinished() {
        if (currentLevel < 4) {
            sendUpdatePacketToNearby(new SPMSSpawnLevelBeatParticles());

            sendToNearby(new ChatComponentTranslation("msg.lootgames.stage_complete"), NotifyColor.SUCCESS);
            WorldExt.playSoundServerly(getWorld(), getGameCenter(), Sounds.PLAYER_LEVELUP, 0.75F, 1.0F);

            Snapshot.StageSnapshot stageSnapshot = configSnapshot.getStageByIndex(currentLevel + 1);

            board.resetBoard(stageSnapshot.getBoardSize(), stageSnapshot.getBombCount());
            currentLevel++;

            saveAndSync();
        } else {
            currentLevel++;
            triggerGameWin();
        }
    }


    @Override
    protected void triggerGameWin() {
        super.triggerGameWin();

        List<EntityPlayerMP> players = getPlayersNearby(getGameCenter(), getBroadcastDistance());

        genLootChests(players);
    }

    private void genLootChests(List<EntityPlayerMP> players) {
        if (currentLevel < 2) {
            throw new RuntimeException("GenLootChests method was called in an appropriate time!");
        }

        BlockPos central = getGameCenter();

        if (currentLevel > 4) { // end of the game
            players.forEach(LGAchievements.MS_BEAT_LEVEL4::trigger);
        }

        RewardUtils.spawnFourStagedReward(((WorldServer) getWorld()), this, central, currentLevel - 1, LGConfigs.REWARDS.rewardsMinesweeper);
    }

    @Override
    protected void triggerGameLose() {
        super.triggerGameLose();

        BlockPos expPos = getGameCenter();
        WorldExt.explode(getWorld(), null, expPos.getX(), expPos.getY() + 1.5, expPos.getZ(), 9, true);
    }

    @Override
    protected void onStageUpdate(BoardStage oldStage, BoardStage newStage, boolean shouldDelayPacketSending) {
        ticks = 0;
        super.onStageUpdate(oldStage, newStage, shouldDelayPacketSending);
    }

    public MSBoard getBoard() {
        return board;
    }

    public int getTicks() {
        return ticks;
    }

    @Override
    public void writeNBT(NBTTagCompound nbt, SerializationType type) {
        super.writeNBT(nbt, type);

        if (type == SerializationType.SAVE) {
            if (isBoardGenerated()) {
                NBTTagCompound boardTag = board.writeNBTForSaving();
                nbt.setTag("board", boardTag);
            }

            nbt.setInteger("attempt_count", attemptCount);
        } else {
            nbt.setBoolean("is_generated", isBoardGenerated());
            if (isBoardGenerated()) {
                NBTTagCompound boardTag = board.writeNBTForClient();
                nbt.setTag("board", boardTag);
            }
        }

        nbt.setInteger("bomb_count", board.getBombCount());
        nbt.setInteger("board_size", board.size());
        nbt.setInteger("ticks", ticks);
        nbt.setInteger("current_level", currentLevel);
        nbt.setTag("config_snapshot", Snapshot.serialize(configSnapshot));
    }

    @Override
    public void readNBT(NBTTagCompound nbt, SerializationType type) {
        super.readNBT(nbt, type);

        if (type == SerializationType.SAVE) {
            if (nbt.hasKey("board")) {
                NBTTagCompound boardTag = nbt.getCompoundTag("board");
                board.readNBTFromSave(boardTag);
            }

            attemptCount = nbt.getInteger("attempt_count");
        } else {
            cIsGenerated = nbt.getBoolean("is_generated");

            if (nbt.hasKey("board")) {
                NBTTagCompound boardTag = nbt.getCompoundTag("board");
                board.readNBTFromClient(boardTag);
            }
        }

        board.setBombCount(nbt.getInteger("bomb_count"));
        board.setSize(nbt.getInteger("board_size"));
        ticks = nbt.getInteger("ticks");
        currentLevel = nbt.getInteger("current_level");
        configSnapshot = Snapshot.deserialize(nbt.getCompoundTag("config_snapshot"));
    }

    @Override
    public BoardStage createStageFromNBT(String id, NBTTagCompound stageNBT, SerializationType serializationType) {
        switch (id) {
            case StageWaiting.ID:
                return new StageWaiting();
            case StageDetonating.ID:
                return new StageDetonating(stageNBT.getInteger("detonation_time"));
            case StageExploding.ID:
                return new StageExploding();
            default:
                throw new IllegalArgumentException("Unknown state with id: " + id + "!");
        }
    }

    public static class Factory implements ILootGameFactory {
        @Override
        public void genOnPuzzleMasterClick(World world, BlockPos puzzleMasterPos) {
            BlockPos floorCenterPos = puzzleMasterPos.offset(0, -3/*instead of GameDungeonStructure.MASTER_BLOCK_OFFSET*/ + 1, 0);
            WorldExt.setBlock(world, floorCenterPos, LGBlocks.MS_ACTIVATOR);
        }
    }

    public class StageWaiting extends BoardStage {
        private static final String ID = "waiting";

        public StageWaiting() {
        }

        @Override
        protected void onClick(EntityPlayer player, Pos2i pos, MouseClickType type) {
            if (isServerSide()) {
                EntityPlayerMP sPlayer = (EntityPlayerMP) player;
                WorldExt.playSoundServerly(getWorld(), convertToBlockPos(pos), Sounds.NOTE_BLOCK_HARP, 0.6F, 0.8F);

                if (!board.isGenerated()) {
                    generateBoard(sPlayer, pos);
                } else {
                    playRevealNeighboursSound = true;

                    if (type == MouseClickType.LEFT) {
                        if (board.getMark(pos) == Mark.NO_MARK) {
                            revealField(sPlayer, pos);
                        }
                    } else {
                        if (player.isSneaking()) {
                            revealAllNeighbours(sPlayer, pos, false);
                        } else if (board.isHidden(pos)) {
                            swapFieldMark(pos);
                        }
                    }
                }
            }
        }

        public void generateBoard(EntityPlayerMP player, Pos2i clickedPos) {
            board.generate(clickedPos);
            sendUpdatePacketToNearby(new SPMSGenBoard(GameMineSweeper.this));
            revealField(player, clickedPos);

            save();
        }

        public void revealField(EntityPlayerMP player, Pos2i pos) {
            if (board.isHidden(pos)) {
                board.reveal(pos);

                Type type = board.getType(pos);

                sendUpdatePacketToNearby(new SPMSFieldChanged(pos, board.getField(pos)));

                if (type == Type.EMPTY) {
                    if (playRevealNeighboursSound) {
                        WorldExt.playSoundServerly(getWorld(), convertToBlockPos(pos), LGSounds.MS_ON_EMPTY_REVEAL_NEIGHBOURS, 0.6F, 1.0F);
                        playRevealNeighboursSound = false;
                    }

                    revealAllNeighbours(player, pos, true);
                } else if (type == Type.BOMB) {
                    triggerBombs(pos);
                }

                save();

                if (board.checkWin()) {
                    onLevelSuccessfullyFinished();
                }
            }
        }

        private void revealAllNeighbours(EntityPlayerMP player, Pos2i mainPos, boolean revealMarked) {
            if (!revealMarked) {
                if (board.isHidden(mainPos)) {
                    sendTo(player, new ChatComponentTranslation("msg.lootgames.ms.reveal_on_hidden"), NotifyColor.WARN);
                    return;
                }

                int bombsAround = board.getType(mainPos).getId();
                if (bombsAround < 1) throw new IllegalStateException();

                int marked = 0;
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        Pos2i pos = mainPos.add(x, y);
                        if (board.hasFieldOn(pos) && board.getMark(pos) == Mark.FLAG) {
                            marked++;
                        }
                    }
                }

                if (marked != bombsAround) {
                    sendTo(player, new ChatComponentTranslation("msg.lootgames.ms.reveal_invalid_mark_count"), NotifyColor.WARN);
                    return;
                }
            }

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == 0 && y == 0) {
                        continue;
                    }

                    Pos2i pos = mainPos.add(x, y);
                    if (board.hasFieldOn(pos) && board.isHidden(pos)) {
                        if (revealMarked || board.getMark(pos) != Mark.FLAG) {
                            revealField(player, pos);
                        }
                    }
                }
            }
        }

        public void swapFieldMark(Pos2i pos) {
            if (board.isHidden(pos)) {
                board.swapMark(pos);

                sendUpdatePacketToNearby(new SPMSFieldChanged(pos, board.getField(pos)));
                save();
            }

            if (board.checkWin()) {
                onLevelSuccessfullyFinished();
            }

        }

        private void triggerBombs(Pos2i pos) {
            WorldExt.playSoundServerly(getWorld(), convertToBlockPos(pos), LGSounds.MS_BOMB_ACTIVATED, 0.6F, 1.0F);

            switchStage(new StageDetonating());

            board.forEach((x, y) -> {
                if (board.isBomb(x, y)) {
                    board.reveal(x, y);
                }
            });

            sendToNearby(new ChatComponentTranslation("msg.lootgames.ms.bomb_touched"), NotifyColor.FAIL);

            saveAndSync();

            attemptCount++;
        }

        @Override
        public String getID() {
            return ID;
        }
    }

    public class StageDetonating extends BoardStage {
        private static final String ID = "detonating";
        private final int detonationTicks;

        public StageDetonating() {
            this(LGConfigs.MINESWEEPER.detonationTime);
        }

        public StageDetonating(int detonationTicks) {
            this.detonationTicks = detonationTicks;
        }

        @Override
        public void onTick() {
            if (isServerSide()) {
                if (ticks >= detonationTicks) {
                    if (attemptCount < LGConfigs.MINESWEEPER.attemptCount) {
                        switchStage(new StageExploding());
                    } else {
                        if (currentLevel > 1) {
                            triggerGameWin();
                        } else {
                            triggerGameLose();
                        }
                    }
                }
            }

            ticks++;
        }

        public int getDetonationTicks() {
            return detonationTicks;
        }

        @Override
        public String getID() {
            return ID;
        }

        @Override
        public NBTTagCompound serialize(SerializationType serializationType) {
            NBTTagCompound nbt = super.serialize(serializationType);
            nbt.setInteger("detonation_time", detonationTicks);
            return nbt;
        }
    }

    public class StageExploding extends BoardStage {
        private static final String ID = "exploding";

        public StageExploding() {
        }

        @Override
        public void postInit() {
            int longestDetTime = detonateBoard(currentLevel + 3, false);
            ticks = longestDetTime + 20; //number represents some pause after detonating
        }

        @Override
        public void onTick() {
            if (isServerSide()) {
                ticks--;

                if (ticks <= 0) {
                    sendToNearby(new ChatComponentTranslation("msg.lootgames.ms.new_attempt"), NotifyColor.NOTIFY);
                    sendToNearby(new ChatComponentTranslation("msg.lootgames.attempt_left", LGConfigs.MINESWEEPER.attemptCount - attemptCount), NotifyColor.GRAVE_NOTIFY);

                    switchStage(new StageWaiting());

                    board.resetBoard();

                    sendUpdatePacketToNearby(new SPMSResetFlags());

                    saveAndSync();
                }
            }
        }

        /**
         * Adds detonating tasks to scheduler.
         * Returns the longest detonating time, after which all bombs will explode
         */
        private int detonateBoard(int strength, boolean affectBlocks) {
            Wrapper<Integer> longestDetTime = new Wrapper<>(0);

            board.forEach(pos2i -> {
                if (board.getType(pos2i) == Type.BOMB) {

                    int detTime = RandHelper.RAND.nextInt(45);

                    if (longestDetTime.get() < detTime) {
                        longestDetTime.set(detTime);
                    }

                    taskScheduler.addTask(new TaskCreateExplosion(convertToBlockPos(pos2i), strength, affectBlocks), detTime);
                }
            });

            return longestDetTime.get();
        }

        @Override
        public String getID() {
            return ID;
        }
    }
}

