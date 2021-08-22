package ru.timeconqueror.lootgames;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.relauncher.Side;
import eu.usrv.legacylootgames.LootGamesLegacy;
import eu.usrv.legacylootgames.command.LootGamesCommand;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.timeconqueror.lootgames.api.minigame.FieldManager;
import ru.timeconqueror.lootgames.api.minigame.GameManager;
import ru.timeconqueror.lootgames.client.ClientEventHandler;
import ru.timeconqueror.lootgames.client.IconLoader;
import ru.timeconqueror.lootgames.client.render.MSOverlayHandler;
import ru.timeconqueror.lootgames.client.render.tile.GOLMasterRenderer;
import ru.timeconqueror.lootgames.client.render.tile.MSMasterRenderer;
import ru.timeconqueror.lootgames.common.block.tile.GOLMasterTile;
import ru.timeconqueror.lootgames.common.block.tile.MSMasterTile;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.lootgames.common.packet.LGNetwork;
import ru.timeconqueror.lootgames.registry.LGAchievements;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.registry.LGGamePackets;
import ru.timeconqueror.lootgames.registry.LGGames;
import ru.timeconqueror.timecore.api.common.CommonEventHandler;
import ru.timeconqueror.timecore.api.common.config.Config;
import ru.timeconqueror.timecore.api.util.Hacks;

@Mod(modid = LootGames.MODID,
        dependencies = "required-after:Forge@[10.13.4.1614,);" + "required-after:YAMCore@[0.5.76,);",
        name = LootGames.MODNAME,
        version = LootGames.VERSION,
        certificateFingerprint = "1cca375192a26693475fb48268f350a462208dce")
public class LootGames {
    public static final String MODID = "lootgames";
    public static final String MODNAME = "LootGames";
    public static final String VERSION = "GRADLETOKEN_VERSION";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(MODNAME) {
        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(LGBlocks.PUZZLE_MASTER);
        }
    };

    @Mod.Instance(LootGames.MODID)
    public static LootGames INSTANCE;

    public static final GameManager gameManager = new GameManager();
    public static FieldManager fieldManager = new FieldManager();

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            MinecraftForge.EVENT_BUS.register(new IconLoader());
            MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
            MinecraftForge.EVENT_BUS.register(new MSOverlayHandler());
        }

        MinecraftForge.EVENT_BUS.register(new CommonEventHandler());

        Config.setConfigDir(event.getModConfigurationDirectory());

        LGConfigs.load();
        LGBlocks.register();

        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            ClientRegistry.bindTileEntitySpecialRenderer(GOLMasterTile.class, Hacks.safeCast(new GOLMasterRenderer()));
            ClientRegistry.bindTileEntitySpecialRenderer(MSMasterTile.class, Hacks.safeCast(new MSMasterRenderer()));
        }

        LootGamesLegacy.PreLoad(event);

        LegacyMigrator.onPreInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LGNetwork.init();
        LGGames.register();
        LGGamePackets.register();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LGAchievements.init();
    }

    @Mod.EventHandler
    public static void onComplete(FMLLoadCompleteEvent event) {
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        LootGamesLegacy.serverLoad(event);
        event.registerServerCommand(new LootGamesCommand());
    }

    public static String namespaced(String name) {
        return LootGames.MODID + ":" + name;
    }

    public static String dotted(String name) {
        return LootGames.MODID + "." + name;
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }
}
