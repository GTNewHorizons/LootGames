
package eu.usrv.legacylootgames.chess.entities;


import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;


public class EntityPawnFigure extends EntitySilverfish implements IChessFigure {
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[]{
            new ResourceLocation("LootGames:textures/entity/pawn_silverfish/black.png"),
            new ResourceLocation("LootGames:textures/entity/pawn_silverfish/white.png")
    };

    private FiguresData _mFiguresData;

    public EntityPawnFigure(World pWorld) {
        super(pWorld);
        targetTasks.taskEntries.clear();
        tasks.taskEntries.clear();
        setAlwaysRenderNameTag(true);
        setCustomNameTag("Pawn");
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
        _mFiguresData = new FiguresData(this, EntityPawnFigure.TEXTURES);
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