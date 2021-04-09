package me.discordlinking.reactions;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.HashMap;
import java.util.Map;

public class AllReactables {
    private static final Map<Long, ReactableMessage> messages = new HashMap<>();

    public static synchronized void addMessage(ReactableMessage message) {
        messages.put(message.getId(), message);
        trim();
    }

    public static synchronized void removeMessage(ReactableMessage message) {
        messages.remove(message.getId());
        trim();
    }

    public static synchronized void dealWithReaction(MessageReactionAddEvent event) {
        ReactableMessage reactable = messages.get(event.getMessageIdLong());
        if (reactable != null) reactable.dealWithReaction(event);
        trim();
    }

    private synchronized static void trim() {
        messages.entrySet().removeIf(longReactableMessageEntry -> longReactableMessageEntry.getValue().isOld());
    }
}
