package me.skinnyjeans.gmd.tabcompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class WorldTabCompleter implements TabCompleter{
	
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
		if(label.equalsIgnoreCase("affinity")) {
			if (args.length==1)
				return new ArrayList<>(Arrays.asList(
						"set","get","add","remove","author"
		                ));
			if (args.length==2) {
				if(args[0].equalsIgnoreCase("get")) {
					List<String> l = new ArrayList<>();
					Bukkit.getOnlinePlayers().forEach(name -> l.add(name.getName()));
					l.add("world");
					return l;
				}
				else if(!args[0].equalsIgnoreCase("author")){
					List<String> l = new ArrayList<>(Arrays.asList("1","2","3","4","5"));
					l.add("world");
					return l;
				}
			}
			if (args.length == 3 && args[1].equalsIgnoreCase("world"))
				return new ArrayList<>(Arrays.asList("1","2","3","4","5"));
        }
		return null;
	}
}
