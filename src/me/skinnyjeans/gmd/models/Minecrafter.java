package me.skinnyjeans.gmd.models;

import java.util.UUID;

public class Minecrafter {
    private String name;
    private UUID uuid;
    private int affinity;
    private int minAffinity;
    private int maxAffinity;

    public Minecrafter(UUID u, String n) {
        uuid = u;
        name = n;
    }

    public int getMinAffinity() { return minAffinity; }
    public int getMaxAffinity() { return maxAffinity; }
    public int getAffinity() { return affinity; }
    public UUID getUUID() { return uuid; }
    public String getName() { return name; }

    public void addAffinity(int value) { setAffinity(value + affinity); }
    public void addMinAffinity(int value, int limit) { setMinAffinity(Math.min(limit, value + minAffinity)); }
    public void addMaxAffinity(int value, int limit) { setMaxAffinity(Math.max(limit, value * -1 + maxAffinity)); }

    public void setMinAffinity(int value) {
        if(maxAffinity != -1 && minAffinity != -1 && minAffinity > maxAffinity) {
            minAffinity = maxAffinity;
        } else {
            minAffinity = value;
        }
    }
    public void setMaxAffinity(int value) {
        if(maxAffinity != -1 && minAffinity != -1 && minAffinity > maxAffinity) {
            maxAffinity = minAffinity;
        } else {
            maxAffinity = value;
        }
    }
    public void setAffinity(int value) {
        if (maxAffinity != -1 && value > maxAffinity) {
            value = maxAffinity;
        } else if (minAffinity != -1 && value < minAffinity) {
            value = minAffinity;
        }
        affinity = value;
    }
}
