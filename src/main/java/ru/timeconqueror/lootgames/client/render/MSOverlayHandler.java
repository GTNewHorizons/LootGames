package ru.timeconqueror.lootgames.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.common.block.tile.MSMasterTile;
import ru.timeconqueror.lootgames.minigame.minesweeper.GameMineSweeper;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.timecore.api.util.MathUtils;
import ru.timeconqueror.timecore.api.util.client.ClientProxy;
import ru.timeconqueror.timecore.api.util.client.DrawHelper;
import ru.timeconqueror.timecore.api.util.client.DrawHelper.TexturedRect;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MSOverlayHandler {
    private static final ArrayList<WeakReference<MSMasterTile>> MS_MASTERS = new ArrayList<>(1);

    private static final TexturedRect FIRST_SLOT_START = new TexturedRect(3 * 1.5F, 16 * 1.5F, 15, 0, 3, 16);
    private static final TexturedRect FIRST_SLOT_REPEAT = new TexturedRect(26 * 1.5F, 16 * 1.5F, 18, 0, 26, 16);
    private static final TexturedRect FIRST_SLOT_END = new TexturedRect(4 * 1.5F, 16 * 1.5F, 44, 0, 4, 16);

    private static final TexturedRect EXTRA_SLOT_START = new TexturedRect(3 * 1.5F, 10 * 1.5F, 15, 16, 3, 10);
    private static final TexturedRect EXTRA_SLOT_REPEAT = new TexturedRect(26 * 1.5F, 10 * 1.5F, 18, 16, 26, 10);
    private static final TexturedRect EXTRA_SLOT_END = new TexturedRect(4 * 1.5F, 10 * 1.5F, 44, 16, 4, 10);

    public static final ResourceLocation OVERLAY = LootGames.rl("textures/gui/minesweeper/ms_overlay.png");

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
            renderNearbyGameBombs();
            MS_MASTERS.clear();
        }
    }

    private static void renderNearbyGameBombs() {
        EntityPlayer player = ClientProxy.player();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        List<MSMasterTile> masters = new ArrayList<>(1);
        Iterator<WeakReference<MSMasterTile>> iterator = MS_MASTERS.iterator();
        while (iterator.hasNext()) {
            MSMasterTile master = iterator.next().get();

            if (master == null) {
                iterator.remove();
                continue;
            }

            GameMineSweeper game = master.getGame();
            BlockPos gamePos = game.getGameCenter();

            if (MathUtils.distSqr(gamePos, player) > game.getBroadcastDistance() * game.getBroadcastDistance()) {
                iterator.remove();
                continue;
            }

            masters.add(master);
        }

        if (masters.isEmpty()) return;

        boolean extendedInfo = masters.size() > 1;

        float maxRectWidth = 0;
        for (MSMasterTile msMaster : masters) {
            GameMineSweeper game = msMaster.getGame();
            String toDisplay = getBombDisplayString(game, extendedInfo);

            maxRectWidth = Math.max(maxRectWidth, fontRenderer.getStringWidth(toDisplay) + 5.5F * 2);
        }

        float startY = 20;

        GL11.glColor4f(1, 1, 1, 1);

        for (int i = 0; i < masters.size(); i++) {
            MSMasterTile msMaster = masters.get(i);
            GameMineSweeper game = msMaster.getGame();

            Color color = game.getStage() instanceof GameMineSweeper.StageDetonating || game.getStage() instanceof GameMineSweeper.StageExploding ? Color.RED : Color.WHITE;
            String toDisplay = getBombDisplayString(game, extendedInfo);

            float finalMaxRectWidth = maxRectWidth;
            if (i == 0) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(OVERLAY);
                DrawHelper.drawTexturedRectByParts(5, 5, 15 * 1.5F, 16 * 1.5F, 0, 0, 0, 15, 16, 48);
                DrawHelper.drawWidthExpandableTexturedRect(5 + 15 * 1.5F, 5, finalMaxRectWidth, 0, FIRST_SLOT_START, FIRST_SLOT_REPEAT, FIRST_SLOT_END, 48);

                DrawHelper.drawYCenteredStringWithShadow(fontRenderer, toDisplay, 33, 17, color.getRGB());
            } else {
                float finalStartY = startY;
                Minecraft.getMinecraft().getTextureManager().bindTexture(OVERLAY);
                DrawHelper.drawWidthExpandableTexturedRect(27.5F, finalStartY, finalMaxRectWidth, 0, EXTRA_SLOT_START, EXTRA_SLOT_REPEAT, EXTRA_SLOT_END, 48);

                DrawHelper.drawYCenteredStringWithShadow(fontRenderer, toDisplay, 33, (int) (startY + 8), color.getRGB());
                startY += 7 * 1.5F;
            }
        }
    }

    private static String getBombDisplayString(GameMineSweeper game, boolean extended) {
        int bombDisplay = game.getStage() instanceof GameMineSweeper.StageDetonating || game.getStage() instanceof GameMineSweeper.StageExploding ? game.getBoard().getBombCount() :
                game.getBoard().getBombCount() - game.getBoard().cGetFlaggedField();
        BlockPos gamePos = game.getGameCenter();
        return extended ? "x" + bombDisplay + " on "/*todo translate */ + gamePos.getX() + ", " + gamePos.getY() + ", " + gamePos.getZ() : "x" + bombDisplay;
    }

    public static void addSupportedMaster(MSMasterTile master) {
        if (!Minecraft.getMinecraft().gameSettings.hideGUI || Minecraft.getMinecraft().currentScreen != null) {
            MS_MASTERS.add(new WeakReference<>(master));
        }
    }
}