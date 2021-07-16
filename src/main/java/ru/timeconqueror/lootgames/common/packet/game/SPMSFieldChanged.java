package ru.timeconqueror.lootgames.common.packet.game;

import net.minecraft.network.PacketBuffer;
import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.IServerGamePacket;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.minigame.minesweeper.GameMineSweeper;
import ru.timeconqueror.lootgames.minigame.minesweeper.MSBoard;

import java.io.IOException;

public class SPMSFieldChanged implements IServerGamePacket {
    private Pos2i pos;
    private MSBoard.MSField field;

    /**
     * Only for using via reflection
     */
    @Deprecated
    public SPMSFieldChanged() {
    }

    public SPMSFieldChanged(Pos2i pos, MSBoard.MSField field) {
        this.pos = pos;
        this.field = field;
    }

    @Override
    public void encode(PacketBuffer bufferTo) throws IOException {
        bufferTo.writeInt(pos.getX());
        bufferTo.writeInt(pos.getY());

        bufferTo.writeNBTTagCompoundToBuffer(MSBoard.MSField.SYNC_CODEC.encode(field));
    }

    @Override
    public void decode(PacketBuffer bufferFrom) throws IOException {
        pos = new Pos2i(bufferFrom.readInt(), bufferFrom.readInt());

        this.field = MSBoard.MSField.SYNC_CODEC.decode(bufferFrom.readNBTTagCompoundFromBuffer());
    }

    @Override
    public <S extends LootGame.Stage, T extends LootGame<S, T>> void runOnClient(LootGame<S, T> game) {
        ((GameMineSweeper) game).getBoard().cSetField(pos, field);
    }
}
