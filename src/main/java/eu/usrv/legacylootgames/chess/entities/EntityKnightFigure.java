
package eu.usrv.legacylootgames.chess.entities;


import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;


public class EntityKnightFigure extends EntityZombie implements IChessFigure {
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[]{
            new ResourceLocation("LootGames:textures/entity/knight_zombie/black.png"),
            new ResourceLocation("LootGames:textures/entity/knight_zombie/white.png")
    };

    private FiguresData _mFiguresData;

    public EntityKnightFigure(World pWorld) {
        super(pWorld);
        targetTasks.taskEntries.clear();
        tasks.taskEntries.clear();
        setAlwaysRenderNameTag(true);
        setCustomNameTag("Knight");
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
        _mFiguresData = new FiguresData(this, EntityKnightFigure.TEXTURES);
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