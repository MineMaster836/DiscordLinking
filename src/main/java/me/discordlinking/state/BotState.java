package me.discordlinking.state;

import apple.utilities.database.SaveFileable;
import apple.utilities.database.singleton.AppleJsonDatabaseSingleton;
import me.discordlinking.Main;
import me.discordlinking.utils.QueueNowService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

public class BotState implements SaveFileable {
    private static BotState instance;
    private static AppleJsonDatabaseSingleton<BotState> databaseManager;
    private ChatLinkingPolicy chatPolicy = ChatLinkingPolicy.ALL;

    public static void load() {
        databaseManager = new AppleJsonDatabaseSingleton<>(Main.get().getDataFolder(), QueueNowService.get());
        @Nullable BotState database = databaseManager.loadNow(BotState.class, getSaveFileNameStatic());
        instance = Objects.requireNonNullElseGet(database, BotState::new);
    }

    private static void save() {
        databaseManager.save(getInstance());
    }

    public static BotState getInstance() {
        return instance;
    }

    public static ChatLinkingPolicy getChatPolicy() {
        return getInstance().chatPolicy;
    }

    public static void setChatPolicy(ChatLinkingPolicy newPolicy) {
        getInstance().chatPolicy = newPolicy;
        save();
    }

    @Override
    public String getSaveFileName() {
        return getSaveFileNameStatic();
    }

    public static String getSaveFileNameStatic() {
        return "persistedState.json";
    }
}
