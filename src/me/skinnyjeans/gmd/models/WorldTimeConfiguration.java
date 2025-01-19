package me.skinnyjeans.gmd.models;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class WorldTimeConfiguration {
    private final SortedMap<Long, Double> timeAffinity = new TreeMap<>();

    public WorldTimeConfiguration(ConfigurationSection config, String name) {
        Set<String> keys = config.getKeys(true);
        for (String key : keys) {
            double difficulty = config.getDouble(key, 0.4);
            long time = Long.parseLong(key);

            timeAffinity.put(time, difficulty);
        }
    }

    public double calculateAffinity(long time) {
        if (timeAffinity.size() == 1 || time < 0) { return timeAffinity.get(timeAffinity.firstKey()); }

        long lastKey = -1, currentKey = -1, nextKey = -1;
        double currentVal = -1, nextVal = -1;
        for (SortedMap.Entry<Long, Double> entry : timeAffinity.entrySet()) {
            currentKey = entry.getKey();
            if(time <= currentKey) {
                if (lastKey == -1) {
                    nextKey = currentKey;
                    nextVal = timeAffinity.get(nextKey);
                    nextKey += 24000L;

                    currentKey = timeAffinity.lastKey();
                    currentVal = timeAffinity.get(currentKey);
                    time += 24000L;
                } else {
                    nextKey = currentKey;
                    nextVal = timeAffinity.get(nextKey);
                    currentKey = lastKey;
                    currentVal = timeAffinity.get(currentKey);
                }

                break;
            } else if (currentKey == timeAffinity.lastKey()) {
                currentVal = timeAffinity.get(currentKey);
                nextKey = timeAffinity.firstKey();
                nextVal = timeAffinity.get(nextKey);
                nextKey += 24000L;
                break;
            }
            lastKey = currentKey;
        }

        if (currentKey != -1 && nextKey != -1 && currentVal != -1 && nextVal != -1) {
            double c = (currentKey == nextKey) ? 0.0
                    : Math.abs(1.0 - (1.0 / (currentKey - nextKey) * (time - nextKey)));

            double l = currentVal - ((currentVal - nextVal) * c);

            Bukkit.getConsoleSender().sendMessage(c + ":= " + l);

            return l;
        }

        return -100L;
    }
}
