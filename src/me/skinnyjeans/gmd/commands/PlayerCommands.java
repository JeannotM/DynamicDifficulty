package me.skinnyjeans.gmd.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.skinnyjeans.gmd.PlayerAffinity;

public class PlayerCommands implements CommandExecutor {
	
	private PlayerAffinity affinity;
	
	public PlayerCommands(PlayerAffinity pa) {
		affinity = pa;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(label.equalsIgnoreCase("affinity")) {
			String msg = "";
			Player arg1 = null;
			int arg2 = -1;
			boolean console = !(sender instanceof Player);
			if(args.length==0){
				msg = "You forgot to include any arguments!";
				if (console) {
					Bukkit.getConsoleSender().sendMessage(msg);
				} else {
					((Player) sender).getPlayer().sendMessage(msg);
				}
				return false;
			}

			if(console || checkPermission(Bukkit.getPlayer(sender.getName()), args[0].toLowerCase())) {
				if(!args[0].equalsIgnoreCase("author") && args.length==1)
					msg = "You forgot to include a user!";

				if(args.length >= 3 && args[2] != null && args[2] != ""){
					if (affinity.hasDifficulty(args[2])) {
						arg2 = affinity.getDifficultyAffinity(args[2]);
					}
					else if(Pattern.compile("(?i)[^a-zA-Z_&&[0-9]]").matcher(args[2]).find()){
						arg2 = Integer.parseInt(args[2]);
					}
					else {
						msg = args[2]+" isn't a recognized difficulty or number";
					}
				}

				if(args.length >= 2 && args[1] != null && args[1] != "") {
					if (Bukkit.getPlayer(args[1]) != null) {
						if (Bukkit.getPlayer(args[1]).isOnline()) {
							arg1 = Bukkit.getPlayer(args[1]);
						} else {
							msg = args[1]+" needs to be online!";
						}
					}
					else if (console) {
						msg = "You can't change the consoles affinity, Silly!";
					}
					else if (affinity.hasDifficulty(args[1])) {
						arg1 = Bukkit.getPlayer(sender.getName());
						arg2 = affinity.getDifficultyAffinity(args[1]);
					}
					else if (Pattern.compile("(?i)[^a-zA-Z_&&[0-9]]").matcher(args[1]).find()){
						arg1 = Bukkit.getPlayer(sender.getName());
						arg2 = Integer.parseInt(args[1]);
					}
					else {
						msg = args[1]+" isn't a recognized difficulty, number or online player";
					}
				}

				// No switch statement so earlier Java Versions are compatible
				if(msg == ""){
					if (args[0].equalsIgnoreCase("set")){ msg = setAffinity(arg1, arg2); }
					else if (args[0].equalsIgnoreCase("get")){ msg =  getAffinity(arg1); }
					else if (args[0].equalsIgnoreCase("add")){ msg = addAffinity(arg1, arg2); }
					else if (args[0].equalsIgnoreCase("remove")){ msg = addAffinity(arg1, arg2 * -1); }
					else if (args[0].equalsIgnoreCase("setmax")){ msg = setMaxAffinity(arg1, arg2); }
					else if (args[0].equalsIgnoreCase("removemax")) { msg = removeMaxAffinity(arg1); }
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
	 * Sets the affinity for the player
	 * 
	 * @param user is the User that will need to be checked
	 * @param perm is the permission to check
	 * @return Boolean whether this player has the permission or not
	 */
	private boolean checkPermission(Player user, String perm) {
		return user.hasPermission("affinity." + perm) || user.isOp() || user.hasPermission("affinity.*");
	}
	
	/**
	 * Sets the affinity for the player
	 * 
	 * @param user is the User who's affinity needs to be changed
	 * @param amount of affinity that will be set to this user
	 * @return String about how it was executed
	 */
	private String setMaxAffinity(Player user, int amount) {
		try {
			UUID uuid = user.getUniqueId();
			amount = affinity.calcAffinity(uuid, amount);
			affinity.setMaxAffinityUser(uuid, amount);
			return "Set the Max Affinity to "+amount+" for "+user.getName();
		}
		catch(Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
			return "Something went wrong, please check the console for more info";
		}
	}
	
	/**
	 * Removes the max affinity for the player
	 * 
	 * @param user is the User who's affinity needs to be changed
	 * @return String about how it was executed
	 */
	private String removeMaxAffinity(Player user) {
		try {
			affinity.setMaxAffinityUser(user.getUniqueId(), -1);
			return "Removed the Max Affinity for "+user.getName();
		}
		catch(Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
			return "Something went wrong, please check the console for more info";
		}
	}
	
	/**
	 * Sets the affinity for the player
	 * 
	 * @param user is the User who's affinity needs to be changed
	 * @param amount of affinity that will be set to this user
	 * @return String about how it was executed
	 */
	private String setAffinity(Player user, int amount) {
		try {
			UUID uuid = user.getUniqueId();
			amount = affinity.calcAffinity(uuid, amount);
			affinity.setAffinityUser(uuid, amount);
			return user.getName()+" is on "+affinity.calcDifficulty(uuid)+" Difficulty with "+affinity.getAffinityUser(uuid)+" Affinity points";
		}
		catch(Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
			return "Something went wrong, please check the console for more info";
		}
	}
	
	/**
	 * Gets the affinity of the player
	 * 
	 * @param user is the User who's affinity needs to be changed
	 * @return Amount of affinity a user has or an error
	 */
	private String getAffinity(Player user) {
		try {
			return user.getName()+" is on "+affinity.calcDifficulty(user.getUniqueId())+" Difficulty with "+affinity.getAffinityUser(user.getUniqueId())+" Affinity points \nmax affinity: "+affinity.getMaxAffinityUser(user.getUniqueId());
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
	 * Adds a given amount of affinity to a certain player
	 * 
	 * @param user is the User who's affinity needs to be changed
	 * @param amount of affinity that will be added to this user
	 * @return String about how it was executed
	 */
	private String addAffinity(Player user, int amount) {
		try {
			UUID uuid = user.getUniqueId();
			int x = affinity.calcAffinity(uuid, affinity.getAffinityUser(uuid) + amount);
			affinity.setAffinityUser(uuid, x);
			return user.getName()+" is on "+affinity.calcDifficulty(uuid)+" Difficulty with "+x+" Affinity points";
		}
		catch(Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
			return "Something went wrong, please check the console for more info";
		}
	}
}
