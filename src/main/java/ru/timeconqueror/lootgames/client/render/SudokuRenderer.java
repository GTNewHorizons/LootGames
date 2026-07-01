package ru.timeconqueror.lootgames.client.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.block.tile.BoardGameMasterTile;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.common.block.tile.SudokuTile;
import ru.timeconqueror.lootgames.common.config.ConfigSudoku;
import ru.timeconqueror.lootgames.minigame.sudoku.GameSudoku;
import ru.timeconqueror.lootgames.minigame.sudoku.SudokuBoard;
import ru.timeconqueror.timecore.api.util.client.DrawHelper;

public class SudokuRenderer extends TileEntitySpecialRenderer implements IResourceManagerReloadListener {

    public static ResourceLocation BOARD = new ResourceLocation(LootGames.MODID, "textures/game/sdk_board.png");

    // Border variant bitmask: bit0=right, bit1=bottom, bit2=left, bit3=top
    private static final int BORDER_RIGHT = 1;
    private static final int BORDER_BOTTOM = 2;
    private static final int BORDER_LEFT = 4;
    private static final int BORDER_TOP = 8;

    // 16 texture variants (indexed by bitmask); null = not yet initialised
    private static int[] boardTextures;

    public SudokuRenderer() {
        IResourceManager rm = Minecraft.getMinecraft().getResourceManager();
        if (rm instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) rm).registerReloadListener(this);
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        // free the generated GL texture ids and force regeneration against the new resources
        if (boardTextures != null) {
            for (int id : boardTextures) {
                if (id != 0) GL11.glDeleteTextures(id);
            }
            boardTextures = null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity teIn, double x, double y, double z, float partialTicks) {
        SudokuTile te = (SudokuTile) teIn;
        GameSudoku game = te.getGame();
        SudokuBoard board = game.getBoard();
        int size = game.getCurrentBoardSize();

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        BoardGameMasterTile.prepareMatrix(te);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        ensureBoardTextures();

        for (int cx = 0; cx < size; cx++) {
            for (int cz = 0; cz < size; cz++) {
                int variant = borderVariant(cx, cz, size);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, boardTextures[variant]);
                DrawHelper.drawTexturedRectByParts(cx, cz, 1, 1, -0.005f, 0, 0, 1, 1, 1);
            }
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Minecraft mc = Minecraft.getMinecraft();

        float revealProgress = game.getBoardRevealProgress();
        float lcProgress = game.getLevelCompleteProgress();

        ConfigSudoku.ConfigSudokuSnapshot snap = game.getConfigSnapshot();
        Map<Pos2i, Integer> hintColors = computeHintColors(board, size, snap);

        for (int cx = 0; cx < size; cx++) {
            for (int cz = 0; cz < size; cz++) {
                // board reveal: skip cells not yet scanned (row-major order)
                if (revealProgress >= 0 && revealProgress < 1f) {
                    int cellIndex = cz * SudokuBoard.SIZE + cx;
                    if (cellIndex > revealProgress * 81f) continue;
                }

                Pos2i pos = new Pos2i(cx, cz);
                int puzzleVal = board.getPuzzleValue(cx, cz);
                int playerVal = board.getPlayerValue(pos);
                int actualVal = puzzleVal != 0 ? puzzleVal : playerVal;

                if (actualVal != 0) {
                    Integer hint = hintColors == null ? null : hintColors.get(pos);
                    int color = hint != null ? hint : (puzzleVal != 0 ? 0x808080 : 0xFFFFFF);

                    if (puzzleVal == 0) {
                        float p = game.getWrongAnswerProgress();
                        if (p >= 0) {
                            int gb = (int) (0xFF * (1f - p));
                            color = (0xFF << 16) | (gb << 8) | gb;
                        }
                    }

                    // level complete: all cells fade to gold
                    if (lcProgress >= 0) {
                        int baseR = (color >> 16) & 0xFF;
                        int baseG = (color >> 8) & 0xFF;
                        int baseB = color & 0xFF;
                        int r = baseR + (int) ((0xFF - baseR) * lcProgress);
                        int g = baseG + (int) ((0xDD - baseG) * lcProgress);
                        int b = (int) (baseB * (1f - lcProgress));
                        color = (r << 16) | (g << 8) | b;
                    }

                    String text = Integer.toString(actualVal);

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
                } else if (puzzleVal == 0) {
                    boolean[] cellNotes = board.getNotes(pos);
                    float noteScale = 0.027f;
                    float px = 1.0f / 16.0f;
                    float notesBoxStart = 1.25f * px;
                    float miniCellSize = 4.5f * px;
                    for (int d = 1; d <= 9; d++) {
                        if (!cellNotes[d - 1]) continue;

                        int noteCol = (d - 1) % 3;
                        int noteRow = (d - 1) / 3;
                        String text = Integer.toString(d);
                        float textWidth = mc.fontRenderer.getStringWidth(text) * noteScale;
                        float textHeight = mc.fontRenderer.FONT_HEIGHT * noteScale;
                        float miniCellX = cx + notesBoxStart + noteCol * miniCellSize;
                        float miniCellZ = cz + notesBoxStart + noteRow * miniCellSize;
                        float nx = miniCellX + (miniCellSize - textWidth) / 2.0f;
                        float nz = miniCellZ + (miniCellSize - textHeight) / 2.0f;

                        GL11.glPushMatrix();
                        GL11.glTranslatef(nx, nz, -0.02f);
                        GL11.glScalef(noteScale, noteScale, noteScale);
                        mc.fontRenderer.drawString(text, 0, 0, 0xAAAAAA, false);
                        GL11.glPopMatrix();
                    }
                }
            }
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();

        SudokuOverlayHandler.addSupportedMaster(te.getBlockPos(), game);
    }

    /**
     * Builds a per-cell hint color override based on the player's current board state and the enabled hint flags.
     * Returns {@code null} when no hints are enabled (caller falls back to the plain puzzle/player colors).
     *
     * Priority: overflow > duplicate > completed section > completed digit.
     */
    private static Map<Pos2i, Integer> computeHintColors(SudokuBoard board, int size,
            ConfigSudoku.ConfigSudokuSnapshot snap) {
        if (snap == null) return null;
        boolean any = snap.isHintDuplicates() || snap.isHintOverflow()
                || snap.isHintCompletedDigit()
                || snap.isHintCompletedSection();
        if (!any) return null;

        Map<Integer, Integer> numberCounts = new HashMap<>();
        for (int cx = 0; cx < size; cx++) {
            for (int cz = 0; cz < size; cz++) {
                int puzzleVal = board.getPuzzleValue(cx, cz);
                int playerVal = board.getPlayerValue(new Pos2i(cx, cz));
                int val = puzzleVal != 0 ? puzzleVal : playerVal;
                if (val != 0) numberCounts.merge(val, 1, Integer::sum);
            }
        }

        Set<Pos2i> duplicatePositions = new HashSet<>();
        Set<Pos2i> correctCompletedPositions = new HashSet<>();

        for (int row = 0; row < size; row++) {
            Pos2i[] section = new Pos2i[size];
            for (int col = 0; col < size; col++) section[col] = new Pos2i(row, col);
            scanSection(board, section, duplicatePositions, correctCompletedPositions);
        }
        for (int col = 0; col < size; col++) {
            Pos2i[] section = new Pos2i[size];
            for (int row = 0; row < size; row++) section[row] = new Pos2i(row, col);
            scanSection(board, section, duplicatePositions, correctCompletedPositions);
        }
        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                Pos2i[] section = new Pos2i[9];
                int i = 0;
                for (int dy = 0; dy < 3; dy++) {
                    for (int dx = 0; dx < 3; dx++) {
                        section[i++] = new Pos2i(boxRow * 3 + dy, boxCol * 3 + dx);
                    }
                }
                scanSection(board, section, duplicatePositions, correctCompletedPositions);
            }
        }

        Map<Pos2i, Integer> out = new HashMap<>();
        for (int cx = 0; cx < size; cx++) {
            for (int cz = 0; cz < size; cz++) {
                Pos2i pos = new Pos2i(cx, cz);
                int puzzleVal = board.getPuzzleValue(cx, cz);
                int playerVal = board.getPlayerValue(pos);
                int actualVal = puzzleVal != 0 ? puzzleVal : playerVal;
                if (actualVal == 0) continue;

                int count = numberCounts.getOrDefault(actualVal, 0);
                if (snap.isHintOverflow() && count > 9) {
                    out.put(pos, 0xFFAAAA);
                } else if (snap.isHintDuplicates() && duplicatePositions.contains(pos)) {
                    out.put(pos, 0xFFFF00);
                } else if (snap.isHintCompletedSection() && correctCompletedPositions.contains(pos)) {
                    out.put(pos, 0x00FFFF);
                } else if (snap.isHintCompletedDigit() && count == 9) {
                    out.put(pos, 0x00FF00);
                }
            }
        }
        return out;
    }

    /**
     * Scans a row/column/box of 9 positions. Adds every position that participates in a duplicate digit to
     * {@code duplicates}, and adds all positions to {@code completed} when the section contains the digits 1-9 exactly
     * once.
     */
    private static void scanSection(SudokuBoard board, Pos2i[] section, Set<Pos2i> duplicates, Set<Pos2i> completed) {
        Map<Integer, Integer> counts = new HashMap<>();
        boolean valid = true;
        for (Pos2i pos : section) {
            int val = board.getPuzzleValue(pos.getX(), pos.getY());
            if (val == 0) val = board.getPlayerValue(pos);
            if (val < 1 || val > 9) {
                valid = false;
            } else {
                counts.merge(val, 1, Integer::sum);
            }
        }
        for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
            if (e.getValue() > 1) {
                valid = false;
                for (Pos2i pos : section) {
                    int val = board.getPuzzleValue(pos.getX(), pos.getY());
                    if (val == 0) val = board.getPlayerValue(pos);
                    if (val == e.getKey()) duplicates.add(pos);
                }
            }
        }
        if (valid && counts.size() == 9) {
            for (Pos2i pos : section) completed.add(pos);
        }
    }

    private static int borderVariant(int cx, int cz, int size) {
        int v = 0;
        if ((cx + 1) % 3 == 0) v |= BORDER_RIGHT;
        if ((cz + 1) % 3 == 0) v |= BORDER_BOTTOM;
        if (cx == 0) v |= BORDER_LEFT;
        if (cz == 0) v |= BORDER_TOP;
        return v;
    }

    private static void ensureBoardTextures() {
        if (boardTextures != null) return;

        boardTextures = new int[16];
        try {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(BOARD);
            BufferedImage base = javax.imageio.ImageIO.read(res.getInputStream());
            int w = base.getWidth();
            int h = base.getHeight();
            int borderPx = Math.max(1, w / 16);

            for (int variant = 0; variant < 16; variant++) {
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.drawImage(base, 0, 0, null);
                if (variant != 0) {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                    g.setColor(new Color(0.85f, 0.85f, 0.85f));
                    if ((variant & BORDER_RIGHT) != 0) g.fillRect(w - borderPx, 0, borderPx, h);
                    if ((variant & BORDER_BOTTOM) != 0) g.fillRect(0, h - borderPx, w, borderPx);
                    if ((variant & BORDER_LEFT) != 0) g.fillRect(0, 0, borderPx, h);
                    if ((variant & BORDER_TOP) != 0) g.fillRect(0, 0, w, borderPx);
                }
                g.dispose();
                boardTextures[variant] = uploadTexture(img);
            }
        } catch (IOException e) {
            LootGames.LOGGER.error("Failed to load board texture variants", e);
        }
    }

    private static int uploadTexture(BufferedImage img) {
        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        int w = img.getWidth();
        int h = img.getHeight();
        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);

        ByteBuffer buf = ByteBuffer.allocateDirect(w * h * 4).order(ByteOrder.nativeOrder());
        for (int px : pixels) {
            buf.put((byte) ((px >> 16) & 0xFF)); // R
            buf.put((byte) ((px >> 8) & 0xFF)); // G
            buf.put((byte) (px & 0xFF)); // B
            buf.put((byte) ((px >> 24) & 0xFF)); // A
        }
        buf.flip();

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        return id;
    }
}
