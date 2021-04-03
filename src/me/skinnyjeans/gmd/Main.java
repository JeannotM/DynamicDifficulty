/**
 * Main handler for the Gameplay-Modulated-difficulty plugin.
 * Here all the default values and commands will be processed and/or initialized.
 * 
 * @version 1.0
 * @author SkinnyJeans
 */
package me.skinnyjeans.gmd;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import me.skinnyjeans.gmd.commands.PlayerCommands;
import me.skinnyjeans.gmd.commands.WorldCommands;
import me.skinnyjeans.gmd.tabcompleter.PlayerTabCompleter;
import me.skinnyjeans.gmd.tabcompleter.WorldTabCompleter;

public class Main extends JavaPlugin {
	
	public DataManager data = new DataManager(this);
	public WorldAffinity wa = null;
	public PlayerAffinity pa = null;
	
	@Override
	public void onEnable() {
		Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Thanks for installing DynamicDifficulty!");
		if(data.getConfig().getBoolean("per-player-difficulty")) {
			pa = new PlayerAffinity(this);
			getServer().getPluginManager().registerEvents(pa, this);
			this.getCommand("affinity").setExecutor(new PlayerCommands(pa));
			this.getCommand("affinity").setTabCompleter(new PlayerTabCompleter());
			Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Currently on Per Player Difficulty mode!");
		}
		else {
			wa = new WorldAffinity(this);
			getServer().getPluginManager().registerEvents(wa, this);
			this.getCommand("affinity").setExecutor(new WorldCommands(wa));
			this.getCommand("affinity").setTabCompleter(new WorldTabCompleter());
			Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Currently on World Difficulty mode!");
		}
		
		saveDataEveryFifteenMinutes();
		onInterval();
	}
	
	@Override
	public void onDisable() {
		if(pa != null) {
			pa.saveAllPlayerData();
		}
		else {
			wa.saveAllData();
		}
		
	}
	
	public void onInterval() {
		if(data.getConfig().getInt("on-interval") != 0) {
			int timer = data.getConfig().getInt("interval-timer");
			if(timer > 0) {
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
		        	@Override
					public void run() {
		        		if (Bukkit.getOnlinePlayers().size() > 0) {
		        			if(pa != null) {
		        				pa.saveAllPlayerData();
		        			}
		        			else {
		        				wa.saveAllData();
		        			}
		        		}
					}
				}, 0L, 20L*(60*timer));
			}
		}
	}
	
	public void saveDataEveryFifteenMinutes() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
        	@Override
			public void run() {
        		if (Bukkit.getOnlinePlayers().size() > 0) {
        			if(pa != null) {
        				pa.saveAllPlayerData();
        			}
        			else {
        				wa.saveAllData();
        			}
        		}
			}
		}, 0L, 20L*(60*15));
	}
}

