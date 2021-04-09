package me.discordlinking;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.discordlinking.commands.DisableBotCommand;
import me.discordlinking.commands.DiscordCommand;
import me.discordlinking.commands.EnableBotCommand;
import me.discordlinking.commands.ReloadCommand;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin {

    private static Main instance;

    public static Main get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        new SpigotListeners(this);
        new DisableBotCommand(this);
        new EnableBotCommand(this);
        new ReloadCommand(this);
        new DiscordCommand(this);

        WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername("Server >> Status");
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
    public void onLoad() {
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
        System.out.println("This is being loaded!");

        DiscordBot.client.getPresence().setActivity(Activity.watching(Bukkit.getServer().getOnlinePlayers().size() + "/"
                + Bukkit.getServer().getMaxPlayers() + " Players"));
    }

    @Override
    public void onDisable() {
        WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername("Server >> Status");
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
        DiscordBot.client.shutdown();
    }

    public static boolean isReloading(String reloading) {
        switch (reloading) {
            case "null":
                return new File("serverReloading").exists();
            case "true":
                try {
                    new File("serverReloading").createNewFile();
                } catch (IOException error) {
                    //ignore mess-ups
                }
                return true;
            case "false":
                new File("serverReloading").delete();
                return true;
        }
        return false;
    }
}
