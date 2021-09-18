package me.discordlinking.format;

public enum GameChangeEvent {
    ENABLE_SERVER(0),
    DISABLE_SERVER(0),
    CHAT(0),
    PLAYER_JOIN(0),
    PLAYER_LEAVE(-1),
    PLAYER_DEATH(0),
    CHANGE_LINKING_POLICY(0);

    private final int playerOnlineChange;

    GameChangeEvent(int playerOnlineChange) {
        this.playerOnlineChange = playerOnlineChange;
    }

    public int getPlayersOnlineChange() {
        return playerOnlineChange;
    }
}
