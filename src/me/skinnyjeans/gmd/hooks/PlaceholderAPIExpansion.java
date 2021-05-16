package me.skinnyjeans.gmd.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private Main plugin;
    private Affinity affinity;

    public PlaceholderAPIExpansion(Main plugin, Affinity af){
        this.plugin = plugin;
        this.affinity = af;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getIdentifier() {
        return "dynamicdifficulty";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){
        if(identifier.equals("text_difficulty"))
            return affinity.calcDifficulty(player.getUniqueId());
        if(identifier.equals("affinity_points"))
            return affinity.getAffinity(player.getUniqueId()) + "";
        if(identifier.equals("world_text_difficulty"))
            return affinity.calcDifficulty(null);
        if(identifier.equals("world_affinity_points"))
            return affinity.getAffinity(null)+"";
        if(identifier.equals("max_affinity"))
            return affinity.getVariableMaxAffinity()+"";
        if(identifier.equals("min_affinity"))
            return affinity.getVariableMinAffinity()+"";

        return null;
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier){
        if(player == null)
            return "";

        if(identifier.equals("text_difficulty"))
            return affinity.calcDifficulty(player.getUniqueId());
        if(identifier.equals("affinity_points"))
            return affinity.getAffinity(player.getUniqueId()) + "";
        if(identifier.equals("world_text_difficulty"))
            return affinity.calcDifficulty(null);
        if(identifier.equals("world_affinity_points"))
            return affinity.getAffinity(null)+"";
        if(identifier.equals("max_affinity"))
            return affinity.getVariableMaxAffinity()+"";
        if(identifier.equals("min_affinity"))
            return affinity.getVariableMinAffinity()+"";

        return null;
    }
}