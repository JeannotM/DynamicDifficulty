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
	private File configFile = null;
	private File dataFile = null;
    
	public DataManager(Main plugin) {
		this.plugin = plugin;
		saveDefaultConfig();
	}
	
	public void reloadConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
		if (this.dataFile == null)
			this.dataFile = new File(this.plugin.getDataFolder(), "data.yml");
		
		this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);
		this.customConfig = YamlConfiguration.loadConfiguration(this.dataFile);
		InputStream defaultStream = this.plugin.getResource("config.yml");
		InputStream defaultData = this.plugin.getResource("data.yml");
		if (defaultStream != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
			this.dataConfig.setDefaults(defaultConfig);
		}
		if (defaultData != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultData));
			this.customConfig.setDefaults(defaultConfig);
		}
	}
	
	public void saveDefaultConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
		
		if (!this.configFile.exists()) {
			this.plugin.saveResource("config.yml", false);
		}
	}
	
	public void saveData() {
		if (this.dataFile == null || this.customConfig == null)
			return;
		
		try {
			this.getDataFile().save(this.dataFile);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "rip", e);
		}
    }
	
	public void saveConfig() {
		if (this.dataConfig == null || this.configFile == null)
			return;
		
		try {
			this.getConfig().save(this.configFile);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "rip", e);
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

    
}
