package ru.timeconqueror.lootgames.api.minigame;

import eu.usrv.legacylootgames.StructureGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import ru.timeconqueror.lootgames.api.Markers;
import ru.timeconqueror.lootgames.api.block.tile.GameMasterTile;
import ru.timeconqueror.lootgames.api.packet.IClientGamePacket;
import ru.timeconqueror.lootgames.api.packet.IServerGamePacket;
import ru.timeconqueror.lootgames.api.task.TETaskScheduler;
import ru.timeconqueror.lootgames.api.util.Auxiliary;
import ru.timeconqueror.lootgames.api.util.LootGameCleaner;
import ru.timeconqueror.lootgames.common.packet.CPacketGameUpdate;
import ru.timeconqueror.lootgames.common.packet.LGNetwork;
import ru.timeconqueror.lootgames.common.packet.SPacketGameUpdate;
import ru.timeconqueror.lootgames.common.packet.game.SPChangeStage;
import ru.timeconqueror.lootgames.common.packet.game.SPDelayedChangeStage;
import ru.timeconqueror.lootgames.registry.LGAchievements;
import ru.timeconqueror.lootgames.registry.LGSounds;
import ru.timeconqueror.lootgames.utils.Trackers;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.ChatComponentExt;
import ru.timeconqueror.lootgames.utils.future.LoggerExt;
import ru.timeconqueror.lootgames.utils.future.WorldExt;
import ru.timeconqueror.timecore.api.common.tile.SerializationType;
import ru.timeconqueror.timecore.api.util.NetworkUtils;
import ru.timeconqueror.timecore.api.util.Pair;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class LootGame<STAGE extends LootGame.Stage, G extends LootGame<STAGE, G>> {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Marker DEBUG_MARKER = Markers.LOOTGAME.getMarker();

    protected GameMasterTile<G> masterTileEntity;
    protected TETaskScheduler taskScheduler;

    /**
     * Determines if tile entity is placed, but was never read from nbt.
     */
    private boolean justPlaced = true;
    @Nullable
    private Pair<Stage, Stage> pendingStageUpdate = null;
    private STAGE stage;

    public void setMasterTileEntity(GameMasterTile<G> masterTileEntity) {
        this.masterTileEntity = masterTileEntity;
    }

    /**
     * Method where you can init anything, that requires world and {@link #masterTileEntity} to be not null.
     */
    @OverridingMethodsMustInvokeSuper
    public void onLoad() {
        if (taskScheduler == null && isServerSide()) {
            taskScheduler = new TETaskScheduler(masterTileEntity);
        }

        if (justPlaced) {
            justPlaced = false;
            onPlace();
        }
    }

    /**
     * Called for both sides when tile entity is just placed in world and has never been write to and read from nbt.
     */
    public void onPlace() {
    }

    @OverridingMethodsMustInvokeSuper
    public void onTick() {
        if (isServerSide()) {
            taskScheduler.onUpdate();

            if (pendingStageUpdate != null) {
                if (pendingStageUpdate.right() == getStage()) { //if it's still the same
                    sendUpdatePacketToNearby(new SPDelayedChangeStage(this, pendingStageUpdate.left()));
                }

                pendingStageUpdate = null;
            }
        }

        if (getStage() != null) {
            getStage().onTick();
        }
    }

    public boolean isServerSide() {
        return !isClientSide();
    }

    public boolean isClientSide() {
        return getWorld().isRemote;
    }

    public World getWorld() {
        return Objects.requireNonNull(masterTileEntity.getWorld());
    }

    public BlockPos getMasterPos() {
        return masterTileEntity.getBlockPos();
    }

    /**
     * You should call it, when the game is won.
     * Overriding is fine.
     */
    @OverridingMethodsMustInvokeSuper
    protected void triggerGameWin() {
        onGameEnd();
        NetworkUtils.forEachPlayerNearby(getGameCenter(), getBroadcastDistance(),
                player -> {
                    LGAchievements.WIN_GAME.trigger(player);
                    sendTo(player, new ChatComponentTranslation("msg.lootgames.win"), NotifyColor.SUCCESS);
                });

        WorldExt.playSoundServerly(getWorld(), getGameCenter(), LGSounds.GAME_WIN, 0.75F, 1.0F);
    }

    /**
     * You should call it, when the game is lost.
     * Overriding is fine.
     */
    @OverridingMethodsMustInvokeSuper
    protected void triggerGameLose() {
        onGameEnd();
        NetworkUtils.forEachPlayerNearby(getGameCenter(), getBroadcastDistance(),
                player -> {
                    LGAchievements.LOSE_GAME.trigger(player);
                    sendTo(player, new ChatComponentTranslation("msg.lootgames.lose"), NotifyColor.FAIL);
                });


        WorldExt.playSoundServerly(getWorld(), getGameCenter(), LGSounds.GAME_LOSE, 0.75F, 1.0F);
    }

    /**
     * Will be called when the game ends.
     * Overriding is fine.
     */
    protected void onGameEnd() {
        LootGameCleaner.resetUnbreakablePlayField(getWorld(), getRoomFloorPos());
        masterTileEntity.onDestroy();
    }

    protected abstract BlockPos getGameCenter();

    /**
     * Returns default broadcast distance (radius) from dungeon central position.
     * You may use it, for example, for triggering advancements and sending text messages.
     */
    public int getBroadcastDistance() {
        return StructureGenerator.getRoomWidth() / 2 + 3;//3 - it is just extra block distance after passing dungeon wall. Not so much, not so little.
    }

    public void sendTo(EntityPlayer player, IChatComponent component) {
        NetworkUtils.sendMessage(player, component);
    }

    public void sendTo(EntityPlayer player, IChatComponent component, EnumChatFormatting format) {
        sendTo(player, ChatComponentExt.withStyle(component, format));
    }

    public void sendTo(EntityPlayer player, IChatComponent component, NotifyColor format) {
        sendTo(player, component, format.getColor());
    }

    public void sendToNearby(IChatComponent component) {
        NetworkUtils.sendForEachPlayerNearby(getGameCenter(), getBroadcastDistance(), component);
    }

    public void sendToNearby(IChatComponent component, EnumChatFormatting format) {
        sendToNearby(ChatComponentExt.withStyle(component, format));
    }

    public void sendToNearby(IChatComponent component, NotifyColor format) {
        sendToNearby(component, format.getColor());
    }

    /**
     * Applies the consumer for every player that are nearby.
     * Server-only.
     */
    public void forEachPlayerNearby(Consumer<EntityPlayerMP> action) {
        NetworkUtils.forEachPlayerNearby(getGameCenter(), getBroadcastDistance(), action);
    }

    /**
     * Should return the position of block, that is a part of shielded dungeon floor or has a neighbor, that is a shielded floor block.
     * Used, for example, to recursively reset shield of unbreakability on dungeon floor.
     */
    protected abstract BlockPos getRoomFloorPos();

    /**
     * Saves current data to the disk and sends update to client.
     */
    public void saveAndSync() {
        if (isServerSide()) {
            masterTileEntity.saveAndSync();
        }
    }

    /**
     * Saves current data to the disk without sending update to client.
     */
    public void save() {
        if (isServerSide()) {
            masterTileEntity.save();
        }
    }

    @OverridingMethodsMustInvokeSuper
    public void writeNBT(NBTTagCompound nbt, SerializationType type) {
        if (type == SerializationType.SAVE) {
            nbt.setTag("task_scheduler", taskScheduler.serializeNBT());
        }

        serializeStage(this, nbt, type);
        LOGGER.debug(DEBUG_MARKER, formatLogMessage("stage '{}' was serialized for {}."), getStage(), type == SerializationType.SAVE ? "saving" : "syncing");
    }

    @OverridingMethodsMustInvokeSuper
    public void readNBT(NBTTagCompound nbt, SerializationType type) {
        if (type == SerializationType.SAVE) {
            NBTTagList schedulerTag = (NBTTagList) nbt.getTag("task_scheduler");

            taskScheduler = new TETaskScheduler(masterTileEntity);
            taskScheduler.deserializeNBT(Objects.requireNonNull(schedulerTag));
        }

        setStage(deserializeStage(this, nbt, type));
        LOGGER.debug(DEBUG_MARKER, formatLogMessage("stage '{}' was deserialized {}."), getStage(), type == SerializationType.SAVE ? "from saved file" : "on client");

        justPlaced = false;

        onStageStart(type == SerializationType.SYNC);
    }

    /**
     * Sends update packet to the client with given {@link NBTTagCompound} to all players, tracking the game.
     */
    public void sendUpdatePacketToNearby(IServerGamePacket packet) {
        if (!isServerSide()) {
            return;
        }

        BlockPos masterPos = getMasterPos();
        Trackers.forPlayersWatchingChunk(((WorldServer) getWorld()), masterPos.getX() >> 4, masterPos.getZ() >> 4, entityPlayerMP -> {
            LGNetwork.INSTANCE.sendTo(new SPacketGameUpdate(this, packet), entityPlayerMP);
        });

        LoggerExt.debug(LOGGER, DEBUG_MARKER, () -> logMessage("update packet '{}' was sent.", packet.getClass().getSimpleName()));
    }

    public void sendUpdatePacketToNearbyExcept(EntityPlayerMP excepting, IServerGamePacket packet) {
        if (!isServerSide()) {
            return;
        }

        BlockPos masterPos = getMasterPos();
        Trackers.forPlayersWatchingChunk(((WorldServer) getWorld()), masterPos.getX() >> 4, masterPos.getZ() >> 4, entityPlayerMP -> {
            if (!entityPlayerMP.getUniqueID().equals(excepting.getUniqueID())) {
                LGNetwork.INSTANCE.sendTo(new SPacketGameUpdate(this, packet), entityPlayerMP);
            }
        });

        LoggerExt.debug(LOGGER, DEBUG_MARKER, () -> logMessage("update packet '{}' to all tracking except {} was sent.", packet.getClass().getSimpleName(), excepting.getGameProfile().getName()));
    }

    /**
     * Fired on client when {@link IServerGamePacket} comes from server.
     */
    public void onUpdatePacket(IServerGamePacket packet) {
        packet.runOnClient(this);
    }

    /**
     * Sends update packet to the server with given {@link NBTTagCompound}.
     */
    public void sendFeedbackPacket(IClientGamePacket packet) {
        if (isServerSide()) {
            return;
        }

        LGNetwork.INSTANCE.sendToServer(new CPacketGameUpdate(this, packet));
        LoggerExt.debug(LOGGER, DEBUG_MARKER, () -> logMessage("feedback packet '{}' was sent.", packet.getClass().getSimpleName()));
    }

    /**
     * Fired on server when {@link IClientGamePacket} comes from client.
     */
    public void onFeedbackPacket(EntityPlayerMP sender, IClientGamePacket packet) {
        packet.runOnServer(sender, this);
    }

    /**
     * Only for usage from {@link #onPlace()}.
     * Will be synced later.
     *
     * @param stage initial stage of game
     */
    public void setupInitialStage(STAGE stage) {
        LOGGER.debug(DEBUG_MARKER, formatLogMessage("initial stage '{}' was set up."), stage);

        setStage(stage);
        onStageUpdate(null, stage, true);
        onStageStart(isClientSide());
    }

    public void switchStage(@Nullable STAGE stage) {
        STAGE old = this.getStage();
        if (old != null) old.onEnd();

        LOGGER.debug(DEBUG_MARKER, formatLogMessage("switching from stage '{}' to '{}'."), old, stage);

        setStage(stage);
        onStageUpdate(old, stage, false);
        onStageStart(isClientSide());
    }

    /**
     * Called for both logical sides when the game was switched to this stage.
     * For server: called only when you manually change stage on server side via {@link #setupInitialStage(Stage)} or {@link #switchStage(Stage)}
     * For client: called every time server sends new state, including deserializing from saved nbt.
     */
    protected void onStageUpdate(@Nullable STAGE oldStage, @Nullable STAGE newStage, boolean shouldDelayPacketSending) {
        if (isServerSide()) {
            if (newStage != null) newStage.preInit();
            save();
            if (shouldDelayPacketSending) {
                pendingStageUpdate = Pair.of(oldStage, newStage);
                LoggerExt.debug(LOGGER, DEBUG_MARKER, () -> logMessage("update packet '{}' was delayed for sending till the next tick."));
            } else {
                sendUpdatePacketToNearby(new SPChangeStage(this));
            }

            if (newStage != null) newStage.postInit();
        }
    }

    @Nullable
    public abstract STAGE createStageFromNBT(String id, NBTTagCompound stageNBT, SerializationType serializationType);

    @Nullable
    public STAGE getStage() {
        return stage;
    }

    private void setStage(@Nullable STAGE stage) {
        this.stage = stage;
    }

    /**
     * Called for both logical sides when the game was switched to this stage:
     * <ol>- by changing stage via {@link #setupInitialStage(Stage)} or {@link #switchStage(Stage)}</ol>
     * <ol>- by deserializing and syncing</ol>
     * <p>
     * Warning: {@link #getWorld()} can return null here, because world is set after reading from nbt!
     */
    protected void onStageStart(boolean clientSide) {
        if (this.stage != null) {
            this.stage.onStart(clientSide);
        }
    }

    public abstract void onDestroy();

    public abstract static class Stage {
        /**
         * Called for both logical sides when the game was switched to this stage:
         * <ol>- by changing stage via {@link #setupInitialStage(Stage)} or {@link #switchStage(Stage)}</ol>
         * <ol>- by deserializing and syncing</ol>
         * <p>
         * Warning: {@link #getWorld()} can return null here, because world is set after reading from nbt!
         */
        protected void onStart(boolean clientSide) {

        }

        /**
         * Called on every tick for both logical sides.
         */
        protected void onTick() {

        }

        /**
         * Called for both logical sides when the game was switched from this stage to another one.
         */
        protected void onEnd() {

        }

        /**
         * Serializes stage according to the provided serialization type.
         * If you have some sensitive data you can check here for type before adding it to nbt or not.
         *
         * @param serializationType defines for which purpose stage is serializing.
         */
        public NBTTagCompound serialize(SerializationType serializationType) {
            return new NBTTagCompound();
        }

        public abstract String getID();

        @Override
        public String toString() {
            return getID();
        }

        /**
         * Called on server side right after the game switched to this stage {@link #setupInitialStage(Stage)} or {@link #switchStage(Stage)},
         * but BEFORE it will be saved and synced.
         * <p>
         * Is not called upon serializing and deserializing.
         */
        public void preInit() {

        }

        /**
         * Called on server side right after the game switched to this stage {@link #setupInitialStage(Stage)} or {@link #switchStage(Stage)},
         * and AFTER it will be saved and synced.
         * <p>
         * Is not called upon serializing and deserializing.
         */
        public void postInit() {

        }
    }

    public static <STAGE extends Stage> void serializeStage(LootGame<STAGE, ?> game, NBTTagCompound nbt, SerializationType serializationType) {
        Stage stage = game.getStage();
        if (stage != null) {
            NBTTagCompound stageWrapper = new NBTTagCompound();
            stageWrapper.setTag("stage", stage.serialize(serializationType));
            stageWrapper.setString("id", stage.getID());
            nbt.setTag("stage_wrapper", stageWrapper);
        }
    }

    @Nullable
    public static <S extends Stage, T extends LootGame<S, T>> S deserializeStage(LootGame<S, T> game, NBTTagCompound nbt, SerializationType serializationType) {
        if (nbt.hasKey("stage_wrapper")) {
            NBTTagCompound stageWrapper = nbt.getCompoundTag("stage_wrapper");
            return game.createStageFromNBT(stageWrapper.getString("id"), stageWrapper.getCompoundTag("stage"), serializationType);
        } else {
            return null;
        }
    }

    private Message logMessage(String message, Object... arguments) {
        return Auxiliary.makeLogMessage(formatLogMessage(message), arguments);
    }

    private String formatLogMessage(String message) {
        return EnumChatFormatting.DARK_BLUE + getClass().getSimpleName() + ": " + message;
    }
}
