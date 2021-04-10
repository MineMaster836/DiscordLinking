package me.discordlinking.commands;

import me.discordlinking.DiscordBot;
import me.discordlinking.utils.Formats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class DMCommand implements CommandExecutor {
    public static String commandName = "discord_dm";

    public DMCommand(JavaPlugin plugin) {
        Objects.requireNonNull(plugin.getCommand(commandName)).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String commandName, @NotNull String[] args) {
        if (args.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "Please specify the user id and the message to send to the player");
            return false;
        }
        long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(ChatColor.RED + "'" + args[0] + "'" + " is not a number");
            return false;
        }
        DiscordBot.client.retrieveUserById(id).queue(user -> {
            if (user == null) {
                commandSender.sendMessage(ChatColor.RED + "That user does not exist");
                return;
            }
            String msg = String.join(" ", Arrays.asList(args).subList(1, args.length));
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(commandSender.getName(), null, commandSender instanceof Player ? Formats.getAvatarFromUUID(((Player) commandSender).getUniqueId()) : null);
            embed.setDescription(msg);
            embed.setTitle("Incoming");
            embed.setColor(DiscordBot.chatColor);
            user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage(embed.build()).queue(
                        s -> {
                            TextComponent message = new TextComponent();
                            message.setText(ChatColor.GRAY + "[" + ChatColor.BLUE + commandSender.getName() + ChatColor.DARK_GRAY + " -> " + ChatColor.BLUE + user.getName() + ChatColor.GRAY + "] " + ChatColor.WHITE + msg);
                            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + DMCommand.commandName + " " + id + " "));
                            commandSender.spigot().sendMessage(message);
                        });
            });
        });
        return true;
    }
}
