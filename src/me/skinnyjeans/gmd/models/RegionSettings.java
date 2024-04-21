package me.skinnyjeans.gmd.models;

import me.skinnyjeans.gmd.managers.MainManager;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class RegionSettings extends DifficultySettings {
    private final HashMap<String, Double> worldRadius = new HashMap<>();
    private boolean isReversed, isSquare;
    private int minAffinity, maxAffinity;
    private double defaultRadius, x, z;

    public RegionSettings(MainManager mainManager) {
        super(mainManager);
        reloadConfig();
    }

    @Override
    public int calculateAffinity(Player player, int affinity) {
        Location playerLocation = player.getLocation();

        double distance;
        if(isSquare) {
            double playerX = playerLocation.getX();
            double playerZ = playerLocation.getZ();

            distance = Math.max(Math.abs(playerX - x), Math.abs(playerZ - z));
        } else {
            Location location = new Location(player.getWorld(), x, playerLocation.getY(), z);
            distance = location.distance(playerLocation);
        }

        double ans = Math.min(1.0
                / worldRadius.getOrDefault(playerLocation.getWorld().getName(), defaultRadius)
                * distance, 1.0);

        if(isReversed) { ans = 1.0 - ans; }
        return (int) Math.floor(minAffinity + (maxAffinity - minAffinity) * ans);
    }

    @Override
    public void reloadConfig() {
        ConfigurationSection config = MAIN_MANAGER.getDataManager().getConfig();
        x = config.getDouble("difficulty-type.region.x", 0);
        z = config.getDouble("difficulty-type.region.z", 0);

        isReversed = config.getBoolean("difficulty-type.region.reversed", false);
        isSquare = config.getString("difficulty-type.region.type", "circle").equalsIgnoreCase("square");

        minAffinity = config.getInt("min-affinity", 0);
        maxAffinity = config.getInt("max-affinity", 1500);

        defaultRadius = config.getDouble("difficulty-type.region.default-radius", 5000);
        ConfigurationSection world = config.getConfigurationSection("difficulty-type.region.radius");

        if(world == null) { return; }
        for(String key : world.getKeys(false)) {
            worldRadius.put(key.toLowerCase(), world.getDouble(key));
        }
    }
}
