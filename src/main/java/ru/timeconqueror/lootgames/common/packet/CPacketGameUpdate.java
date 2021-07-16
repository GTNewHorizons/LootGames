package ru.timeconqueror.lootgames.common.packet;

import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.api.packet.GamePacketRegistry;
import ru.timeconqueror.lootgames.api.packet.IClientGamePacket;

public class CPacketGameUpdate extends PacketGameUpdate<IClientGamePacket> {
    public <G extends LootGame<?, G>> CPacketGameUpdate(LootGame<?, G> game, IClientGamePacket gamePacket) {
        super(game, gamePacket);
    }

    public CPacketGameUpdate() {
    }

    @Override
    public GamePacketRegistry.Storage<IClientGamePacket> getStorage() {
        return GamePacketRegistry.clientStorage();
    }

    public static Handler<IClientGamePacket, CPacketGameUpdate> makeHandler() {
        return new Handler<>((context, lootGame, packet) -> lootGame.onFeedbackPacket(context.getServerHandler().playerEntity, packet));
    }
}
