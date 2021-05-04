package me.skinnyjeans.gmd.commands;

import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.skinnyjeans.gmd.WorldAffinity;

public class WorldCommands implements CommandExecutor{
	
	private WorldAffinity affinity;
	
	public WorldCommands(WorldAffinity wa) {
		affinity = wa;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(label.equalsIgnoreCase("affinity")) {
			String msg = "";
			Player arg1 = null;
			int arg2 = -1;
			boolean console = !(sender instanceof Player);

			if(console || checkPermission(Bukkit.getPlayer(sender.getName()), args[0].toLowerCase())) {
				if(args.length >= 2 && args[1] != null && args[1] != "") {
					if (Bukkit.getPlayer(args[1]) != null) {
						if (Bukkit.getPlayer(args[1]).isOnline()) {
							arg1 = Bukkit.getPlayer(args[1]);
						} else {
							msg = args[1] + " needs to be online!";
						}
					} else if (console) {
						arg2 = 0;
					} else if (affinity.hasDifficulty(args[1])) {
						arg2 = affinity.getDifficultyAffinity(args[1]);
					} else if (args[1].equalsIgnoreCase("world")) {
						arg2 = 0;
					} else if (Pattern.compile("(?i)[^a-zA-Z_&&[0-9]]").matcher(args[1]).find()) {
						arg2 = Integer.parseInt(args[1]);
					} else {
						msg = args[1] + " isn't a recognized difficulty or online player";
					}
				}
				if(args.length >= 3 && args[2] != null && args[2] != "") {
					if (affinity.hasDifficulty(args[2])) {
						arg2 = affinity.getDifficultyAffinity(args[2]);
					} else if (Pattern.compile("(?i)[^a-zA-Z_&&[0-9]]").matcher(args[2]).find()) {
						arg2 = Integer.parseInt(args[2]);
					} else if (args[1].equalsIgnoreCase("world")) {
						arg2 = Integer.parseInt(args[2]);
					} else {
						msg = args[2] + " isn't a recognized difficulty or number";
					}
				}

				// No switch statement so earlier Java Versions are compatible
				if(msg == ""){
					if (args[0].equalsIgnoreCase("set")) { msg = setAffinity(arg2); }
					else if (args[0].equalsIgnoreCase("get") && arg1 == null && arg2 != -1){ msg = getWorldAffinity(); }
					else if (args[0].equalsIgnoreCase("get")){ msg =  getAffinity(arg1); }
					else if (args[0].equalsIgnoreCase("add")){ msg = addAffinity(arg2); }
					else if (args[0].equalsIgnoreCase("remove")){ msg = addAffinity(arg2 * -1); }
					else if (args[0].equalsIgnoreCase("author")){ msg = "The author of this plugin is: SkinnyJeans. Thank you for asking about me!"; }
					else { msg = "Sorry, I don't recognize the command: " + args[0]; }
				}
			}
			else {
				msg = "You don't have permission to do that";
			}

			if (console) {
				Bukkit.getConsoleSender().sendMessage(msg);
			} else {
				((Player) sender).getPlayer().sendMessage(msg);
			}
			return true;
		}
		return false;
	}

	/**
	 * Checks the permission of the player
	 * 
	 * @param user is the User that will need to be checked
	 * @param perm is the permission to check
	 * @return Boolean whether this player has the permission or not
	 */
	private boolean checkPermission(Player user, String perm) {
		return user.hasPermission("affinity." + perm) || user.isOp() || user.hasPermission("affinity.*") ;
	}

	/**
	 * Sets the affinity of the world
	 * 
	 * @param amount of affinity that will be set to the world
	 * @return String about how it was executed
	 */
	private String setAffinity(int amount) {
		try {
			amount = affinity.calcAffinity(amount);
			affinity.setAffinityWorld(amount);
			return "World set on "+affinity.calcDifficulty()+" Difficulty with "+amount+" Affinity points";
		}
		catch(Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
			return "Something went wrong, please check the console for more info";
		}
	}
	
	/**
	 * Gets the affinity of the player
	 * 
	 * @param user is the User who's affinity needs to be returned
	 * @return Amount of affinity a user has or an error
	 */
	private String getAffinity(Player user) {
		try {
			return user.getName()+" is on "+affinity.calcDifficultyUser(user.getUniqueId())+" Difficulty with "+affinity.getAffinityUser(user.getUniqueId())+" Affinity points \nmax affinity: "+affinity.getMaxAffinityUser(user.getUniqueId());
		}
		catch(NullPointerException e){
			return "You forgot to include the user!";
		}
		catch(Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
			return "Something went wrong, please check the console for more info";
		}
	}
	
	/**
	 * Gets the world difficulty
	 * 
	 * @return Amount of affinity the world is at or an error
	 */
	private String getWorldAffinity() {
		try {
			return "World is on "+affinity.calcDifficulty()+" Difficulty with "+affinity.getAffinityWorld()+" Affinity points";
		}
		catch(Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
			return "Something went wrong, please check the console for more info";
		}
	}
	
	/**
	 * Adds a given amount of affinity to the world
	 * 
	 * @param amount of affinity that will be added to the world
	 * @return String about how it was executed
	 */
	private String addAffinity(int amount) {
		try {
			int x = affinity.calcAffinity(affinity.getAffinityWorld() + amount);
			affinity.setAffinityWorld(x);
			return "World is on "+affinity.calcDifficulty()+" Difficulty with "+affinity.getAffinityWorld()+" Affinity points";
		}
		catch(Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
			return "Something went wrong, please check the console for more info";
		}
	}
}
