package me.skinnyjeans.gmd.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Difficulty {
    public String difficultyName;
    public String prefix;

    public double damageDoneByMobs;
    public double damageDoneOnMobs;
    public double damageDoneOnTamed;
    public double hungerDrainChance;
    public double damageByRangedMobs;
    public double experienceMultiplier;
    public double doubleLootChance;
    public double doubleDurabilityDamageChance;
    public double damagePerArmorPoint;
    public double chanceCancelDeath;
    public double armorDropChance;
    public double chanceToEnchant = Double.NaN;
    public double chanceToHaveArmor;
    public double chanceToHaveWeapon;
    public double weaponDropChance;

    public int affinityRequirement;
    public int maxEnchants;
    public int maxEnchantLevel;
    public int difficultyUntil;
    public int minimumStarvationHealth;
    public int maximumHealth;

    public boolean allowPVP;
    public boolean keepInventory;
    public boolean allowHealthRegen;
    public boolean effectsWhenAttacked;

    public List<String> disabledCommands = new ArrayList<>();
    public List<String> mobsIgnoredPlayers = new ArrayList<>();
    public List<String> commandsOnJoin = new ArrayList<>();
    public List<String> commandsOnSwitchFromPrev = new ArrayList<>();
    public List<String> commandsOnSwitchFromNext = new ArrayList<>();
    public List<MythicMobProfile> mythicMobProfiles = new ArrayList<>();
    public HashMap<EquipmentItems, Double> armorChance = new HashMap<EquipmentItems, Double>();
    public HashMap<ArmorTypes, Double> armorDamageMultipliers = new HashMap<ArmorTypes, Double>();

    public Difficulty(String name) { difficultyName = name; }

    public String getDifficultyName() { return difficultyName; }
    public int getAffinity() { return affinityRequirement; }

    public double getArmorDamageMultipliers(ArmorTypes type) { return armorDamageMultipliers.getOrDefault(type, 0.0); }
    public double getArmorChance(EquipmentItems type) { return armorChance.getOrDefault(type, 0.0); }
}
