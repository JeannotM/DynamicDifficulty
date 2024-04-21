package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {

    private final MainManager MAIN_MANAGER;
    private final HashMap<UUID, Minecrafter> PLAYER_LIST = new HashMap<>();
    private DifficultySettings difficultySettings;

    private int maxAffinityGainPerMinute;
    private int maxAffinityLossPerMinute;
    private DifficultyTypes difficultyType;

    public PlayerManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    public void addPlayer(Entity player) {
        if(difficultyType == DifficultyTypes.player) {
            MAIN_MANAGER.getDataManager().playerExists(player.getUniqueId(), exists -> {
                if(exists) {
                    MAIN_MANAGER.getDataManager().getAffinityValues(player.getUniqueId(), playerData -> {
                        PLAYER_LIST.put(player.getUniqueId(), playerData);
                        MAIN_MANAGER.getDifficultyManager().calculateDifficulty(player.getUniqueId());
                    });
                } else {
                    Minecrafter playerData = MAIN_MANAGER.getAffinityManager().getDefault().clone();
                    playerData.name = player.getName();
                    playerData.uuid = player.getUniqueId();
                    PLAYER_LIST.put(player.getUniqueId(), playerData);
                    MAIN_MANAGER.getDataManager().updatePlayer(player.getUniqueId());
                    MAIN_MANAGER.getDifficultyManager().calculateDifficulty(player.getUniqueId());
                }
            });
        } else {
            Minecrafter playerData = MAIN_MANAGER.getAffinityManager().getDefault().clone();
            playerData.name = player.getName();
            playerData.uuid = player.getUniqueId();
            playerData.affinity = difficultySettings.calculateAffinity((Player) player, -1);
            PLAYER_LIST.put(player.getUniqueId(), playerData);

            MAIN_MANAGER.getDifficultyManager().calculateDifficulty(player.getUniqueId());
        }
    }

    public boolean playerExist(Entity player) { return PLAYER_LIST.containsKey(player.getUniqueId()); }

    public boolean isPlayerValid(Entity player) {
        if(player == null) return false;
        if(!(player instanceof Player)) return false;
        if(player.hasMetadata("NPC")) return false;
        if(MAIN_MANAGER.getDataManager().isWorldDisabled(player.getWorld().getName())) return false;
        if(!playerExist(player)) addPlayer(player);
        return true;
    }

    public void unloadPlayer(UUID uuid) {
        PLAYER_LIST.remove(uuid);
    }

    public HashMap<UUID, Minecrafter> getPlayerList() { return PLAYER_LIST; }

    public Minecrafter getPlayerAffinity(UUID uuid) {
        return PLAYER_LIST.get(uuid);
    }

    public int addAffinity(UUID uuid, int value) {
        if(difficultyType == DifficultyTypes.region) {
            return setAffinity(uuid, difficultySettings.calculateAffinity(Bukkit.getPlayer(uuid), -1));
        } else if(difficultyType == DifficultyTypes.world) {
            return setAffinity(uuid, difficultySettings.calculateAffinity(Bukkit.getPlayer(uuid), value));
        }

        Minecrafter player = getPlayerAffinity(uuid);
        boolean ignoreTheCap = value > maxAffinityGainPerMinute || value < maxAffinityLossPerMinute;

        if (value == 0) {
            return player.affinity;
        } else if (value > 0) {
            if (!ignoreTheCap && player.gainedThisMinute + value > maxAffinityGainPerMinute) {
                value = maxAffinityGainPerMinute - player.gainedThisMinute;
            }
        } else if (!ignoreTheCap && player.gainedThisMinute + value < maxAffinityLossPerMinute) {
            value = maxAffinityLossPerMinute - player.gainedThisMinute;
        }

        if (!ignoreTheCap)
            player.gainedThisMinute = player.gainedThisMinute + value;
        return setAffinity(uuid, player.affinity + value);
    }

    public void resetAllGainsThisMinute() {
        for(Minecrafter player : PLAYER_LIST.values()) {
            player.gainedThisMinute = 0;
        }
    }

    public int addMinAffinity(UUID uuid, int value) {
        Minecrafter player = getPlayerAffinity(uuid);
        return value == 0 ? player.minAffinity : setMinAffinity(uuid, player.minAffinity + value);
    }

    public int addMaxAffinity(UUID uuid, int value) {
        Minecrafter player = getPlayerAffinity(uuid);
        return value == 0 ? player.maxAffinity : setMaxAffinity(uuid, player.maxAffinity + value);
    }

    public int setAffinity(UUID uuid, int value) {
        value = MAIN_MANAGER.getAffinityManager().withinPlayerLimits(uuid, value);
        getPlayerAffinity(uuid).affinity = value;
        return value;
    }

    public int setMinAffinity(UUID uuid, int value) {
        Minecrafter player = getPlayerAffinity(uuid);
        if(value != -1) {
            value = MAIN_MANAGER.getAffinityManager().withinServerLimits(value);
            if(player.maxAffinity != -1)
                value = Math.min(player.maxAffinity, value);
        }
        player.minAffinity = value;
        return value;
    }

    public int setMaxAffinity(UUID uuid, int value) {
        Minecrafter player = getPlayerAffinity(uuid);
        if(value != -1) {
            value = MAIN_MANAGER.getAffinityManager().withinServerLimits(value);
            if(player.minAffinity != -1)
                value = Math.max(player.minAffinity, value);
        }
        player.maxAffinity = value;
        return value;
    }

    public void reloadConfig() {
        FileConfiguration config = MAIN_MANAGER.getDataManager().getConfig();
        maxAffinityGainPerMinute = config.getInt("max-affinity-gain-per-minute", 0);
        maxAffinityLossPerMinute = config.getInt("max-affinity-loss-per-minute", 0);
        String type = (config.getString("toggle-settings.difficulty-type", "player")).toLowerCase();

        try {
            difficultyType = DifficultyTypes.valueOf(type);
        } catch (IllegalArgumentException ignored) {
            difficultyType = DifficultyTypes.player;
        }

        if(difficultyType == DifficultyTypes.region) {
            difficultySettings = new RegionSettings(MAIN_MANAGER);
        } else if (difficultyType == DifficultyTypes.world) {
            difficultySettings = new WorldSettings(MAIN_MANAGER);
        }
    }
}
