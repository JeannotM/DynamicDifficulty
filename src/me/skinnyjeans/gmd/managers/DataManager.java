package me.skinnyjeans.gmd.managers;

import java.util.HashSet;

public class DataManager {

    private final MainManager MAIN_MANAGER;

    private HashSet<String> DISABLED_WORLDS = new HashSet<>();

    public DataManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    public boolean isWorldDisabled(String worldName) { return DISABLED_WORLDS.contains(worldName); }
}
