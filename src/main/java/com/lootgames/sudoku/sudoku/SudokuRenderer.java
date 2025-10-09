package com.lootgames.sudoku.sudoku;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.lootgames.sudoku.block.SudokuTile;

import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.block.tile.BoardGameMasterTile;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.timecore.api.util.client.DrawHelper;

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

        DrawHelper.drawGridLines(size, 0.02f);

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

                    float stringWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(Integer.toString(actualVal));
                    // 计算中心X
                    float centerX = 0.5f - (stringWidth/2);
                    float centerY = 0.5f - ((float) Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT /2);
                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glTranslatef(cx + 0.25f, cz + 0.175f, -0.02f);
                    GL11.glScalef(0.08f, 0.08f, 0.08f);
                    Minecraft.getMinecraft().fontRenderer.drawString(Integer.toString(actualVal), 0, 0, color, false);
                    GL11.glPopMatrix();

                    GL11.glPushMatrix();
                    GL11.glTranslatef(cx+0.25f+0.025f, cz+0.175f+0.025f, -0.01f);
                    GL11.glScalef(0.08f, 0.08f, 0.08f);
                    color = (color & 0xfcfcfc) >> 2 | color & 0xff000000;
                    Minecraft.getMinecraft().fontRenderer.drawString(Integer.toString(actualVal), 0, 0, color, false);
                    GL11.glPopMatrix();
                    // DrawHelper.drawStringWithShadow(
                    //     Minecraft.getMinecraft().fontRenderer,
                    //     Integer.toString(actualVal),
                    //     0,
                    //     0,
                    //     color);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    // GL11.glPopMatrix();
                }
            }
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();

        SudokuOverlayHandler.addSupportedMaster(te.getBlockPos(), game);
    }
}
