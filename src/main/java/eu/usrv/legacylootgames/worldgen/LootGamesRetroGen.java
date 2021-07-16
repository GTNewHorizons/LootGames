package eu.usrv.legacylootgames.worldgen;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import eu.usrv.legacylootgames.LootGamesLegacy;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import ru.timeconqueror.lootgames.common.config.LGConfigs;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;


// Borrowed from https://github.com/jtmnf/SimpleOreGenerator/blob/master/src/main/java/com/jtmnf/simpleoregen/handler/RetroGenWorld.java
public class LootGamesRetroGen {
    public static LootGamesRetroGen instance = new LootGamesRetroGen();
    public static HashMap<Integer, Queue<ChunkInfo>> _mRetroChunk = new HashMap<Integer, Queue<ChunkInfo>>();
    private static final String NBTMODID = "lootgames";
    private static final String NBTGENERATED = "generated";

    public static void initRetroGen() {
        if (LGConfigs.GENERAL.worldGen.retroGenDungeons)
            MinecraftForge.EVENT_BUS.register(instance);
    }

    @SubscribeEvent
    public void worldTickEvent(TickEvent.WorldTickEvent pEvent) {
        if (pEvent.side != Side.SERVER)
            return;

        World tWorld = pEvent.world;
        int tDimensionID = tWorld.provider.dimensionId;

        if (pEvent.phase == TickEvent.Phase.END) {
            Queue<ChunkInfo> tChunks = _mRetroChunk.get(tDimensionID);

            if (tChunks != null && !tChunks.isEmpty()) {
                ChunkInfo tChunkInfo = tChunks.poll();

                LootGamesLegacy.WorldGen.generate(null, tChunkInfo.mChunkX, tChunkInfo.mChunkZ, tWorld, null, null);

                _mRetroChunk.put(tDimensionID, tChunks);
            } else if (tChunks != null) {
                _mRetroChunk.remove(tDimensionID);
            }
        }
    }

    @SubscribeEvent
    public void chunkLoadEvent(ChunkDataEvent.Load pEvent) {
        int tDimensionID = pEvent.world.provider.dimensionId;

        NBTTagCompound tTagCompound = (NBTTagCompound) pEvent.getData().getTag(NBTMODID);

        boolean tRetronGen = tTagCompound != null && !tTagCompound.hasKey(NBTGENERATED);

        if (tRetronGen) {
            Queue<ChunkInfo> tChunks = _mRetroChunk.get(tDimensionID);

            if (tChunks == null) {
                _mRetroChunk.put(tDimensionID, new ArrayDeque<ChunkInfo>());
                tChunks = _mRetroChunk.get(tDimensionID);
            }
            if (tChunks != null) {
                tChunks.add(new ChunkInfo(pEvent.getChunk().xPosition, pEvent.getChunk().zPosition));
                _mRetroChunk.put(tDimensionID, tChunks);
            }
        }
    }

    @SubscribeEvent
    public void chunkSaveEvent(ChunkDataEvent.Save pEvent) {
        NBTTagCompound tagCompound = pEvent.getData().getCompoundTag(NBTMODID);
        if (!tagCompound.hasKey(NBTGENERATED)) {
            tagCompound.setBoolean(NBTGENERATED, true);
        }
        pEvent.getData().setTag(NBTMODID, tagCompound);
    }

    public static class ChunkInfo {
        public int mChunkX;
        public int mChunkZ;

        public ChunkInfo(int pChunkX, int pChunkZ) {
            mChunkX = pChunkX;
            mChunkZ = pChunkZ;
        }
    }
}
