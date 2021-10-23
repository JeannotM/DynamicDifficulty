package me.skinnyjeans.gmd.models;

import java.util.UUID;

public class Minecrafter {
    private String name;
    private int affinity;
    private int minAffinity = -1;
    private int maxAffinity = -1;

    public Minecrafter(String n) {
        name = n;
    }

    public int getMinAffinity() { return minAffinity; }
    public int getMaxAffinity() { return maxAffinity; }
    public int getAffinity() { return affinity; }
    public String getName() { return name; }

    public void setAffinity(int value) { affinity = value; }
    public void setMinAffinity(int value) {
        if(maxAffinity != -1 && maxAffinity < value) {
            minAffinity = maxAffinity;
        } else {
            minAffinity = value;
        }
    }
    public void setMaxAffinity(int value) {
        if(minAffinity != -1 && minAffinity > value) {
            maxAffinity = minAffinity;
        } else {
            maxAffinity = value;
        }
    }
}
