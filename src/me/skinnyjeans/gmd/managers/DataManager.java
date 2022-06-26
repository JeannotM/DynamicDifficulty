package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.databases.*;
import me.skinnyjeans.gmd.models.ISaveManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class DataManager {

    private final MainManager MAIN_MANAGER;

    private final HashSet<String> DISABLED_WORLDS = new HashSet<>();

    private String culture;
    private FileConfiguration config;
    private FileConfiguration language;
    private File configFile;
    private File langFile;
    private ISaveManager DATABASE;

    public DataManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;

        loadConfig();

        if (!getConfig().getString("version").equals(MAIN_MANAGER.getPlugin().getDescription().getVersion()) || !getConfig().contains("version",true))
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[DynamicDifficulty] Your configuration file is not up to date. Please remove it or update it yourself, because I don't know how to do it with Java without deleting existing configs. Sorry :'(");

        try {
            String saveType = config.getString("saving-data.type", "file").toLowerCase();
            if(saveType.equals("mysql") || saveType.equals("sqlite") || saveType.equals("postgresql")){
                DATABASE = new SQL(MAIN_MANAGER.getPlugin(), this, saveType);
            } else if(saveType.equals("mongodb")) {
                DATABASE = new MongoDB(MAIN_MANAGER.getPlugin(), this);
            } else if(saveType.equals("none")){
                DATABASE = new None(this);
            } else DATABASE = new me.skinnyjeans.gmd.databases.File(MAIN_MANAGER.getPlugin(), this);
        } catch(Exception e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[DynamicDifficulty] Can't connect to the database, switching to 'file' mode");
            DATABASE = new me.skinnyjeans.gmd.databases.File(MAIN_MANAGER.getPlugin(), this);
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(MAIN_MANAGER.getPlugin(), () ->
            Bukkit.getOnlinePlayers().forEach(player -> updatePlayer(player.getUniqueId())), 20*60*5, 20*60*5);
    }

    public void loadConfig() {
        if (configFile == null) configFile = new File(MAIN_MANAGER.getPlugin().getDataFolder(), "config.yml");
        if (langFile == null) langFile = new File(MAIN_MANAGER.getPlugin().getDataFolder(), "lang.yml");

        config = YamlConfiguration.loadConfiguration(configFile);
        language = YamlConfiguration.loadConfiguration(langFile);

        if (! configFile.exists())
            MAIN_MANAGER.getPlugin().saveResource("config.yml", false);

        InputStream configStream = MAIN_MANAGER.getPlugin().getResource("config.yml");

        if(configStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(configStream));
            config.setDefaults(defaultConfig);
        }

        if (! langFile.exists())
            MAIN_MANAGER.getPlugin().saveResource("lang.yml", false);

        InputStream languageStream = MAIN_MANAGER.getPlugin().getResource("lang.yml");

        if(languageStream != null) {
            YamlConfiguration defaultLang = YamlConfiguration.loadConfiguration(new InputStreamReader(languageStream));
            language.setDefaults(defaultLang);
        }

        culture = "lang." + language.getString("culture", "en-US") + ".";

        if(! language.isSet("lang." + culture)) culture = "lang.en-US.";
    }

    public FileConfiguration getConfig() { return config; }
    public ConfigurationSection getLang() { return language.getConfigurationSection(culture); }
    public void updatePlayer(UUID uuid) { DATABASE.updatePlayer(MAIN_MANAGER.getPlayerManager().getPlayerList().get(uuid)); }
    public void getAffinityValues(UUID uuid, final ISaveManager.findCallback callback) { DATABASE.getAffinityValues(uuid, callback); }
    public void playerExists(UUID uuid, final ISaveManager.findBooleanCallback callback) { DATABASE.playerExists(uuid, callback); }

    public String replaceString(String item, HashMap<String, String> replaceables) {
        for(String key : replaceables.keySet()) item = item.replace(key, replaceables.get(key));
        return ChatColor.translateAlternateColorCodes('&', item);
    }

    public String getLanguageString(String item) {
        String entry = language.getString(culture + item);
        if(entry == null) return "";
        return ChatColor.translateAlternateColorCodes('&', entry);
    }

    public String getLanguageString(String item, boolean isRight) {
        String entry = language.getString(culture + item);
        if(entry == null) return "";
        return ChatColor.translateAlternateColorCodes('&', (isRight ? language.getString("command-right-prefix") : language.getString("command-wrong-prefix")) + entry);
    }

    public void saveData() {
        Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () ->
                Bukkit.getOnlinePlayers().forEach(player -> updatePlayer(player.getUniqueId())));
    }

    public void reloadConfig() {
        MAIN_MANAGER.getPlugin().reloadConfig();
        loadConfig();
    }

    public boolean isWorldDisabled(String worldName) { return DISABLED_WORLDS.contains(worldName); }
}
