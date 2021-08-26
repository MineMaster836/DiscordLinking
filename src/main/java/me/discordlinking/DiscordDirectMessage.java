package me.discordlinking;

import me.discordlinking.reactions.ReactableWhoToSend;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

public class DiscordDirectMessage { // If i being honest i don't really know whats happening here lol. todo learn what this does and comment it
    private static Map<Long, DirectConversation> conversations = new HashMap<>();

    public static void dealWithMessage(MessageReceivedEvent event) {
        DirectConversation conversation = conversations.get(event.getAuthor().getIdLong());
        if (conversation == null) {
            // start a new conversation
            new ReactableWhoToSend(event);
        } else {
            // deal with the current conversation
            conversation.dealWithMessage(event.getMessage());
        }
    }

    private static class DirectConversation {
        public void dealWithMessage(Message message) {

        }
    }
}
