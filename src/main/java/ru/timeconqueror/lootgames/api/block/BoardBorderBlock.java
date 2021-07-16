package ru.timeconqueror.lootgames.api.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.BlockState;
import ru.timeconqueror.lootgames.utils.future.WorldExt;

import javax.annotation.ParametersAreNonnullByDefault;

//TODO textures
@ParametersAreNonnullByDefault
public class BoardBorderBlock extends GameBlock implements IGameField {
    public BoardBorderBlock() {
        super();
    }

    public static BlockPos getMasterPos(World world, BlockPos pos, BlockState oldState) {
        BlockPos.Mutable currentPos = pos.mutable();

        Block currentBlock = oldState.getBlock();
        int currentMeta = oldState.getMeta();
        int limit = 128;

        while (--limit >= 0) {
            if (!(currentBlock instanceof BoardBorderBlock)) {
                break;
            }


            Type type = Type.byMeta(currentMeta);

            switch (type) {
                case HORIZONTAL:
                case TOP_RIGHT:
                    currentPos.move(-1, 0, 0);
                    break;
                case BOTTOM_RIGHT:
                case BOTTOM_LEFT:
                case VERTICAL:
                    currentPos.move(0, 0, -1);
                    break;
            }

            currentBlock = WorldExt.getBlock(world, currentPos);
            currentMeta = WorldExt.getMeta(world, currentPos);
        }

        return currentPos.immutable();
    }

    private IIcon bottomLeft;
    private IIcon bottomRight;
    private IIcon topLeft;
    private IIcon topRight;
    private IIcon horizontal;
    private IIcon vertical;

    @Override
    public void registerIcons(IIconRegister reg) {
        bottomLeft = reg.registerIcon(LootGames.namespaced("border/bottom_left"));
        bottomRight = reg.registerIcon(LootGames.namespaced("border/bottom_right"));
        topLeft = reg.registerIcon(LootGames.namespaced("border/top_left"));
        topRight = reg.registerIcon(LootGames.namespaced("border/top_right"));
        horizontal = reg.registerIcon(LootGames.namespaced("border/horizontal"));
        vertical = reg.registerIcon(LootGames.namespaced("border/vertical"));
    }

    @Override
    public IIcon getIcon(int sideIn, int meta) {
        ForgeDirection side = ForgeDirection.getOrientation(sideIn);
        Type type = Type.byMeta(meta);

        if (type == Type.HORIZONTAL || side.offsetY == 0) {
            return horizontal;
        } else if (type == Type.VERTICAL) {
            return vertical;
        } else if (type == Type.TOP_LEFT) {
            return topLeft;
        } else if (type == Type.TOP_RIGHT) {
            return topRight;
        } else if (type == Type.BOTTOM_RIGHT) {
            return bottomRight;
        } else if (type == Type.BOTTOM_LEFT) {
            return bottomLeft;
        }

        return super.getIcon(sideIn, meta);
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        if (!worldIn.isRemote) {
            BlockPos pos = BlockPos.of(x, y, z);
            LootGamesAPI.getFieldManager().onFieldBlockBroken(worldIn, () -> getMasterPos(worldIn, pos, BlockState.of(blockBroken, meta)));
        }

        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
    }

    public enum Type {
        HORIZONTAL,
        VERTICAL,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT;

        private static final Type[] VALUES = values();

        public static Type byMeta(int meta) {
            if (meta >= VALUES.length)
                throw new IllegalArgumentException(String.format("Provided meta (%s) which is more than type storage size (%s)", meta, VALUES.length));
            return VALUES[meta];
        }
    }
}
