package ru.timeconqueror.lootgames.minigame.gol;

import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.NoteBlockEvent;
import ru.timeconqueror.lootgames.api.Marker;
import ru.timeconqueror.lootgames.api.minigame.BoardLootGame;
import ru.timeconqueror.lootgames.api.minigame.ILootGameFactory;
import ru.timeconqueror.lootgames.api.minigame.NotifyColor;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.api.util.RewardUtils;
import ru.timeconqueror.lootgames.common.config.ConfigGOL;
import ru.timeconqueror.lootgames.common.config.ConfigGOL.Fail;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.lootgames.common.packet.game.CPGOLSymbolsShown;
import ru.timeconqueror.lootgames.common.packet.game.SPGOLDrawMark;
import ru.timeconqueror.lootgames.common.packet.game.SPGOLSendDisplayedSymbol;
import ru.timeconqueror.lootgames.common.packet.game.SPGOLSpawnStageUpParticles;
import ru.timeconqueror.lootgames.registry.LGAchievements;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.registry.LGSounds;
import ru.timeconqueror.lootgames.utils.MouseClickType;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.WorldExt;
import ru.timeconqueror.lootgames.utils.sanity.Particles;
import ru.timeconqueror.lootgames.utils.sanity.Sounds;
import ru.timeconqueror.timecore.api.common.tile.SerializationType;
import ru.timeconqueror.timecore.api.util.RandHelper;
import ru.timeconqueror.timecore.api.util.client.ClientProxy;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

//TODO add question mark flicker on WaitingStartStage
//TODO add reward giving upon some combination
public class GameOfLight extends BoardLootGame<GameOfLight> {
    private static final Marker DEBUG_MARKER = Marker.GAME_OF_LIGHT;

    public static final int BOARD_SIZE = 3;

    private int round = 0;
    private int stage = 0;
    private int ticks = 0;
    private int attempt = 1;
    private int maxReachedStage = 0;

    private Timer resetTimer;

    private final List<DisplayedSymbol> displayedSymbols = new ArrayList<>();

    @Nullable
    private QMarkAppearance specialMarkAppearance = null;

    public GameOfLight() {
        resetTimer = new Timer(0);
        resetTimer();
    }

    @Override
    public void onPlace() {
        if (isServerSide()) {
            setupInitialStage(new StageUnderExpanding());
        }
    }

    private void resetTimer() {
        resetTimer.reset(LGConfigs.GOL.timeout * 20);
    }

    @Override
    public void onTick() {
        super.onTick();

        if (isServerSide()) {
            if (resetTimer.isActive() && resetTimer.ended()) {
                DEBUG_LOG.debug(DEBUG_MARKER, "Time is out! Failing the current run...");
                failGame(true);
                return;
                //TODO add animation of hard switch to waiting for sequence?
            }

            if (!resetTimer.isActive()) {
                resetTimer();
            }

            resetTimer.update();
        } else {
            displayedSymbols.removeIf((symbol) -> System.currentTimeMillis() - symbol.getClickedTime() > 600);

            if (specialMarkAppearance != null && specialMarkAppearance.isFinished()) {
                specialMarkAppearance = null;
            }
        }
    }

    private void failGame(boolean dueTimeout) {
        WorldExt.playSoundServerly(getWorld(), getGameCenter(), LGSounds.GOL_SEQUENCE_WRONG, dueTimeout ? 0.2F : 0.75F, 1.0F);
        sendToNearby(new ChatComponentTranslation("msg.lootgames.gol.wrong_block"), NotifyColor.FAIL);
        sendUpdatePacketToNearby(SPGOLDrawMark.denied());

        DEBUG_LOG.debug(DEBUG_MARKER, "The run was failed! Current attempt: {} / {}", attempt, LGConfigs.GOL.attemptCount);

        if (attempt >= LGConfigs.GOL.attemptCount) {
            DEBUG_LOG.debug(DEBUG_MARKER, "Attempts are over! Forcing switch to game end...");
            if (maxReachedStage == 0) {
                triggerGameLose();
            } else {
                triggerGameWin();
            }
        } else {
            attempt++;

            WorldExt.playSoundServerly(getWorld(), getGameCenter(), LGSounds.GOL_START_GAME, dueTimeout ? 0.2F : 0.75F, 1.0F);
            switchStage(new StageWaitingStart());
        }
    }

    private void playFeedbackSound(EntityPlayer player, Symbol symbol) {
        NoteBlockEvent.Note note = NoteBlockEvent.Note.G_SHARP;
        NoteBlockEvent.Octave octave = NoteBlockEvent.Octave.LOW;
        switch (symbol) {
            case NORTH_WEST:
                note = NoteBlockEvent.Note.G;
                break;
            case NORTH:
                note = NoteBlockEvent.Note.A;
                break;
            case NORTH_EAST:
                note = NoteBlockEvent.Note.B;
                break;
            case EAST:
                note = NoteBlockEvent.Note.C;
                break;
            case SOUTH_EAST:
                note = NoteBlockEvent.Note.D;
                break;
            case SOUTH:
                note = NoteBlockEvent.Note.E;
                break;
            case SOUTH_WEST:
                note = NoteBlockEvent.Note.F;
                break;
            case WEST:
                note = NoteBlockEvent.Note.G;
                octave = NoteBlockEvent.Octave.MID;
                break;
        }

        WorldExt.playSoundCliently(getWorld(), getGameCenter(), Sounds.NOTE_BLOCK_HARP, 3.0F, getPitchForNote(note, octave), false);
    }

    private float getPitchForNote(NoteBlockEvent.Note note, NoteBlockEvent.Octave octave) {
        int noteID = note.ordinal() + octave.ordinal() * 12;
        return (float) Math.pow(2.0D, (double) (noteID - 12) / 12.0D);
    }

    @Override
    protected void triggerGameLose() {
        super.triggerGameLose();

        World world = getWorld();

        EnumSet<Fail> fails = LGConfigs.GOL.getAllowedFails();
        if (fails.isEmpty()) return;

        Fail fail = RandHelper.chooseEqually(fails);

        BlockPos center = getGameCenter();
        if (fail == Fail.EXPLOSION) {
            WorldExt.explode(world, null, center.getX(), center.getY() + 1.5, center.getZ(), 9, true);
        } else if (fail == Fail.ZOMBIES) {
            for (int i = 0; i < 10; i++) {
                EntityZombie zombie = new EntityZombie(world);
                zombie.setLocationAndAngles(center.getX() + RandHelper.RAND.nextFloat() * 2, center.getY() + 1, center.getZ() + RandHelper.RAND.nextFloat() * 2, MathHelper.wrapAngleTo180_float(RandHelper.RAND.nextFloat() * 360.0F), 0);
                world.spawnEntityInWorld(zombie);

                if (RandHelper.RAND.nextBoolean()) {
                    zombie.playLivingSound();
                }
            }
        } else if (fail == Fail.LAVA) {
            BlockPos.Mutable mutable = center.mutable();
            for (int x = -5; x <= 5; x++)
                for (int z = -5; z <= 5; z++)
                    for (int y = 1; y < 3; y++) {
                        mutable.set(center.getX(), center.getY(), center.getZ());

                        Block block = WorldExt.getBlock(world, mutable.move(x, y, z));
                        if (block.getMaterial().isReplaceable()) {
                            WorldExt.setBlock(world, center.offset(x, y, z), Blocks.lava);
                        }
                    }
        } else {
            throw new IllegalArgumentException("Unknown fail type: " + fail);
        }
    }

    @Override
    protected void triggerGameWin() {
        super.triggerGameWin();

        forEachPlayerNearby(player -> {
            sendTo(player, new ChatComponentTranslation("msg.lootgames.gol.reward_level_info", stage, maxReachedStage), NotifyColor.SUCCESS);

            if (maxReachedStage >= 3) {
                LGAchievements.GOL_MASTER_LEVEL3.trigger(player);
            }

            if (maxReachedStage == 4) {
                LGAchievements.GOL_MASTER_LEVEL4.trigger(player);
            }
        });

        RewardUtils.spawnFourStagedReward(((WorldServer) getWorld()), this, getGameCenter(), maxReachedStage, LGConfigs.REWARDS.rewardsGol);
    }

    @Override
    public int getCurrentBoardSize() {
        return getAllocatedBoardSize();
    }

    @Override
    public int getAllocatedBoardSize() {
        return BOARD_SIZE;
    }

    @Override
    public @Nullable
    BoardStage createStageFromNBT(String id, NBTTagCompound stageNBT, SerializationType serializationType) {
        switch (id) {
            case StageUnderExpanding.ID:
                return new StageUnderExpanding(stageNBT.getInteger("ticks"));
            case StageWaitingStart.ID:
                return new StageWaitingStart();
            case StageShowSequence.ID:
                return serializationType != SerializationType.SAVE
                        ? new StageShowSequence(stageNBT)
                        : new StageWaitingStart(); /*resetting if world was reloaded while game was in show mode*/
            case StageWaitingForSequence.ID:
                return serializationType == SerializationType.SAVE
                        ? new StageWaitingForSequence(stageNBT)
                        : new StageWaitingForSequence(Collections.emptyList());
            default:
                throw new IllegalArgumentException("Unknown state with id: " + id + "!");
        }
    }

    @Override
    public void writeNBT(NBTTagCompound nbt, SerializationType type) {
        super.writeNBT(nbt, type);

        if (type == SerializationType.SAVE) {
            nbt.setInteger("round", round);
            nbt.setInteger("level", stage);
            nbt.setInteger("attempt", attempt);

            nbt.setTag("reset_timer", Timer.toNBT(resetTimer));
        }

        nbt.setInteger("ticks", ticks);
    }

    @Override
    public void readNBT(NBTTagCompound nbt, SerializationType type) {
        super.readNBT(nbt, type);

        if (type == SerializationType.SAVE) {
            round = nbt.getInteger("round");
            stage = nbt.getInteger("level");
            attempt = nbt.getInteger("attempt");
            resetTimer = Timer.fromNBT(nbt.getTag("reset_timer"));
        }

        ticks = nbt.getInteger("ticks");
    }

    private boolean isCenter(Pos2i pos) {
        return !Symbol.exists(pos);
    }

    @Override
    protected void onStageUpdate(BoardStage oldStage, BoardStage newStage, boolean shouldDelayPacketSending) {
        ticks = 0;
        resetTimer.disable();
        super.onStageUpdate(oldStage, newStage, shouldDelayPacketSending);
    }

    public void spawnFeedbackParticles(String particleName, BlockPos pos) {
        if (isClientSide()) {
            for (int i = 0; i < 20; i++) {
                getWorld().spawnParticle(particleName, pos.getX() + RandHelper.RAND.nextFloat(), pos.getY() + 1F + RandHelper.RAND.nextFloat() / 2, pos.getZ() + RandHelper.RAND.nextFloat(), RandHelper.RAND.nextGaussian() * 0.02D, (0.02D + RandHelper.RAND.nextGaussian()) * 0.02D, RandHelper.RAND.nextGaussian() * 0.02D);
            }
        }
    }

    public class StageUnderExpanding extends BoardStage {
        private static final String ID = "under_expanding";
        public static final int MAX_TICKS_EXPANDING = 20;
        private int ticks;

        public StageUnderExpanding() {
            this(0);
        }

        public StageUnderExpanding(int ticks) {
            this.ticks = ticks;
        }

        @Override
        public String getID() {
            return ID;
        }

        @Override
        protected void onTick() {
            if (ticks > MAX_TICKS_EXPANDING) {
                if (isServerSide()) {
                    switchStage(new StageWaitingStart());
                }
            } else {
                ticks++;
            }
        }

        public int getTicks() {
            return ticks;
        }

        @Override
        public NBTTagCompound serialize(SerializationType serializationType) {
            NBTTagCompound nbt = super.serialize(serializationType);
            nbt.setInteger("ticks", ticks);
            return nbt;
        }
    }

    public class StageWaitingStart extends BoardStage {
        private static final String ID = "waiting_start";

        @Override
        public void preInit() {
            super.preInit();
            stage = 0;
            round = 0;
        }

        @Override
        public String getID() {
            return ID;
        }

        @Override
        protected void onClick(EntityPlayer player, Pos2i pos, MouseClickType type) {
            super.onClick(player, pos, type);

            if (isServerSide()) {
                if (isCenter(pos)) {
                    sendTo(player, new ChatComponentTranslation("msg.lootgames.gol.rules"), NotifyColor.NOTIFY);

                    switchStage(new StageShowSequence(true, new ArrayList<>()));
                } else {
                    sendTo(player, new ChatComponentTranslation("msg.lootgames.gol.click_center"), NotifyColor.WARN);
                }
            }
        }
    }

    public class StageShowSequence extends BoardStage {
        private static final int TICKS_PAUSE_BETWEEN_SYMBOLS = 12;
        private static final int TICKS_PAUSE_BETWEEN_ROUNDS = 25;
        private static final int FEEDBACK_WAIT_TICKS = 8 * 20;
        private static final String ID = "show_sequence";
        private final List<Symbol> sequence;
        private final int displayTime;

        private boolean pauseBeforeShowing = true;
        private int symbolIndex = 0;

        private boolean feedbackPacketReceived = false;
        private boolean cFeedbackPacketSent = false;

        /**
         * Timer in case client can't or don't want to send packet about finishing watching sequence
         */
        private final Timer feedbackWaitTimer = new Timer(FEEDBACK_WAIT_TICKS);

        public StageShowSequence(NBTTagCompound nbt) {
            sequence = deserializeSequence(nbt.getIntArray("sequence"));
            displayTime = nbt.getInteger("display_time");
        }

        public StageShowSequence(boolean isNewStage, List<Symbol> prevSequence) {
            int stageIndex = stage;
            ConfigGOL.StageConfig stage = LGConfigs.GOL.getStageByIndex(stageIndex);
            if (isNewStage) {
                if (stageIndex == 0) {
                    sequence = generateSequence(stageIndex, LGConfigs.GOL.startDigitAmount);
                } else if (stage.randomizeSequence) {
                    sequence = generateSequence(stageIndex, prevSequence.size() + 1);
                } else {
                    sequence = withNewSymbol(stageIndex, prevSequence);
                }
            } else {
                sequence = withNewSymbol(stageIndex, prevSequence);
            }

            displayTime = stage.displayTime;
        }

        @Override
        protected void onTick() {
            if (pauseBeforeShowing) {
                if (ticks > TICKS_PAUSE_BETWEEN_ROUNDS) {
                    ticks = 0;
                    pauseBeforeShowing = false;
                } else {
                    if (ticks == Math.max(20, TICKS_PAUSE_BETWEEN_ROUNDS - 5)) {
                        displayedSymbols.clear(); // hard reset, because now we will show new symbols
                    }

                    ticks++;
                }
            } else {
                // makes sound and spawn particles before the first symbol. Others will be handled in the next if-statement
                boolean showParticles = symbolIndex == 0 && ticks == 0;

                // if it's a last symbol we don't need to wait for pause.
                if ((symbolIndex == sequence.size() - 1 && ticks > displayTime) || (ticks > displayTime + TICKS_PAUSE_BETWEEN_SYMBOLS)) {
                    ticks = 0;
                    symbolIndex++;
                    showParticles = true;
                } else {
                    ticks++;
                }

                if (isClientSide()) {
                    if (isShowingSymbols() && showParticles) {
                        Symbol symbol = sequence.get(symbolIndex);
                        playFeedbackSound(ClientProxy.player(), symbol);

                        BlockPos pos = convertToBlockPos(symbol.getPos());
                        spawnFeedbackParticles(Particles.SPELL, pos);
                    }

                    if (!isShowingSymbols() && !cFeedbackPacketSent) {
                        sendFeedbackPacket(new CPGOLSymbolsShown());
                        cFeedbackPacketSent = true;
                    }
                } else {
                    if (feedbackPacketReceived) {
                        // at least one watched the full sequence, switching...
                        switchStage(new StageWaitingForSequence(sequence));
                        return;
                    }

                    if (!isShowingSymbols()) {
                        //hard switch to waiting-for-sequence stage if no one want to send a reply
                        if (feedbackWaitTimer.ended()) {
                            switchStage(new StageWaitingForSequence(sequence));
                            return;
                        }

                        feedbackWaitTimer.update();
                    }
                }
            }
        }

        public void onSequenceShown() {
            this.feedbackPacketReceived = true;
        }

        @Override
        protected void onClick(EntityPlayer player, Pos2i pos, MouseClickType type) {
            super.onClick(player, pos, type);

            if (isServerSide()) {
                sendTo(player, new ChatComponentTranslation("msg.lootgames.gol.not_ready"), NotifyColor.WARN);
            }
        }

        private boolean isShowingSymbols() {
            return !pauseBeforeShowing && symbolIndex < sequence.size();
        }

        public boolean shouldRenderSymbol() {
            return ticks < displayTime && isShowingSymbols();
        }

        @Nullable
        public Symbol getSymbolForRender() {
            return shouldRenderSymbol() ? sequence.get(symbolIndex) : null;
        }

        private List<Symbol> generateSequence(int stage, int size) {
            List<Symbol> sequence = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                withNewSymbol(stage, sequence);
            }

            return sequence;
        }

        private List<Symbol> withNewSymbol(int stage, List<Symbol> sequence) {
            if (stage >= LGConfigs.GOL.expandFieldAtStage - 1) {
                sequence.add(RandHelper.chooseEqually(Symbol.values()));
            } else {
                sequence.add(RandHelper.chooseEqually(Symbol.NWES_SYMBOLS));
            }

            return sequence;
        }

        @Override
        public String getID() {
            return ID;
        }

        @Override
        public NBTTagCompound serialize(SerializationType serializationType) {
            NBTTagCompound nbt = super.serialize(serializationType);
            nbt.setIntArray("sequence", serializeSequence(sequence));
            nbt.setInteger("display_time", displayTime);

            return nbt;
        }
    }

    public class StageWaitingForSequence extends BoardStage {
        private static final String ID = "waiting_for_sequence";

        private final List<Symbol> sequence;
        private int currentSymbol;

        public StageWaitingForSequence(List<Symbol> sequence) {
            this.sequence = sequence;
        }

        public StageWaitingForSequence(NBTTagCompound nbt) {
            this.sequence = deserializeSequence(nbt.getIntArray("sequence"));
            this.currentSymbol = nbt.getInteger("symbol_index");
        }

        @Override
        protected void onStart(boolean clientSide) {
            resetTimer.enable();
        }

        @Override
        protected void onClick(EntityPlayer player, Pos2i pos, MouseClickType type) {
            if (isClientSide() && isCenter(pos)) {
                sendTo(player, new ChatComponentTranslation("msg.lootgames.gol.rules"), NotifyColor.WARN);
                return;
            }

            if (!isCenter(pos)) {
                Symbol chosen = Symbol.byPos(pos);

                DEBUG_LOG.debug(DEBUG_MARKER, "{}: Chosen symbol: {} ({} ns, {} ms)", player.getDisplayName(), chosen, System.nanoTime(), System.currentTimeMillis());

                playFeedbackSound(player, chosen);
                if (isClientSide()) {
                    addDisplayedSymbol(chosen);
                } else {
                    sendUpdatePacketToNearbyExcept(((EntityPlayerMP) player), new SPGOLSendDisplayedSymbol(chosen));

                    Symbol correct = sequence.get(currentSymbol);
                    if (chosen != correct) {
                        DEBUG_LOG.debug(DEBUG_MARKER, "{}: Symbol {} was denied. The correct one is {}", player.getDisplayName(), chosen, correct);
                        failGame(false);
                        return;
                    }

                    DEBUG_LOG.debug(DEBUG_MARKER, "{}: Symbol {} was accepted.", player.getDisplayName(), chosen);
                    resetTimer();

                    if (currentSymbol == sequence.size() - 1) {
                        onSuccessSequence((EntityPlayerMP) player);
                    } else {
                        currentSymbol++;
                    }
                }
            }
        }

        private void onSuccessSequence(EntityPlayerMP player) {
            WorldExt.playSoundServerly(getWorld(), getGameCenter(), LGSounds.GOL_SEQUENCE_COMPLETE, 0.75F, 1.0F);
            sendUpdatePacketToNearby(SPGOLDrawMark.accepted());

            ConfigGOL.StageConfig stageCfg = LGConfigs.GOL.getStageByIndex(stage);
            int maxRounds = stageCfg.rounds;
            if (round == maxRounds - 1) {//move to the next stage
                if (stage == 3) {
                    stage++;
                    maxReachedStage = 4;
                    save();
                    triggerGameWin();
                } else {
                    stage++;
                    round = 0;
                    maxReachedStage = Math.max(maxReachedStage, stage);
                    save();

                    WorldExt.playSoundServerly(getWorld(), getGameCenter(), Sounds.PLAYER_LEVELUP, 0.75F, 1.0F);
                    sendUpdatePacketToNearby(new SPGOLSpawnStageUpParticles());
                    sendToNearby(new ChatComponentTranslation("msg.lootgames.stage_complete"), NotifyColor.SUCCESS);

                    switchStage(new StageShowSequence(true, sequence));
                }
            } else {
                round++;
                save();

                switchStage(new StageShowSequence(false, sequence));
            }
        }

        @Override
        public NBTTagCompound serialize(SerializationType serializationType) {
            NBTTagCompound nbt = super.serialize(serializationType);

            if (serializationType == SerializationType.SAVE) {
                nbt.setIntArray("sequence", serializeSequence(sequence));
                nbt.setInteger("symbol_index", currentSymbol);
            }

            return nbt;
        }

        @Override
        public String getID() {
            return ID;
        }

    }

    public void addDisplayedSymbol(Symbol symbol) {
        if (isClientSide()) {
            displayedSymbols.add(new DisplayedSymbol(System.currentTimeMillis(), symbol));
        }
    }

    public void addMarkAppearance(QMarkAppearance.State state) {
        if (isClientSide() && QMarkAppearance.canBeHandled(state)) {
            specialMarkAppearance = new QMarkAppearance(state, getWorld().getTotalWorldTime());
        }
    }

    public List<DisplayedSymbol> getDisplayedSymbols() {
        return displayedSymbols;
    }

    public QMarkAppearance.State getMarkState() {
        return specialMarkAppearance != null ? specialMarkAppearance.getState() : getStage() instanceof StageShowSequence ? QMarkAppearance.State.SHOWING : QMarkAppearance.State.NONE;
    }

    private static List<Symbol> deserializeSequence(int[] data) {
        return Arrays.stream(data).mapToObj(Symbol::byIndex).collect(Collectors.toList());
    }

    private static int[] serializeSequence(List<Symbol> sequence) {
        return sequence.stream().mapToInt(Symbol::getIndex).toArray();
    }

    public static class Factory implements ILootGameFactory {
        @Override
        public void genOnPuzzleMasterClick(World world, BlockPos puzzleMasterPos) {
            BlockPos floorCenterPos = puzzleMasterPos.offset(0, -2, 0);//TODo make puzzle master be also manually placeable and in this case in should spawn activator in its place
            WorldExt.setBlock(world, floorCenterPos, LGBlocks.GOL_ACTIVATOR);
        }
    }
}
