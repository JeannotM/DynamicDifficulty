package me.skinnyjeans.gmd.models;

import me.skinnyjeans.gmd.managers.MainManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WorldSettings extends DifficultySettings {
    private final HashMap<UUID, Minecrafter> worldAffinity = new HashMap<>();
    private int startAffinity, minAffinity, maxAffinity;

    public WorldSettings(MainManager mainManager) {
        super(mainManager);
        reloadConfig();
    }

    @Override
    public int calculateAffinity(Player player, int affinity) {
        int newValue = worldAffinity.get(player.getWorld().getUID()).affinity
                += affinity;

        return worldAffinity.get(player.getWorld().getUID()).affinity
                = Math.min(maxAffinity, Math.max(minAffinity, newValue));
    }

    @Override
    public void reloadConfig() {
        List<World> worldList = Bukkit.getWorlds();

        ConfigurationSection config = MAIN_MANAGER.getDataManager().getConfig();
        startAffinity = config.getInt("starting-affinity", 0);
        minAffinity = config.getInt("min-affinity", 0);
        maxAffinity = config.getInt("max-affinity", 1500);

        for(World world : worldList) {
            UUID uuid = world.getUID();
            MAIN_MANAGER.getDataManager().getAffinityValues(uuid, (affinity) -> {
                if(affinity == null) {
                    affinity = new Minecrafter(world.getName(), uuid);
                    affinity.affinity = startAffinity;
                }

                worldAffinity.put(uuid, affinity);
            });
        }
    }
}
