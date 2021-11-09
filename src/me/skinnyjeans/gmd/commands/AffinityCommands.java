package me.skinnyjeans.gmd.commands;

import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.DataManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

public class AffinityCommands implements CommandExecutor {
    private final Affinity af;
    private final FileConfiguration lang;
    private final String difficulty = "%difficulty%";
    private final String number = "%number%";
    private final String pUser = "%user%";
    private final ArrayList<String> noArg = new ArrayList<>(Arrays.asList("author", "reload", "force-save", "help", "playergui"));
    private final ArrayList<String> oneArg = new ArrayList<>(Arrays.asList("get", "delmax", "delmin"));
    private final ArrayList<String> twoArg = new ArrayList<>(Arrays.asList("add", "remove", "set", "setmax", "setmin"));

    public AffinityCommands(Affinity af, DataManager data) {
        this.af = af;
        this.lang = data.getLang();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0)
            return sendMSG(returnHelp(label, (Player)sender), sender, false);

        boolean console = !(sender instanceof Player);
        HashSet<String> playerList = new HashSet<>();
        String userName = "";
        String cmd = args[0].toLowerCase();

        if(noArg.contains(cmd)){
            if(console || hasPermission(Bukkit.getPlayer(af.getPlayerUUID(sender.getName())), cmd)) {
                if(cmd.equals("author")){ return sendMSG(sendAuthor((Player)sender), sender, true); }
                else if(cmd.equals("reload")){ return sendMSG(reloadConfig(), sender, true); }
                else if(cmd.equals("force-save")){ return sendMSG(forceSave(), sender, true); }
                else if(cmd.equals("help")){ return sendMSG(returnHelp(label, (Player)sender), sender, true); }
                else if(cmd.equals("playergui")){
                    if(console)
                        return sendMSG(lang.getString("error.console.cannot-open-playergui"), sender, false);
                    af.openPlayersInventory(Bukkit.getOfflinePlayer(af.getPlayerUUID(sender.getName())).getPlayer(), 0);
                    return true;
                }
            }
            return sendMSG(lang.getString("error.no-permission"), sender, false);
        } else if(oneArg.contains(cmd)) {
            if (args.length == 1) {
                if(!console) {
                    if(af.getPlayerUUID(sender.getName()) != null) {
                        userName = sender.getName();
                    } else {
                        return sendMSG(lang.getString("error.cannot-find-self"), sender, false);
                    }
                } else {
                    return sendMSG(lang.getString("error.console.no-player-name"), sender, false);
                }
            } else {
                String p = args[1].toLowerCase();
                if(p.equals("@a")) { Bukkit.getOnlinePlayers().forEach(pl -> playerList.add(pl.getName())); }
                else if(p.equals("@p") || p.equals("@r")) { userName = Bukkit.selectEntities(sender, p).get(0).getName(); }
                else if(p.equals("@s")) {
                    if(console)
                        return sendMSG(lang.getString("error.console.console-selected"), sender, false);
                    userName = sender.getName();
                }

                if(playerList.size() != 0) {
                    if(args.length < 3 || !args[2].equalsIgnoreCase("force"))
                        return sendMSG(lang.getString("error.add-force"), sender, false);
                } else if(args[1].equalsIgnoreCase("world") || af.getPlayerUUID(args[1]) != null) {
                    userName = args[1];
                } else {
                    return sendMSG(lang.getString("error.cannot-be-found").replaceAll(pUser, args[1]), sender, false);
                }
            }

            if(!console)
                if(sender.getName().equalsIgnoreCase(userName)) {
                    if(!hasSelfPermission(Bukkit.getPlayer(af.getPlayerUUID(sender.getName())), cmd))
                        return sendMSG(lang.getString("error.no-permission"), sender, false);
                } else {
                    if(!hasOtherPermission(Bukkit.getPlayer(af.getPlayerUUID(sender.getName())), cmd))
                        return sendMSG(lang.getString("error.no-permission"), sender, false);
                }
            if(playerList.size() != 0) {
                String msg = "";
                for(String user : playerList) {
                    if(cmd.equals("delmax")){ msg = removeMaxAffinity(user); }
                    else if(cmd.equals("delmin")){ msg = removeMinAffinity(user);}
                    else if(cmd.equals("get")){ return sendMSG(lang.getString("command.selector.cannot-use-get-for-all"), sender, false);}
                }
                if(!msg.startsWith("Something went wrong")) {
                    if(cmd.equals("delmax")){ return sendMSG(lang.getString("command.selector.all-max-affinity-removed"), sender, true); }
                    else if(cmd.equals("delmin")){ return sendMSG(lang.getString("command.selector.all-min-affinity-removed"), sender, true); }
                }
                return sendMSG(msg, sender, false);
            } else {
                if(cmd.equals("delmax")){ return sendMSG(removeMaxAffinity(userName), sender, true); }
                else if(cmd.equals("delmin")){ return sendMSG(removeMinAffinity(userName), sender, true);}
                else if(cmd.equals("get")){ return sendMSG(getAffinity(userName), sender, true);}
            }

        } else if(twoArg.contains(cmd)) {
            int numberGiven = -1;
            if (args.length == 1) {
                return sendMSG(lang.getString("error.include-number"), sender, false);
            } else if (args.length == 2) {
                if(!console) {
                    if(af.getPlayerUUID(sender.getName()) != null) {
                        userName = sender.getName();
                        if (af.hasDifficulty(args[1])) {
                            numberGiven = af.getDifficultyAffinity(args[1]);
                        } else {
                            try {
                                numberGiven = Integer.parseInt(args[1]);
                            } catch(Exception ignored) {
                                return sendMSG(lang.getString("error.not-a-number").replaceAll(number, args[1]), sender, false);
                            }
                        }
                    } else {
                        return sendMSG(lang.getString("error.cannot-find-self"), sender, false);
                    }
                } else {
                    return sendMSG(lang.getString("error.console.no-player-name"), sender, false);
                }
            } else {
                String p = args[1].toLowerCase();
                if(p.equals("@a")) { Bukkit.getOnlinePlayers().forEach(pl -> playerList.add(pl.getName())); }
                else if(p.equals("@p") || p.equals("@r")) { userName = Bukkit.selectEntities(sender, p).get(0).getName(); }
                else if(p.equals("@s")) {
                    if(console)
                        return sendMSG(lang.getString("error.console.console-selected"), sender, false);
                    userName = sender.getName();
                }

                if(playerList.size() != 0) {
                    if(args.length < 4 || !args[3].equalsIgnoreCase("force"))
                        return sendMSG(lang.getString("error.add-force"), sender, false);
                } else if(userName.equals("")){
                    if(args[1].equalsIgnoreCase("world") || af.getPlayerUUID(args[1]) == null)
                        return sendMSG(lang.getString("error.cannot-be-found").replaceAll(pUser, args[1]), sender, false);
                    userName = args[1];
                }

                if (af.hasDifficulty(args[2])) {
                    numberGiven = af.getDifficultyAffinity(args[2]);
                } else {
                    try {
                        numberGiven = Integer.parseInt(args[2]);
                    } catch(Exception ignored) {
                        return sendMSG(lang.getString("error.not-a-number").replaceAll(number, args[2]), sender, false);
                    }
                }
            }

            if(sender.getName().equalsIgnoreCase(userName)) {
                if(!hasSelfPermission(Bukkit.getOfflinePlayer(af.getPlayerUUID(sender.getName())).getPlayer(), cmd))
                    return sendMSG(lang.getString("error.no-permission"), sender, false);
            } else {
                if(!hasOtherPermission(Bukkit.getOfflinePlayer(af.getPlayerUUID(sender.getName())).getPlayer(), cmd))
                    return sendMSG(lang.getString("error.no-permission"), sender, false);
            }

            if(playerList.size() != 0) {
                String msg = "";
                for(String user : playerList) {
                    if(cmd.equals("remove")){ msg = setAffinity(user, af.getAffinity(af.getPlayerUUID(user)) + (numberGiven * -1)); }
                    else if(cmd.equals("setmax")){ msg = setMaxAffinity(user, numberGiven); }
                    else if(cmd.equals("setmin")){ msg = setMinAffinity(user, numberGiven); }
                    else if(cmd.equals("set")){ msg = setAffinity(user, numberGiven); }
                    else if(cmd.equals("add")){ msg = setAffinity(user, af.getAffinity(af.getPlayerUUID(user)) + numberGiven); }
                }
                if(!msg.equals(lang.getString("error.something-wrong"))) {
                    if(cmd.equals("remove")){ return sendMSG(lang.getString("command.remove.remove-from-all").replaceAll(number, numberGiven + ""), sender, true); }
                    else if(cmd.equals("setmax")){ return sendMSG(lang.getString("command.set.max-affinity-for-all").replaceAll(number, numberGiven + ""), sender, true); }
                    else if(cmd.equals("setmin")){ return sendMSG(lang.getString("command.set.min-affinity-for-all").replaceAll(number, numberGiven + ""), sender, true); }
                    else if(cmd.equals("set")){ return sendMSG(lang.getString("command.set.affinity-for-all").replaceAll(number, numberGiven + ""), sender, true); }
                    else if(cmd.equals("add")){ return sendMSG(lang.getString("command.add.added-to-all").replaceAll(number, numberGiven + ""), sender, true); }
                }
                return sendMSG(msg, sender, false);
            } else {
                if(cmd.equals("remove")){ return sendMSG(setAffinity(userName, af.getAffinity(af.getPlayerUUID(userName)) + (numberGiven * -1)), sender, true); }
                else if(cmd.equals("setmax")){ return sendMSG(setMaxAffinity(userName, numberGiven), sender, true); }
                else if(cmd.equals("setmin")){ return sendMSG(setMinAffinity(userName, numberGiven), sender, true); }
                else if(cmd.equals("set")){ return sendMSG(setAffinity(userName, numberGiven), sender, true); }
                else if(cmd.equals("add")){ return sendMSG(setAffinity(userName, af.getAffinity(af.getPlayerUUID(userName)) + numberGiven), sender, true); }
            }
        }
        return sendMSG("Command "+cmd+" doesn't exist!", sender, false);
    }

    /**
     * Checks the permission of the player
     *
     * @param user is the User that will need to be checked
     * @param perm is the permission to check
     * @return Boolean whether this player has the permission or not
     */
    private boolean hasPermission(Player user, String perm) {
        return user.hasPermission("affinity." + perm) || user.hasPermission("affinity.*") || user.isOp() ;
    }
    private boolean hasSelfPermission(Player user, String perm) {
        return user.hasPermission("affinity." + perm + ".self") || hasPermission(user, perm) ;
    }
    private boolean hasOtherPermission(Player user, String perm) {
        return user.hasPermission("affinity." + perm + ".other") || hasPermission(user, perm) ;
    }

    /**
     * Sends information about all the commands to the Player
     *
     * @param labelUsed The label the player used to call the /help command
     * @param user The Player the message needs to be sent to
     * @return An empty String
     */
    private String returnHelp(String labelUsed, Player user) {
        ChatColor r = ChatColor.RED;
        String s = r + "/" + labelUsed + " ";
        String arrow = ChatColor.GRAY+"> "+ChatColor.GOLD;
        String nameNnum = " <?Name> <Number>";
        String name = " <?Name>";

        user.sendMessage(r+"======== "+ ChatColor.GOLD+"["+ChatColor.YELLOW+"DynamicDifficulty"+ChatColor.GOLD+"] "+r+"========"+ChatColor.WHITE);
        user.sendMessage("Click or hover over commands for explanations");
        user.sendMessage("Providing a Difficulty instead of a Number is also fine");
        user.sendMessage("Will use the user who sent the request if no name was given");
        user.spigot().sendMessage(constructText(s+"setmax"+nameNnum, arrow+"Sets the MaxAffinity of a Player"));
        user.spigot().sendMessage(constructText(s+"setmin"+nameNnum, arrow+"Sets the MinAffinity of a Player"));
        user.spigot().sendMessage(constructText(s+"set"+nameNnum, arrow+"Sets the Affinity of a Player"));
        user.spigot().sendMessage(constructText(s+"remove"+nameNnum, arrow+"Removes n points from a Player's Affinity"));
        user.spigot().sendMessage(constructText(s+"add"+nameNnum, arrow+"Adds n points to a Player's Affinity"));
        user.spigot().sendMessage(constructText(s+"delmax"+name, arrow+"Removes the MaxAffinity of a Player"));
        user.spigot().sendMessage(constructText(s+"delmin"+name, arrow+"Removes the MinAffinity of a Player"));
        user.spigot().sendMessage(constructText(s+"get"+name, arrow+"Gets a Player's Min, Max and normal Affinity"));
        user.spigot().sendMessage(constructText(s+"author", arrow+"Returns the Authors name"));
        user.spigot().sendMessage(constructText(s+"reload", arrow+"Reloads Dynamic Difficulty (safely)"));
        user.spigot().sendMessage(constructText(s+"force-save", arrow+"Saves the Players Affinity"));
        user.spigot().sendMessage(constructText(s+"playergui", arrow+"Shows all players in a chest gui"));
        return "";
    }

    private TextComponent constructText(String cmd, String hoverItem) {
        TextComponent message = new TextComponent(cmd);
        String cmdSuggest = cmd.split(" ")[0].substring(2) + " " + cmd.split(" ")[1]+ " ";
        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmdSuggest));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(cmd + " " + hoverItem)));
        return message;
    }

    /**
     * Sets the affinity for the player or the world
     *
     * @param user is the User whose affinity needs to be changed
     * @param amount of affinity that will be set to this user
     * @return String about how it was executed
     */
    private String setAffinity(String user, int amount) {
        try {
            UUID uuid = !user.equalsIgnoreCase("world") ? af.getPlayerUUID(user) : null;
            amount = af.calcAffinity(uuid, amount);
            af.setAffinity(uuid, amount);
            return lang.getString("command.set.set-affinity").replaceAll(number, amount+"").replaceAll(difficulty, af.calcDifficulty(uuid)).replaceAll(pUser, user);
        } catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return lang.getString("error.something-wrong");
        }
    }

    /**
     * Sets the max affinity for the player
     *
     * @param user is the User whose affinity needs to be changed
     * @param amount of affinity that will be set to this user
     * @return String about how it was executed
     */
    private String setMaxAffinity(String user, int amount) {
        try {
            if(user.equalsIgnoreCase("world"))
                return lang.getString("error.world.need-max-affinity");

            UUID uuid = af.getPlayerUUID(user);
            amount = af.calcAffinity(null, amount);
            af.setMaxAffinity(uuid, amount);
            return lang.getString("command.set.set-max-affinity").replaceAll(pUser, user).replaceAll(number, amount+"").replaceAll(difficulty, af.calcDifficulty(uuid));
        } catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return lang.getString("error.something-wrong");
        }
    }

    /**
     * Sets the min affinity for the player
     *
     * @param user is the User whose affinity needs to be changed
     * @param amount of affinity that will be set to this user
     * @return String about how it was executed
     */
    private String setMinAffinity(String user, int amount) {
        try {
            if(user.equalsIgnoreCase("world"))
                return lang.getString("error.world.need-min-affinity");

            UUID uuid = af.getPlayerUUID(user);
            amount = af.calcAffinity(null, amount);
            af.setMinAffinity(uuid, amount);
            return lang.getString("command.set.set-min-affinity").replaceAll(pUser, user).replaceAll(number, amount+"").replaceAll(difficulty, af.calcDifficulty(uuid));
        } catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return lang.getString("error.something-wrong");
        }
    }

    /**
     * Removes the max affinity for the player
     *
     * @param user is the User whose affinity needs to be changed
     * @return String about how it was executed
     */
    private String removeMaxAffinity(String user) {
        try {
            if(user.equalsIgnoreCase("world"))
                return lang.getString("error.world.have-max-affinity");

            af.setMaxAffinity(af.getPlayerUUID(user), -1);
            return lang.getString("command.remove.max-affinity").replaceAll(pUser, user);
        }
        catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return lang.getString("error.something-wrong");
        }
    }

    /**
     * Removes the min affinity for the player
     *
     * @param user is the User whose affinity needs to be changed
     * @return String about how it was executed
     */
    private String removeMinAffinity(String user) {
        try {
            if(user.equalsIgnoreCase("world"))
                return lang.getString("error.world.have-min-affinity");
            af.setMinAffinity(af.getPlayerUUID(user), -1);
            return lang.getString("command.remove.min-affinity").replaceAll(pUser, user);
        }
        catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return lang.getString("error.something-wrong");
        }
    }

    /**
     * Gets the affinity of a player/world
     *
     * @param user is the User whose affinity needs to be returned
     * @return Amount of affinity a user has or an error
     */
    private String getAffinity(String user) {
        try {
            UUID uuid = !user.equalsIgnoreCase("world") ? af.getPlayerUUID(user) : null;

            String msg = lang.getString("command.get.has-affinity").replaceAll(pUser, user).replaceAll(number, af.getAffinity(uuid)+"").replaceAll(difficulty, af.calcDifficulty(uuid));
            msg+="\n"+lang.getString("command.get.currently-on").replaceAll(number, af.getAffinity(uuid)+"").replaceAll(difficulty, af.calcDifficulty(uuid));
            if(uuid != null && af.getMaxAffinity(uuid) != -1)
                msg+="\n"+lang.getString("command.get.max-affinity").replaceAll(number, af.getMaxAffinity(uuid)+"");
            if(uuid != null && af.getMinAffinity(uuid) != -1)
                msg+="\n"+lang.getString("command.get.min-affinity").replaceAll(number, af.getMinAffinity(uuid)+"");
            return msg;
        } catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception caught: "+e);
            return lang.getString("error.something-wrong");
        }
    }

    /**
     * Sends the author with a Hover and Click event to a player
     *
     * @param sender The Player in question
     * @return An empty message
     */
    private String sendAuthor(Player sender) {
        TextComponent message = new TextComponent("The author of DynamicDifficulty is: SkinnyJeans, click for the Spigot page");
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Thanks for asking about me!")));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/dynamic-difficulty.92025/"));
        sender.spigot().sendMessage(message);
        return "";
    }

    private String forceSave() {
        af.saveData();
        return "saved Succesfully";
    }

    private String reloadConfig() {
        af.reloadConfig();
        return "Succesfully reloaded the config!";
    }

    /**
     * This function send a given message to the player
     *
     * @param msg The message to return to the Player
     * @param sender The Player (or Console) in question
     * @param r The boolean to return
     * @return a boolean
     */
    private boolean sendMSG(String msg, CommandSender sender, boolean r) {
        if(msg == null || msg.equals(""))
            return r;

        String coloredMessage = ((r) ? ChatColor.WHITE : ChatColor.RED) + msg;

        if (sender instanceof Player) {
            ((Player) sender).getPlayer().sendMessage(coloredMessage);
        } else {
            Bukkit.getConsoleSender().sendMessage(coloredMessage);
        }
        return r;
    }
}
