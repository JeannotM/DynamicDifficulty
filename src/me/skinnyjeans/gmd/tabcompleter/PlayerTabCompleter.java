package me.skinnyjeans.gmd.tabcompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.skinnyjeans.gmd.PlayerAffinity;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class PlayerTabCompleter implements TabCompleter {

	private ArrayList<String> difficulties = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5"));
	private ArrayList<String> twoArgs = new ArrayList<>(Arrays.asList("removemax","get","author"));

	public PlayerTabCompleter(PlayerAffinity pa){ difficulties.addAll(pa.getDifficulties()); }

	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
		if(label.equalsIgnoreCase("affinity")) {
			if (args.length<=1)
				return new ArrayList<>(Arrays.asList(
						"set","get","add","remove",
						"setmax","removemax","author"
				));
			if (args.length==2) {
				if(args[1].equalsIgnoreCase("set")){
					List<String> l = new ArrayList<>();
					Bukkit.getOnlinePlayers().forEach(name -> l.add(name.getName()));
					return l;
				}
			}
			if (args.length>=3 && !twoArgs.contains(args[0].toLowerCase())){
				if(args[0].equalsIgnoreCase("set"))
					return difficulties;
				return new ArrayList<>(Arrays.asList("1","2","3","4","5"));
			}
        }
		return null;
	}
}
