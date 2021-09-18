package me.discordlinking;

import me.discordlinking.format.DiscordMessageFormat;
import me.discordlinking.format.GameChangeEvent;
import me.discordlinking.state.BotState;
import me.discordlinking.utils.ChatUtils;
import me.discordlinking.utils.Formats;
import me.discordlinking.utils.QueueNowService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class SpigotListeners implements Listener {
    Main plugin;
    static boolean serverChat = false;

    public SpigotListeners(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncPlayerChatEvent e) {
        if (!BotState.getChatPolicy().isToDiscord()) return;
        String message = ChatUtils.trimMessage(e.getMessage());
        // queue up atify-ing the message, then once we finish that, do finishAsyncChat
        ChatUtils.atifyMessageGetMembers(message, (String newMessage) -> this.finishAsyncChat(newMessage, e));
    }

    private void finishAsyncChat(String newMessage, AsyncPlayerChatEvent e) {
        newMessage = ChatColor.stripColor(newMessage);
        String playerName = ChatColor.stripColor(e.getPlayer().getDisplayName());
        String username = DiscordMessageFormat.Chat.Normal.username(newMessage, playerName);
        String avatarURL = Formats.getAvatarFromUUID(e.getPlayer().getUniqueId());
        String content = DiscordMessageFormat.Chat.Normal.message(newMessage, playerName);
        DiscordMessageFormat.sendMessage(username, avatarURL, content, GameChangeEvent.CHAT);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!BotState.getChatPolicy().isToDiscord()) return;
        String message = ChatColor.stripColor(e.getJoinMessage());
        String playerName = ChatColor.stripColor(e.getPlayer().getDisplayName());
        String username = DiscordMessageFormat.Login.username(message, playerName);
        String content = DiscordMessageFormat.Login.message(message, playerName);
        String avatarURL = Formats.getAvatarFromUUID(e.getPlayer().getUniqueId());
        DiscordMessageFormat.sendMessage(username, avatarURL, content, GameChangeEvent.PLAYER_JOIN);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (!BotState.getChatPolicy().isToDiscord()) return;
        String message = ChatColor.stripColor(e.getQuitMessage());
        String playerName = ChatColor.stripColor(e.getPlayer().getDisplayName());
        String username = DiscordMessageFormat.Logout.username(message, playerName);
        String content = DiscordMessageFormat.Logout.message(message, playerName);
        String avatarURL = Formats.getAvatarFromUUID(e.getPlayer().getUniqueId());
        DiscordMessageFormat.sendMessage(username, avatarURL, content, GameChangeEvent.PLAYER_LEAVE);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (!BotState.getChatPolicy().isToDiscord()) return;
        if (DiscordBot.showDeaths) {
            String message = ChatColor.stripColor(e.getDeathMessage());
            String playerName = ChatColor.stripColor(e.getEntity().getDisplayName());
            String username = DiscordMessageFormat.Death.username(message, playerName);
            String content = DiscordMessageFormat.Death.message(message, playerName);
            String avatarURL = Formats.getAvatarFromUUID(e.getEntity().getUniqueId());
            DiscordMessageFormat.sendMessage(username, avatarURL, content, GameChangeEvent.PLAYER_DEATH);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPreCommand(PlayerCommandPreprocessEvent e) {
        String[] args = e.getMessage().split(" ");
        if (args[0].equalsIgnoreCase("/reload") || args[0].equalsIgnoreCase("/rl")) {
            Main.isReloading("true");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent e) {
        String[] args = e.getCommand().split(" ");
        if (e.getCommand().equalsIgnoreCase(".chat")) {
            if (!serverChat) {
                e.setCancelled(true);
                serverChat = true;
                e.getSender().sendMessage("CONSOLE Chat Enabled");
            } else {
                e.setCancelled(true);
                serverChat = false;
                e.getSender().sendMessage("CONSOLE Chat Disabled");
            }
        } else if (serverChat) {
            e.setCancelled(true);
            if (e.getSender() instanceof ConsoleCommandSender) {
                if (BotState.getChatPolicy().isToMinecraft()) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&2CONSOLE &7>>&r " + e.getCommand()));
                }
                if (BotState.getChatPolicy().isToDiscord()) {
                    String username = DiscordMessageFormat.Chat.Console.username(e.getCommand());
                    String content = DiscordMessageFormat.Chat.Console.message(ChatColor.stripColor(e.getCommand()));
                    DiscordMessageFormat.sendMessage(username, content, GameChangeEvent.CHAT);
                }
            }
        } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
            Main.isReloading("true");
        }
    }
}
