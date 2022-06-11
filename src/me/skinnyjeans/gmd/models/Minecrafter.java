package me.skinnyjeans.gmd.models;

import java.util.UUID;

public class Minecrafter {
    private UUID uuid;
    private String name;
    private int affinity;
    private int minAffinity = -1;
    private int maxAffinity = -1;

    public Minecrafter(String name, UUID uuid) { this.name = name; this.uuid = uuid; }
    public Minecrafter(String name) { this.name = name; }
    public Minecrafter(UUID uuid) { this.uuid = uuid; }
    public Minecrafter() {}

    public UUID getUUID() { return uuid; }
    public String getName() { return name; }
    public int getAffinity() { return affinity; }
    public int getMinAffinity() { return minAffinity; }
    public int getMaxAffinity() { return maxAffinity; }

    public void setUUID(UUID value) { uuid = value; }
    public void setName(String value) { name = value; }
    public void setAffinity(int value) { affinity = value; }
    public void setMinAffinity(int value) { minAffinity = value; }
    public void setMaxAffinity(int value) { maxAffinity = value; }
}
