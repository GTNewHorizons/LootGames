package ru.timeconqueror.lootgames.common.block.tile;

import ru.timeconqueror.lootgames.registry.LGSounds;
import ru.timeconqueror.lootgames.utils.future.WorldExt;
import ru.timeconqueror.timecore.api.common.tile.SimpleTile;
import ru.timeconqueror.timecore.api.util.RandHelper;

public class PuzzleMasterTile extends SimpleTile {
    private long lastSoundTick = 0L;

    @Override
    public void updateEntity() {
        if (!isClientSide()) {
            if (RandHelper.RAND.nextInt(100) <= 10) {
                if (lastSoundTick < System.currentTimeMillis()) {
                    lastSoundTick = System.currentTimeMillis() + (RandHelper.RAND.nextInt(90) + 30) * 1000;
                    WorldExt.playSoundServerly(worldObj, getBlockPos(), LGSounds.PUZZLE_MASTER_STRANGE, 0.5F, 1.0F);
                }
            }
        }
    }
}
