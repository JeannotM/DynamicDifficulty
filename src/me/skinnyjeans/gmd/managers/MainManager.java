package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.Main;

public class MainManager {

    private final DifficultyManager DIFFICULTY_MANAGER;
    private final AffinityManager AFFINITY_MANAGER;
    private final PlayerManager PLAYER_MANAGER;
    private final DataManager DATA_MANAGER;
    private final Main PLUGIN;

    public MainManager(Main main) {
        DATA_MANAGER = new DataManager(this);

        DIFFICULTY_MANAGER = new DifficultyManager(this);
        AFFINITY_MANAGER = new AffinityManager(this);
        PLAYER_MANAGER = new PlayerManager(this);
        PLUGIN = main;

        startUp();
    }

    public void startUp() {

    }

    public AffinityManager getAffinityManager() { return AFFINITY_MANAGER; }
    public PlayerManager getPlayerManager() { return PLAYER_MANAGER; }
    public DataManager getDataManager() { return DATA_MANAGER; }
    public Main getPlugin() { return PLUGIN; }
}
