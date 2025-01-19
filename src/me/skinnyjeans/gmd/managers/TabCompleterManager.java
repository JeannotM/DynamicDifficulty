package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class TabCompleterManager implements TabCompleter {

    private final MainManager MAIN_MANAGER;
    private static final HashSet<String> noNumbers = new HashSet<>(Arrays.asList("author", "forceremoval", "me", "difficulties", "reload", "forcesave", "help", "playergui"));
    private static final HashSet<String> allCommands = new HashSet<>(Arrays.asList(
            "set","get","add","remove","help","setmax", "forceremoval", "setmin","delmax","delmin","author","reload","forcesave","playergui","difficulties"
    ));
    private final HashSet<String> numbers = new HashSet<>();

    public TabCompleterManager(MainManager mainManager) { MAIN_MANAGER = mainManager; }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        ArrayList<String> l = new ArrayList<>();
        if (args.length == 1) {
            for(String c : allCommands)
                if(c.contains(args[0]))
                    l.add(c);
        } else if (args.length >= 2 && !noNumbers.contains(args[0])) {
            String arg = args[1].toLowerCase();
            for(String c : numbers)
                if(c.contains(arg))
                    l.add(c);
            Collection<Minecrafter> list = MAIN_MANAGER.getPlayerManager().getPlayerList().values();
            for (Minecrafter pl : list)
                if(pl.name.contains(arg))
                    l.add(pl.name);
        }

        return l;
    }

    public void reloadConfig() {
        numbers.clear();
        numbers.addAll(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "@a", "@s", "@p", "@r"));
        numbers.addAll(MAIN_MANAGER.getDifficultyManager().getDifficultyNames());
    }
}
