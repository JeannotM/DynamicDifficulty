package me.skinnyjeans.gmd.tabcompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class PlayerTabCompleter implements TabCompleter {
	
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
		if(label.equalsIgnoreCase("affinity")) {
			if (args.length==1)
				return new ArrayList<String>(Arrays.asList(
						"set","get","add","remove",
		                "setmax","removemax","author"
		                ));
			if (args.length==2) {
				List<String> l = new ArrayList<String>();
				Bukkit.getOnlinePlayers().forEach(name -> { l.add(name.getName()); });
				return l;
			}
			if (args.length==3 && args[0].toLowerCase() != "author" && args[0].toLowerCase() != "removemax"){
				if(args[0].toLowerCase() == "set")
					return new ArrayList<String>(Arrays.asList("1","2","3","4","5"));
				return new ArrayList<String>(Arrays.asList("1","2","3","4","5"));
			}
        }
		return null;
	}
}
