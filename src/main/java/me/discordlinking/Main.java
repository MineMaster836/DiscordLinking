package me.discordlinking;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.discordlinking.commands.*;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin { // First thing that gets run (its the main duh)

    private static Main instance;

    public static Main get() {
        return instance;
    }

    @Override
    public void onEnable() { // Runs when the server starts or reloads, it adds all commands and listeners and sends a server started or reloaded message
        instance = this;

        // Listener and Commands Loader
        new SpigotListeners(this);
        new DisableBotCommand(this);
        new EnableBotCommand(this);
        new ReloadCommand(this);
        new DiscordCommand(this);
        new DMCommand(this);

        // Start and reload Sender
        WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername("Server Status");
        builder.setAvatarUrl(DiscordBot.avatarURL);

        if (!isReloading("null")) {
            try {
                builder.setContent("**SERVER STARTED** :white_check_mark:");
                DiscordBot.client.getPresence().setActivity(Activity.watching(Bukkit.getServer().getOnlinePlayers().size() + "/"
                        + Bukkit.getServer().getMaxPlayers() + " Players"));
            } catch (Exception e) {
                // ignore mess ups
            }
        } else {
            try {
                builder.setContent("**RELOAD COMPLETE** :white_check_mark:");
                DiscordBot.client.getPresence().setActivity(Activity.watching(Bukkit.getServer().getOnlinePlayers().size() + "/"
                        + Bukkit.getServer().getMaxPlayers() + " Players"));
            } catch (Exception e) {
                // ignore mess ups
            }
            isReloading("false");
        }
        client.send(builder.build());
        client.close();
    }

    @Override
    public void onLoad() { // Waits until server is ready of functions, this will start the bot and set the bots status
        // Bot start up, and check if it started
        DiscordBot bot = new DiscordBot();
        try {
            bot.startup();
        } catch (LoginException e) {
            System.out.println("[DiscordLinking] The Bot has not logged in!");
            return;
        }
        if (DiscordBot.channelID == 0) {
            return;
        }
        // System.out.println("This is being loaded!");

        // Setting bots status
        DiscordBot.client.getPresence().setActivity(Activity.watching(Bukkit.getServer().getOnlinePlayers().size() + "/"
                + Bukkit.getServer().getMaxPlayers() + " Players"));
    }

    @Override
    public void onDisable() { /* When server stops or reloads, This will send a stop or reload message,
                              and "should" stop the bot without error, but its kinda broken only with reloading tho */

        // Sending stop / reload message
        WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername("Server Status");
        builder.setAvatarUrl(DiscordBot.avatarURL);

        if (!isReloading("null")) {
            try {
                builder.setContent("**SERVER STOPPED** :x:");
            } catch (Exception e) {
                // ignore mess ups
            }
        } else {
            try {
                builder.setContent("**SERVER RELOADING** :arrows_counterclockwise:");
            } catch (Exception e) {
                // ignore mess ups
            }
        }
        client.send(builder.build());
        client.close();

        // Stopping bot... kinda
        DiscordBot.client.shutdown();
    }

    public static boolean isReloading(String reloading) { // Checks if server was reloaded and is being reloaded
        switch (reloading) {
            // Checking if "serverReloading files exists
            case "null":
                return new File("serverReloading").exists();
            // Making new "serverReloading" file
            case "true":
                try {
                    new File("serverReloading").createNewFile();
                } catch (IOException error) {
                    //ignore mess-ups
                }
                return true;
            // Deleting old "serverReloading" file
            case "false":
                new File("serverReloading").delete();
                return true;
        }
        return false;
    }
}
