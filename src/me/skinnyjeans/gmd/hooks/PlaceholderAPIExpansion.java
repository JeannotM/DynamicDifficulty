package me.skinnyjeans.gmd.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.managers.MainManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private Main plugin;
    private final MainManager MAIN_MANAGER;

    public PlaceholderAPIExpansion(Main plugin, MainManager mainManager){
        this.plugin = plugin;
        this.MAIN_MANAGER = mainManager;
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
        return "dd";
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
    public String onPlaceholderRequest(Player player, String identifier) { return placeholderRequest(player, identifier); }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) { return placeholderRequest(player, identifier); }

    public String placeholderRequest(OfflinePlayer player, String identifier) {
        if(player != null) {
            UUID uuid = player.getUniqueId();
            if(identifier.equals("user_difficulty")) return MAIN_MANAGER.getDifficultyManager().getPrefix(uuid);
            if(identifier.equals("user_progress")) return MAIN_MANAGER.getDifficultyManager().getProgress(uuid);
            if(identifier.equals("user_next_difficulty")) return MAIN_MANAGER.getDifficultyManager().getPrefix(MAIN_MANAGER.getDifficultyManager().getNextDifficulty(uuid).getAffinity());
            if(identifier.equals("user_affinity")) return String.valueOf(MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid).getAffinity());
            if(identifier.equals("user_min_affinity")) return String.valueOf(MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid).getMinAffinity());
            if(identifier.equals("user_max_affinity")) return String.valueOf(MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid).getMaxAffinity());
        }
        return null;
    }
}
