package me.discordlinking;

import me.bed0.jWynn.WynncraftAPI;
import me.bed0.jWynn.api.v1.guild.WynncraftGuild;
import me.bed0.jWynn.api.v1.guild.WynncraftGuildMember;
import me.discordlinking.commands.DMCommand;
import me.discordlinking.format.DiscordMessageFormat;
import me.discordlinking.format.GameChangeEvent;
import me.discordlinking.format.MinecraftMessageFormat;
import me.discordlinking.reactions.AllReactables;
import me.discordlinking.state.BotState;
import me.discordlinking.state.ChatLinkingPolicy;
import me.discordlinking.utils.Formats;
import me.discordlinking.utils.QueueNowService;
import me.discordlinking.utils.WriteConfigFile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class DiscordBot extends ListenerAdapter {
    public static JDA client;
    @Nullable
    public static Guild DISCORD_GUILD;
    public static String botToken;
    public static long channelID;
    public static String webhookURL;
    public static String avatarURL;
    public static boolean showDeaths;
    public static boolean enableWynnApi;
    public static int chatColor;
    public static DiscordBot instance;

    public DiscordBot() {
        instance = this;
        File file = new File(Main.get().getDataFolder(), "config.yml");
        if (!file.exists()) {
            WriteConfigFile.run(file, "configExample.yml");
        }
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
        if (client != null) {
            client.removeEventListener(this);
            client.shutdown();
        }
        JDABuilder builder = JDABuilder.create(botToken,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_WEBHOOKS,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS
        );
        builder.disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS);
        // not caching members will make @user messages from inside the game more expensive,
        // but I don't think it's enough of an impact to cache them
        // it really could go either way though
        builder.setMemberCachePolicy(MemberCachePolicy.NONE);
        builder.addEventListeners(this);
        client = builder.build();
        TextChannel channel = client.getTextChannelById(channelID);
        DISCORD_GUILD = channel == null ? null : channel.getGuild();
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        User user = event.getUser();
        if (user == null || user.isBot()) return;
        AllReactables.dealWithReaction(event);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getChannelType().isGuild()) {
            // this is a direct message
            DiscordDirectMessage.dealWithMessage(event);
            return;
        }
        if (event.getChannel().getIdLong() != channelID) return; // ignore messages from other channels
        Member member = event.getMember();
        if (member == null) return; // this should be impossible so we'll just return
        Message discordMessage = event.getMessage();
        boolean isAdmin = member.hasPermission(Permission.MANAGE_CHANNEL) || member.getIdLong() == 392410817049526273L;
        String discordContent = discordMessage.getContentRaw().toLowerCase(Locale.ROOT);
        String[] args = Arrays.copyOfRange(discordContent.split(" "), 1, discordContent.split(" ").length);

        if (discordContent.toLowerCase().startsWith((">list"))) {
            if (listCommand(event, args)) return;
        } else if (discordContent.startsWith((">disablebot")) && isAdmin) {
            if (endisablebotCommand(event, ChatLinkingPolicy.NONE)) return;
        } else if (discordContent.startsWith((">enablebot")) && isAdmin) {
            if (endisablebotCommand(event, ChatLinkingPolicy.ALL)) return;
        } else if (discordContent.startsWith((">w")) && (isAdmin)) {
            if (wynnCommand(event)) return;
        } else if (discordContent.startsWith(">help")) {
            helpCommand(event);
            return;
        }

        if (BotState.getChatPolicy().isToMinecraft()) {
            List<Role> roles = member.getRoles();

            if (roles.size() == 0) {
                final User author = event.getAuthor();
                TextComponent message = Formats.getDiscordToServerMessage(discordMessage.getContentDisplay(), null, null, null, null, event.getAuthor());
                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + DMCommand.commandName + " " + author.getIdLong() + " "));
                Bukkit.getServer().spigot().broadcast(message);
            } else {
                Role userRole = roles.get(0);
                //gets the players discord role color and sets it to hexadecimal numbers
                Color userColour = member.getColor();
                String userColourHex;
                if (userColour != null) {
                    int r = userColour.getRed();
                    int g = userColour.getGreen();
                    int b = userColour.getBlue();
                    userColourHex = String.format("#%02x%02x%02x", r, g, b);
                } else userColourHex = null;

                //send a message in the Minecraft chat "[Discord] <Role> | <Username>: <msg>"
                final User author = event.getAuthor();
                TextComponent sendToMC = Formats.getDiscordToServerMessage(discordMessage.getContentDisplay(), null, member, userRole, userColourHex, author);
                sendToMC.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + DMCommand.commandName + " " + author.getIdLong() + " "));
                Bukkit.getServer().spigot().broadcast(sendToMC);
            }
        }
    }

    private void helpCommand(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Help page");
        embed.addField(">list", "Lists all online players", false);
        embed.addField(">disablebot", "Disable the bot sending messages", false);
        embed.addField(">enablebot", "Enable the bot sending messages", false);
        embed.addField(">w", "Wynn command", false);
        embed.setColor(DiscordBot.chatColor);
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    private boolean wynnCommand(@NotNull MessageReceivedEvent event) {
        if (enableWynnApi) {
            WynncraftAPI wynnAPI = new WynncraftAPI();
            wynnAPI.v1().guildStats(event.getMessage().getContentStripped().replace(">w ", "")).runAsync(
                    (WynncraftGuild guild) -> {
                        WynncraftGuildMember[] guildMembers = guild.getMembers();
                        StringBuilder builder = new StringBuilder();
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.get(), () -> {
                            for (WynncraftGuildMember guildMember : guildMembers) {
                                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(guildMember.getUuid()));
                                if (!player.isWhitelisted()) {
                                    player.setWhitelisted(true);
                                    builder.append(player.getName()).append(", ");
                                }
                            }
                            event.getChannel().sendMessage("Added " + builder + "to the whitelist").queue();
                        });
                    });
            return true;
        }
        return false;
    }

    /**
     * @param event     the received message event
     * @param newPolicy the new linkingPolicy
     * @return true if the command was successful
     */
    private boolean endisablebotCommand(@NotNull MessageReceivedEvent event, ChatLinkingPolicy newPolicy) {
        Member member = event.getMember();
        if (member == null) return false;
        String memberName = member.getEffectiveName();
        MinecraftMessageFormat.chatPolicyChange(memberName, newPolicy);
        String username;
        String message;
        if (newPolicy == ChatLinkingPolicy.ALL) {
            username = DiscordMessageFormat.Status.Enable.username(memberName);
            message = DiscordMessageFormat.Status.Enable.message(memberName);
        } else {
            username = DiscordMessageFormat.Status.Disable.username(memberName);
            message = DiscordMessageFormat.Status.Disable.message(memberName);
        }
        DiscordMessageFormat.sendMessage(username, message, GameChangeEvent.CHANGE_LINKING_POLICY);
        return true;
    }

    /**
     * @param event the received message event
     * @param args  the args given in the message
     * @return true if the command was successful
     */
    private boolean listCommand(@NotNull MessageReceivedEvent event, String[] args) {
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
            playerMessage += playerList.get(i).getName() + "\n";
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
        event.getMessage().getChannel().sendMessageEmbeds(playerEmbed.build()).queue();
        return true;
    }
}