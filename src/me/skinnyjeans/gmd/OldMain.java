///**
// * Main handler for the Gameplay-Modulated-difficulty plugin.
// * Here all the default values and commands will be processed and/or initialized.
// *
// * @version 1.5
// * @author SkinnyJeans
// */
//package me.skinnyjeans.gmd;
//
//import me.skinnyjeans.gmd.commands.AffinityCommands;
//import me.skinnyjeans.gmd.events.AffinityEvents;
//import me.skinnyjeans.gmd.hooks.Metrics;
//import me.skinnyjeans.gmd.hooks.PlaceholderAPIExpansion;
//import me.skinnyjeans.gmd.tabcompleter.AffinityTabCompleter;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.Difficulty;
//import org.bukkit.plugin.java.JavaPlugin;
//
//public class Main extends JavaPlugin {
//
//	public DataManager data = new DataManager(this);
//	public AffinityEvents af;
//
//	@Override
//	public void onEnable() {
//		af = new AffinityEvents(this);
//		checkData();
//		Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Thank you for installing DynamicDifficulty!");
//
//		String difficultyType = data.getConfig().getString("difficulty-modifiers.type", "player").toLowerCase();
//		if(difficultyType.equals("world")) {
//			Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Currently on World Difficulty mode!");
//		} else if(difficultyType.equals("biome")) {
//			Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Currently on Biome Difficulty mode!");
//		} else if(difficultyType.equals("radius")) {
//			Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Currently on Radius Difficulty mode!");
//		} else {
//			Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Currently on Per Player Difficulty mode!");
//		}
//		getServer().getPluginManager().registerEvents(af, this);
//		this.getCommand("affinity").setExecutor(new AffinityCommands(af, data));
//		this.getCommand("affinity").setTabCompleter(new AffinityTabCompleter(af, data));
//
//		if(!difficultyType.equals("radius")) {
//			saveDataEveryFewMinutes();
//			onInterval();
//		}
//	}
//
//	@Override
//	public void onDisable() {
//		if(af != null)
//			af.exitProgram();
//		af = null;
//		data = null;
//	}
//
//	public void onInterval() {
//		if(data.getConfig().getInt("points-per-minute") != 0)
//			Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
//				if (Bukkit.getOnlinePlayers().size() > 0)
//					af.onInterval();
//			}, 0L, 1200L);
//	}
//
//	public void saveDataEveryFewMinutes() {
//		Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
//			if (Bukkit.getOnlinePlayers().size() > 0)
//				af.saveData();
//		}, 0L, 6000L);
//	}
//
//	/* To check a few settings and hooks */
//	public void checkData() {
//		if (!getConfig().getString("version").equals(Bukkit.getPluginManager().getPlugin("DynamicDifficulty").getDescription().getVersion()) || !getConfig().contains("version",true))
//			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[DynamicDifficulty] Your configuration file is not up to date. Please remove it or update it yourself, because I don't know how to do it with Java without deleting existing configs. Sorry :'(");
//
//		StringBuilder worldNames = new StringBuilder("");
//		for(int x=0;x<Bukkit.getWorlds().size();x++)
//			if(Bukkit.getWorlds().get(x).getDifficulty() != Difficulty.HARD)
//				worldNames.append(Bukkit.getWorlds().get(x).getName()).append(", ");
//
//		if(worldNames.length() != 0)
//			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[DynamicDifficulty] The following worlds do not have their difficulty on Hard mode: "+worldNames.substring(0, worldNames.length() - 2));
//
//		Metrics m = new Metrics(this, 11417);
//		m.addCustomChart(new Metrics.SimplePie("difficulty_type", () ->
//			data.getConfig().getString("difficulty-modifiers.type").toLowerCase()
//		));
//		m.addCustomChart(new Metrics.SimplePie("save_type", () ->
//			data.getConfig().getString("saving-data.type").toLowerCase()
//		));
//		m.addCustomChart(new Metrics.SimplePie("amount_of_difficulties", () ->
//			String.valueOf(data.getConfig().getConfigurationSection("difficulty").getKeys(false).size())
//		));
//		m.addCustomChart(new Metrics.SimplePie("custom_armor_and_item_spawn_chance", () ->
//			data.getConfig().getString("advanced-features.custom-mob-items-spawn-chance", "false").toLowerCase()
//		));
//
//		if(data.getConfig().getBoolean("plugin-support.allow-papi"))
//			if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
//				new PlaceholderAPIExpansion(this, af, data.getConfig().getBoolean("plugin-support.use-prefix")).register();
//	}
//}
//
