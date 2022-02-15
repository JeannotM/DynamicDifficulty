package me.skinnyjeans.gmd.managers;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;

public class DataManager {

    private final MainManager MAIN_MANAGER;

    private HashSet<String> DISABLED_WORLDS = new HashSet<>();

    private static DataManager instance;

    public DataManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
        instance = this;
    }

    public static DataManager getInstance() { return instance; }

    public FileConfiguration getConfig() { }

    public FileConfiguration getLang() { }

    public boolean isWorldDisabled(String worldName) { return DISABLED_WORLDS.contains(worldName); }
}
