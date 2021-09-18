package me.discordlinking.commands;

import me.discordlinking.DiscordBot;
import me.discordlinking.Main;
import me.discordlinking.state.BotState;
import me.discordlinking.state.ChatLinkingPolicy;
import me.discordlinking.utils.Formats;
import me.discordlinking.utils.PrettyStrings;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DiscordCommand implements CommandExecutor {
    private static final UUID MINEMASTER = UUID.fromString("c3b2053a-a871-464c-af58-a9bf3a272361");
    private static final UUID APPLEPTR16 = UUID.fromString("fee05d22-06b2-4479-9ca5-6fd4db985227");

    public DiscordCommand(JavaPlugin plugin) {
        PluginCommand command = plugin.getCommand("discord");
        if (command == null) {
            Main.log(Level.WARNING, "Could not find command 'discord'");
            return;
        }
        command.setExecutor(this);
        command.setTabCompleter(new TabCompleter() {
            @Nullable
            @Override
            public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
                List<String> tabCompletions = new ArrayList<>();
                tabCompletions.add("settoken");
                tabCompletions.add("setchan");
                tabCompletions.add("setwebhook");
                tabCompletions.add("avatar");
                for (ChatLinkingPolicy policy : ChatLinkingPolicy.values()) {
                    tabCompletions.add("policy " + PrettyStrings.uppercaseFirstWord(policy.name()));
                }
                String rest = String.join(" ", strings);
                int lengthOfArgsString = rest.length();
                return tabCompletions.stream()
                        .filter(tab -> tab.toLowerCase(Locale.ROOT).startsWith(rest.toLowerCase(Locale.ROOT)))
                        .map(tab -> {
                            StringBuilder currentWord = new StringBuilder();
                            int length = Math.min(tab.length() - 1, lengthOfArgsString);
                            for (int i = 0; i < length; i++) {
                                if (tab.charAt(i) == ' ') {
                                    currentWord = new StringBuilder();
                                } else {
                                    currentWord.append(tab.charAt(i));
                                }
                            }
                            return currentWord + tab.substring(length);
                        })
                        .collect(Collectors.toList());
            }
        });
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command string, @NotNull String s, @NotNull String[] args) {
        if (!commandSender.hasPermission("discordlinking.discord") &&
                !(commandSender instanceof Player player &&
                        (player.getUniqueId().equals(MINEMASTER) ||
                                player.getUniqueId().equals(APPLEPTR16)))
        ) {
            commandSender.sendMessage(Formats.ERROR + " invalid permission");
            return false;
        }


        if (args.length < 1) {
            commandSender.sendMessage(Formats.ERROR + " syntax error");
            return false;
        }

        try {

            if (args[0].equalsIgnoreCase("settoken") && args.length == 2) {
                DiscordBot.client.shutdown();

                File file = new File("plugins" + File.separator + "DiscordLinking" + File.separator + "config.yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                config.set("discordBot.token", args[1]);
                config.save(file);
                DiscordBot.botToken = config.getString("discordBot.token");
                DiscordBot bot = new DiscordBot();
                try {
                    bot.startup();
                } catch (LoginException e) {
                    Main.log(Level.INFO, "[DiscordLinking] The Bot has not logged in!");
                    return false;
                }
                commandSender.sendMessage(Formats.SUCCESS + " set bot token");

            } else if (args[0].equalsIgnoreCase("setchan") && args.length == 2) {
                DiscordBot.client.shutdown();

                File file = new File("plugins" + File.separator + "DiscordLinking" + File.separator + "config.yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                long channelId = 0;
                try {
                    channelId = Long.parseLong(args[1]);
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(Formats.ERROR + String.format("'%s' is an invalid channel id", args[1]));
                }
                config.set("discordBot.channel", channelId);
                config.save(file);
                DiscordBot.channelID = config.getLong("discordBot.channel");
                DiscordBot bot = new DiscordBot();
                try {
                    bot.startup();
                } catch (LoginException e) {
                    Main.log(Level.INFO, "[DiscordLinking] The Bot has not logged in!");
                    return false;
                }
                commandSender.sendMessage(Formats.SUCCESS + " set channel");
            } else if (args[0].equalsIgnoreCase("setwebhook") && args.length == 2) {
                DiscordBot.client.shutdown();

                File file = new File("plugins" + File.separator + "DiscordLinking" + File.separator + "config.yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                config.set("webhook.webhookURL", args[1]);
                config.save(file);
                DiscordBot.webhookURL = config.getString("webhook.webhookURL");
                DiscordBot bot = new DiscordBot();
                try {
                    bot.startup();
                } catch (LoginException e) {
                    Main.log(Level.WARNING, "[DiscordLinking] The Bot has not logged in!");
                    return false;
                }
                commandSender.sendMessage(Formats.SUCCESS + " set webhookURL");
            } else if (args[0].equalsIgnoreCase("avatar") && args.length == 2) {
                DiscordBot.client.shutdown();

                File file = new File("plugins" + File.separator + "DiscordLinking" + File.separator + "config.yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                config.set("webhook.avatarURL", args[1]);
                config.save(file);
                DiscordBot.avatarURL = config.getString("webhook.avatarURL");
                DiscordBot bot = new DiscordBot();
                try {
                    bot.startup();
                } catch (LoginException e) {
                    Main.log(Level.INFO, "The Bot has not logged in!");
                    return false;
                }
                commandSender.sendMessage(Formats.SUCCESS + " set webhookURL");
            } else if (args[0].equalsIgnoreCase("policy") && args.length == 2) {
                try {
                    BotState.setChatPolicy(ChatLinkingPolicy.valueOf(args[1].toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    commandSender.sendMessage(Formats.error(String.format("There is no policy named '%s'", args[1])));
                }
            } else {
                commandSender.sendMessage(Formats.ERROR + " syntax error");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
