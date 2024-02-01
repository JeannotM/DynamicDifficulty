package me.skinnyjeans.gmd.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;

public class EntityManager {

    private final MainManager MAIN_MANAGER;
    private final HashSet<EntityType> DISABLED_MOBS = new HashSet<>();
    private final HashSet<Integer> IGNORED_MOBS = new HashSet<>();
    private final HashSet<Integer> OVERRIDE_IGNORE = new HashSet<>();
    private final HashMap<EntityType, Integer> MOBS = new HashMap<>();

    public EntityManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    public boolean hasEntityPoints(EntityType entity) { return MOBS.containsKey(entity); }

    public int getEntityPoints(EntityType entity) { return MOBS.get(entity); }

    public HashMap<EntityType, Integer> getMobs() { return MOBS; }

    public boolean isEntityDisabled(Entity entity) { return DISABLED_MOBS.contains(entity.getType()); }

    public boolean wasEntityAttacked(Entity entity) { return OVERRIDE_IGNORE.contains(entity.getEntityId()); }

    public boolean isEntityIgnored(Entity entity) { return IGNORED_MOBS.contains(entity.getEntityId()); }

    public void entityHit(Entity entity) { OVERRIDE_IGNORE.add(entity.getEntityId()); }

    public void ignoreEntity(Entity entity) { IGNORED_MOBS.add(entity.getEntityId()); }

    public void ignoredEntityKilled(Entity entity) { IGNORED_MOBS.remove(entity.getEntityId()); }

    public void reloadConfig() {
        DISABLED_MOBS.clear();
        IGNORED_MOBS.clear();
        MOBS.clear();

        ConfigurationSection config = MAIN_MANAGER.getDataManager().getConfig();

        for(String key : config.getStringList("disabled-mobs"))
            try {
                DISABLED_MOBS.add(EntityType.valueOf(key));
            } catch (Exception ignored) { }

        for(Object key : config.getList("mobs-count-as-pve").toArray())
            try {
                String[] sep = key.toString().replaceAll("[{|}]","").split("=");
                int value = (sep.length > 1) ? Integer.parseInt(sep[1]) : config.getInt("pve-kill", 2);
                MOBS.put(EntityType.valueOf(sep[0]), value);
            } catch (Exception ignored) { }
    }

}
