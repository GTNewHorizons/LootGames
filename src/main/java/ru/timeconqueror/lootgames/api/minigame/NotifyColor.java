package ru.timeconqueror.lootgames.api.minigame;

import net.minecraft.util.EnumChatFormatting;

/**
 * Default notify colors.
 */
public enum NotifyColor {
    SUCCESS(EnumChatFormatting.GREEN),
    WARN(EnumChatFormatting.YELLOW),
    NOTIFY(EnumChatFormatting.AQUA),
    FAIL(EnumChatFormatting.DARK_PURPLE),
    GRAVE_NOTIFY(EnumChatFormatting.RED);

    private final EnumChatFormatting color;

    NotifyColor(EnumChatFormatting color) {
        this.color = color;
    }

    public EnumChatFormatting getColor() {
        return color;
    }
}
