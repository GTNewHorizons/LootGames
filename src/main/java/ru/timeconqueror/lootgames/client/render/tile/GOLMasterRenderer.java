package ru.timeconqueror.lootgames.client.render.tile;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.block.tile.BoardGameMasterTile;
import ru.timeconqueror.lootgames.api.minigame.BoardLootGame.BoardStage;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.common.block.tile.GOLMasterTile;
import ru.timeconqueror.lootgames.minigame.gol.DisplayedSymbol;
import ru.timeconqueror.lootgames.minigame.gol.GameOfLight;
import ru.timeconqueror.lootgames.minigame.gol.GameOfLight.StageShowSequence;
import ru.timeconqueror.lootgames.minigame.gol.GameOfLight.StageUnderExpanding;
import ru.timeconqueror.lootgames.minigame.gol.QMarkAppearance.State;
import ru.timeconqueror.lootgames.minigame.gol.Symbol;
import ru.timeconqueror.timecore.animation.Ease;
import ru.timeconqueror.timecore.api.util.MathUtils;
import ru.timeconqueror.timecore.api.util.client.DrawHelper;

import static ru.timeconqueror.lootgames.minigame.gol.GameOfLight.StageUnderExpanding.MAX_TICKS_EXPANDING;

//TODO draw board by symbol, not by fully not activated board with activated overlay
public class GOLMasterRenderer extends TileEntitySpecialRenderer {
    public static final ResourceLocation BOARD = LootGames.rl("textures/game/gol_board.png");
    private static final ResourceLocation BOARD_ACTIVE = LootGames.rl("textures/game/gol_board_active.png");
    private static final ResourceLocation MARKS = LootGames.rl("textures/game/gol_marks.png");

    @Override
    public void renderTileEntityAt(TileEntity tileEntityIn, double x, double y, double z, float partialTicks) {
        GL11.glPushMatrix();

        GL11.glTranslated(x, y, z);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glColor4f(1, 1, 1, 1);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GOLMasterTile master = (GOLMasterTile) tileEntityIn;

        BoardGameMasterTile.prepareMatrix(master);

        GameOfLight game = master.getGame();
        BoardStage stage = game.getStage();
        int ticks = stage instanceof StageUnderExpanding ? ((StageUnderExpanding) stage).getTicks() : 0;

        drawBoard(game, ticks, partialTicks);

        if (stage instanceof StageShowSequence) {
            Symbol symbol = ((StageShowSequence) stage).getSymbolForRender();
            if (symbol != null) {
                drawSymbol(symbol);
            }
        }

        for (DisplayedSymbol symbol : game.getDisplayedSymbols()) {
            drawSymbol(symbol.getSymbol());
        }

        State questionMark = game.getMarkState();
        if (questionMark != State.NONE) {
            drawMark(questionMark);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        GL11.glPopMatrix();
    }

    private void drawSymbol(Symbol symbol) {
        bindTexture(BOARD_ACTIVE);

        Pos2i pos = symbol.getPos();

        DrawHelper.drawTexturedRectByParts(pos.getX(), pos.getY(), 1, 1, -0.006F, pos.getX(), pos.getY(), 1, 1, 3);
    }

    private void drawMark(State state) {
        bindTexture(MARKS);

        int textureX = 0;
        int textureY = 0;
        switch (state) {
            case ACCEPTED:
                textureX = 1;
                break;
            case DENIED:
                textureX = 0;
                textureY = 1;
                break;
        }

        DrawHelper.drawTexturedRectByParts(1, 1, 1, 1, -0.006F, textureX, textureY, 1, 1, 2);
    }

    private void drawBoard(GameOfLight game, int ticks, float partialTicks) {
        bindTexture(BOARD);

        float length = 3;
        float textureStart = 0;
        float textureLength = 48;

        if (game.getStage() instanceof StageUnderExpanding) {
            float progress = Ease.inOutQuart((float) MathUtils.coerceInRange(ticks, 0, MAX_TICKS_EXPANDING) / MAX_TICKS_EXPANDING);

            length = MathUtils.lerp(progress, 1, 3);
            textureStart = 16F - 16F * progress;
            textureLength = 32F + 16F * progress - textureStart;
        }

        GL11.glTranslatef(3 / 2F, 3 / 2F, 0);
        DrawHelper.drawTexturedRectByParts(-length / 2, -length / 2, length, length, -0.005F, textureStart, textureStart, textureLength, textureLength, 48);
        GL11.glTranslatef(-3 / 2F, -3 / 2F, 0);
    }
}
