
package eu.usrv.legacylootgames.chess.entities;


import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import ru.timeconqueror.lootgames.LootGames;


public class FiguresData {
    private final EntityLiving _mEntity;
    private ResourceLocation[] _mTextures;
    private boolean _mUpdateTextures;
    private float _mRenderScale;
    private boolean _mIsWhite;

    public FiguresData(EntityLiving pEntity, ResourceLocation... pBaseTextures) {
        _mEntity = pEntity;
        _mTextures = pBaseTextures;
    }

    public void setIsWhite(boolean pFlag) {
        _mIsWhite = pFlag;
    }

    public boolean isWhite() {
        return _mIsWhite;
    }

    public float getRenderScale() {
        return _mRenderScale;
    }

    public void setRenderScale(float pScale) {
        _mRenderScale = pScale;
    }

    /**
     * @return The number of textures for the entity.
     */
    public int getTextureCount() {
        return _mTextures.length;
    }

    /**
     * @return The texture for the entity.
     */
    public ResourceLocation getTexture() {
        return _mTextures[0];
    }

    /**
     * @param index The index of the texture to get.
     * @return The texture for the entity with a specific index.
     */
    public ResourceLocation getTexture(int index) {
        return _mTextures[index];
    }

    /**
     * @return All the textures for the entity.
     */
    public ResourceLocation[] getTextures() {
        return _mTextures;
    }

    /**
     * @param tex The new texture(s) to set for the entity.
     */
    public void setTextures(ResourceLocation... tex) {
        _mTextures = tex;
    }

    /**
     * @param tex The new texture(s) to load for the entity. Called when loaded from NBT or packet.
     */
    public void loadTextures(String... tex) {
        try {
            ResourceLocation[] newTextures = new ResourceLocation[_mTextures.length];
            for (int i = newTextures.length; i-- > 0; ) {
                if (!_mTextures[i].toString().equals(tex[i])) {
                    this._mUpdateTextures = true;
                    newTextures[i] = new ResourceLocation(tex[i]);
                } else {
                    newTextures[i] = _mTextures[i];
                }
            }
            if (this._mUpdateTextures) {
                this.setTextures(newTextures);
            }
        } catch (Exception ex) {
            LootGames.LOGGER.error("Failed to load textures");
        }
    }
}
