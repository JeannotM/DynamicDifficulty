package me.skinnyjeans.gmd.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashSet;

public class EntityManager {

    private final MainManager MAIN_MANAGER;
    private final HashSet<EntityType> DISABLED_MOBS = new HashSet<>();
    private final HashSet<Integer> IGNORED_MOBS = new HashSet<>();

    public EntityManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    public boolean isEntityValid(Entity entity) {
        return !DISABLED_MOBS.contains(entity.getType()) && !IGNORED_MOBS.contains(entity.getEntityId());
    }

    public boolean isEntityDisabled(Entity entity) {
        return !DISABLED_MOBS.contains(entity.getType());
    }

    public boolean isEntityIgnored(Entity entity) { return IGNORED_MOBS.contains(entity.getEntityId()); }

    public void entityHit(Entity entity) {

    }

    public void reloadConfig() {
        DISABLED_MOBS.clear();
        IGNORED_MOBS.clear();

        ConfigurationSection config = MAIN_MANAGER.getDataManager().getConfig();

        StringBuilder weirdMobs = new StringBuilder("");
        for(String key : config.getStringList("disabled-mobs"))
            if(EntityType.valueOf(key) != null) {
                DISABLED_MOBS.add(EntityType.valueOf(key));
            } else {
                if(!weirdMobs.isEmpty()) weirdMobs.append(", ");
                weirdMobs.append(key);
            }

    }

}
