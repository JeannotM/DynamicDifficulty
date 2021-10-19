package me.skinnyjeans.gmd.models;

import java.util.UUID;

public class Minecrafter {
    private String name;
    private int affinity;
    private int minAffinity;
    private int maxAffinity;

    public Minecrafter(String n) {
        name = n;
    }

    public int getMinAffinity() { return minAffinity; }
    public int getMaxAffinity() { return maxAffinity; }
    public int getAffinity() { return affinity; }
    public String getName() { return name; }

    public void setAffinity(int value) { affinity = value; }
    public void setMinAffinity(int value) {
        if(maxAffinity != -1 && minAffinity != -1 && value > maxAffinity) {
            minAffinity = maxAffinity;
        } else {
            minAffinity = value;
        }
    }
    public void setMaxAffinity(int value) {
        if(maxAffinity != -1 && minAffinity != -1 && value < minAffinity) {
            maxAffinity = minAffinity;
        } else {
            maxAffinity = value;
        }
    }
}
