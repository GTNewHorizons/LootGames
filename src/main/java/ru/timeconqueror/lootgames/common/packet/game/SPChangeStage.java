package ru.timeconqueror.lootgames.common.packet.game;

import net.minecraft.nbt.NBTTagCompound;
import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.NBTGamePacket;
import ru.timeconqueror.timecore.api.common.tile.SerializationType;

public class SPChangeStage extends NBTGamePacket {
    /**
     * Only for using via reflection
     */
    public SPChangeStage() {
    }

    public SPChangeStage(LootGame<?, ?> game) {
        super(() -> {
            NBTTagCompound compoundNBT = new NBTTagCompound();
            LootGame.serializeStage(game, compoundNBT, SerializationType.SYNC);
            return compoundNBT;
        });
    }

    @Override
    public <S extends LootGame.Stage, T extends LootGame<S, T>> void runOnClient(LootGame<S, T> game) {
        NBTTagCompound compound = getCompound();

        S stage = LootGame.deserializeStage(game, compound, SerializationType.SYNC);
        game.switchStage(stage);
    }
}
