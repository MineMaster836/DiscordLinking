package me.discordlinking.format;

import me.discordlinking.state.ChatLinkingPolicy;
import me.discordlinking.utils.Formats;
import org.bukkit.Bukkit;

public class MinecraftMessageFormat {
    public static void chatPolicyChange(String memberName, ChatLinkingPolicy newPolicy) {
        Bukkit.broadcastMessage(Formats.success(String.format("%s has changed the chat policy to %s", memberName, newPolicy.pretty())));
    }
}
