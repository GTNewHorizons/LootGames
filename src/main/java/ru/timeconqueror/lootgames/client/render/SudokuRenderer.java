package ru.timeconqueror.lootgames.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.block.tile.BoardGameMasterTile;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.common.block.tile.SudokuTile;
import ru.timeconqueror.lootgames.minigame.sudoku.GameSudoku;
import ru.timeconqueror.lootgames.minigame.sudoku.SudokuBoard;
import ru.timeconqueror.timecore.api.util.client.DrawHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SudokuRenderer extends TileEntitySpecialRenderer {

    public static ResourceLocation BOARD = new ResourceLocation(LootGames.MODID, "textures/game/sdk_board.png");

    @Override
    public void renderTileEntityAt(TileEntity teIn, double x, double y, double z, float partialTicks) {
        SudokuTile te = (SudokuTile) teIn;
        GameSudoku game = te.getGame();
        SudokuBoard board = game.getBoard();
        int size = game.getCurrentBoardSize();

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        BoardGameMasterTile.prepareMatrix(te);

        bindTexture(BOARD);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        for (int cx = 0; cx < size; cx++) {
            for (int cz = 0; cz < size; cz++) {
                DrawHelper.drawTexturedRectByParts(cx, cz, 1, 1, -0.005f, 0, 0, 1, 1, 1);
            }
        }

        drawGridLines(size, 0.02f);

        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Map<Integer, Integer> numberCounts = new HashMap<>();

        for (int cx = 0; cx < size; cx++) {
            for (int cz = 0; cz < size; cz++) {
                int puzzleVal = board.getPuzzleValue(cx, cz);
                int playerVal = board.getPlayerValue(new Pos2i(cx, cz));
                int val = puzzleVal != 0 ? puzzleVal : playerVal;
                if (val != 0) numberCounts.put(val, numberCounts.getOrDefault(val, 0) + 1);
            }
        }

        Set<Pos2i> duplicatePositions = new HashSet<>();
        Set<Pos2i> correctCompletedPositions = new HashSet<>();

        for (int row = 0; row < size; row++) {
            Set<Pos2i> section = new HashSet<>();
            Map<Integer, Integer> counts = new HashMap<>();
            boolean valid = true;

            for (int col = 0; col < size; col++) {
                Pos2i pos = new Pos2i(row, col);
                int val = board.getPuzzleValue(pos);
                if (val == 0) val = board.getPlayerValue(pos);
                section.add(pos);

                if (val < 1 || val > 9) valid = false;
                if (val != 0) {
                    counts.put(val, counts.getOrDefault(val, 0) + 1);
                }
            }

            for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
                if (e.getValue() > 1) {
                    valid = false;
                    for (int col = 0; col < size; col++) {
                        Pos2i pos = new Pos2i(row, col);
                        int val = board.getPuzzleValue(pos);
                        if (val == 0) val = board.getPlayerValue(pos);
                        if (val == e.getKey()) duplicatePositions.add(pos);
                    }
                }
            }

            if (valid && counts.size() == 9) correctCompletedPositions.addAll(section);
        }

        for (int col = 0; col < size; col++) {
            Set<Pos2i> section = new HashSet<>();
            Map<Integer, Integer> counts = new HashMap<>();
            boolean valid = true;

            for (int row = 0; row < size; row++) {
                Pos2i pos = new Pos2i(row, col);
                int val = board.getPuzzleValue(pos);
                if (val == 0) val = board.getPlayerValue(pos);
                section.add(pos);

                if (val < 1 || val > 9) valid = false;
                if (val != 0) {
                    counts.put(val, counts.getOrDefault(val, 0) + 1);
                }
            }

            for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
                if (e.getValue() > 1) {
                    valid = false;
                    for (int row = 0; row < size; row++) {
                        Pos2i pos = new Pos2i(row, col);
                        int val = board.getPuzzleValue(pos);
                        if (val == 0) val = board.getPlayerValue(pos);
                        if (val == e.getKey()) duplicatePositions.add(pos);
                    }
                }
            }

            if (valid && counts.size() == 9) correctCompletedPositions.addAll(section);
        }

        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                Set<Pos2i> section = new HashSet<>();
                Map<Integer, Integer> counts = new HashMap<>();
                boolean valid = true;

                for (int dy = 0; dy < 3; dy++) {
                    for (int dx = 0; dx < 3; dx++) {
                        int row = boxRow * 3 + dy;
                        int col = boxCol * 3 + dx;
                        Pos2i pos = new Pos2i(row, col);
                        int val = board.getPuzzleValue(pos);
                        if (val == 0) val = board.getPlayerValue(pos);
                        section.add(pos);

                        if (val < 1 || val > 9) valid = false;
                        if (val != 0) {
                            counts.put(val, counts.getOrDefault(val, 0) + 1);
                        }
                    }
                }

                for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
                    if (e.getValue() > 1) {
                        valid = false;
                        for (Pos2i pos : section) {
                            int val = board.getPuzzleValue(pos);
                            if (val == 0) val = board.getPlayerValue(pos);
                            if (val == e.getKey()) duplicatePositions.add(pos);
                        }
                    }
                }

                if (valid && counts.size() == 9) correctCompletedPositions.addAll(section);
            }
        }

        for (int cx = 0; cx < size; cx++) {
            for (int cz = 0; cz < size; cz++) {
                Pos2i pos = new Pos2i(cx, cz);
                int puzzleVal = board.getPuzzleValue(cx, cz);
                int playerVal = board.getPlayerValue(pos);
                int actualVal = puzzleVal != 0 ? puzzleVal : playerVal;

                if (actualVal != 0) {
                    int count = numberCounts.getOrDefault(actualVal, 0);
                    int color;

                    if (count > 9) {
                        color = 0xFFAAAA;
                    } else if (duplicatePositions.contains(pos)) {
                        color = 0xFFFF00;
                    } else if (correctCompletedPositions.contains(pos)) {
                        color = 0x00FFFF;
                    } else if (count == 9) {
                        color = 0x00FF00;
                    } else {
                        color = puzzleVal != 0 ? 0x808080 : 0xFFFFFF;
                    }

                    String text = Integer.toString(actualVal);
                    Minecraft mc = Minecraft.getMinecraft();

                    float stringWidth = mc.fontRenderer.getStringWidth(text);
                    float stringHeight = mc.fontRenderer.FONT_HEIGHT;

                    float scale = 0.08f;
                    float offsetX = -stringWidth * scale / 2f;
                    float offsetY = -stringHeight * scale / 2f;

                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glTranslatef(cx + 0.525f, cz + 0.55f, -0.02f);
                    GL11.glScalef(scale, scale, scale);
                    GL11.glTranslatef(offsetX / scale, offsetY / scale, 0);
                    mc.fontRenderer.drawString(text, 0, 0, color, false);
                    GL11.glPopMatrix();

                    GL11.glPushMatrix();
                    GL11.glTranslatef(cx + 0.525f + 0.025f, cz + 0.55f + 0.025f, -0.01f);
                    GL11.glScalef(scale, scale, scale);
                    GL11.glTranslatef(offsetX / scale, offsetY / scale, 0);
                    int shadowColor = (color & 0xfcfcfc) >> 2;
                    mc.fontRenderer.drawString(text, 0, 0, shadowColor, false);
                    GL11.glPopMatrix();

                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                }
            }
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();

        SudokuOverlayHandler.addSupportedMaster(te.getBlockPos(), game);
    }

    /**
     * Draws the main grid lines for a Sudoku board using OpenGL.
     * <p>
     * This method renders both the vertical and horizontal lines at every third interval to visually separate 3x3
     * subgrids. The lines are drawn as quads with the specified thickness to ensure consistent width regardless of
     * scale.
     * </p>
     *
     * <p>
     * <b>Rendering Notes:</b>
     * </p>
     * <ul>
     * <li>Texture mapping is disabled during rendering.</li>
     * <li>Alpha blending is enabled for smooth line edges.</li>
     * <li>Cull face is disabled to ensure both sides of the lines are visible.</li>
     * <li>Lines are drawn slightly offset along the Z-axis (z = -0.01f) to prevent z-fighting with other elements
     * rendered on the same plane.</li>
     * </ul>
     *
     * @param size      the size of the grid (e.g., 9 for a 9x9 Sudoku board)
     * @param thickness the thickness of each line in world units
     */
    public static void drawGridLines(int size, float thickness) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        float z = -0.01f;
        float half = thickness / 2.0f;

        GL11.glBegin(GL11.GL_QUADS);

        for (int i = 0; i <= size; i++) {
            if (i % 3 == 0) {
                GL11.glVertex3f(i - half, 0, z);
                GL11.glVertex3f(i + half, 0, z);
                GL11.glVertex3f(i + half, size, z);
                GL11.glVertex3f(i - half, size, z);
            }
        }

        for (int i = 0; i <= size; i++) {
            if (i % 3 == 0) {
                GL11.glVertex3f(0, i - half, z);
                GL11.glVertex3f(size, i - half, z);
                GL11.glVertex3f(size, i + half, z);
                GL11.glVertex3f(0, i + half, z);
            }
        }

        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }
}
