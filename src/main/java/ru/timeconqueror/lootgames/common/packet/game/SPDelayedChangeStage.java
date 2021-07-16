package ru.timeconqueror.lootgames.common.packet.game;

import net.minecraft.network.PacketBuffer;
import ru.timeconqueror.lootgames.api.minigame.LootGame;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

public class SPDelayedChangeStage extends SPChangeStage {
    @Nullable
    private String prevStageId;

    public SPDelayedChangeStage() {
    }

    public SPDelayedChangeStage(LootGame<?, ?> game, @Nullable LootGame.Stage stage) {
        super(game);
        this.prevStageId = stage != null ? stage.getID() : null;
    }

    @Override
    public void encode(PacketBuffer bufferTo) throws IOException {
        super.encode(bufferTo);
        bufferTo.writeBoolean(prevStageId != null);
        if (prevStageId != null) {
            bufferTo.writeStringToBuffer(prevStageId);
        }
    }

    @Override
    public void decode(PacketBuffer bufferFrom) throws IOException {
        super.decode(bufferFrom);
        if (bufferFrom.readBoolean()) {
            prevStageId = bufferFrom.readStringFromBuffer(Short.MAX_VALUE);
        }
    }

    @Override
    public <S extends LootGame.Stage, T extends LootGame<S, T>> void runOnClient(LootGame<S, T> game) {
        String currentStageId = game.getStage() != null ? game.getStage().getID() : null;
        if (Objects.equals(currentStageId, this.prevStageId)) {
            super.runOnClient(game);
        }
    }
}