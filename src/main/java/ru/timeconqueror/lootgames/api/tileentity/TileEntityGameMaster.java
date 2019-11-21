package ru.timeconqueror.lootgames.api.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import ru.timeconqueror.lootgames.api.minigame.LootGame;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileEntityGameMaster<T extends LootGame> extends TileEntity {
    protected T game;

    public TileEntityGameMaster(T game) {
        this.game = game;
        game.setMasterTileEntity(this);
    }

    /**
     * For saving/sending data use {@link #writeNBTForSaving(NBTTagCompound)}
     */
    @Override
    public final NBTTagCompound writeToNBT(NBTTagCompound compound) {
        writeNBTForSaving(compound);
        compound.setTag("game", game.writeNBTForSaving());
        return compound;
    }

    /**
     * For saving/sending data use {@link #readNBTFromSave(NBTTagCompound)}
     */
    @Override
    public final void readFromNBT(NBTTagCompound compound) {
        this.readNBTFromSave(compound);

        //If read from client side
        if (compound.hasKey("client_flag")) {
            game.readNBTFromClient(compound.getCompoundTag("game_synced"));
        } else {
            game.readNBTFromSave(compound.getCompoundTag("game"));
        }
    }

    /**
     * Improved analog of {@link #writeToNBT(NBTTagCompound)}. Overriding is fine.
     */
    protected NBTTagCompound writeNBTForSaving(NBTTagCompound compound) {
        return super.writeToNBT(compound);
    }

    /**
     * Improved analog of {@link #readFromNBT(NBTTagCompound)}. Overriding is fine.
     */
    protected void readNBTFromSave(NBTTagCompound compound) {
        super.readFromNBT(compound);
    }

    /**
     * Reads the part of data permitted for sending to client. Overriding is fine.
     */
    protected void readNBTFromClient(NBTTagCompound compound) {
        super.readFromNBT(compound);
    }

    /**
     * Writes the part of data permitted for sending to client. Overriding is fine.
     */
    protected NBTTagCompound writeNBTForClient(NBTTagCompound compound) {
        return super.writeToNBT(compound);
    }

    @Nonnull
    @Override
    public final NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();

        writeNBTForClient(compound);

        compound.setTag("game_synced", game.writeNBTForClient());

        compound.setByte("client_flag", (byte) 0);
        return compound;
    }

    @Override
    public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound compound = pkt.getNbtCompound();

        readNBTFromClient(compound);

        game.readNBTFromClient(compound.getCompoundTag("game_synced"));
    }

    @Nullable
    @Override
    public final SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
    }

    /**
     * Saves the block to disk, sends packet to update it on client.
     */
    protected void setBlockToUpdateAndSave() {
//        world.markBlockRangeForRenderUpdate(pos, pos);
        world.notifyBlockUpdate(pos, getState(), getState(), 3);
//        world.scheduleBlockUpdate(pos, this.getBlockType(), 0, 0);
        markDirty();
    }

    /**
     * Will be called when subordinate block is clicked by player.
     *
     * @param subordinatePos pos of subordinate block.
     * @param player         player, who clicked the subordinate block.
     */
    public void onSubordinateBlockClicked(BlockPos subordinatePos, EntityPlayer player) {
    }

    /**
     * Returns the blockstate on tileentity pos.
     */
    public IBlockState getState() {
        return world.getBlockState(pos);
    }

    public T getGame() {
        return game;
    }
}