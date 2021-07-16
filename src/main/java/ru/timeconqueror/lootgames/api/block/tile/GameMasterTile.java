package ru.timeconqueror.lootgames.api.block.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.timecore.api.common.tile.SerializationType;
import ru.timeconqueror.timecore.api.common.tile.SyncableTile;

public abstract class GameMasterTile<T extends LootGame<?, T>> extends SyncableTile {
    protected T game;
    private long age;

    public GameMasterTile(T game) {
        this.game = game;
        game.setMasterTileEntity(this);
    }

    public long getAge() {
        return age;
    }

    @Override
    public void updateEntity() {
        age++;
        game.onTick();
    }

    @Override
    public void setWorldObj(World worldIn) {
        super.setWorldObj(worldIn);
        game.onLoad();
    }

    /**
     * Called when TileEntityGameMaster or BlockSubordinate should be broken.
     * Block with TileEntity still exists at this point.
     * This method implies destroying of all game blocks.
     */
    public void onDestroy() {
        game.onDestroy();
    }

    @Override
    protected void writeNBT(NBTTagCompound nbt, SerializationType type) {
        super.writeNBT(nbt, type);

        NBTTagCompound gameTag = new NBTTagCompound();
        game.writeNBT(gameTag, type);
        nbt.setTag("game", gameTag);
    }

    @Override
    protected void readNBT(NBTTagCompound nbt, SerializationType type) {
        super.readNBT(nbt, type);

        game.readNBT(nbt.getCompoundTag("game"), type);
    }

    /**
     * Will be called when subordinate block is right-clicked by player.
     *
     * @param player         player, who clicked the subordinate block.
     * @param subordinatePos pos of subordinate block.
     */
    public void onBlockRightClick(EntityPlayer player, BlockPos subordinatePos) {
    }

    /**
     * Will be called when subordinate block is left-clicked by player.
     *
     * @param player         player, who clicked the subordinate block.
     * @param subordinatePos pos of subordinate block.
     */
    public void onBlockLeftClick(EntityPlayer player, BlockPos subordinatePos) {

    }

    public T getGame() {
        return game;
    }
}
