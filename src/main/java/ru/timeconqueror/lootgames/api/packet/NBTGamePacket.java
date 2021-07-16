package ru.timeconqueror.lootgames.api.packet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Supplier;

public abstract class NBTGamePacket implements IServerGamePacket {
    @Nullable
    private NBTTagCompound compound;

    /**
     * Only for using in constructor on reception side.
     */
    public NBTGamePacket() {
    }

    public NBTGamePacket(Supplier<NBTTagCompound> compoundSupplier) {
        this(compoundSupplier.get());
    }

    public NBTGamePacket(@Nullable NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public void encode(PacketBuffer bufferTo) throws IOException {
        bufferTo.writeBoolean(compound != null);

        if (compound != null) {
            bufferTo.writeNBTTagCompoundToBuffer(compound);
        }
    }

    @Override
    public void decode(PacketBuffer bufferFrom) throws IOException {
        boolean isNotNull = bufferFrom.readBoolean();
        if (isNotNull) {
            compound = bufferFrom.readNBTTagCompoundFromBuffer();
        } else compound = null;
    }

    @Nullable
    public NBTTagCompound getCompound() {
        return compound;
    }

    public void setCompound(@Nullable NBTTagCompound compound) {
        this.compound = compound;
    }
}
