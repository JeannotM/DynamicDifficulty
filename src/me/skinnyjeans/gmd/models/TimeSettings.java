package me.skinnyjeans.gmd.models;

import me.skinnyjeans.gmd.managers.MainManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Set;

public class TimeSettings extends DifficultySettings {
    private final HashMap<String, WorldTimeConfiguration> timeAffinity = new HashMap<>();
    private int defaultValue, startAffinity;

    private boolean ignoreNether, ignoreTheEnd;

    public TimeSettings(MainManager mainManager) {
        super(mainManager);
        reloadConfig();
    }

    @Override
    public int calculateAffinity(Player player, int affinity) {
        World world = player.getWorld();

        if(!timeAffinity.containsKey(world.getName())
            || ignoreNether && world.getEnvironment().equals(World.Environment.NETHER)
            || ignoreTheEnd && world.getEnvironment().equals(World.Environment.THE_END)) {

            return defaultValue;
        }

        double t = timeAffinity.get(world.getName()).calculateAffinity(world.getTime());
        return (int) Math.round(startAffinity
                * t);
    }

    @Override
    public void reloadConfig() {
        timeAffinity.clear();

        ConfigurationSection config = MAIN_MANAGER.getDataManager().getConfig();

        ignoreNether = config.getBoolean("difficulty-type.time.ignore-nether", false);
        ignoreTheEnd = config.getBoolean("difficulty-type.time.ignore-the-end", false);

        startAffinity = config.getInt("max-affinity", 1500);
        defaultValue = (int) Math.round(startAffinity
                * config.getDouble("difficulty-type.time.default", 0.4));


        ConfigurationSection worldScales = config.getConfigurationSection("difficulty-type.time.world-scales");
        Set<String> keys = worldScales.getKeys(false);
        for (String key : keys) {
            ConfigurationSection worldScaleSection = worldScales.getConfigurationSection(key);
            timeAffinity.put(key, new WorldTimeConfiguration(worldScaleSection, key));
        }
    }
}
