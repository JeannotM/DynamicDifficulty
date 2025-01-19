package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.Difficulty;
import me.skinnyjeans.gmd.models.Minecrafter;
import me.skinnyjeans.gmd.utils.StaticInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CommandManager implements CommandExecutor {

    private final MainManager MAIN_MANAGER;

    private static final HashSet<String> NO_ARG = new HashSet<>(Arrays.asList("difficulties", "me", "help", "reload", "author", "forcesave", "forceremoval", "playergui"));
    private static final HashSet<String> ONE_ARG = new HashSet<>(Arrays.asList("delmin", "delmax", "get"));
    private static final HashSet<String> TWO_ARGS = new HashSet<>(Arrays.asList("setmin", "setmax", "set", "remove", "add"));

    private static final String authorMessage = "DynamicDifficulty: SkinnyJeans\nhttps://www.spigotmc.org/resources/dynamic-difficulty.92025/\n\n";
    private static final String PREFIX_NUMBER = "%number%";
    private static final String PREFIX_USER = "%user%";
    private static final String PREFIX_DIFFICULTY = "%difficulty%";

    private String consoleOpenPlayerGUI;
    private String noPermission;
    private String includeNumber;
    private String helpMessage;
    private String notNumber;
    private String notFound;

    private String userAffinityGet;
    private String userDifficultyGet;
    private String userMaxAffinityGet;
    private String userMinAffinityGet;

    private String translatorMessage;
    private String maxAffinityRemoved;
    private String minAffinityRemoved;
    private String minAffinitySet;
    private String maxAffinitySet;
    private String dataSaved;
    private String affinitySet;
    private String configReloaded;

    private String allUserAffinityGet;
    private String allUserAffinitySet;
    private String allUserMaxAffinitySet;
    private String allUserMinAffinitySet;
    private String allUserMinAffinityRemoved;
    private String allUserMaxAffinityRemoved;

    private final HashSet<String> DIFFICULTIES = new HashSet<>();

    public CommandManager(MainManager mainManager) { MAIN_MANAGER = mainManager; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) return sendMessage(sender, helpMessage, true);

        String argument = args[0].toLowerCase();

        if (argument.equals("time")) {
            Player player = (Player)sender;
            sender.sendMessage(player.getWorld().getTime() + "");
            player.sendMessage(player.getWorld().getTime() + "");
            return true;
        }

        if(NO_ARG.contains(argument)) {
            if(hasPermission(sender, argument)) {
                if(argument.equals("author")) return sendMessage(sender, authorMessage + translatorMessage, true);
                if(argument.equals("reload")) return sendMessage(sender, reloadPlugin(), true);
                if(argument.equals("forcesave")) return sendMessage(sender, forceSave(), true);
                if(argument.equals("playergui")) return openPlayerGUI(sender);
                if(argument.equals("difficulties")) return openDifficultyGUI(sender);
                if(argument.equals("me")) return openMyInventoryGUI(sender);
                if(argument.equals("forceremoval")) {
                    OfflinePlayer[] list = Bukkit.getOfflinePlayers();
                    for (OfflinePlayer pl : list) {
                        Player player = pl.getPlayer();
                        if (player != null) {
                            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
                        }
                    }

                    MAIN_MANAGER.getPlugin().onDisable();
                    MAIN_MANAGER.getPlugin().getPluginLoader().disablePlugin(MAIN_MANAGER.getPlugin());
                    return sendMessage(sender, "DynamicDifficulty disabled", true);
                }
            }
        } else if (ONE_ARG.contains(argument)) {
            if(args.length > 1 && args[1].equals("@a")) {
                if(hasAnyPermission(sender, argument, "other")) {
                    if(argument.equals("delmin")) {
                        Set<UUID> keys = MAIN_MANAGER.getPlayerManager().getPlayerList().keySet();
                        for (UUID key : keys) { MAIN_MANAGER.getPlayerManager().setMinAffinity(key, -1); }
                        return sendMessage(sender, allUserMinAffinityRemoved, true);
                    }
                    if(argument.equals("delmax")) {
                        Set<UUID> keys = MAIN_MANAGER.getPlayerManager().getPlayerList().keySet();
                        for (UUID key : keys) { MAIN_MANAGER.getPlayerManager().setMaxAffinity(key, -1); }
                        return sendMessage(sender, allUserMaxAffinityRemoved, true);
                    }
                    if(argument.equals("get")) {
                        return sendMessage(sender, allUserAffinityGet, true);
                    }
                }
            } else {
                String name = args.length > 1 ? args[1] : sender.getName();
                UUID uuid = needsUuid(sender, name);
                if (uuid == null) { return sendMessage(sender, notFound, false); }

                if(hasPermission(sender, name, argument)) {
                    if(argument.equals("delmin")) {
                        Minecrafter data = MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid);
                        MAIN_MANAGER.getPlayerManager().setMinAffinity(uuid, -1);
                        return sendMessage(sender, minAffinityRemoved.replace(PREFIX_USER, data.name), true);
                    }
                    if(argument.equals("delmax")) {
                        Minecrafter data = MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid);
                        MAIN_MANAGER.getPlayerManager().setMaxAffinity(uuid, -1);
                        return sendMessage(sender, maxAffinityRemoved.replace(PREFIX_USER, data.name), true);
                    }
                    if(argument.equals("get")) {
                        Minecrafter data = MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid);
                        StringBuilder message = new StringBuilder(userAffinityGet.replace(PREFIX_USER, data.name).replace(PREFIX_NUMBER, data.affinity + ""))
                                .append("\n").append(userDifficultyGet.replace(PREFIX_DIFFICULTY, MAIN_MANAGER.getDifficultyManager().getDifficulty(data.uuid).prefix)
                                        .replace(PREFIX_NUMBER, data.affinity + ""));

                        if(data.maxAffinity != -1) message.append("\n").append(userMaxAffinityGet.replace(PREFIX_NUMBER, data.maxAffinity + ""));
                        if(data.minAffinity != -1) message.append("\n").append(userMinAffinityGet.replace(PREFIX_NUMBER, data.minAffinity + ""));

                        return sendMessage(sender, message.toString(), true);
                    }
                }
            }
        } else if (TWO_ARGS.contains(argument)) {
            if (args.length < 2) return sendMessage(sender, includeNumber, false);

            int number = needsNumber(args.length == 2 ? args[1] : args[2]);
            if (number == -550055) return sendMessage(sender, notNumber.replace(PREFIX_NUMBER, args.length == 2 ? args[1] : args[2]), false);

            if(args[1].equals("@a")) {
                if(hasAnyPermission(sender, argument, "other")) {
                    if (argument.equals("setmin")) {
                        Set<UUID> keys = MAIN_MANAGER.getPlayerManager().getPlayerList().keySet();
                        for (UUID key : keys) { MAIN_MANAGER.getPlayerManager().setMinAffinity(key, number); }
                        return sendMessage(sender, allUserMinAffinitySet.replace(PREFIX_NUMBER, String.valueOf(number)), true);
                    } else if (argument.equals("setmax")) {
                        Set<UUID> keys = MAIN_MANAGER.getPlayerManager().getPlayerList().keySet();
                        for (UUID key : keys) { MAIN_MANAGER.getPlayerManager().setMaxAffinity(key, number); }
                        return sendMessage(sender, allUserMaxAffinitySet.replace(PREFIX_NUMBER, String.valueOf(number)), true);
                    } else if (argument.equals("set")) {
                        Set<UUID> keys = MAIN_MANAGER.getPlayerManager().getPlayerList().keySet();
                        for (UUID key : keys) { MAIN_MANAGER.getPlayerManager().setAffinity(key, number); }
                        return sendMessage(sender, allUserAffinitySet.replace(PREFIX_NUMBER, String.valueOf(number)), true);
                    } else if (argument.equals("remove")) {
                        Set<UUID> keys = MAIN_MANAGER.getPlayerManager().getPlayerList().keySet();
                        for (UUID key : keys) { MAIN_MANAGER.getPlayerManager().addAffinity(key, number * -1); }
                        return sendMessage(sender, allUserAffinitySet.replace(PREFIX_NUMBER, String.valueOf(number)), true);
                    } else if (argument.equals("add")) {
                        Set<UUID> keys = MAIN_MANAGER.getPlayerManager().getPlayerList().keySet();
                        for (UUID key : keys) { MAIN_MANAGER.getPlayerManager().addAffinity(key, number); }
                        return sendMessage(sender, allUserAffinitySet.replace(PREFIX_NUMBER, String.valueOf(number)), true);
                    }
                }
            } else {
                String name = args[1].toLowerCase();
                UUID uuid = needsUuid(sender, name);
                if (uuid == null) { return sendMessage(sender, notFound, false); }

                if(hasPermission(sender, name, argument)) {
                    if(argument.equals("setmin")) {
                        int setAffinity = MAIN_MANAGER.getPlayerManager().setMinAffinity(uuid, number);
                        Minecrafter affinity = MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid);
                        return sendMessage(sender,
                                minAffinitySet.replace(PREFIX_USER, affinity.name).replace(PREFIX_NUMBER,setAffinity + ""),
                                true);
                    }
                    if(argument.equals("setmax")) {
                        int setAffinity = MAIN_MANAGER.getPlayerManager().setMaxAffinity(uuid, number);
                        Minecrafter affinity = MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid);
                        return sendMessage(sender,
                                maxAffinitySet.replace(PREFIX_USER, affinity.name).replace(PREFIX_NUMBER,setAffinity + ""),
                                true);
                    }
                    if(argument.equals("set")) {
                        int setAffinity = MAIN_MANAGER.getPlayerManager().setAffinity(uuid, number);
                        Minecrafter affinity = MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid);
                        Difficulty diff = MAIN_MANAGER.getDifficultyManager().calculateDifficulty(affinity);
                        return sendMessage(sender,
                                affinitySet.replace(PREFIX_USER, affinity.name).replace(PREFIX_DIFFICULTY, diff.difficultyName)
                                        .replace(PREFIX_NUMBER,setAffinity + ""),
                                true);
                    }
                    if(argument.equals("remove")) {
                        int setAffinity = MAIN_MANAGER.getPlayerManager().addAffinity(uuid, number * -1);
                        Minecrafter affinity = MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid);
                        String difficulty = MAIN_MANAGER.getDifficultyManager().getDifficulty(uuid).difficultyName;
                        return sendMessage(sender,
                                affinitySet.replace(PREFIX_USER, affinity.name).replace(PREFIX_DIFFICULTY, difficulty)
                                        .replace(PREFIX_NUMBER,setAffinity + ""),
                                true);
                    }
                    if(argument.equals("add")) {
                        int setAffinity = MAIN_MANAGER.getPlayerManager().addAffinity(uuid, number);
                        Minecrafter affinity = MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid);
                        String difficulty = MAIN_MANAGER.getDifficultyManager().getDifficulty(uuid).difficultyName;
                        return sendMessage(sender,
                                affinitySet.replace(PREFIX_USER, affinity.name).replace(PREFIX_DIFFICULTY, difficulty)
                                        .replace(PREFIX_NUMBER,setAffinity + ""),
                                true);
                    }
                }
            }
        }

        return sendMessage(sender, noPermission, false);
    }

    private boolean openDifficultyGUI(CommandSender sender) {
        if(sender instanceof Player) {
            MAIN_MANAGER.getInventoryManager().openBaseDifficultyInventory((Player) sender);
        } else sendMessage(sender, consoleOpenPlayerGUI, false);
        return true;
    }

    private boolean openPlayerGUI(CommandSender sender) {
        if(sender instanceof Player) {
            MAIN_MANAGER.getInventoryManager().openInventory((Player) sender, 1);
        } else sendMessage(sender, consoleOpenPlayerGUI, false);
        return true;
    }

    private boolean openMyInventoryGUI(CommandSender sender) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            Difficulty difficulty = MAIN_MANAGER.getDifficultyManager().getDifficulty(player.getUniqueId());
            Inventory inventory = Bukkit.createInventory(null, 36, StaticInfo.DIFFICULTY_INVENTORY);
            MAIN_MANAGER.getInventoryManager().createInventory(difficulty, inventory);
            player.openInventory(inventory);
        } else sendMessage(sender, consoleOpenPlayerGUI, false);
        return true;
    }

    private String forceSave() {
        MAIN_MANAGER.getDataManager().saveData();
        return dataSaved;
    }

    private String reloadPlugin() {
        MAIN_MANAGER.reloadConfig();
        MAIN_MANAGER.getDifficultyManager().calculateAllPlayers();
        return configReloaded;
    }

    public UUID needsUuid(CommandSender sender, String command) {
        if (command == null || command.length() <= 1
                || sender.getName().equalsIgnoreCase(command)
                || command.equals("@s")
                || needsNumber(command) != -550055) {
            return MAIN_MANAGER.getPlayerManager().determineUuid((Player) sender);
        }
        if (MAIN_MANAGER.getPlayerManager().hasPlayer(command)) {
            return MAIN_MANAGER.getPlayerManager().getPlayerAffinity(command.toLowerCase()).uuid;
        }
        if (command.equals("@r")) {
            Object[] players = MAIN_MANAGER.getPlayerManager().getPlayerList().keySet().toArray();
            return (UUID) players[new Random().nextInt(players.length)];
        } else if (command.equals("@p")) {
            List<Entity> entities = Bukkit.selectEntities(sender, "@p");
            for(Entity entity : entities)
                return MAIN_MANAGER.getPlayerManager().determineUuid((Player) entity);
        }
        return null;
    }

    public int needsNumber(String command) {
        if (command.matches("^[0-9-]+$")) return MAIN_MANAGER.getPlayerManager().withinServerLimits(Integer.parseInt(command));
        if (DIFFICULTIES.contains(command)) return MAIN_MANAGER.getDifficultyManager().getDifficulty(command).getAffinity();
        return -550055;
    }

    public boolean hasPermission(CommandSender sender, String name, String command) {
        if(sender instanceof Player) {
            if(sender.getName().equalsIgnoreCase(name)) return hasAnyPermission(sender, command, "self");
            return hasAnyPermission(sender, command, "other");
        } else return true;
    }

    public boolean hasPermission(CommandSender sender, String command) {
        if(sender instanceof Player) return hasAnyPermission(sender, command);
        return true;
    }

    public boolean hasAnyPermission(CommandSender sender, String command, String prefix) {
        return sender.hasPermission("affinity." + command + "." + prefix) || sender.hasPermission("affinity.*." + prefix) || hasAnyPermission(sender, command);
    }

    public boolean hasAnyPermission(CommandSender sender, String command) {
        return sender.isOp() || sender.hasPermission("affinity.*") || sender.hasPermission("affinity." + command);
    }

    public boolean sendMessage(CommandSender sender, String message, boolean success) {
        if(sender instanceof Player) {
            sender.sendMessage(message);
        } else Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(message));
        return success;
    }

    public void reloadConfig() {
        consoleOpenPlayerGUI = MAIN_MANAGER.getDataManager().getLanguageString("error.console.cannot-open-playergui", false);
        noPermission = MAIN_MANAGER.getDataManager().getLanguageString("error.no-permission", false);
        includeNumber = MAIN_MANAGER.getDataManager().getLanguageString("error.include-number", false);
        notNumber = MAIN_MANAGER.getDataManager().getLanguageString("error.not-a-number", false);
        notFound = MAIN_MANAGER.getDataManager().getLanguageString("error.cannot-be-found", false);

        dataSaved = MAIN_MANAGER.getDataManager().getLanguageString("command.other.force-save", true);
        configReloaded = MAIN_MANAGER.getDataManager().getLanguageString("command.other.reload-config", true);
        affinitySet = MAIN_MANAGER.getDataManager().getLanguageString("command.set.affinity", true);
        minAffinitySet = MAIN_MANAGER.getDataManager().getLanguageString("command.set.min-affinity", true);
        maxAffinitySet = MAIN_MANAGER.getDataManager().getLanguageString("command.set.max-affinity", true);
        maxAffinityRemoved = MAIN_MANAGER.getDataManager().getLanguageString("command.remove.max-affinity", true);
        minAffinityRemoved = MAIN_MANAGER.getDataManager().getLanguageString("command.remove.min-affinity", true);

        userAffinityGet = MAIN_MANAGER.getDataManager().getLanguageString("command.get.has-affinity", true);
        userDifficultyGet = MAIN_MANAGER.getDataManager().getLanguageString("command.get.currently-on", true);
        userMaxAffinityGet = MAIN_MANAGER.getDataManager().getLanguageString("command.get.max-affinity", true);
        userMinAffinityGet = MAIN_MANAGER.getDataManager().getLanguageString("command.get.min-affinity", true);

        allUserAffinityGet = MAIN_MANAGER.getDataManager().getLanguageString("command.get.cannot-use-get-for-all", true);
        allUserAffinitySet = MAIN_MANAGER.getDataManager().getLanguageString("command.set.all", true);
        allUserMaxAffinitySet = MAIN_MANAGER.getDataManager().getLanguageString("command.set.max-affinity-all", true);
        allUserMinAffinitySet = MAIN_MANAGER.getDataManager().getLanguageString("command.set.min-affinity-all", true);
        allUserMinAffinityRemoved = MAIN_MANAGER.getDataManager().getLanguageString("command.remove.min-affinity-all", true);
        allUserMaxAffinityRemoved = MAIN_MANAGER.getDataManager().getLanguageString("command.remove.max-affinity-all", true);

        translatorMessage = MAIN_MANAGER.getDataManager().getLanguageString("translated-by", true) + "\n" + MAIN_MANAGER.getDataManager().getLanguageString("translator-url", true);

        ConfigurationSection language = MAIN_MANAGER.getDataManager().getCultureLang();
        String label = language.getString("command.help.label", "&f/dd");
        String nameNum = language.getString("command.help.name-num", "<?Name> <Number>");
        String name = language.getString("command.help.name", "<?Name>");
        StringBuilder help = new StringBuilder();

        for(String key : language.getConfigurationSection("command.help.command").getKeys(false))
            help.append(language.getString("command.help.command." + key).replace("%label%", label)
                    .replace("%name-num%", nameNum).replace("%name%", name)).append("\n");

        helpMessage = ChatColor.translateAlternateColorCodes('&', help.toString());

        DIFFICULTIES.clear();
        DIFFICULTIES.addAll(MAIN_MANAGER.getDifficultyManager().getDifficultyNames());
    }

    private void dispatchCommandsInCurrentThread(UUID uuid, List<String> commands) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        try {
            for (String command : commands) {
                command = command.replace("%player%", player.getName());
                if (command.startsWith("/")) command = command.substring(1);
                boolean foundCommand = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                if (!foundCommand) {
                    MAIN_MANAGER.getPlugin().getLogger().warning("Command not found: " + command);
                    break;
                }
            }
        } catch (Exception e) {
            MAIN_MANAGER.getPlugin().getLogger().warning("Error dispatching commands for " + player.getName());
            e.printStackTrace();
        }
    }

    public void dispatchCommandsIfOnline(UUID uuid, List<String> commands) {
        Bukkit.getScheduler().runTask(MAIN_MANAGER.getPlugin(), () ->
            dispatchCommandsInCurrentThread(uuid, commands));
    }
}
