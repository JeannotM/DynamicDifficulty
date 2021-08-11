package me.skinnyjeans.gmd.commands;

import me.skinnyjeans.gmd.Affinity;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class AffinityCommands implements CommandExecutor {
    private final Affinity af;
    private final ArrayList<String> oneArg = new ArrayList<>(Arrays.asList("author", "reload", "force-save", "playergui"));

    public AffinityCommands(Affinity af) {
        this.af = af;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean console = !(sender instanceof Player);
        String arg1 = "";
        String msg = "";
        int arg2 = -1;

        if(args.length==0)
            return sendMSG("You forgot to include any arguments!", sender, false);

        if(console || checkPermission(Bukkit.getPlayer(sender.getName()), args[0].toLowerCase())) {
            if(!oneArg.contains(args[0]) && args.length==1)
                msg = "You forgot to include a user!";

            if(msg == "" && args.length >= 2 && args[1] != null && args[1] != "") {
                if (args[1].equalsIgnoreCase("world")) {
                    arg1 = "world";
                } else if (af.getPlayerUUID(args[1]) != null) {
                    if(af.getVariable("unload-player") == -1) {
                        if(af.getAffinity(af.getPlayerUUID(args[1])) != -1) {
                            arg1 = args[1];
                        } else {
                            msg = args[1] + " hasn't been online yet!";
                        }
                    } else if (Bukkit.getOfflinePlayer(af.getPlayerUUID(args[1])).isOnline()) {
                        arg1 = Bukkit.getPlayer(args[1]).getName();
                    }
                }
                if(arg1 == null && msg.equals(""))
                    msg = args[1] + " needs to be online!";
            }

            if(msg == "" && args.length >= 3 && args[2] != null && args[2] != "") {
                if (af.hasDifficulty(args[2])) {
                    arg2 = af.getDifficultyAffinity(args[2]);
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
            if(msg.equals("")) {
                if(args[0].equalsIgnoreCase("get")){ msg = getAffinity(arg1); }
                else if(args[0].equalsIgnoreCase("set")){ msg = setAffinity(arg1, arg2); }
                else if(args[0].equalsIgnoreCase("add")){ msg = setAffinity(arg1, af.getAffinity(Bukkit.getPlayer(arg1).getUniqueId()) + (arg2)); }
                else if(args[0].equalsIgnoreCase("remove")){ msg = setAffinity(arg1, af.getAffinity(Bukkit.getPlayer(arg1).getUniqueId()) + (arg2*-1)); }
                else if(args[0].equalsIgnoreCase("setmax")){ msg = setMaxAffinity(arg1, arg2); }
                else if(args[0].equalsIgnoreCase("delmax")){ msg = removeMaxAffinity(arg1); }
                else if(args[0].equalsIgnoreCase("setmin")){ msg = setMinAffinity(arg1, arg2); }
                else if(args[0].equalsIgnoreCase("delmin")){ msg = removeMinAffinity(arg1); }
                else if(args[0].equalsIgnoreCase("reload")){ msg = reloadConfig(); }
                else if(args[0].equalsIgnoreCase("force-save")){ msg = forceSave(); }
                else if(args[0].equalsIgnoreCase("author")){ msg = "The author of this plugin is: SkinnyJeans. Thank you for asking about me!"; }
                else if(args[0].equalsIgnoreCase("playergui") && !console){ af.openPlayersInventory(Bukkit.getPlayer(arg1), 0); return true; }
                else { return sendMSG("Sorry, I don't recognize the command: " + args[0],sender,false); }
            } else {
                return sendMSG(msg,sender,false);
            }
            return sendMSG(msg,sender,true);
        } else {
            return sendMSG("You don't have the permission to do that",sender,true);
        }
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
            if(!user.equalsIgnoreCase("world"))
                uuid = af.getPlayerUUID(user);

            amount = af.calcAffinity(uuid, amount);
            af.setAffinity(uuid, amount);
            return user+"'s set to "+ af.calcDifficulty(uuid)+" Difficulty with "+amount+" Affinity points";
        } catch(Exception e) {
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
            if(!user.equalsIgnoreCase("world"))
                uuid = af.getPlayerUUID(user);

            String msg = user+" has "+ af.getAffinity(uuid)+" Affinity points";
            msg+="\nCurrently on "+ af.calcDifficulty(uuid)+" Difficulty";
            if(uuid != null && af.getMaxAffinity(uuid) != -1)
                msg+="\nMax affinity: "+ af.getMaxAffinity(uuid);
            if(uuid != null && af.getMinAffinity(uuid) != -1)
                msg+="\nMin affinity: "+ af.getMinAffinity(uuid);
            return msg;
        } catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return "Something went wrong, please check the console for more info";
        }
    }

    /**
     * Sets the max affinity for the player
     *
     * @param user is the User who's affinity needs to be changed
     * @param amount of affinity that will be set to this user
     * @return String about how it was executed
     */
    private String setMaxAffinity(String user, int amount) {
        try {
            if(user.equalsIgnoreCase("world"))
                return "The world doesn't need a Max Affinity!";

            UUID uuid = af.getPlayerUUID(user);
            amount = af.calcAffinity(null, amount);
            af.setMaxAffinity(uuid, amount);
            return "Set the Max Affinity to "+amount+" for "+user;
        } catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return "Something went wrong, please check the console for more info";
        }
    }

    private String forceSave() {
        af.saveData();
        return "saved Succesfully";
    }

    /**
     * Removes the max affinity for the player
     *
     * @param user is the User who's affinity needs to be changed
     * @return String about how it was executed
     */
    private String removeMaxAffinity(String user) {
        try {
            if(user.equalsIgnoreCase("world"))
                return "The world doesn't have a Max Affinity!";
            UUID uuid = af.getPlayerUUID(user);
            af.setMaxAffinity(uuid, -1);
            return "Removed the Max Affinity for "+user;
        }
        catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return "Something went wrong, please check the console for more info";
        }
    }

    /**
     * Sets the min affinity for the player
     *
     * @param user is the User who's affinity needs to be changed
     * @param amount of affinity that will be set to this user
     * @return String about how it was executed
     */
    private String setMinAffinity(String user, int amount) {
        try {
            if(user.equalsIgnoreCase("world"))
                return "The world doesn't need a Min Affinity!";

            UUID uuid = af.getPlayerUUID(user);
            amount = af.calcAffinity(null, amount);
            af.setMinAffinity(uuid, amount);
            return "Set the Min Affinity to "+amount+" for "+user;
        } catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return "Something went wrong, please check the console for more info";
        }
    }

    /**
     * Removes the min affinity for the player
     *
     * @param user is the User who's affinity needs to be changed
     * @return String about how it was executed
     */
    private String removeMinAffinity(String user) {
        try {
            if(user.equalsIgnoreCase("world"))
                return "The world doesn't have a Min Affinity!";
            UUID uuid = af.getPlayerUUID(user);
            af.setMinAffinity(uuid, -1);
            return "Removed the Min Affinity for "+user;
        }
        catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return "Something went wrong, please check the console for more info";
        }
    }

    private String reloadConfig() {
        af.reloadConfig();
        return "Succesfully reloaded the config!";
    }

    private boolean sendMSG(String msg, CommandSender sender, boolean r) {
        if (sender instanceof Player) {
            ((Player) sender).getPlayer().sendMessage(msg);
        } else {
            Bukkit.getConsoleSender().sendMessage(msg);
        }
        return r;
    }
}
