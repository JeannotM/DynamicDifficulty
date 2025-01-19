package me.skinnyjeans.gmd.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.skinnyjeans.gmd.managers.MainManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final MainManager MAIN_MANAGER;

    public PlaceholderAPIExpansion(MainManager mainManager) { this.MAIN_MANAGER = mainManager; }

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
        return MAIN_MANAGER.getPlugin().getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return MAIN_MANAGER.getPlugin().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) { return placeholderRequest(player, identifier); }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) { return placeholderRequest(player, identifier); }

    public String placeholderRequest(OfflinePlayer offlinePlayer, String identifier) {
        if(offlinePlayer != null && offlinePlayer.isOnline()) {
            if(identifier.equals("user_progress")) return MAIN_MANAGER.getDifficultyManager().getProgress(offlinePlayer.getUniqueId());
            if(identifier.equals("user_next_difficulty")) return MAIN_MANAGER.getDifficultyManager().getNextDifficulty(offlinePlayer.getUniqueId()).prefix;
            if(identifier.equals("user_difficulty")) return MAIN_MANAGER.getDifficultyManager().getDifficulty(offlinePlayer.getPlayer()).prefix;
            if(identifier.equals("user_affinity")) return String.valueOf(MAIN_MANAGER.getPlayerManager().getPlayerAffinity(offlinePlayer.getPlayer()).affinity);
            if(identifier.equals("user_min_affinity")) return String.valueOf(MAIN_MANAGER.getPlayerManager().getPlayerAffinity(offlinePlayer.getPlayer()).minAffinity);
            if(identifier.equals("user_max_affinity")) return String.valueOf(MAIN_MANAGER.getPlayerManager().getPlayerAffinity(offlinePlayer.getPlayer()).maxAffinity);
        }
        return null;
    }
}
