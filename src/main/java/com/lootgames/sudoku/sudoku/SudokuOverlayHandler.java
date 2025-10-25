package com.lootgames.sudoku.sudoku;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ru.timeconqueror.lootgames.ClientProxy;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.timecore.api.util.client.DrawHelper;

public class SudokuOverlayHandler {

    public static final Map<BlockPos, WeakReference<GameSudoku>> ACTIVE_GAMES = Maps.newHashMapWithExpectedSize(1);

    public static final ResourceLocation OVERLAY = LootGames.rl("textures/gui/sudoku/sdk_overlay.png");

    public static void addSupportedMaster(BlockPos pos, GameSudoku game) {
        if (!Minecraft.getMinecraft().gameSettings.hideGUI || Minecraft.getMinecraft().currentScreen != null) {
            ACTIVE_GAMES.put(pos, new WeakReference<>(game));
        }
    }

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) return;

        EntityPlayer player = ClientProxy.player();
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        List<GameSudoku> visible = new ArrayList<>();

        Iterator<Map.Entry<BlockPos, WeakReference<GameSudoku>>> it = ACTIVE_GAMES.entrySet()
            .iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, WeakReference<GameSudoku>> e = it.next();
            GameSudoku game = e.getValue()
                .get();
            if (game == null) {
                it.remove();
                continue;
            }
            BlockPos gamePos = game.getGameCenter();
            if (game.getBroadcastDistance() * game.getBroadcastDistance()
                < player.getDistanceSq(gamePos.getX(), gamePos.getY(), gamePos.getZ())) {
                it.remove();
                continue;
            }
            visible.add(game);
        }

        if (visible.isEmpty()) return;

        // Only show one game's remaining blanks
        GameSudoku game = visible.get(0);
        int totalBlanks = game.getBoard()
            .countTotalBlanks();
        int filled = game.getBoard()
            .countFilledCells();
        int remaining = totalBlanks - filled;
        long lastTime = game.getBoard()
            .getLastClickTime() >= 0
                ? (player.worldObj.getTotalWorldTime() - game.getBoard()
                    .getLastClickTime()) / 20
                : 0;

        String textRemaining = String.format(StatCollector.translateToLocal("msg.lootgames.sdk.renaming"), remaining);
        String textLastTime = String
            .format(StatCollector.translateToLocal("msg.lootgames.sdk.last_click_time"), lastTime);
        int widthRemaining = font.getStringWidth(textRemaining);
        int widthLastTime = font.getStringWidth(textLastTime);

        GL11.glPushMatrix();
        GL11.glColor4f(1f, 1f, 1f, 1f);

        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(OVERLAY);

        DrawHelper.drawTexturedRectByParts(5, 5, 15 * 1.5F, 16 * 1.5F, 0, 0, 0, 15, 16, 48);
        DrawHelper.drawWidthExpandableTexturedRect(
            5 + 15 * 1.5F,
            5,
            widthRemaining + widthLastTime + 20,
            0,
            FIRST_SLOT_START,
            FIRST_SLOT_REPEAT,
            FIRST_SLOT_END,
            48);

        DrawHelper.drawStringWithShadow(Minecraft.getMinecraft().fontRenderer, textRemaining, 33, 13, 0xFFFFFF);
        DrawHelper.drawStringWithShadow(
            Minecraft.getMinecraft().fontRenderer,
            textLastTime,
            widthRemaining + 40,
            13,
            0xFFFFFF);

        GL11.glPopMatrix();

        ACTIVE_GAMES.clear();
    }

    public static DrawHelper.TexturedRect FIRST_SLOT_START = new DrawHelper.TexturedRect(
        3 * 1.5F,
        16 * 1.5F,
        15,
        0,
        3,
        16);
    public static DrawHelper.TexturedRect FIRST_SLOT_REPEAT = new DrawHelper.TexturedRect(
        26 * 1.5F,
        16 * 1.5F,
        18,
        0,
        26,
        16);
    public static DrawHelper.TexturedRect FIRST_SLOT_END = new DrawHelper.TexturedRect(
        4 * 1.5F,
        16 * 1.5F,
        44,
        0,
        4,
        16);
}
