package eu.usrv.legacylootgames.auxiliary;


import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;


/**
 * MOVE THIS TO YAMCORE!!
 */
public class TeleportHelper {
    public static Entity teleportEntity(Entity pEntity, TeleportPoint pDestination) {
        final boolean changeDim = pEntity.worldObj.provider.dimensionId != pDestination.dimID;
        if (changeDim)
            teleportToDimensionNew(pEntity, pDestination);
        else {
            Entity mount = pEntity.ridingEntity;
            if (pEntity.ridingEntity != null) {
                pEntity.mountEntity(null);
                mount = teleportEntity(mount, pDestination);
            }
            if ((pEntity instanceof EntityPlayerMP)) {
                final EntityPlayerMP player = (EntityPlayerMP) pEntity;
                player.setPositionAndUpdate(pDestination.x, pDestination.y, pDestination.z);
            } else
                pEntity.setPosition(pDestination.x, pDestination.y, pDestination.z);
            pEntity.setLocationAndAngles(pDestination.x, pDestination.y, pDestination.z, (float) pDestination.yaw, (float) pDestination.pitch);
            if (mount != null)
                pEntity.mountEntity(mount);
        }
        return pEntity;
    }

    public static Entity teleportToDimensionNew(Entity pEntity, TeleportPoint pDestination) {
        if (!pEntity.worldObj.isRemote) {
            Entity mount = pEntity.ridingEntity;
            if (pEntity.ridingEntity != null) {
                pEntity.mountEntity(null);
                mount = teleportToDimensionNew(mount, pDestination);
            }

            pDestination.y += 0.5D;
            final int currentDim = pEntity.worldObj.provider.dimensionId;
            final MinecraftServer minecraftserver = MinecraftServer.getServer();
            final WorldServer currentServer = minecraftserver.worldServerForDimension(currentDim);
            final WorldServer targetServer = minecraftserver.worldServerForDimension(pDestination.dimID);
            currentServer.updateEntityWithOptionalForce(pEntity, false);
            if ((pEntity instanceof EntityPlayerMP)) {
                final EntityPlayerMP player = (EntityPlayerMP) pEntity;

                player.dimension = pDestination.dimID;

                player.playerNetServerHandler.sendPacket(new S07PacketRespawn(player.dimension, player.worldObj.difficultySetting, player.worldObj.getWorldInfo().getTerrainType(), player.theItemInWorldManager.getGameType()));
                currentServer.removePlayerEntityDangerously(player);
                player.isDead = false;
            } else {
                pEntity.dimension = pDestination.dimID;
                pEntity.isDead = false;
            }
            pEntity.setLocationAndAngles(pDestination.x, pDestination.y, pDestination.z, (float) pDestination.yaw, (float) pDestination.pitch);
            targetServer.theChunkProviderServer.loadChunk((int) pDestination.x >> 4, (int) pDestination.z >> 4);
            targetServer.spawnEntityInWorld(pEntity);
            targetServer.updateEntityWithOptionalForce(pEntity, false);
            pEntity.setWorld(targetServer);
            if (!(pEntity instanceof EntityPlayerMP)) {
                final NBTTagCompound entityNBT = new NBTTagCompound();
                pEntity.isDead = false;
                pEntity.writeToNBTOptional(entityNBT);
                pEntity.isDead = true;
                pEntity = EntityList.createEntityFromNBT(entityNBT, targetServer);
                if (pEntity == null)
                    return null;
                pEntity.setLocationAndAngles(pDestination.x, pDestination.y, pDestination.z, (float) pDestination.yaw, (float) pDestination.pitch);
                targetServer.spawnEntityInWorld(pEntity);
                pEntity.setWorld(targetServer);
                pEntity.dimension = pDestination.dimID;
            }
            if ((pEntity instanceof EntityPlayerMP)) {
                final EntityPlayerMP player = (EntityPlayerMP) pEntity;
                if (currentServer != null)
                    currentServer.getPlayerManager().removePlayer(player);
                targetServer.getPlayerManager().addPlayer(player);
                targetServer.theChunkProviderServer.loadChunk((int) player.posX >> 4, (int) player.posZ >> 4);
                targetServer.updateEntityWithOptionalForce(pEntity, false);

                player.playerNetServerHandler.setPlayerLocation(pDestination.x, pDestination.y, pDestination.z, (float) pDestination.yaw, (float) pDestination.pitch);
                player.theItemInWorldManager.setWorld(targetServer);
                player.mcServer.getConfigurationManager().updateTimeAndWeatherForPlayer(player, targetServer);
                player.mcServer.getConfigurationManager().syncPlayerInventory(player);
                FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, currentDim, pDestination.dimID);
                player.setPositionAndUpdate(pDestination.x, pDestination.y, pDestination.z);
                player.setLocationAndAngles(pDestination.x, pDestination.y, pDestination.z, (float) pDestination.yaw, (float) pDestination.pitch);
            }
            pEntity.setLocationAndAngles(pDestination.x, pDestination.y, pDestination.z, (float) pDestination.yaw, (float) pDestination.pitch);
            if (mount != null) {
                if ((pEntity instanceof EntityPlayerMP))
                    targetServer.updateEntityWithOptionalForce(pEntity, true);
                pEntity.mountEntity(mount);
                targetServer.updateEntities();
                teleportEntity(pEntity, pDestination);
            }
        }
        return pEntity;
    }

    public static class TeleportPoint {
        public int dimID;
        public String pointName;
        public double x;
        public double y;
        public double z;
        public double yaw;
        public double pitch;
        public boolean defPoint;
    }
}
