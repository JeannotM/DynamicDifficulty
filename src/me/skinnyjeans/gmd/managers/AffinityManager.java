package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;

import java.util.UUID;

public class AffinityManager {

    private final MainManager MAIN_MANAGER;
    private Minecrafter defaultData;
    private int intervalAffinity;
    private int serverMinAffinity;
    private int serverMaxAffinity;

    public AffinityManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;

        Bukkit.getScheduler().runTaskTimerAsynchronously(MAIN_MANAGER.getPlugin(), () ->
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (mainManager.getPlayerManager().isPlayerValid(player))
                    MAIN_MANAGER.getPlayerManager().addAffinity(player.getUniqueId(), intervalAffinity);
            }), 20 * 60, 20 * 60);
    }

    public int withinServerLimits(int value) {
        return Math.max(serverMinAffinity, Math.min(value, serverMaxAffinity));
    }

    public int withinPlayerLimits(UUID uuid, int value) {
        Minecrafter data = MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid);
        value = withinServerLimits(value);

        if(data.getMinAffinity() != -1) value = Math.max(data.getMinAffinity(), value);
        if(data.getMaxAffinity() != -1) value = Math.min(data.getMaxAffinity(), value);

        return value;
    }

    public Minecrafter getDefault() { return defaultData; }

    public void resetAffinity(UUID uuid) {
        MAIN_MANAGER.getPlayerManager().setMinAffinity(uuid, defaultData.getMinAffinity());
        MAIN_MANAGER.getPlayerManager().setMaxAffinity(uuid, defaultData.getMaxAffinity());
        MAIN_MANAGER.getPlayerManager().setAffinity(uuid, defaultData.getAffinity());
    }

    public void reloadConfig() {
        serverMinAffinity = MAIN_MANAGER.getDataManager().getConfig().getInt("min-affinity", 0);
        serverMaxAffinity = MAIN_MANAGER.getDataManager().getConfig().getInt("max-affinity", 1500);
        intervalAffinity = MAIN_MANAGER.getDataManager().getConfig().getInt("points-per-minute", 3);

        defaultData = new Minecrafter();
        defaultData.setAffinity(MAIN_MANAGER.getDataManager().getConfig().getInt("starting-affinity", 600));
        defaultData.setMinAffinity(MAIN_MANAGER.getDataManager().getConfig().getInt("starting-min-affinity", -1));
        defaultData.setMaxAffinity(MAIN_MANAGER.getDataManager().getConfig().getInt("starting-max-affinity", -1));
    }
}
