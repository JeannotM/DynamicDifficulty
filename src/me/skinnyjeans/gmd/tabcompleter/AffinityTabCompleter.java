package me.skinnyjeans.gmd.tabcompleter;

import me.skinnyjeans.gmd.Affinity;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AffinityTabCompleter implements TabCompleter {

    private ArrayList<String> oneArg = new ArrayList<>(Arrays.asList("author", "reload", "force-save"));
    private ArrayList<String> twoArgs = new ArrayList<>(Arrays.asList("delmax","get","author", "reload", "force-save","delmin"));
    private ArrayList<String> commands = new ArrayList<>(Arrays.asList("set","get","add","remove","setmax","setmin","delmax","delmin","author","reload","force-save"));
    private ArrayList<String> numbers = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5"));

    public AffinityTabCompleter(Affinity af){ numbers.addAll(af.getDifficulties()); }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        if (args.length == 1) { return commands; }
        if (args.length == 2) {
            if (!oneArg.contains(args[0])) {
                ArrayList<String> l = new ArrayList<String>(Arrays.asList("world"));
                Bukkit.getOnlinePlayers().forEach(name -> { l.add(name.getName()); });
                return l;
            }
        } else if (args.length == 3) {
            if (!twoArgs.contains(args[0]) && !oneArg.contains(args[0]))
                return numbers;
        }
        return null;
    }
}
