package ru.timeconqueror.lootgames.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import ru.timeconqueror.lootgames.LootGames;

public class IconLoader {
    public static IIcon shieldedDungeonFloor;

    @SubscribeEvent
    public void regIcons(TextureStitchEvent.Pre event) {
        TextureMap reg = event.map;
        if (reg.getTextureType() == 0) {// are for blocks
            shieldedDungeonFloor = reg.registerIcon(LootGames.namespaced("shielded_dungeon_floor"));
        }
    }
}
