package me.skinnyjeans.gmd.models;

import java.util.UUID;

public class Minecrafter {
    private String name;
    private UUID uuid;
    private int affinity;
    private int minAffinity;
    private int maxAffinity;
    private int serverMaxAffinity;
    private int serverMinAffinity;

    public Minecrafter(UUID u, String n, int maxAf, int minAf) {
        uuid = u;
        name = n;
        serverMaxAffinity = maxAf;
        serverMinAffinity = minAf;
    }

    public int getMinAffinity() { return minAffinity; }
    public int getMaxAffinity() { return maxAffinity; }
    public int getAffinity() { return affinity; }
    public UUID getUUID() { return uuid; }
    public String getName() { return name; }

    public void addAffinity(int value) { setAffinity(value + affinity); }
    public void addMinAffinity(int value, int limit) { setMinAffinity(Math.min(value + minAffinity, limit)); }
    public void addMaxAffinity(int value, int limit) { setMaxAffinity(Math.max((value * -1) + maxAffinity, limit)); }

    public void setMinAffinity(int value) {
        if(maxAffinity != -1 && minAffinity != -1 && minAffinity > maxAffinity) {
            minAffinity = maxAffinity;
        } else if (value > serverMaxAffinity) {
            minAffinity = serverMaxAffinity;
        } else if (serverMinAffinity > value) {
            minAffinity = serverMinAffinity;
        } else {
            minAffinity = value;
        }
    }
    public void setMaxAffinity(int value) {
        if(maxAffinity != -1 && minAffinity != -1 && maxAffinity < minAffinity) {
            maxAffinity = minAffinity;
        } else if (value > serverMaxAffinity) {
            maxAffinity = serverMaxAffinity;
        } else if (serverMinAffinity > value) {
            maxAffinity = serverMinAffinity;
        } else {
            maxAffinity = value;
        }
    }
    public void setAffinity(int value) {
        if (maxAffinity != -1 && value > maxAffinity) {
            value = maxAffinity;
        } else if (minAffinity != -1 && minAffinity > value) {
            value = minAffinity;
        } else if (value > serverMaxAffinity) {
            value = serverMaxAffinity;
        } else if (serverMinAffinity > value) {
            value = serverMinAffinity;
        }
        affinity = value;
    }
}
