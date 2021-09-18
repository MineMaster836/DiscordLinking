package me.discordlinking.commands;

import me.discordlinking.DiscordBot;
import me.discordlinking.format.DiscordMessageFormat;
import me.discordlinking.format.GameChangeEvent;
import me.discordlinking.format.MinecraftMessageFormat;
import me.discordlinking.state.BotState;
import me.discordlinking.state.ChatLinkingPolicy;
import me.discordlinking.utils.Formats;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DisableBotCommand implements CommandExecutor {

    public DisableBotCommand(JavaPlugin plugin) {
        Objects.requireNonNull(plugin.getCommand("disablebot")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!commandSender.hasPermission("discordlinking.disable")) {
            commandSender.sendMessage(Formats.ERROR + " Sorry but you don't have permission to execute this command.");
            return false;
        }

        String playerName;
        Player player = Bukkit.getPlayer(commandSender.getName());
        if (player == null) {
            playerName = DiscordMessageFormat.Status.Misc.botname();
        } else {
            playerName = player.getName();
        }

        if (BotState.getChatPolicy() == ChatLinkingPolicy.NONE) {
            commandSender.sendMessage(Formats.ERROR + " bot is already disabled");
            return false;
        }

        BotState.setChatPolicy(ChatLinkingPolicy.NONE);
        MinecraftMessageFormat.chatPolicyChange(playerName, ChatLinkingPolicy.NONE);
        String username = DiscordMessageFormat.Status.Disable.username(playerName);
        String content = DiscordMessageFormat.Status.Disable.message(playerName);
        DiscordMessageFormat.sendMessage(username, content, GameChangeEvent.CHANGE_LINKING_POLICY);

        return true;
    }
}
