/**
 * Main handler for the Gameplay-Modulated-difficulty plugin.
 * Here all the default values and commands will be processed and/or initialized.
 *
 * @version 1.4
 * @author SkinnyJeans
 */
package me.skinnyjeans.gmd;

import me.skinnyjeans.gmd.commands.AffinityCommands;
import me.skinnyjeans.gmd.events.AffinityEvents;
import me.skinnyjeans.gmd.hooks.Metrics;
import me.skinnyjeans.gmd.hooks.PlaceholderAPIExpansion;
import me.skinnyjeans.gmd.tabcompleter.AffinityTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class Main extends JavaPlugin {
	
	public DataManager data = new DataManager(this);
	public AffinityEvents af;

	@Override
	public void onEnable() {
		af = new AffinityEvents(this);
		checkData();
		Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Thank you for installing DynamicDifficulty!");
		if(data.getConfig().getString("difficulty-modifiers.type").equalsIgnoreCase("world")) {
			Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Currently on World Difficulty mode!");
		} else {
			Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Currently on Per Player Difficulty mode!");
		}
		getServer().getPluginManager().registerEvents(af, this);
		this.getCommand("affinity").setExecutor(new AffinityCommands(af));
		this.getCommand("affinity").setTabCompleter(new AffinityTabCompleter(af));

		saveDataEveryFewMinutes();
		onInterval();
	}
	
	@Override
	public void onDisable() {
		af.exitProgram();
		af = null;
		data = null;
	}
	
	public void onInterval() {
		if(data.getConfig().getInt("points-per-minute") != 0) {
			Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
				if (Bukkit.getOnlinePlayers().size() > 0)
					af.onInterval();
			}, 0L, 1200L);
		}
	}

	public void saveDataEveryFewMinutes() {
		Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
			if (Bukkit.getOnlinePlayers().size() > 0)
				af.saveData();
		}, 0L, 12000L);
	}

	/* To check a few settings and hooks */
	public void checkData() {
		if (!getConfig().getString("version").equals(Bukkit.getPluginManager().getPlugin("DynamicDifficulty").getDescription().getVersion()) || !getConfig().contains("version",true))
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[DynamicDifficulty] Your configuration file is not up to date. Please remove it or update it yourself, because I don't know how to do it with Java without deleting existing configs. Sorry :'(");

		String worldNames = "";
		for(int x=0;x<Bukkit.getWorlds().size();x++)
			if(Bukkit.getWorlds().get(x).getDifficulty() != Difficulty.HARD)
				worldNames += Bukkit.getWorlds().get(x).getName() + ", ";

		if(worldNames != "")
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[DynamicDifficulty] The following worlds do not have their difficulty on Hard mode: "+worldNames);

		if(data.getConfig().getBoolean("plugin-support.allow-bstats")){
			Metrics m = new Metrics(this, 11417);
			m.addCustomChart(new Metrics.SimplePie("difficulty_type", () ->
				data.getConfig().getString("difficulty-modifiers.type").toLowerCase(Locale.ROOT)
			));
			m.addCustomChart(new Metrics.SimplePie("save_type", () ->
				data.getConfig().getString("saving-data.type").toLowerCase(Locale.ROOT)
			));
			m.addCustomChart(new Metrics.SimplePie("amount_of_difficulties", () ->
				String.valueOf(data.getConfig().getConfigurationSection("difficulty").getKeys(false).size())
			));
			m.addCustomChart(new Metrics.SimplePie("custom_armor_and_item_spawn_chance", () ->
				String.valueOf(data.getConfig().getBoolean("advanced-features.custom-mob-items-spawn-chance"))
			));
			m.addCustomChart(new Metrics.SimplePie("auto_calculate_minaffinity", () ->
				String.valueOf(data.getConfig().getBoolean("advanced-features.auto-calculate-min-affinity"))
			));
			m.addCustomChart(new Metrics.SimplePie("auto_calculate_maxaffinity", () ->
				String.valueOf(data.getConfig().getBoolean("advanced-features.auto-calculate-max-affinity"))
			));
		}

		if(data.getConfig().getBoolean("plugin-support.allow-papi"))
			if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
				new PlaceholderAPIExpansion(this, af, data.getConfig().getBoolean("plugin-support.use-prefix")).register();
	}
}

