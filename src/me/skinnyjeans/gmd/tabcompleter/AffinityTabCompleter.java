package me.skinnyjeans.gmd.tabcompleter;

import me.skinnyjeans.gmd.Affinity;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AffinityTabCompleter implements TabCompleter {

    private final ArrayList<String> noArg = new ArrayList<>(Arrays.asList("author", "reload", "force-save", "help", "playergui"));
    private final ArrayList<String> oneArg = new ArrayList<>(Arrays.asList("get", "delmax", "delmin"));
    private final ArrayList<String> commands = new ArrayList<>(Arrays.asList("set","get","add","remove","help","setmax","setmin","delmax","delmin","author","reload","force-save","playergui"));
    private final ArrayList<String> numbers = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5"));

    public AffinityTabCompleter(Affinity af){ numbers.addAll(af.getDifficulties()); }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        if (args.length == 1) {
            ArrayList<String> l = new ArrayList<>();
            for(String c : commands)
                if(c.contains(args[0]))
                    l.add(c);
            return l;
        }
        if (args.length == 2) {
            if (!noArg.contains(args[0])) {
                ArrayList<String> names = new ArrayList<>(Arrays.asList("world", "@p", "@a", "@r", "@s"));
                Bukkit.getOnlinePlayers().forEach(name -> names.add(name.getName()));
                ArrayList<String> l = new ArrayList<>();
                for(String c : names)
                    if(c.contains(args[1]))
                        l.add(c);
                return l;
            }
        } else if (args.length == 3) {
            if (!oneArg.contains(args[0]) && !noArg.contains(args[0])) {
                ArrayList<String> l = new ArrayList<>();
                for(String c : numbers)
                    if(c.contains(args[1]))
                        l.add(c);
                return l;
            }
        }
        return new ArrayList<>();
    }
}
