
package eu.usrv.legacylootgames.chess.entities;


import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;


public class EntityRookFigure extends EntityIronGolem implements IChessFigure {
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[]{
            new ResourceLocation("LootGames:textures/entity/rook_golem/black.png"),
            new ResourceLocation("LootGames:textures/entity/rook_golem/white.png")
    };

    private FiguresData _mFiguresData;

    public EntityRookFigure(World pWorld) {
        super(pWorld);
        targetTasks.taskEntries.clear();
        tasks.taskEntries.clear();
        setAlwaysRenderNameTag(true);
        setCustomNameTag("Rook");
    }

    @Override
    public FiguresData getFiguresData() {
        return _mFiguresData;
    }

    @Override
    public boolean isEntityInvulnerable() {
        return true;
    }

    @Override
    protected void entityInit() {
        _mFiguresData = new FiguresData(this, EntityRookFigure.TEXTURES);
        super.entityInit();
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }
}