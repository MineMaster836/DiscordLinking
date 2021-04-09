package me.discordlinking.reactions;

import me.discordlinking.DiscordBot;
import me.discordlinking.Main;
import me.discordlinking.commands.DMCommand;
import me.discordlinking.utils.Formats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;

public class ReactableWhoToSend implements ReactableMessage {
    private static final long TIME_TO_OLD = 1000 * 60 * 5;
    private static final int PLAYERS_PER_PAGE = 5;
    private final String toSend;
    private final User author;
    private Message message;
    private long lastUpdated;
    private int page = 0;

    private final List<UUID> players = new ArrayList<>();
    private final List<String> playerNames = new ArrayList<>();

    public ReactableWhoToSend(MessageReceivedEvent event) {
        toSend = event.getMessage().getContentDisplay();
        this.author = event.getAuthor();
        this.lastUpdated = System.currentTimeMillis();
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.get(), () -> {
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            for (Player player : players) {
                this.players.add(player.getUniqueId());
                this.playerNames.add(player.getName());
            }
            this.message = event.getChannel().sendMessage(makeMessage()).complete();
            for (int j = 0; j < Math.min(players.size(), PLAYERS_PER_PAGE); j++) {
                this.message.addReaction(MyReactionsKnowledge.emojiAlphabet.get(j)).queue();
            }
            AllReactables.addMessage(this);
        }, 0);
    }

    private String makeMessage() {
        if (playerNames.isEmpty()) {
            return "There is nobody online to send this message to.";
        } else {
            StringBuilder content = new StringBuilder();
            content.append("\n");
            content.append("Who should I send this too?");
            final int max = Math.min(playerNames.size(), (page + 1) * PLAYERS_PER_PAGE);
            for (int i = page * PLAYERS_PER_PAGE; i < max; i++) {
                content.append("\n").append(MyReactionsKnowledge.emojiAlphabet.get(i)).append(" ").append("**").append(playerNames.get(i)).append("**");
            }
            content.append("\n------------------");
            int allowedSize = 1900 - content.length();
            final String msg;
            if (this.toSend.length() >= allowedSize) {
                msg = "```" + this.toSend.substring(0, allowedSize) + "...```";
            } else
                msg = "```" + this.toSend + "```";
            return msg + content.toString();
        }
    }

    @Override
    public void dealWithReaction(MessageReactionAddEvent event) {
        switch (MyReactionsKnowledge.KnownReaction.toReaction(event.getReactionEmote())) {
            case LEFT:
                left();
            case RIGHT:
                right();
            case EMOJI_ALPHABET:
                selected(event.getReaction().getReactionEmote().getEmoji());
        }
    }

    private void selected(String emoji) {
        int index = MyReactionsKnowledge.toAlphabetIndex(emoji);
        if (index < players.size()) {
            UUID uuid = players.get(index);
            @Nullable Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                message.editMessage("That player is no longer online.").queue();
            } else {
                message.editMessage(sentMessage(player, toSend)).queue();
                message.addReaction(MyReactionsKnowledge.KnownReaction.CHECKMARK.getEmoji()).queue();
                AllReactables.removeMessage(this);
                TextComponent message = new TextComponent();
                message.setText(ChatColor.GRAY + "[" + ChatColor.BLUE + author.getName() + ChatColor.DARK_GRAY + " -> " + ChatColor.BLUE + player.getName() + ChatColor.GRAY + "] " + ChatColor.WHITE + toSend);
                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + DMCommand.commandName + " " + author.getIdLong() + " "));
                player.spigot().sendMessage(message);
            }
        }
    }

    private Message sentMessage(Player player, String toSend) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(player.getName(), null, Formats.getAvatarFromUUID(player.getUniqueId()));
        embed.setDescription(toSend);
        embed.setTitle("Outgoing");
        embed.setColor(DiscordBot.chatColor);
        MessageBuilder msg = new MessageBuilder();
        msg.setContent(".");
        msg.setEmbed(embed.build());
        return msg.build();
    }

    private void left() {
        this.page = Math.max(page - 1, 0);
        this.message.editMessage(makeMessage()).queue();
        this.lastUpdated = System.currentTimeMillis();
    }

    private void right() {
        this.page = Math.min(players.size() / PLAYERS_PER_PAGE, this.page + 1);
        this.message.editMessage(makeMessage()).queue();
        this.lastUpdated = System.currentTimeMillis();
    }

    @Override
    public long getId() {
        return this.message.getIdLong();
    }

    @Override
    public boolean isOld() {
        return System.currentTimeMillis() - TIME_TO_OLD > lastUpdated;
    }
}
