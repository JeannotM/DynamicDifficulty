package me.skinnyjeans.gmd.commands;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.skinnyjeans.gmd.WorldAffinity;

public class WorldCommands implements CommandExecutor{
	
	private WorldAffinity affinity = null;
	
	public WorldCommands(WorldAffinity wa) {
		affinity = wa;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(label.equalsIgnoreCase("affinity")) {
			String msg = "";
			boolean console = false;
			
			if(!(sender instanceof Player))
				console = true;
			
			if(checkPermission(Bukkit.getPlayer(sender.getName()), args[0].toLowerCase()) || console) {
				try {
					switch(args[0].toLowerCase()){
						case "set":
							if(args[1].toLowerCase() == "world")
								msg = setAffinity(Integer.parseInt(args[2]));
							if(args.length == 2)
								msg = setAffinity(Integer.parseInt(args[1]));
							break;
						case "get":
							if(args.length == 1 || args[1].equalsIgnoreCase("world")) {
								msg = getWorldAffinity();
							}
							else {
								msg = getAffinity(Bukkit.getPlayer(args[1]));
							}
							break;
						case "add":
							if(args[1].equalsIgnoreCase("world"))
								msg = addAffinity(Integer.parseInt(args[2]));
							if(args.length == 2)
								msg = addAffinity(Integer.parseInt(args[1]));
							break;
						case "remove":
							if(args[1].equalsIgnoreCase("world"))
								msg = addAffinity(Integer.parseInt(args[2])*-1);
							if(args.length == 2)
								msg = addAffinity(Integer.parseInt(args[1])*-1);
							break;
						case "author":
							msg = "The author of this plugin is: SkinnyJeans";
							break;
						default:
							msg = "Sorry, I don't recognize the command: " + args[0];
							break;
					}
				}
				catch(NumberFormatException e) {
					msg = "Second argument requires a number";
				}
				catch(Exception e) {
					msg = "Something went wrong, please check the console for more info";
					System.out.println(e);
				}
			}
			else {
				msg = "You don't have permission to do that";
			}
			
			if (sender instanceof Player) {
				((Player) sender).getPlayer().sendMessage(msg);
			} else {
				Bukkit.getConsoleSender().sendMessage(msg);
			}
			return true;
		}
		return false;
	}

	/**
	 * Sets the affinity for the player
	 * 
	 * @param UUID of the user
	 * @param perm is the permission to check
	 * @return Boolean whether this player has the permission or not
	 */
	private boolean checkPermission(Player user, String perm) {
		if(user.hasPermission("affinity."+perm) || user.isOp())
			return true;
		return false;
	}

	/**
	 * Sets the affinity of the world
	 * 
	 * @param Amount of affinity that will be set to the world
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
	 * @param UUID of the user
	 * @return Amount of affinity a user has or an error
	 */
	private String getAffinity(Player user) {
		try {
			return user.getName()+" is on "+affinity.calcDifficultyUser(user.getUniqueId())+" Difficulty with "+affinity.getAffinityUser(user.getUniqueId())+" Affinity points \nmax affinity: "+affinity.getMaxAffinityUser(user.getUniqueId());
		}
		catch(NullPointerException e) {
			return "I'm sorry, this user doesn't exist";
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
	 * @param Amount of affinity that will be added to the world
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
