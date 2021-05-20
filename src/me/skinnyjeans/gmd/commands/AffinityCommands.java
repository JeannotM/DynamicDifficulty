package me.skinnyjeans.gmd.commands;

import me.skinnyjeans.gmd.Affinity;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class AffinityCommands implements CommandExecutor {
    private Affinity affinity;

    public AffinityCommands(Affinity af) {
        affinity = af;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(label.equalsIgnoreCase("affinity")) {
            boolean console = !(sender instanceof Player);
            String arg1 = "";
            String msg = "";
            int arg2 = -1;

            if(args.length==0)
                return sendMSG("You forgot to include any arguments!", sender, false);

            if(console || checkPermission(Bukkit.getPlayer(sender.getName()), args[0].toLowerCase())) {
                if(!args[0].equalsIgnoreCase("author") && args.length==1)
                    msg = "You forgot to include a user!";

                if(msg == "" && args.length >= 2 && args[1] != null && args[1] != "") {
                    if (args[1].equalsIgnoreCase("world")) {
                        arg1 = "world";
                    } else if (Bukkit.getPlayer(args[1]) != null) {
                        if (Bukkit.getPlayer(args[1]).isOnline()) {
                            arg1 = Bukkit.getPlayer(args[1]).getName();
                        } else {
                            msg = args[1] + " needs to be online!";
                        }
                    } else {
                        msg = args[1] + " isn't a recognized online player";
                    }
                }

                if(msg == "" && args.length >= 3 && args[2] != null && args[2] != "") {
                    if (affinity.hasDifficulty(args[2])) {
                        arg2 = affinity.getDifficultyAffinity(args[2]);
                    } else if (Pattern.compile("(?i)[^a-zA-Z_&&[0-9]]").matcher(args[2]).find() || args[1].equalsIgnoreCase("world")) {
                        try{
                            arg2 = Integer.parseInt(args[2]);
                        }
                        catch(Exception e){
                            msg = args[2] + " isn't a recognized difficulty or number";
                        }

                    } else {
                        msg = args[2] + " isn't a recognized difficulty or number";
                    }
                }

                // No switch statement so earlier Java Versions are compatible
                if(msg == ""){
                    if(args[0].equalsIgnoreCase("get")){ msg = getAffinity(arg1); }
                    else if(args[0].equalsIgnoreCase("set")){ msg = setAffinity(arg1, arg2); }
                    else if(args[0].equalsIgnoreCase("add")){ msg = addAffinity(arg1, arg2); }
                    else if(args[0].equalsIgnoreCase("remove")){ msg = addAffinity(arg1, arg2*-1); }
                    else if(args[0].equalsIgnoreCase("setmax")){ msg = setMaxAffinity(arg1, arg2); }
                    else if(args[0].equalsIgnoreCase("removemax")){ msg = removeMaxAffinity(arg1); }
                    else if(args[0].equalsIgnoreCase("author")){ msg = "The author of this plugin is: SkinnyJeans. Thank you for asking about me!";; }
                    else { return sendMSG("Sorry, I don't recognize the command: " + args[0],sender,false); }
                } else {
                    return sendMSG(msg,sender,false);
                }
                return sendMSG(msg,sender,true);
            }
            else {
                return sendMSG("You don't have permission to do that",sender,true);
            }
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
     * Sets the affinity for the player or the world
     *
     * @param user is the User who's affinity needs to be changed
     * @param amount of affinity that will be set to this user
     * @return String about how it was executed
     */
    private String setAffinity(String user, int amount) {
        try {
            UUID uuid = null;
            if(!user.equalsIgnoreCase("world")) {
                uuid = Bukkit.getPlayer(user).getUniqueId();
            }

            amount = affinity.calcAffinity(uuid, amount);
            affinity.setAffinity(uuid, amount);
            return user+"'s set to "+affinity.calcDifficulty(uuid)+" Difficulty with "+amount+" Affinity points";
        }
        catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return "Something went wrong, please check the console for more info";
        }
    }

    /**
     * Gets the affinity of a player/world
     *
     * @param user is the User who's affinity needs to be returned
     * @return Amount of affinity a user has or an error
     */
    private String getAffinity(String user) {
        try {
            UUID uuid = null;
            if(!user.equalsIgnoreCase("world")) {
                uuid = Bukkit.getPlayer(user).getUniqueId();
            }

            String msg = user+" is on "+affinity.calcDifficulty(uuid)+" Difficulty with "+affinity.getAffinity(uuid)+" Affinity points ";
            if(uuid != null)
                msg+="\nmax affinity: "+affinity.getMaxAffinity(uuid);
            return msg;
        }
        catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return "Something went wrong, please check the console for more info";
        }
    }

    /**
     * Adds a given amount of affinity to a certain player or world
     *
     * @param user is the User who's affinity needs to be changed
     * @param amount of affinity that will be added to this user
     * @return String about how it was executed
     */
    private String addAffinity(String user, int amount) {
        try {
            UUID uuid = null;
            if(!user.equalsIgnoreCase("world")) {
                uuid = Bukkit.getPlayer(user).getUniqueId();
            }

            int x = affinity.calcAffinity(null,affinity.getAffinity(uuid) + amount);
            affinity.setAffinity(uuid, x);
            return "World is on "+affinity.calcDifficulty(uuid)+" Difficulty with "+affinity.getAffinity(uuid)+" Affinity points";
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
    private String setMaxAffinity(String user, int amount) {
        try {
            if(user.equalsIgnoreCase("world")) {
                return "The world doesn't need a max Affinity!";
            }
            UUID uuid = Bukkit.getPlayer(user).getUniqueId();

            amount = affinity.calcAffinity(uuid, amount);
            affinity.setMaxAffinity(uuid, amount);
            return "Set the Max Affinity to "+amount+" for "+user;
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
    private String removeMaxAffinity(String user) {
        try {
            if(user.equalsIgnoreCase("world")) {
                return "The world doesn't have a max Affinity!";
            }
            UUID uuid = Bukkit.getPlayer(user).getUniqueId();
            affinity.setMaxAffinity(uuid, -1);
            return "Removed the Max Affinity for "+user;
        }
        catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return "Something went wrong, please check the console for more info";
        }
    }



    private boolean sendMSG(String msg, CommandSender sender, boolean r){
        if (sender instanceof Player) {
            ((Player) sender).getPlayer().sendMessage(msg);
        } else {
            Bukkit.getConsoleSender().sendMessage(msg);
        }
        return r;
    }
}
