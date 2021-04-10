package me.discordlinking.commands;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.discordlinking.DiscordBot;
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

        String playerName = "null";
        Player player = Bukkit.getPlayer(commandSender.getName());
        if (player == null) {
            playerName = "Server";
        } else {
            playerName = player.getName();
        }

        if (!DiscordBot.botEnabled) {
            commandSender.sendMessage(Formats.ERROR + " bot is already disabled");
            return false;
        }

        DiscordBot.botEnabled = false;
        Bukkit.broadcastMessage(Formats.SUCCESS + playerName + " disabled MC Chat!");
        WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername("Server");
        builder.setAvatarUrl(DiscordBot.avatarURL);
        builder.setContent(playerName + " disabled MC Chat!");
        client.send(builder.build());
        client.close();

        return true;
    }
}
