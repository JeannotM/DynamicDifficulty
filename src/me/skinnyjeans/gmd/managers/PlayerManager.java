package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {

    private final MainManager MAIN_MANAGER;
    private final HashMap<UUID, Minecrafter> PLAYER_LIST = new HashMap<>();
    private final HashMap<NamespacedKey, Minecrafter> BIOME_LIST = new HashMap<>();
    private final Minecrafter WORLD;

    public PlayerManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;

        WORLD = new Minecrafter("world");
    }

    public void addPlayer(Player player) {
        MAIN_MANAGER.getDifficultyManager().calculateDifficulty(player.getUniqueId());
    }

    public boolean playerExist(Player player) { return PLAYER_LIST.containsKey(player.getUniqueId()); }

    public boolean isPlayerValid(Entity player) {
        if(player == null) return false;
        if(MAIN_MANAGER.getDataManager().isWorldDisabled(player.getWorld().getName())) return false;
        if(!(player instanceof Player)) return false;
        if(player.hasMetadata("NPC")) return false;
        if(!playerExist((Player) player)) addPlayer((Player) player);
        return true;
    }

    public HashMap<UUID, Minecrafter> getPlayerList() { return PLAYER_LIST; }

    public Minecrafter getPlayerAffinity(UUID uuid) { return PLAYER_LIST.getOrDefault(uuid, WORLD); }

    public void addAffinity(UUID uuid, int x) {
        if(x != 0) {
            switch(MAIN_MANAGER.getDifficultyManager().getType()) {
                case World:
                    WORLD.setAffinity(WORLD.getAffinity() + x);
                    break;
                case Biome:
                    Player p = Bukkit.getOfflinePlayer(uuid).getPlayer();
                    NamespacedKey key = p.getWorld().getBiome(p.getLocation()).getKey();
                    BIOME_LIST.get(key).setAffinity(BIOME_LIST.get(key).getAffinity() + x);
                    break;
                default:
                    if(uuid == null) {
                        WORLD.setAffinity(WORLD.getAffinity() + x);
                    } else PLAYER_LIST.get(uuid).setAffinity(PLAYER_LIST.get(uuid).getAffinity() + x);
                    break;
            }
        }
    }

    public void savePlayer(UUID uuid) {

    }

    public void addMinAffinity(UUID uuid, int x) {
        if(x != 0) PLAYER_LIST.get(uuid).setMinAffinity(PLAYER_LIST.get(uuid).getMinAffinity() + x);
    }
    public void addMaxAffinity(UUID uuid, int x) {
        if(x != 0) PLAYER_LIST.get(uuid).setMaxAffinity(PLAYER_LIST.get(uuid).getMaxAffinity() + x);
    }
}
