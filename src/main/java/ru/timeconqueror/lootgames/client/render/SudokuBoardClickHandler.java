package ru.timeconqueror.lootgames.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.timeconqueror.lootgames.api.block.SmartSubordinateBlock;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.common.block.tile.SudokuTile;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.CPSudokuEndGameCheck;
import ru.timeconqueror.lootgames.common.packet.game.sudoku.CPSudokuToggleNote;
import ru.timeconqueror.lootgames.minigame.sudoku.GameSudoku;
import ru.timeconqueror.lootgames.utils.future.BlockPos;

@SideOnly(Side.CLIENT)
public class SudokuBoardClickHandler {

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (event.button != 0 || !event.buttonstate) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null || mc.theWorld == null || mc.thePlayer == null) return;

        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
        if (mop.sideHit != 1) return;

        World world = mc.theWorld;
        Block block = world.getBlock(mop.blockX, mop.blockY, mop.blockZ);
        if (!(block instanceof SmartSubordinateBlock)) return;

        BlockPos clickedPos = BlockPos.of(mop.blockX, mop.blockY, mop.blockZ);
        BlockPos masterPos = SmartSubordinateBlock.getMasterPos(world, clickedPos);
        TileEntity te = world.getTileEntity(masterPos.getX(), masterPos.getY(), masterPos.getZ());
        if (!(te instanceof SudokuTile)) return;

        GameSudoku game = ((SudokuTile) te).getGame();
        Pos2i cellPos = game.convertToGamePos(clickedPos);
        EntityPlayer player = mc.thePlayer;

        if (player.isSneaking()) {
            game.sendFeedbackPacket(new CPSudokuEndGameCheck(cellPos));
        } else {
            double subX = mop.hitVec.xCoord - Math.floor(mop.hitVec.xCoord);
            double subZ = mop.hitVec.zCoord - Math.floor(mop.hitVec.zCoord);
            // clamp to [0, 2] so a hit exactly on the cell edge (sub == 1.0) doesn't overflow the 1-9 range
            int col = Math.min(2, (int) (subX * 3));
            int row = Math.min(2, (int) (subZ * 3));
            int digit = row * 3 + col + 1;
            game.sendFeedbackPacket(new CPSudokuToggleNote(cellPos, digit));
        }

        event.setCanceled(true);
    }
}
