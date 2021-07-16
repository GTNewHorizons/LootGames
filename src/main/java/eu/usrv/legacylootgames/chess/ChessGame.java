package eu.usrv.legacylootgames.chess;


import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import eu.usrv.legacylootgames.ILootGame;
import eu.usrv.legacylootgames.chess.blocks.BlockChessBlock;
import eu.usrv.legacylootgames.chess.entities.*;
import eu.usrv.legacylootgames.chess.tiles.TEChessMasterBlock;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.LootGames;


public class ChessGame implements ILootGame {
    public static BlockChessBlock GameBlock;
    private static final Block SQUARE_WHITE = Blocks.quartz_block;
    private static final Block SQUARE_BLACK = Blocks.obsidian;

    @Override
    public void init() {
        GameBlock = new BlockChessBlock();
        GameRegistry.registerTileEntity(TEChessMasterBlock.class, "LOOTGAMES_CHESS_TE");
        GameRegistry.registerBlock(GameBlock, "ChessMasterBlock");

        int id = 0;
        EntityRegistry.registerModEntity(EntityBishopFigure.class, "LootGamesChessBishop", id++, LootGames.INSTANCE, 1, 5, false);
        EntityRegistry.registerModEntity(EntityKingFigure.class, "LootGamesChessKing", id++, LootGames.INSTANCE, 1, 5, false);
        EntityRegistry.registerModEntity(EntityKnightFigure.class, "LootGamesChessKnight", id++, LootGames.INSTANCE, 1, 5, false);
        EntityRegistry.registerModEntity(EntityPawnFigure.class, "LootGamesChessPawn", id++, LootGames.INSTANCE, 1, 5, false);
        EntityRegistry.registerModEntity(EntityQueenFigure.class, "LootGamesChessQueen", id++, LootGames.INSTANCE, 1, 5, false);
        EntityRegistry.registerModEntity(EntityRookFigure.class, "LootGamesChessRook", id++, LootGames.INSTANCE, 1, 5, false);
    }

    @Override
    public boolean onGenerateBlock(World pWorldObject, int pMaxXZ, int pMaxY, int pCenterX, int pBottom, int pCenterZ, int pOffsetX, int pOffsetY, int pOffsetZ) {
        boolean tPlaced = false;
        Block tTBP = Blocks.nether_brick;

        // Why is a chess board even? Damn you OCD!
        if (pOffsetY == (pBottom + 1)) {
            tPlaced = true;

            if ((pOffsetX >= -3 && pOffsetX <= 4) && (pOffsetZ >= -3 && pOffsetZ <= 4)) {
                if ((((pOffsetX + 4) + (pOffsetZ + 4)) % 2) == 0)
                    tTBP = SQUARE_WHITE;
                else
                    tTBP = SQUARE_BLACK;
            }

            pWorldObject.setBlock(pOffsetX + pCenterX, pOffsetY, pOffsetZ + pCenterZ, tTBP);
        }

        return tPlaced;
    }

}
