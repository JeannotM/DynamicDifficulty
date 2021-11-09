package me.skinnyjeans.gmd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

// https://www.youtube.com/watch?v=-ZrIjYXOkn0
public class DataManager {
	
	private Main plugin;
	private FileConfiguration dataConfig = null;
	private FileConfiguration customConfig = null;
	private FileConfiguration messageConfig = null;
	private File configFile = null;
	private File dataFile = null;
	private File messageFile = null;
    
	public DataManager(Main plugin) {
		this.plugin = plugin;
		saveDefaultConfig();
		saveDefaultLang();
	}
	
	public void reloadConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
		if (this.dataFile == null)
			this.dataFile = new File(this.plugin.getDataFolder(), "data.yml");
		if (this.messageFile == null)
			this.messageFile = new File(this.plugin.getDataFolder(), "lang.yml");
		
		this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);
		this.customConfig = YamlConfiguration.loadConfiguration(this.dataFile);
		this.messageConfig = YamlConfiguration.loadConfiguration(this.messageFile);
		InputStream defaultStream = this.plugin.getResource("config.yml");
		InputStream defaultData = this.plugin.getResource("data.yml");
		InputStream defaultMessage = this.plugin.getResource("lang.yml");
		if (defaultStream != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
			this.dataConfig.setDefaults(defaultConfig);
		}
		if (defaultData != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultData));
			this.customConfig.setDefaults(defaultConfig);
		}
		if (defaultMessage != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultMessage));
			this.messageConfig.setDefaults(defaultConfig);
		}
	}
	
	public void saveDefaultConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
		
		if (!this.configFile.exists())
			this.plugin.saveResource("config.yml", false);
	}

	public void saveDefaultLang() {
		if (this.messageFile == null)
			this.messageFile = new File(this.plugin.getDataFolder(), "lang.yml");

		if (!this.messageFile.exists())
			this.plugin.saveResource("lang.yml", false);
	}
	
	public void saveData() {
		if (this.dataFile == null)
			return;
		
		try {
			this.getDataFile().save(this.dataFile);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Couldn't save the data file", e);
		}
    }

	public FileConfiguration getConfig() {
		if(this.dataConfig == null)
			reloadConfig();
		return this.dataConfig;
	}
	
	public FileConfiguration getDataFile() {
		if(this.customConfig == null)
			reloadConfig();
		return this.customConfig;
    }

	public FileConfiguration getLang() {
		if(this.messageConfig == null)
			reloadConfig();
		return this.messageConfig;
	}
}
