package me.skinnyjeans.gmd.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class DataManager {

    private final MainManager MAIN_MANAGER;

    private HashSet<String> DISABLED_WORLDS = new HashSet<>();

    private File configFile;
    private File langFile;
    private FileConfiguration config;
    private FileConfiguration language;

    private static DataManager instance;

    public DataManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
        instance = this;

        configFile = new File(MAIN_MANAGER.getPlugin().getDataFolder(), "config.yml");
        langFile = new File(MAIN_MANAGER.getPlugin().getDataFolder(), "lang.yml");
        config = new YamlConfiguration();
        language = new YamlConfiguration();

        if (!configFile.exists()) MAIN_MANAGER.getPlugin().saveResource("config.yml",false);
        if (!langFile.exists()) MAIN_MANAGER.getPlugin().saveResource("lang.yml",false);

        try {
            config.load(configFile);
            language.load(langFile);
        } catch(Exception e) { e.printStackTrace(); }
    }

    public static DataManager getInstance() { return instance; }

    public FileConfiguration getConfig() { return config; }

    public FileConfiguration getLang() { return language; }

    public String getString(String item, HashMap<String, String> replaceables) {
        String entry = language.getString(item);

        if(entry == null) return null;

        for(String key : replaceables.keySet()) entry.replace(key, replaceables.get(key));
        return entry;
    }

    public boolean langExists(String location) {
        return language.isSet(location) && language.getString(location).length() != 0 && !language.getString(location).equals("");
    }

    public void reloadConfig() {
        configFile = new File(MAIN_MANAGER.getPlugin().getDataFolder(), "config.yml");
        langFile = new File(MAIN_MANAGER.getPlugin().getDataFolder(), "lang.yml");
        config = new YamlConfiguration();
        language = new YamlConfiguration();

        try {
            config.load(configFile);
            language.load(langFile);
        } catch(Exception e) { e.printStackTrace(); }

        MAIN_MANAGER.getEntityManager().reloadConfig();
        MAIN_MANAGER.getEventManager().reloadConfig();
    }

    public boolean isWorldDisabled(String worldName) { return DISABLED_WORLDS.contains(worldName); }
}
