package me.skinnyjeans.gmd.managers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TabCompleterManager implements TabCompleter {

    private final MainManager MAIN_MANAGER;
    private final HashSet<String> noNumbers = new HashSet<>(Arrays.asList("author", "reload", "forcesave", "help", "playergui", "get", "delmax", "delmin"));
    private final HashSet<String> allCommands = new HashSet<>(Arrays.asList(
            "set","get","add","remove","help","setmax","setmin","delmax","delmin","author","reload","forcesave","playergui"
    ));
    private final HashSet<String> numbers = new HashSet<>();

    public TabCompleterManager(MainManager mainManager) { MAIN_MANAGER = mainManager; }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        ArrayList<String> l = new ArrayList<>();
        if (args.length == 1)
            for(String c : allCommands)
                if(c.contains(args[0]))
                    l.add(c);
        if (args.length >= 2 && !noNumbers.contains(args[0]))
            for(String c : numbers)
                if(c.contains(args[1]))
                    l.add(c);
        return l;
    }

    public void reloadConfig() {
        numbers.clear();
        for(int i = 1; i < 10; i++) numbers.add(String.valueOf(i));
        numbers.addAll(MAIN_MANAGER.getDifficultyManager().getDifficultyNames());
        numbers.addAll(Arrays.asList("@a", "@s", "@p", "@r"));
    }
}
