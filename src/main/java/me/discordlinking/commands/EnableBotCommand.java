package me.discordlinking.commands;

import me.discordlinking.DiscordBot;
import me.discordlinking.format.DiscordMessageFormat;
import me.discordlinking.utils.Formats;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EnableBotCommand implements CommandExecutor {

    public EnableBotCommand(JavaPlugin plugin) {
        Objects.requireNonNull(plugin.getCommand("enablebot")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!commandSender.hasPermission("discordlinking.enable")) {
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

        if (DiscordBot.botEnabled) {
            commandSender.sendMessage(Formats.ERROR + " bot is already enabled!");
            return false;
        }

        DiscordBot.botEnabled = true;
        Bukkit.broadcastMessage(Formats.SUCCESS + playerName + " enabled MC Chat!");
        String username = DiscordMessageFormat.Status.Enable.username(playerName);
        String content = DiscordMessageFormat.Status.Enable.message(playerName);
        DiscordMessageFormat.sendMessage(username, content);

        return true;
    }
}
