package me.skinnyjeans.gmd.models;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

public class MythicMobProfile {

    public final EntityType replacedWith;
    public final String mythicMobName;
    public final double chanceToReplace;

    public MythicMobProfile(String key, ConfigurationSection config) {
        mythicMobName = key;
        chanceToReplace = config.getDouble(key + ".chance-to-replace", 5.0);
        replacedWith = EntityType.valueOf(config.getString(key + ".replace-with", "ZOMBIE"));
    }
}
