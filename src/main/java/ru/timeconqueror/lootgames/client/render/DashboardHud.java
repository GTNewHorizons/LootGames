package ru.timeconqueror.lootgames.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.dashboard.api.HudBounds;
import com.gtnewhorizons.dashboard.api.HudElement;
import com.gtnewhorizons.dashboard.api.HudRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Lets players move, resize and hide the minigame info in Dashboard's HUD editor, for Minesweeper and Sudoku alike. The
 * only class using Dashboard, so only call it while Dashboard is loaded.
 */
@SideOnly(Side.CLIENT)
public final class DashboardHud {

    private static final HudElement GAME_INFO = new HudElement("lootgames:game_info", DashboardHud::getBounds);

    private DashboardHud() {}

    public static void register() {
        GAME_INFO.setPreview(
                (screenWidth, screenHeight) -> HudRegistry
                        .render(GAME_INFO, () -> SudokuOverlayHandler.drawPanel(getSudokuText(12))));
        HudRegistry.register(GAME_INFO);
    }

    /** False means the player hid it, so skip drawing it and do not call {@link #endRender()}. */
    static boolean beginGameInfo(ScaledResolution resolution) {
        return HudRegistry.beginRender(GAME_INFO, resolution);
    }

    static void endRender() {
        HudRegistry.endRender();
    }

    /**
     * The icon and the panel next to it, as both overlays draw them in the top left, as wide as the wider panel with a
     * typical text. More Minesweeper games nearby add smaller rows below.
     */
    private static HudBounds getBounds(int screenWidth, int screenHeight) {
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        int sudokuPanel = font.getStringWidth(getSudokuText(81)) + 20;
        int minesweeperPanel = font.getStringWidth("x99") + 11;
        int width = (int) Math.ceil(15 * 1.5F + Math.max(sudokuPanel, minesweeperPanel));
        return new HudBounds(5, 5, width, 24);
    }

    private static String getSudokuText(int remaining) {
        return String.format(StatCollector.translateToLocal("msg.lootgames.sdk.remaining"), remaining);
    }
}
