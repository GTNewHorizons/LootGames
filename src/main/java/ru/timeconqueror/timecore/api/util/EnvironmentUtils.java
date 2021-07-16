package ru.timeconqueror.timecore.api.util;

import net.minecraft.launchwrapper.Launch;

public class EnvironmentUtils {
    public static boolean isInDev() {
        return (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    }
}
