package me.skinnyjeans.gmd.tabcompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.skinnyjeans.gmd.PlayerAffinity;
import me.skinnyjeans.gmd.WorldAffinity;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class WorldTabCompleter implements TabCompleter{

	private ArrayList<String> difficulties = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5"));
	private ArrayList<String> twoArgs = new ArrayList<>(Arrays.asList("get","author"));

	public WorldTabCompleter(WorldAffinity wa){ difficulties.addAll(wa.getDifficulties()); }
	
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
		if(label.equalsIgnoreCase("affinity")) {
			if (args.length<=1)
				return new ArrayList<>(Arrays.asList(
						"set","get","add","remove","author"
		                ));
			if (args.length==2) {
				if(args[0].equalsIgnoreCase("set"))
					return difficulties;
				if(args[0].equalsIgnoreCase("get")) {
					List<String> l = new ArrayList<>();
					Bukkit.getOnlinePlayers().forEach(name -> l.add(name.getName()));
					l.add("world");
					return l;
				}
				if(!args[0].equalsIgnoreCase("author"))
					return new ArrayList<>(Arrays.asList("1","2","3","4","5"));
			}
			if (args.length==3 && !twoArgs.contains(args[0].toLowerCase()))
				if(args[0].equalsIgnoreCase("set"))
					return difficulties;
				return new ArrayList<>(Arrays.asList("1","2","3","4","5"));
        }
		return null;
	}
}
