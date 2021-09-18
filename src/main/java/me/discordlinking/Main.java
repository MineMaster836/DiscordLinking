package me.discordlinking;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.discordlinking.commands.*;
import me.discordlinking.format.DiscordMessageFormat;
import me.discordlinking.format.GameChangeEvent;
import me.discordlinking.state.BotState;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Main extends JavaPlugin { // First thing that gets run (its the main duh)

    private static Main instance;

    public static Main get() {
        return instance;
    }

    public static void log(Level level, String log) {
        get().getLogger().log(level, log);
    }

    @Override
    public void onLoad() { // Waits until server is ready of functions, this will start the bot and set the bots status
        instance = this;

        // Bot start up, and check if it started
        DiscordBot bot = new DiscordBot();
        try {
            bot.startup();
        } catch (LoginException e) {
            Main.get().getLogger().log(Level.INFO, "[DiscordLinking] The Bot has not logged in!");
            return;
        }
        if (DiscordBot.channelID == 0) {
            return;
        }

        // Setting bots status
        DiscordMessageFormat.setPresence(GameChangeEvent.ENABLE_SERVER);
    }

    @Override
    public void onEnable() { // Runs when the server starts or reloads, it adds all commands and listeners and sends a server started or reloaded message
        // load any persistent variables in BotState
        BotState.load();

        // Listener and Commands Loader
        new SpigotListeners(this);
        new DisableBotCommand(this);
        new EnableBotCommand(this);
        new ReloadCommand(this);
        new DiscordCommand(this);
        new DMCommand(this);
        // Start and reload Sender
        String username;
        String content;

        if (!isReloading("null")) {
            username = DiscordMessageFormat.Status.Start.username();
            content = DiscordMessageFormat.Status.Start.message();
        } else {
            isReloading("false");
            username = DiscordMessageFormat.Status.ReloadStart.username();
            content = DiscordMessageFormat.Status.ReloadStart.message();
        }
        DiscordMessageFormat.setPresence(GameChangeEvent.ENABLE_SERVER);
        DiscordMessageFormat.sendMessage(username, content, GameChangeEvent.ENABLE_SERVER);
    }


    @Override
    public void onDisable() { /* When server stops or reloads, This will send a stop or reload message,
                              and "should" stop the bot without error, but its kinda broken only with reloading tho */

        // Sending stop / reload message
        String username;
        String content;
        if (!isReloading("null")) {
            username = DiscordMessageFormat.Status.Stop.username();
            content = DiscordMessageFormat.Status.Stop.message();
        } else {
            username = DiscordMessageFormat.Status.ReloadStop.username();
            content = DiscordMessageFormat.Status.ReloadStop.message();
        }
        DiscordMessageFormat.sendMessage(username, content, GameChangeEvent.DISABLE_SERVER);
        // Stopping bot... kinda
        DiscordBot.client.shutdownNow();
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
