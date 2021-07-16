package ru.timeconqueror.lootgames.common.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.block.tile.GameMasterTile;
import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.GamePacketRegistry.Key;
import ru.timeconqueror.lootgames.api.packet.GamePacketRegistry.Storage;
import ru.timeconqueror.lootgames.api.packet.IGamePacket;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.ThreeConsumer;
import ru.timeconqueror.lootgames.utils.future.WorldExt;
import ru.timeconqueror.timecore.api.util.client.ClientProxy;

import java.io.IOException;

public abstract class PacketGameUpdate<T extends IGamePacket> implements IMessage {
    private T gamePacket;
    private BlockPos masterPos;

    public <G extends LootGame<?, G>> PacketGameUpdate(LootGame<?, G> game, T gamePacket) {
        this.masterPos = game.getMasterPos();
        this.gamePacket = gamePacket;
    }

    public PacketGameUpdate() {

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            PacketBuffer buffer = new PacketBuffer(buf);

            BlockPos masterPos = BlockPos.of(buffer.readLong());

            Key key = new Key(buffer.readStringFromBuffer(Short.MAX_VALUE), buffer.readInt());
            Class<? extends T> packetClass = getStorage().getPacketClass(key);

            T gamePacket;
            try {
                gamePacket = packetClass.newInstance();
                gamePacket.decode(buffer);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Can't decode received game packet, due to lack of public nullary constructor in " + packetClass, e);
            }

            setMasterPos(masterPos);
            setGamePacket(gamePacket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            PacketBuffer buffer = new PacketBuffer(buf);

            buffer.writeLong(getMasterPos().asLong());

            Storage<T> storage = getStorage();
            Key info = storage.getKey(getGamePacketClass());

            buffer.writeStringToBuffer(info.getModId());
            buffer.writeInt(info.getPacketId());
            getGamePacket().encode(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public T getGamePacket() {
        return gamePacket;
    }

    protected void setMasterPos(BlockPos masterPos) {
        this.masterPos = masterPos;
    }

    protected void setGamePacket(T gamePacket) {
        this.gamePacket = gamePacket;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends T> getGamePacketClass() {
        return (Class<? extends T>) gamePacket.getClass();
    }

    public abstract Storage<T> getStorage();

    public static class Handler<T extends IGamePacket, P extends PacketGameUpdate<T>> implements IMessageHandler<P, IMessage> {
        private final ThreeConsumer<MessageContext, LootGame<?, ?>, T> gameUpdater;

        public Handler(ThreeConsumer<MessageContext, LootGame<?, ?>, T> gameUpdater) {
            this.gameUpdater = gameUpdater;
        }

        @Override
        public IMessage onMessage(P packet, MessageContext ctx) {
            World world = getWorld(ctx);
            TileEntity te = WorldExt.getTileEntity(world, packet.getMasterPos());
            if (te instanceof GameMasterTile<?>) {
                GameMasterTile<?> master = ((GameMasterTile<?>) te);
                gameUpdater.accept(ctx, master.getGame(), packet.getGamePacket());
            } else {
                LootGames.LOGGER.error("Something went wrong. Can't find TileEntityMaster on pos " + packet.getMasterPos() + " for packet " + packet.getGamePacketClass().getName());
            }

            return null;
        }

        @SuppressWarnings("ConstantConditions")
        private World getWorld(MessageContext ctx) {
            return ctx.side == Side.CLIENT ? ClientProxy.world() : ctx.getServerHandler().playerEntity.worldObj;
        }
    }
}
