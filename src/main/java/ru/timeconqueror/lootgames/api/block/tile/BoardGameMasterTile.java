package ru.timeconqueror.lootgames.api.block.tile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;
import ru.timeconqueror.lootgames.api.minigame.BoardLootGame;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.utils.MouseClickType;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.Vector3i;

public class BoardGameMasterTile<T extends BoardLootGame<T>> extends GameMasterTile<T> {
    public BoardGameMasterTile(T game) {
        super(game);
    }

    @Override
    public void onBlockLeftClick(EntityPlayer player, BlockPos subordinatePos) {
        super.onBlockLeftClick(player, subordinatePos);
        onClick(player, subordinatePos, MouseClickType.LEFT);
    }

    @Override
    public void onBlockRightClick(EntityPlayer player, BlockPos subordinatePos) {
        super.onBlockRightClick(player, subordinatePos);
        onClick(player, subordinatePos, MouseClickType.RIGHT);
    }

    private void onClick(EntityPlayer player, BlockPos subordinatePos, MouseClickType type) {
        Pos2i pos = game.convertToGamePos(subordinatePos);

        int size = game.getCurrentBoardSize();
        if (pos.getX() >= 0 && pos.getX() < size
                && pos.getY() >= 0 && pos.getY() < size) {
            game.onClick(player, pos, type);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void prepareMatrix(BoardGameMasterTile<?> master) {
        BlockPos boardOrigin = master.getGame().getBoardOrigin();
        Vector3i originOffset = boardOrigin.subtract(master.getBlockPos()).offset(0, 1, 0);

        GL11.glTranslatef(originOffset.getX(), originOffset.getY(), originOffset.getZ());
        GL11.glRotatef(90, 1, 0, 0);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }
}
