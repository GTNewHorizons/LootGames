package ru.timeconqueror.lootgames.registry;

import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.common.packet.game.*;

public class LGGamePackets {
    public static void register() {
        LootGamesAPI.regServerPacket(SPChangeStage.class);
        LootGamesAPI.regServerPacket(SPDelayedChangeStage.class);

        LootGamesAPI.regServerPacket(SPMSFieldChanged.class);
        LootGamesAPI.regServerPacket(SPMSGenBoard.class);
        LootGamesAPI.regServerPacket(SPMSResetFlags.class);
        LootGamesAPI.regServerPacket(SPMSSpawnLevelBeatParticles.class);

        LootGamesAPI.regServerPacket(SPGOLSendDisplayedSymbol.class);
        LootGamesAPI.regClientPacket(CPGOLSymbolsShown.class);
        LootGamesAPI.regServerPacket(SPGOLDrawMark.class);
        LootGamesAPI.regServerPacket(SPGOLSpawnStageUpParticles.class);
    }
}
