package me.skinnyjeans.gmd.models;

public class EntityDifficulty {

    private String difficulty;
    private int damageDoneByMobs;
    private int damageDoneOnMobs;
    private int experienceMultiplier;
    private int hungerDrainChance;
    private int doubleLootChance;
    private int maxEnchants;
    private int maxEnchantLevel;
    private int damageByRangedMobs;
    private int doubleDurabilityDamageChance;
    private double armorDropChance;
    private double chanceToEnchant = Double.NaN;
    private double chanceToHaveArmor;
    private double weaponDropChance;

    public String getDifficulty() { return difficulty; }
    public int getDamageByMobs() { return damageDoneByMobs; }
    public int getDamageOnMobs() { return damageDoneOnMobs; }
    public int getHungerDrain() { return hungerDrainChance; }
    public int getDoubleLoot() { return doubleLootChance; }
    public int getMaxEnchants() { return maxEnchants; }
    public int getMaxEnchantLevel() { return maxEnchantLevel; }
    public int getDamageByRangedMobs() { return damageByRangedMobs; }
    public int getExperienceMultiplier() { return experienceMultiplier; }
    public int getDoubleDurabilityDamageChance() { return doubleDurabilityDamageChance; }
    public double getArmorDropChance() { return armorDropChance; }
    public double getChanceToEnchant() { return chanceToEnchant; }
    public double getChanceToHaveArmor() { return chanceToHaveArmor; }
    public double getWeaponDropChance() { return weaponDropChance; }

    public void setDifficulty(String value) { difficulty = value; }
    public void setDamageByMobs(int value) { damageDoneByMobs = value; }
    public void setDamageOnMobs(int value) { damageDoneOnMobs = value; }
    public void setHungerDrain(int value) { hungerDrainChance = value; }
    public void setDoubleLoot(int value) { doubleLootChance = value; }
    public void setMaxEnchants(int value) { maxEnchants = value; }
    public void setMaxEnchantLevel(int value) { maxEnchantLevel = value; }
    public void setDamageByRangedMobs(int value) { damageByRangedMobs = value; }
    public void setExperienceMultiplier(int value) { experienceMultiplier = value; }
    public void setDoubleDurabilityDamageChance(int value) { doubleDurabilityDamageChance = value; }
    public void setArmorDropChance(Double value) { armorDropChance = value; }
    public void setWeaponDropChance(Double value) { weaponDropChance = value; }
    public void setChanceToEnchant(Double value) { chanceToEnchant = value; }
    public void setChanceToHaveArmor(Double value) { chanceToHaveArmor = value; }
}
