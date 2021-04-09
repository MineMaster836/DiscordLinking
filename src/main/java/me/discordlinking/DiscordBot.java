package me.discordlinking;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.bed0.jWynn.WynncraftAPI;
import me.bed0.jWynn.api.v1.guild.WynncraftGuild;
import me.bed0.jWynn.api.v1.guild.WynncraftGuildMember;
import me.discordlinking.utils.Formats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class DiscordBot extends ListenerAdapter {
    public static JDA client;
    public static String botToken;
    public static long channelID;
    public static boolean botEnabled;
    public static String webhookURL;
    public static String avatarURL;
    public static boolean showDeaths;
    public static boolean enableWynnApi;
    public static int MAX_MESSAGE_LENGTH = 2000;
    public static int chatColor;

    public DiscordBot() {
        File file = new File("plugins" + File.separator + "DiscordLinking" + File.separator + "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        botToken = config.getString("discordBot.token");
        channelID = config.getLong("discordBot.channel");
        webhookURL = config.getString("webhook.webhookURL");
        avatarURL = config.getString("webhook.avatarURL");
        showDeaths = config.getBoolean("options.showDeaths");
        enableWynnApi = config.getBoolean("options.wynnApi");
        chatColor = config.getInt("discordBot.color");
    }

    public void startup() throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(botToken);
        builder.addEventListeners(this);
        client = builder.build();
        botEnabled = true;
    }

    public static boolean sendMessage;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        sendMessage = true;
        if (event.getAuthor().isBot() || event.getChannel().getIdLong() != channelID) return;
        String[] args = Arrays.copyOfRange(event.getMessage().getContentRaw().split(" "), 1, event.getMessage().getContentRaw().split(" ").length);

        if (event.getMessage().getContentStripped().contains((">list").toLowerCase()) && event.getMessage().getContentRaw().charAt(0) == '>') {
            sendMessage = false;
            ArrayList<Player> playerList = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
            int page = 0;
            if (args.length > 0 && args[0].chars().allMatch(Character::isDigit) && Integer.parseInt(args[0]) > 0) {
                page = Integer.parseInt(args[0]);
            }
            int pageOffset = page * 10;
            String playerMessage = "";
            for (int i = pageOffset; i < 10 + pageOffset; i++) {
                if (i >= playerList.size()) {
                    break;
                }
                playerMessage += playerList.get(i).getDisplayName() + "\n";
            }
            String author = "";
            if (playerList.size() != 1) {
                author = "There are currently " + playerList.size() + " players online";
            } else {
                author = "There is currently 1 player online";
            }
            EmbedBuilder playerEmbed = new EmbedBuilder();
            playerEmbed.setTitle("Player List")
                    .setDescription(playerMessage)
                    .setAuthor(author, null, "https://i.imgur.com/64R8O3D.png")
                    .addField("^^^^^^^^^", "Do >list [page] to get the rest of the players", false)
                    .setColor(new Color(0x70e992));
            event.getMessage().getChannel().sendMessage(playerEmbed.build()).queue();
            DiscordBot.client.getPresence().setActivity(Activity.watching(Bukkit.getServer().getOnlinePlayers().size() + "/"
                    + Bukkit.getServer().getMaxPlayers() + " Players"));
        } else if (event.getMessage().getContentStripped().contains((">disablebot").toLowerCase()) && event.getMessage().getContentRaw().charAt(0) == '>' && event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            sendMessage = false;
            DiscordBot.botEnabled = false;
            Member member = event.getMember();
            String memberName = member.getEffectiveName();
            Bukkit.broadcastMessage(Formats.SUCCESS + memberName + " disabled MC Chat!");
            WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername("Server");
            builder.setAvatarUrl(DiscordBot.avatarURL);
            builder.setContent(memberName + " disabled MC Chat!");
            client.send(builder.build());
            client.close();
        } else if (event.getMessage().getContentStripped().contains((">enablebot").toLowerCase()) && event.getMessage().getContentRaw().charAt(0) == '>' && event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            sendMessage = false;
            DiscordBot.botEnabled = true;
            Member member = event.getMember();
            String memberName = member.getEffectiveName();
            Bukkit.broadcastMessage(Formats.SUCCESS + memberName + " enabled MC Chat!");
            WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername("Server");
            builder.setAvatarUrl(DiscordBot.avatarURL);
            builder.setContent(memberName + " enabled MC Chat!");
            client.send(builder.build());
            client.close();
        } else if (event.getMessage().getContentStripped().contains((">w").toLowerCase()) && event.getMessage().getContentRaw().charAt(0) == '>' && (event.getMember().hasPermission(Permission.MANAGE_CHANNEL) || event.getMember().getId().equals("392410817049526273"))) {
            if (enableWynnApi) {
                sendMessage = false;
                WynncraftAPI wynnAPI = new WynncraftAPI();
                WynncraftGuild guild = wynnAPI.v1().guildStats(event.getMessage().getContentStripped().replace(">w ", "")).run();
                WynncraftGuildMember[] guildMembers = guild.getMembers();
                StringBuilder builder = new StringBuilder();

                for (WynncraftGuildMember guildMember : guildMembers) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(guildMember.getName());
                    if (!player.isWhitelisted()) {
                        player.setWhitelisted(true);
                        builder.append(player.getName()).append(", ");
                    }
                }
                event.getChannel().sendMessage("Added " + builder + "to the whitelist").queue();
            }
        }

        if (sendMessage && botEnabled) {
            Member member = event.getMember();
            List<Role> roles = member.getRoles();

            if (roles.size() == 0) {
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', String.format("%s &7| &r%s &8>> &r%s",
                        ChatColor.of("#8EA0E1") + "Discord", member.getEffectiveName(), event.getMessage().getContentRaw())));
            } else {
                Role userRole = roles.get(0);
                if (userRole.getName().equalsIgnoreCase("*") || userRole.getName().equalsIgnoreCase("admin") || userRole.getName().equalsIgnoreCase("moderator")) {
                    userRole = roles.get(1);
                }

                //gets the players discord role color and sets it to hexadecimal numbers
                Color userColour = event.getMember().getColor();
                int r = userColour.getRed();
                int g = userColour.getGreen();
                int b = userColour.getBlue();
                String userColourHex = String.format("#%02x%02x%02x", r, g, b);

                //send a message in the Minecraft chat "[Discord] <Role> | <Username>: <msg>"
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', String.format("%s &7| %s &r%s &8>> &r%s",
                        ChatColor.of("#8EA0E1") + "Discord", ChatColor.of(userColourHex) + "" + ChatColor.BOLD + userRole.getName(), ChatColor.of(userColourHex) + member.getEffectiveName(),
                        event.getMessage().getContentRaw())));
            }
        }
    }
}