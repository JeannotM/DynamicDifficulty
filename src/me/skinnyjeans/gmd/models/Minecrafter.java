package me.skinnyjeans.gmd.models;

import java.util.UUID;

public class Minecrafter {
    public UUID uuid;
    public String name;
    public int affinity;
    public int gainedThisMinute;
    public int minAffinity = -1;
    public int maxAffinity = -1;

    public Minecrafter(String name, UUID uuid) { this.name = name; this.uuid = uuid; }
    public Minecrafter(String name) { this.name = name; }
    public Minecrafter(UUID uuid) { this.uuid = uuid; }
    public Minecrafter() {}

    public Minecrafter clone() {
        Minecrafter clone = new Minecrafter();
        clone.uuid = uuid;
        clone.name = name;
        clone.affinity = affinity;
        clone.minAffinity = minAffinity;
        clone.maxAffinity = maxAffinity;
        clone.gainedThisMinute = gainedThisMinute;
        return clone;
    }
}
