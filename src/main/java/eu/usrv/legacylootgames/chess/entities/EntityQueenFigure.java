
package eu.usrv.legacylootgames.chess.entities;


import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;


public class EntityQueenFigure extends EntityWitch implements IChessFigure {
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[]{
            new ResourceLocation("LootGames:textures/entity/queen_witch/black.png"),
            new ResourceLocation("LootGames:textures/entity/queen_witch/white.png")
    };

    private FiguresData _mFiguresData;

    public EntityQueenFigure(World pWorld) {
        super(pWorld);
        targetTasks.taskEntries.clear();
        tasks.taskEntries.clear();
        setAlwaysRenderNameTag(true);
        setCustomNameTag("Queen");
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
        _mFiguresData = new FiguresData(this, EntityQueenFigure.TEXTURES);
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