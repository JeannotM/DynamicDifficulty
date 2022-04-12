package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.databases.*;
import me.skinnyjeans.gmd.models.ISaveManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class DataManager {

    private final MainManager MAIN_MANAGER;

    private final HashSet<String> DISABLED_WORLDS = new HashSet<>();

    private String culture;
    private File configFile;
    private File langFile;
    private FileConfiguration config;
    private FileConfiguration language;
    private ISaveManager DATABASE;

    public DataManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;

        loadConfig();

        try {
            String saveType = config.getString("saving-data.type", "file").toLowerCase();
            if(saveType.equals("mysql") || saveType.equals("sqlite") || saveType.equals("postgresql")){
                DATABASE = new SQL(MAIN_MANAGER.getPlugin(), this, saveType);
            } else if(saveType.equals("mongodb")) {
                DATABASE = new MongoDB(MAIN_MANAGER.getPlugin(), this);
            } else if(saveType.equals("none")){
                DATABASE = new None();
            } else DATABASE = new me.skinnyjeans.gmd.databases.File(MAIN_MANAGER.getPlugin(), this);
        } catch(Exception e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[DynamicDifficulty] Can't connect to the database, switching to 'file' mode");
            DATABASE = new me.skinnyjeans.gmd.databases.File(MAIN_MANAGER.getPlugin(), this);
        }
    }

    public void loadConfig() {
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

        culture = "lang." + language.getString("culture", "en-US") + ".";
    }

    public FileConfiguration getConfig() { return config; }
    public FileConfiguration getLang() { return language; }
    public void updatePlayer(UUID uuid) { DATABASE.updatePlayer(MAIN_MANAGER.getPlayerManager().getPlayerList().get(uuid)); }
    public void getAffinityValues(UUID uuid, ISaveManager.findCallback callback) { DATABASE.getAffinityValues(uuid, callback); }
    public void playerExists(UUID uuid, ISaveManager.findBooleanCallback callback) { DATABASE.playerExists(uuid, callback); }

    public String getString(String item, HashMap<String, String> replaceables) {
        String entry = language.getString(culture + item);

        if(entry == null) return null;

        for(String key : replaceables.keySet()) entry.replace(key, replaceables.get(key));
        return ChatColor.translateAlternateColorCodes('&', entry);
    }

    public String getLanguageString(String item) {
        String entry = language.getString(culture + item);

        if(entry == null) return null;

        return ChatColor.translateAlternateColorCodes('&', entry);
    }

    public String getLanguageString(String item, boolean isRight) {
        String entry = language.getString(culture + item);

        if(entry == null) return null;

        return ChatColor.translateAlternateColorCodes('&', (isRight ? language.getString("command-right-prefix") : language.getString("command-wrong-prefix")) + entry);
    }

    public boolean langExists(String location) {
        return language.isSet(location) && language.getString(location).length() != 0 && !language.getString(location).equals("");
    }

    public void reloadConfig() {
        loadConfig();

        MAIN_MANAGER.getDifficultyManager().reloadConfig();
        MAIN_MANAGER.getAffinityManager().reloadConfig();
        MAIN_MANAGER.getPlayerManager().reloadConfig();
        MAIN_MANAGER.getEntityManager().reloadConfig();
        MAIN_MANAGER.getEventManager().reloadConfig();
        MAIN_MANAGER.getDataManager().reloadConfig();
    }

    public boolean isWorldDisabled(String worldName) { return DISABLED_WORLDS.contains(worldName); }
}
