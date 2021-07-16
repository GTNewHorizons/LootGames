package ru.timeconqueror.lootgames.client.render.tile;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.block.tile.BoardGameMasterTile;
import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.client.render.MSOverlayHandler;
import ru.timeconqueror.lootgames.common.block.tile.MSMasterTile;
import ru.timeconqueror.lootgames.minigame.minesweeper.GameMineSweeper;
import ru.timeconqueror.lootgames.minigame.minesweeper.GameMineSweeper.StageDetonating;
import ru.timeconqueror.lootgames.minigame.minesweeper.GameMineSweeper.StageExploding;
import ru.timeconqueror.lootgames.minigame.minesweeper.Mark;
import ru.timeconqueror.lootgames.minigame.minesweeper.Type;
import ru.timeconqueror.timecore.api.util.client.DrawHelper;

public class MSMasterRenderer extends TileEntitySpecialRenderer {
    private static final ResourceLocation BOARD = LootGames.rl("textures/game/ms_board.png");

    @Override
    public void renderTileEntityAt(TileEntity teIn, double x, double y, double z, float partialTicks) {
        MSMasterTile te = (MSMasterTile) teIn;
        GameMineSweeper game = te.getGame();
        int boardSize = game.getCurrentBoardSize();
        LootGame.Stage stage = game.getStage();

        GL11.glPushMatrix();

        GL11.glTranslated(x, y, z);
        BoardGameMasterTile.prepareMatrix(te);

        bindTexture(BOARD);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glColor4f(1, 1, 1, 1);

        if (!game.cIsGenerated) {
            for (int xL = 0; xL < boardSize; xL++) {
                for (int zL = 0; zL < boardSize; zL++) {
                    //brightened
                    DrawHelper.drawTexturedRectByParts(xL, zL, 1, 1, -0.005F, 0, 0, 1, 1, 4);
                }
            }
        } else {
            for (int xL = 0; xL < boardSize; xL++) {
                for (int zL = 0; zL < boardSize; zL++) {
                    boolean isHidden = game.getBoard().isHidden(xL, zL);

                    Type type = game.getBoard().getType(xL, zL);

                    if (!isHidden && type == Type.BOMB) {
                        int max = stage instanceof StageDetonating ? ((StageDetonating) stage).getDetonationTicks() : 1;
                        int ticks = game.getTicks();

                        float period = 8;

                        float times = max / period;

                        float extendedPeriod = period * (times + 1) / times; // is needed because we want for it to explode at red state that comes on half period.
                        double alphaFactor = stage instanceof StageExploding ? 1 : Math.abs(Math.sin(Math.toRadians(ticks / extendedPeriod * 180F)));
                        int alphaColor = DrawHelper.changeAlpha(0xFFFFFFFF, (int) (alphaFactor * 255));

                        //brightened
                        DrawHelper.drawTexturedRectByParts(xL, zL, 1, 1, -0.005F, 1, 0, 1, 1, 4F);

                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        GL11.glEnable(GL11.GL_ALPHA_TEST);
                        //brightened translucent
                        DrawHelper.drawTexturedRectByParts(xL, zL, 1, 1, -0.005F, 1, 3, 1, 1, 4F, alphaColor);

                        GL11.glDisable(GL11.GL_BLEND);
                        GL11.glDisable(GL11.GL_ALPHA_TEST);
                    } else {

                        Mark mark = game.getBoard().getMark(xL, zL);

                        int textureX;
                        int textureY;

                        if (isHidden) {
                            if (mark == Mark.NO_MARK) {
                                textureX = 0;
                                textureY = 0;
                            } else if (mark == Mark.FLAG) {
                                textureX = 3;
                                textureY = 0;
                            } else {
                                textureX = 0;
                                textureY = 3;
                            }
                        } else {
                            if (type.getId() > 0) {
                                textureX = type.getId() % 4 == 0 ? 3 : (type.getId() % 4 - 1);
                                textureY = type.getId() <= 4 ? 1 : 2;
                            } else /*if type == empty*/ {
                                textureX = 2;
                                textureY = 0;
                            }
                        }

                        DrawHelper.drawTexturedRectByParts(xL, zL, 1, 1, -0.005F, textureX, textureY, 1, 1, 4);
                    }
                }
            }
        }

        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();

        MSOverlayHandler.addSupportedMaster(te);
    }
}
