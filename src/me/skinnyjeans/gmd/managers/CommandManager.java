package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CommandManager implements CommandExecutor {

    private final MainManager MAIN_MANAGER;

    private final HashSet<String> NO_ARG = new HashSet<>(Arrays.asList("info", "help", "reload", "author", "forcesave", "playergui"));
    private final HashSet<String> ONE_ARG = new HashSet<>(Arrays.asList("delmin", "delmax", "get"));
    private final HashSet<String> TWO_ARGS = new HashSet<>(Arrays.asList("setmin", "setmax", "set", "remove", "add"));

    private final String authorMessage = "DynamicDifficulty: SkinnyJeans\nhttps://www.spigotmc.org/resources/dynamic-difficulty.92025/\n\n";
    private final String PREFIX_NUMBER = "%number%";
    private final String PREFIX_USER = "%user%";
    private final String PREFIX_DIFFICULTY = "%difficulty%";

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

        if(NO_ARG.contains(argument)) {
            if(hasPermission(sender, argument)) {
                if(argument.equals("author")) return sendMessage(sender, author(), true);
                if(argument.equals("reload")) return sendMessage(sender, reloadPlugin(), true);
                if(argument.equals("forcesave")) return sendMessage(sender, forceSave(), true);
                if(argument.equals("playergui")) return openPlayerGUI(sender);
                if(argument.equals("info")) return sendMessage(sender, info(), true);
            }
        } else if(ONE_ARG.contains(argument)) {
            if(args.length > 1 && args[1].equals("@a")) {
                if(hasAnyPermission(sender, argument, "other")) {
                    if(argument.equals("delmin")) {
                        multipleUsers(this::removeMinAffinity);
                        return sendMessage(sender, allUserMinAffinityRemoved, true);
                    }
                    if(argument.equals("delmax")) {
                        multipleUsers(this::removeMaxAffinity);
                        return sendMessage(sender, allUserMaxAffinityRemoved, true);
                    }
                    if(argument.equals("get")) {
                        return sendMessage(sender, allUserAffinityGet, true);
                    }
                }
            } else {
                Player affectedPlayer = needsPlayer(sender, args.length > 1 ? args[1] : "@s");
                if(affectedPlayer == null) return sendMessage(sender, notFound, false);

                if(hasPermission(sender, affectedPlayer, argument))
                    if(argument.equals("delmin")) return sendMessage(sender, removeMinAffinity(affectedPlayer), true);
                    if(argument.equals("delmax"))  return sendMessage(sender, removeMaxAffinity(affectedPlayer), true);
                    if(argument.equals("get")) return sendMessage(sender, getAffinity(affectedPlayer), true);
            }
        } else if(TWO_ARGS.contains(argument)) {
            if (args.length < 2) return sendMessage(sender, includeNumber, false);

            int number = needsNumber(args.length == 2 ? args[1] : args[2]);
            if (number == -550055) return sendMessage(sender, notNumber.replace(PREFIX_NUMBER, args.length == 2 ? args[1] : args[2]), false);

            if(args[1].equals("@a")) {
                if(hasAnyPermission(sender, argument, "other")) {
                    if (argument.equals("setmin")) {
                        multipleUsers(this::setMinAffinity, number);
                        return sendMessage(sender, allUserMinAffinitySet.replace(PREFIX_NUMBER, String.valueOf(number)), true);
                    } else if (argument.equals("setmax")) {
                        multipleUsers(this::setMaxAffinity, number);
                        return sendMessage(sender, allUserMaxAffinitySet.replace(PREFIX_NUMBER, String.valueOf(number)), true);
                    } else if (argument.equals("set")) {
                        multipleUsers(this::setAffinity, number);
                        return sendMessage(sender, allUserAffinitySet.replace(PREFIX_NUMBER, String.valueOf(number)), true);
                    } else if (argument.equals("remove")) {
                        multipleUsers(this::addAffinity, number * -1);
                        return sendMessage(sender, allUserAffinitySet.replace(PREFIX_NUMBER, String.valueOf(number)), true);
                    } else if (argument.equals("add")) {
                        multipleUsers(this::addAffinity, number);
                        return sendMessage(sender, allUserAffinitySet.replace(PREFIX_NUMBER, String.valueOf(number)), true);
                    }
                }
            } else {
                Player affectedPlayer = needsPlayer(sender, args[1]);
                if(affectedPlayer == null) return sendMessage(sender, notFound, false);

                if(hasPermission(sender, affectedPlayer, argument)) {
                    if(argument.equals("setmin")) return sendMessage(sender, setMinAffinity(affectedPlayer, number), true);
                    if(argument.equals("setmax")) return sendMessage(sender, setMaxAffinity(affectedPlayer, number), true);
                    if(argument.equals("set")) return sendMessage(sender, setAffinity(affectedPlayer, number), true);
                    if(argument.equals("remove")) return sendMessage(sender, addAffinity(affectedPlayer, number * -1), true);
                    if(argument.equals("add")) return sendMessage(sender, addAffinity(affectedPlayer, number), true);
                }
            }
        }
        return sendMessage(sender, noPermission, false);
    }

    private void multipleUsers(Function<Player, String> method) {
        for(Player player : Bukkit.getOnlinePlayers()) method.apply(player);
    }

    private void multipleUsers(BiFunction<Player, Integer, String> method, int value) {
        for(Player player : Bukkit.getOnlinePlayers()) method.apply(player, value);
    }

    private String removeMaxAffinity(Player player) {
        MAIN_MANAGER.getPlayerManager().setMaxAffinity(player.getUniqueId(), -1);
        return maxAffinityRemoved.replace(PREFIX_USER, player.getName());
    }

    private String removeMinAffinity(Player player) {
        MAIN_MANAGER.getPlayerManager().setMinAffinity(player.getUniqueId(), -1);
        return minAffinityRemoved.replace(PREFIX_USER, player.getName());
    }

    private String author() {
        return authorMessage + translatorMessage;
    }

    private String info() {
        StringBuilder message = new StringBuilder("Language: ").append(MAIN_MANAGER.getDataManager().getLang().getCurrentPath()).append("\n");

        message.append("Difficulties: (").append(MAIN_MANAGER.getDifficultyManager().getDifficulties().size()).append(") ");
        MAIN_MANAGER.getDifficultyManager().getDifficulties().forEach(d -> message.append(d.getDifficultyName()).append(" : ").append(d.getAffinity()).append(", "));
        message.append("\n");

        MAIN_MANAGER.getEntityManager().getMobs().forEach((key, value) -> message.append(key).append(" : ").append(value).append(", "));

        return message.toString();
    }

    private boolean openPlayerGUI(CommandSender sender) {
        if(sender instanceof Player) {
            MAIN_MANAGER.getInventoryManager().openInventory((Player) sender, 1);
        } else sendMessage(sender, consoleOpenPlayerGUI, false);
        return true;
    }

    private String getAffinity(Player player) {
        Minecrafter data = MAIN_MANAGER.getPlayerManager().getPlayerAffinity(player.getUniqueId());
        StringBuilder message = new StringBuilder(userAffinityGet.replace(PREFIX_USER, player.getName()).replace(PREFIX_NUMBER, data.getAffinity() + ""))
                .append("\n").append(userDifficultyGet.replace(PREFIX_DIFFICULTY, MAIN_MANAGER.getDifficultyManager().getDifficulty(data.getUUID()).getPrefix())
                        .replace(PREFIX_NUMBER, data.getAffinity() + ""));

        if(data.getMaxAffinity() != -1) message.append("\n").append(userMaxAffinityGet.replace(PREFIX_NUMBER, data.getMaxAffinity() + ""));
        if(data.getMinAffinity() != -1) message.append("\n").append(userMinAffinityGet.replace(PREFIX_NUMBER, data.getMinAffinity() + ""));

        return message.toString();
    }

    private String setAffinity(Player player, int value) {
        int setAffinity = MAIN_MANAGER.getPlayerManager().setAffinity(player.getUniqueId(), value);
        MAIN_MANAGER.getDifficultyManager().calculateDifficulty(player.getUniqueId());
        return affinitySet.replace(PREFIX_USER, player.getName()).replace(PREFIX_NUMBER, String.valueOf(setAffinity))
                .replace(PREFIX_DIFFICULTY, MAIN_MANAGER.getDifficultyManager().getDifficulty(player.getUniqueId()).getPrefix());
    }

    private String addAffinity(Player player, int value) {
        Minecrafter difficulty = MAIN_MANAGER.getPlayerManager().getPlayerAffinity(player.getUniqueId());
        return setAffinity(player, difficulty.getAffinity() + value);
    }

    private String setMaxAffinity(Player player, int value) {
        int setAffinity = MAIN_MANAGER.getPlayerManager().setMaxAffinity(player.getUniqueId(), value);
        return maxAffinitySet.replace(PREFIX_USER, player.getName()).replace(PREFIX_NUMBER,setAffinity + "");
    }

    private String setMinAffinity(Player player, int value) {
        int setAffinity = MAIN_MANAGER.getPlayerManager().setMinAffinity(player.getUniqueId(), value);
        return minAffinitySet.replace(PREFIX_USER, player.getName()).replace(PREFIX_NUMBER,setAffinity + "");
    }

    private String forceSave() {
        MAIN_MANAGER.getDataManager().saveData();
        return dataSaved;
    }

    private String reloadPlugin() {
        MAIN_MANAGER.reloadConfig();
        return configReloaded;
    }

    public Player needsPlayer(CommandSender sender, String command) {
        if (command.equals("@s") || needsNumber(command) != -550055) return sender instanceof Player ? ((Player) sender).getPlayer() : null;
        if (Bukkit.getOfflinePlayer(command) != null && Bukkit.getOfflinePlayer(command).hasPlayedBefore()) return Bukkit.getOfflinePlayer(command).getPlayer();
        if (command.equals("@r")) {
            Player[] players = (Player[]) Bukkit.getOnlinePlayers().toArray();
            return players[new Random().nextInt(players.length)];
        } else if (command.equals("@p")) {
            List<Entity> entities = Bukkit.selectEntities(sender, "@p");
            if(entities == null || entities.size() == 0) return null;
            for(Entity entity : entities) if(entity instanceof Player) return (Player) entity;
        }
        return null;
    }

    public int needsNumber(String command) {
        if (command.matches("^[0-9-]+$")) return MAIN_MANAGER.getAffinityManager().withinServerLimits(Integer.parseInt(command));
        if (DIFFICULTIES.contains(command)) return MAIN_MANAGER.getDifficultyManager().getDifficulty(command).getAffinity();
        return -550055;
    }

    public boolean hasPermission(CommandSender sender, Player player, String command) {
        if(sender instanceof Player) {
            if(sender.getName().equals(player.getName())) return hasAnyPermission(sender, command, "self");
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

        ConfigurationSection language = MAIN_MANAGER.getDataManager().getLang();
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
}
