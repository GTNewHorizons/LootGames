package ru.timeconqueror.timecore.api.common.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Objects;

/**
 * Syncable TileEntity.
 * <p>
 * You can also control what should be sent to client
 * and what should be only used for saving.
 */
public class SyncableTile extends SimpleTile {
    /**
     * For saving/sending data use {@link #writeNBT(NBTTagCompound, SerializationType)}
     */
    @Override
    public final void writeToNBT(NBTTagCompound compound) {
        writeNBT(compound, SerializationType.SAVE);
    }

    @Override
    public final void readFromNBT(NBTTagCompound compound) {
        //If read from client side
        if (compound.hasKey("client_flag")) {
            readNBT(compound, SerializationType.SYNC);
        } else {
            readNBT(compound, SerializationType.SAVE);
        }
    }

    @OverridingMethodsMustInvokeSuper
    protected void writeNBT(NBTTagCompound nbt, SerializationType type) {
        super.writeToNBT(nbt);
    }

    @OverridingMethodsMustInvokeSuper
    protected void readNBT(NBTTagCompound nbt, SerializationType type) {
        super.readFromNBT(nbt);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound compound = new NBTTagCompound();
        writeNBT(compound, SerializationType.SYNC);
        compound.setByte("client_flag", (byte) 0);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 99, compound);
    }

    @Override
    public final void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        NBTTagCompound compound = pkt.getNbtCompound();

        readNBT(compound, SerializationType.SYNC);
    }

    /**
     * Saves current data to the disk and sends update to client.
     */
    public void saveAndSync() {
        if (isServerSide()) {
            setBlockToUpdateAndSave();
        }
    }

    /**
     * Saves current data to the disk without sending update to client.
     */
    public void save() {
        if (isServerSide()) {
            markDirty();
        }
    }

    /**
     * Saves the block to disk, sends packet to update it on client.
     */
    private void setBlockToUpdateAndSave() {
        Objects.requireNonNull(worldObj);

        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
}
