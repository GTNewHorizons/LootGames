package ru.timeconqueror.lootgames.utils.future;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ChatComponentExt {
    public static <T extends IChatComponent> T withStyle(T component, EnumChatFormatting color) {
        component.setChatStyle(component.getChatStyle().setColor(color));
        return component;
    }
}
