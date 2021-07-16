
package eu.usrv.legacylootgames.chess.entities;


import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;


public class EntityKingFigure extends EntityVillager implements IChessFigure {
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[]{
            new ResourceLocation("LootGames:textures/entity/king_villager/black.png"),
            new ResourceLocation("LootGames:textures/entity/king_villager/white.png")
    };

    private FiguresData _mFiguresData;

    public EntityKingFigure(World pWorld) {
        super(pWorld);
        targetTasks.taskEntries.clear();
        tasks.taskEntries.clear();
        setAlwaysRenderNameTag(true);
        setCustomNameTag("King");
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
        _mFiguresData = new FiguresData(this, EntityKingFigure.TEXTURES);
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