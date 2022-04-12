package me.skinnyjeans.gmd.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {

    public final MainManager MAIN_MANAGER;

    public String noPermission;

    public CommandManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) return failedMessage(sender, "");
        if(isNotAllowed(sender, args[0])) return failedMessage(sender, "");
        return false;
    }

    public boolean isNotAllowed(CommandSender sender, String command) {
        if(sender instanceof Player) {
            return !sender.hasPermission("affinity." + command) || !sender.hasPermission("affinity.*") || !sender.isOp();
        } else return true;
    }

    public boolean failedMessage(CommandSender sender, String message) {
        if(sender instanceof Player) {
            sender.sendMessage(message);
        } else Bukkit.getConsoleSender().sendMessage(message);
        return false;
    }

    public void reloadConfig() {
        noPermission = MAIN_MANAGER.getDataManager().getLanguageString("", false);
    }
}
