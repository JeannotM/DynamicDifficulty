package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {

    private final MainManager MAIN_MANAGER;
    private final HashMap<UUID, Minecrafter> PLAYER_LIST = new HashMap<>();

    public PlayerManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    public void addPlayer(Entity player) {
        MAIN_MANAGER.getDataManager().playerExists(player.getUniqueId(), exists -> {
            if(exists) {
                MAIN_MANAGER.getDataManager().getAffinityValues(player.getUniqueId(), playerData -> {
                    PLAYER_LIST.put(player.getUniqueId(), playerData);
                    MAIN_MANAGER.getDifficultyManager().calculateDifficulty(player.getUniqueId());
                });
            } else {
                Minecrafter playerData = MAIN_MANAGER.getAffinityManager().getDefault();
                playerData.setName(player.getName());
                playerData.setUUID(player.getUniqueId());
                PLAYER_LIST.put(player.getUniqueId(), playerData);
                MAIN_MANAGER.getDataManager().updatePlayer(player.getUniqueId());
                MAIN_MANAGER.getDifficultyManager().calculateDifficulty(player.getUniqueId());
            }
        });
    }

    public boolean playerExist(Entity player) { return PLAYER_LIST.containsKey(player.getUniqueId()); }

    public boolean isPlayerValid(Entity player) {
        if(player == null) return false;
        if(MAIN_MANAGER.getDataManager().isWorldDisabled(player.getWorld().getName())) return false;
        if(!(player instanceof Player)) return false;
        if(player.hasMetadata("NPC")) return false;
        if(!playerExist(player)) addPlayer(player);
        return true;
    }

    public void reloadConfig() { }

    public void unloadPlayer(UUID uuid) {
        PLAYER_LIST.remove(uuid);
    }

    public HashMap<UUID, Minecrafter> getPlayerList() { return PLAYER_LIST; }

    public Minecrafter getPlayerAffinity(UUID uuid) { return PLAYER_LIST.get(uuid); }

    public int addAffinity(UUID uuid, int value) {
        return value == 0 ? PLAYER_LIST.get(uuid).getAffinity() : setAffinity(uuid, PLAYER_LIST.get(uuid).getAffinity() + value);
    }

    public int addMinAffinity(UUID uuid, int value) {
        return value == 0 ? PLAYER_LIST.get(uuid).getMinAffinity() : setMinAffinity(uuid, PLAYER_LIST.get(uuid).getMinAffinity() + value);
    }

    public int addMaxAffinity(UUID uuid, int value) {
        return value == 0 ? PLAYER_LIST.get(uuid).getMaxAffinity() : setMaxAffinity(uuid, PLAYER_LIST.get(uuid).getMaxAffinity() + value);
    }

    public int setAffinity(UUID uuid, int value) {
        value = MAIN_MANAGER.getAffinityManager().withinPlayerLimits(uuid, value);
        PLAYER_LIST.get(uuid).setAffinity(value);
        return value;
    }

    public int setMinAffinity(UUID uuid, int value) {
        if(value != -1) {
            value = MAIN_MANAGER.getAffinityManager().withinServerLimits(value);
            if(PLAYER_LIST.get(uuid).getMaxAffinity() != -1)
                value = Math.min(PLAYER_LIST.get(uuid).getMaxAffinity(), value);
        }
        PLAYER_LIST.get(uuid).setMinAffinity(value);
        return value;
    }

    public int setMaxAffinity(UUID uuid, int value) {
        if(value != -1) {
            value = MAIN_MANAGER.getAffinityManager().withinServerLimits(value);
            if(PLAYER_LIST.get(uuid).getMinAffinity() != -1)
                value = Math.max(PLAYER_LIST.get(uuid).getMinAffinity(), value);
        }
        PLAYER_LIST.get(uuid).setMaxAffinity(value);
        return value;
    }
}
