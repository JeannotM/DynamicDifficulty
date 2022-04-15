package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.Minecrafter;

import java.util.UUID;

public class AffinityManager {

    private final MainManager MAIN_MANAGER;
    private int serverMinAffinity;
    private int serverMaxAffinity;

    public AffinityManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
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

    public void setAffinity(UUID uuid, int value) {
        value = withinPlayerLimits(uuid, value);
        MAIN_MANAGER.getPlayerManager().setAffinity(uuid, value);
    }

    public void reloadConfig() {
        serverMinAffinity = MAIN_MANAGER.getDataManager().getConfig().getInt("min-affinity", 0);
        serverMaxAffinity = MAIN_MANAGER.getDataManager().getConfig().getInt("max-affinity", 1500);
    }
}
