package me.skinnyjeans.gmd.models;

import me.skinnyjeans.gmd.managers.MainManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class RegionSettings extends DifficultySettings {
    private double normalRadius, netherRadius, theEndRadius, x, z;
    private boolean isReversed, isSquare;
    private int minAffinity, maxAffinity;

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

        World.Environment environment = playerLocation.getWorld().getEnvironment();

        double ans;
        if(environment == World.Environment.NETHER) {
            ans = Math.min(1.0 / netherRadius * distance, 1.0);
        } else if(environment == World.Environment.THE_END) {
            ans = Math.min(1.0 / theEndRadius * distance, 1.0);
        } else {
            ans = Math.min(1.0 / normalRadius * distance, 1.0);
        }

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

        normalRadius = config.getDouble("difficulty-type.region.radius.normal", 5000);
        netherRadius = config.getDouble("difficulty-type.region.radius.nether", 625);
        theEndRadius = config.getDouble("difficulty-type.region.radius.theend", 1250);
    }
}
