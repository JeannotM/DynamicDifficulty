package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.events.*;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.Bukkit;

import java.util.HashSet;

public class EventManager {

    private final MainManager MAIN_MANAGER;
    private final HashSet<BaseListener> LISTENERS = new HashSet<>();

    public EventManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
        registerEvents();
    }

    public void registerEvents() {
        LISTENERS.add(new CreeperExplodeListener(MAIN_MANAGER));
        LISTENERS.add(new BlockMinedListener(MAIN_MANAGER));
        LISTENERS.add(new CommandListener(MAIN_MANAGER));
        LISTENERS.add(new EntityDeathListener(MAIN_MANAGER));
        LISTENERS.add(new EntityHitListener(MAIN_MANAGER));
        LISTENERS.add(new EntityTargetListener(MAIN_MANAGER));
        LISTENERS.add(new HungerListener(MAIN_MANAGER));
        LISTENERS.add(new InventoryListener(MAIN_MANAGER));
        LISTENERS.add(new ItemDamageListener(MAIN_MANAGER));
        LISTENERS.add(new MobSpawnListener(MAIN_MANAGER));
        LISTENERS.add(new PlayerDeathListener(MAIN_MANAGER));
        LISTENERS.add(new PlayerJoinListener(MAIN_MANAGER));
        LISTENERS.add(new PlayerLeaveListener(MAIN_MANAGER));
        LISTENERS.add(new PotionEffectListener(MAIN_MANAGER));
        LISTENERS.add(new HealthRegenListener(MAIN_MANAGER));
        LISTENERS.add(new EntityDamageListener(MAIN_MANAGER));

        for(BaseListener listener : LISTENERS)
            Bukkit.getPluginManager().registerEvents(listener, MAIN_MANAGER.getPlugin());
    }

    public void reloadConfig() {
        for(BaseListener listener : LISTENERS)
            listener.reloadConfig();
    }
}
