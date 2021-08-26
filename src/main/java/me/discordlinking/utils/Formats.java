package me.discordlinking.utils;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Formats { // All the formats for commands to players
    public static final String SUCCESS = ChatColor.translateAlternateColorCodes('&', "&3DiscordLinking &8- &f");
    public static final String ERROR = ChatColor.translateAlternateColorCodes('&', "&3DiscordLinking &8- &c");

    @NotNull
    public static String getAvatarFromUUID(UUID uniqueId) { // Gets the player avatar for the webhook
        return "https://crafatar.com/avatars/playerUUID?overlay".replace("playerUUID", uniqueId.toString().replaceAll("-", ""));
    }

    public static TextComponent getDiscordToServerMessage(@NotNull String message, String prefix, Member member, Role userRole, String userColourHex, User author) { // Make the message a text component so discord_dm can be a thing

        String s =  ChatColor.translateAlternateColorCodes('&', String.format("%s &7| %s &r%s &8>> &r%s",
                ChatColor.of("#8EA0E1") + "Discord" + (prefix == null ? "" : " " + prefix),
                userRole == null ? "" : userColourHex == null ? "" : ChatColor.of(userColourHex) + "" + ChatColor.BOLD + userRole.getName(),
                member == null ? (author == null ? "" : author.getName()) :(userColourHex == null ? "" : ChatColor.of(userColourHex)) + member.getEffectiveName(),
                message));
        TextComponent t = new TextComponent();
        for(BaseComponent old:TextComponent.fromLegacyText(s)){
            t.addExtra(old);
        }return t;
    }
}
