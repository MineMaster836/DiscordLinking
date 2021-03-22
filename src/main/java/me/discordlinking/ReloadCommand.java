package me.discordlinking;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.Objects;

public class ReloadCommand implements CommandExecutor {
    private Main plugin;

    public ReloadCommand(JavaPlugin plugin){
        Objects.requireNonNull(plugin.getCommand("discordreload")).setExecutor(this);
        plugin = this.plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!commandSender.hasPermission("discordlinking.reload")) {
            commandSender.sendMessage(Formats.ERROR + " Sorry but you don't have permission to execute this command.");
            return false;
        }

        DiscordBot.client.shutdown();

        File file = new File("plugins" + File.separator + "DiscordLinking" + File.separator + "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        DiscordBot.botToken = config.getString("discordBot.token");
        DiscordBot.channelID = config.getLong("discordBot.channel");
        DiscordBot.webhookURL = config.getString("webhook.webhookURL");
        DiscordBot.avatarURL = config.getString("webhook.avatarURL");
        DiscordBot.showDeaths = config.getBoolean("options.showDeaths");
        DiscordBot.enableWynnApi = config.getBoolean("options.wynnApi");

        DiscordBot bot = new DiscordBot();
        try {
            bot.startup();
        } catch (LoginException e) {
            System.out.println("[DiscordLinking] The Bot has not logged in!");
            return false;
        }

        commandSender.sendMessage(Formats.SUCCESS + " reloaded config");
        return true;
    }
}
