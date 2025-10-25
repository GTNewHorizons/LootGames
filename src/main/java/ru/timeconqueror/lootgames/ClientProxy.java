package ru.timeconqueror.lootgames;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import ru.timeconqueror.lootgames.client.ClientEventHandler;
import ru.timeconqueror.lootgames.client.IconLoader;
import ru.timeconqueror.lootgames.client.render.MSOverlayHandler;
import ru.timeconqueror.lootgames.client.render.SudokuOverlayHandler;
import ru.timeconqueror.lootgames.client.render.SudokuRenderer;
import ru.timeconqueror.lootgames.client.render.tile.GOLMasterRenderer;
import ru.timeconqueror.lootgames.client.render.tile.MSMasterRenderer;
import ru.timeconqueror.lootgames.common.block.tile.GOLMasterTile;
import ru.timeconqueror.lootgames.common.block.tile.MSMasterTile;
import ru.timeconqueror.lootgames.common.block.tile.SudokuTile;
import ru.timeconqueror.timecore.api.util.Hacks;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        MinecraftForge.EVENT_BUS.register(new IconLoader());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new MSOverlayHandler());
        MinecraftForge.EVENT_BUS.register(new SudokuOverlayHandler());

        ClientRegistry.bindTileEntitySpecialRenderer(GOLMasterTile.class, Hacks.safeCast(new GOLMasterRenderer()));
        ClientRegistry.bindTileEntitySpecialRenderer(MSMasterTile.class, Hacks.safeCast(new MSMasterRenderer()));
        ClientRegistry.bindTileEntitySpecialRenderer(SudokuTile.class, Hacks.safeCast(new SudokuRenderer()));
    }

    public static EntityPlayer player() {
        return Hacks.safeCast(Minecraft.getMinecraft().thePlayer);
    }

    public static World world() {
        return Hacks.safeCast(Minecraft.getMinecraft().theWorld);
    }
}
