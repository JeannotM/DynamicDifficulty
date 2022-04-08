package me.skinnyjeans.gmd.models;

import java.util.UUID;

public class Minecrafter {
    private String name;
    private UUID uuid;
    private int affinity;
    private int minAffinity = -1;
    private int maxAffinity = -1;

    public Minecrafter(String n, UUID u) {
        name = n;
        uuid = u;
    }

    public int getMinAffinity() { return minAffinity; }
    public int getMaxAffinity() { return maxAffinity; }
    public int getAffinity() { return affinity; }
    public String getName() { return name; }
    public String getUUID() { return uuid; }

    public void setAffinity(int value) { affinity = value; }
    public void setMinAffinity(int value) { minAffinity = value; }
    public void setMaxAffinity(int value) { maxAffinity = value; }

}
