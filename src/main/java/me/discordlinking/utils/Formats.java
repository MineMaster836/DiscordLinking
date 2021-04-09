package me.discordlinking.utils;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Formats {
    public static final String SUCCESS = ChatColor.translateAlternateColorCodes('&', "&3DiscordLinking &8- &f");
    public static final String ERROR = ChatColor.translateAlternateColorCodes('&', "&3DiscordLinking &8- &c");

    @NotNull
    public static String getAvatarFromUUID(UUID uniqueId) {
        return "https://crafatar.com/avatars/playerUUID?overlay".replace("playerUUID", uniqueId.toString().replaceAll("-", ""));
    }
}
