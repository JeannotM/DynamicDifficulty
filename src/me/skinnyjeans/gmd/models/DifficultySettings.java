package me.skinnyjeans.gmd.models;

import me.skinnyjeans.gmd.managers.MainManager;
import org.bukkit.entity.Player;

public abstract class DifficultySettings {
    MainManager MAIN_MANAGER;

    public DifficultySettings(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    public abstract int calculateAffinity(Player player, int affinity);
    public abstract void reloadConfig();
}
