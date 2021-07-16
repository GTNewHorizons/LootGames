package eu.usrv.legacylootgames.command;


import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import eu.usrv.legacylootgames.LootGamesLegacy;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;


public class ProfilingCommand implements ICommand {
    private final List<String> aliases;

    public ProfilingCommand() {
        this.aliases = new ArrayList<>();
    }

    @Override
    public String getCommandName() {
        return "lootgamesprofiler";
    }

    @Override
    public String getCommandUsage(ICommandSender pCommandSender) {
        return "lootgamesprofiler";
    }

    @Override
    public List<String> getCommandAliases() {
        return this.aliases;
    }

    @Override
    public void processCommand(ICommandSender pCommandSender, String[] pArgs) {
        pCommandSender.addChatMessage(new ChatComponentText("Average generator times:"));

        for (String pID : LootGamesLegacy.Profiler.getUniqueItems()) {
            long tTime = LootGamesLegacy.Profiler.GetAverageTime(pID);
            String tInfo;
            if (tTime == -1)
                tInfo = "N/A";
            else
                tInfo = String.format("%d ms", tTime);
            pCommandSender.addChatMessage(new ChatComponentText(String.format("%s : %s", pID, tInfo)));

        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender pCommandSender) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
            return true;

        if (pCommandSender instanceof EntityPlayerMP) {
            EntityPlayerMP tEP = (EntityPlayerMP) pCommandSender;
            return MinecraftServer.getServer().getConfigurationManager().canSendCommands(tEP.getGameProfile());
        }
        return false;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
        return false;
    }
}
