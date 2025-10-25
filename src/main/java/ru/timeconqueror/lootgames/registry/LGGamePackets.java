package ru.timeconqueror.lootgames.registry;

import com.lootgames.sudoku.packet.SPSSyncBoard;
import com.lootgames.sudoku.packet.SPSSyncCell;
import com.lootgames.sudoku.packet.SPSudokuResetNumber;
import com.lootgames.sudoku.packet.SPSudokuSpawnLevelBeatParticles;

import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.common.packet.game.*;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.SPSSyncBoard;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.SPSSyncCell;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.SPSudokuResetNumber;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.SPSudokuSpawnLevelBeatParticles;

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

        LootGamesAPI.regServerPacket(SPSSyncCell.class);
        LootGamesAPI.regServerPacket(SPSSyncBoard.class);
        LootGamesAPI.regServerPacket(SPSudokuSpawnLevelBeatParticles.class);
        LootGamesAPI.regServerPacket(SPSudokuResetNumber.class);
    }
}
