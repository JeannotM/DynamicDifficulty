package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;
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

    public void addPlayer(Player player) {

    }

    public boolean playerExist(Player player) { return PLAYER_LIST.containsKey(player.getUniqueId()); }

    public boolean isPlayerValid(Entity player) {
        if(MAIN_MANAGER.getDataManager().isWorldDisabled(player.getWorld().getName())) return false;
        if(!(player instanceof Player)) return false;
        if(player.hasMetadata("NPC")) return false;
        if(!playerExist((Player) player)) addPlayer((Player) player);
        return true;
    }

    public void addAffinity(UUID uuid, int x) {
        if(x != 0) {
            if (difficultyType.equals("world") || uuid == null) { worldAffinity = calcAffinity((UUID)null, worldAffinity + x); }
            else if (difficultyType.equals("biome")) {
                Player p = Bukkit.getOfflinePlayer(uuid).getPlayer();
                Minecrafter biome = biomeList.get(p.getWorld().getBiome(p.getLocation()).toString());
                biome.setAffinity(calcAffinity(p.getWorld().getBiome(p.getLocation()).toString(), biome.getAffinity() + x));
            }
            else { PLAYER_LIST.get(uuid).setAffinity(calcAffinity(uuid, playerList.get(uuid).getAffinity() + x)); }
        }
    }

    public void addMinAffinity(UUID uuid, int x) {
        if(x != 0) {
            Minecrafter p = PLAYER_LIST.get(uuid);
            p.setMinAffinity(calcAffinity((UUID)null, p.getMinAffinity() + x));
        }
    }
    public void addMaxAffinity(UUID uuid, int x) {
        if(x != 0) {
            Minecrafter p = PLAYER_LIST.get(uuid);
            p.setMaxAffinity(calcAffinity((UUID)null, p.getMaxAffinity() + (x * -1)));
        }
    }
}
