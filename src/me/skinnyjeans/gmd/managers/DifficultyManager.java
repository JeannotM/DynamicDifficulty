package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class DifficultyManager {

    private final MainManager MAIN_MANAGER;

    private final HashMap<UUID, Difficulty> PLAYER_LIST = new HashMap<>();
    private final HashMap<String, Difficulty> DIFFICULTY_LIST = new HashMap<>();
    private final ArrayList<String> DIFFICULTY_LIST_SORTED = new ArrayList<>();

    private DifficultyTypes DifficultyType;

    public DifficultyManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
        Bukkit.getScheduler().runTaskTimerAsynchronously(MAIN_MANAGER.getPlugin(), this::calculateAllPlayers, 20 * 30, 20 * 120);
    }

    public ArrayList<Difficulty> getDifficulties() { return new ArrayList<>(DIFFICULTY_LIST.values()); }

    public ArrayList<String> getDifficultyNames() { return DIFFICULTY_LIST_SORTED; }

    public Difficulty getDifficulty(String name) { return DIFFICULTY_LIST.get(name); }

    public Difficulty getDifficulty(UUID uuid) { return PLAYER_LIST.getOrDefault(uuid, DIFFICULTY_LIST.get(DIFFICULTY_LIST_SORTED.get(0))); }

    public DifficultyTypes getType() { return DifficultyType; }

    public Difficulty calcDifficulty(int affinity) {
        for (String difficulty : DIFFICULTY_LIST_SORTED)
            if(affinity <= DIFFICULTY_LIST.get(difficulty).getUntil())
                return DIFFICULTY_LIST.get(difficulty);
        return DIFFICULTY_LIST.get(DIFFICULTY_LIST_SORTED.get(0));
    }

    public void calculateAllPlayers() {
        if(MAIN_MANAGER.getPlayerManager().getPlayerList().size() != 0)
            MAIN_MANAGER.getPlayerManager().getPlayerList().forEach((key, value) -> calculateDifficulty(key));
    }

    public String getProgress(UUID uuid) {
        int a = getDifficulty(getDifficulty(uuid).getDifficultyName()).getAffinity();
        int b = getNextDifficulty(uuid).getAffinity();

        if(a == b) return "100.0%";
        return Math.round(1000.0 * Math.abs(1.0 - (100.0 / (a - b) * (MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid).getAffinity() - b)) / 100.0)) / 10.0 + "%";
    }

    public Difficulty getNextDifficulty(UUID uuid) {
        int index = DIFFICULTY_LIST_SORTED.indexOf(getDifficulty(uuid).getDifficultyName());
        if (index != DIFFICULTY_LIST_SORTED.size() - 1 || index == -1) index++;

        return DIFFICULTY_LIST.get(DIFFICULTY_LIST_SORTED.get(index));
    }

    public void calculateDifficulty(UUID uuid) {
        Difficulty difficulty = calculateDifficulty(MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid));
        PLAYER_LIST.put(uuid, difficulty);
    }

    public Difficulty calculateDifficulty(Minecrafter affinity) {
        Difficulty first = calcDifficulty(affinity.getAffinity());
        Difficulty second = DIFFICULTY_LIST.get(getNextDifficulty(affinity.getUUID()).getDifficultyName());

        Difficulty difficulty = new Difficulty(first.getDifficultyName());

        int a = first.getAffinity();
        int b = second.getAffinity();
//                            1.0 - (100.0 / (400 - 600) * (550 - 600) / 100.0) = 0.75
        double c = Math.abs(1.0 - (100.0 / (a - b) * (affinity.getAffinity() - b)) / 100.0);

        difficulty.setDoubleLoot(calculatePercentage(first.getDoubleLoot(), second.getDoubleLoot(), c));
        difficulty.setHungerDrain(calculatePercentage(first.getHungerDrain(), second.getHungerDrain(), c));
        difficulty.setDamageByMobs(calculatePercentage(first.getDamageByMobs(), second.getDamageByMobs(), c));
        difficulty.setDamageOnMobs(calculatePercentage(first.getDamageOnMobs(), second.getDamageOnMobs(), c));
        difficulty.setDamageOnTamed(calculatePercentage(first.getDamageOnTamed(), second.getDamageOnTamed(), c));
        difficulty.setArmorDropChance(calculatePercentage(first.getArmorDropChance(), second.getArmorDropChance(), c));
        difficulty.setChanceToHaveArmor(calculatePercentage(first.getChanceToHaveArmor(), second.getChanceToHaveArmor(), c));
        difficulty.setDamageByRangedMobs(calculatePercentage(first.getDamageByRangedMobs(), second.getDamageByRangedMobs(), c));
        difficulty.setExperienceMultiplier(calculatePercentage(first.getExperienceMultiplier(), second.getExperienceMultiplier(), c));
        difficulty.setDoubleDurabilityDamageChance(calculatePercentage(first.getDoubleDurabilityDamageChance(), second.getDoubleDurabilityDamageChance(), c));
        difficulty.setMaxEnchants(calculatePercentage(first.getMaxEnchants(), second.getMaxEnchants(), c));
        difficulty.setMaxEnchantLevel(calculatePercentage(first.getMaxEnchantLevel(), second.getMaxEnchantLevel(), c));
        difficulty.setChanceToEnchant(calculatePercentage(first.getChanceToEnchant(), second.getChanceToEnchant(), c));
        difficulty.setWeaponDropChance(calculatePercentage(first.getWeaponDropChance(), second.getWeaponDropChance(), c));
        HashMap<EquipmentItems, Double> equipmentValues = new HashMap<>();
        for(EquipmentItems item : EquipmentItems.values())
            equipmentValues.put(item, calculatePercentage(first.getEnchantChance(item), second.getEnchantChance(item), c));
        difficulty.setEnchantChances(equipmentValues);

        difficulty.setArmorDamageMultiplier(first.getArmorDamageMultiplier());
        difficulty.setAllowHealthRegen(first.getAllowHealthRegen());
        difficulty.setPrefix(first.getPrefix());
        difficulty.setAllowPVP(first.getAllowPVP());
        difficulty.setKeepInventory(first.getKeepInventory());
        difficulty.setEffectsOnAttack(first.getEffectsOnAttack());
        difficulty.setDisabledCommands(first.getDisabledCommands());
        difficulty.setIgnoredMobs(first.getIgnoredMobs());

        Bukkit.getConsoleSender().sendMessage("diff "+ first.getDifficultyName() + " : " + second.getDifficultyName());
        Bukkit.getConsoleSender().sendMessage("c "+ c);
        Bukkit.getConsoleSender().sendMessage("damby"+ difficulty.getDamageByMobs());
        Bukkit.getConsoleSender().sendMessage("exp"+ difficulty.getExperienceMultiplier());
        Bukkit.getConsoleSender().sendMessage("dbl"+ difficulty.getDoubleLoot());
        Bukkit.getConsoleSender().sendMessage("damon"+ difficulty.getDamageOnMobs());

        return difficulty;
    }

    private int calculatePercentage(int value1, int value2, double percentage) {
        if(value1 == value2) return value1;
        return (int) Math.round(value1 - ((value1 - value2) * percentage));
    }

    private double calculatePercentage(double value1, double value2, double percentage) {
        if(value1 == value2) return value1;
        return Math.round(value1 - ((value1 - value2) * percentage));
    }

    public void reloadConfig() {
        String type = MAIN_MANAGER.getDataManager().getConfig().getString("difficulty-modifiers.type", "player");
        DifficultyType = DifficultyTypes.valueOf(type.substring(0, 1).toUpperCase() + type.substring(1));

        HashMap<Integer, String> tmpMap = new HashMap<>();
        for(String key : MAIN_MANAGER.getDataManager().getConfig().getConfigurationSection("difficulty").getKeys(false)) {
            ConfigurationSection data = MAIN_MANAGER.getDataManager().getConfig().getConfigurationSection("difficulty." + key);
            Difficulty difficulty = new Difficulty(key.replace(" ", "_"));

            difficulty.setAffinity(data.getInt("affinity-required", 0));
            tmpMap.put(difficulty.getAffinity(), difficulty.getDifficultyName());
            difficulty.setDamageByMobs(data.getInt("damage-done-by-mobs", 100));
            difficulty.setDamageOnMobs(data.getInt("damage-done-on-mobs", 100));
            difficulty.setDamageOnTamed(data.getInt("damage-done-on-tamed", 100));
            difficulty.setHungerDrain(data.getInt("hunger-drain-chance", 100));
            difficulty.setDamageByRangedMobs(data.getInt("damage-done-by-ranged-mobs", 100));
            difficulty.setDoubleDurabilityDamageChance(data.getInt("double-durability-damage-chance", 0));
            difficulty.setExperienceMultiplier(data.getInt("experience-multiplier", 100));
            difficulty.setDoubleLoot(data.getInt("double-loot-chance", 1));
            difficulty.setKeepInventory(data.getBoolean("keep-inventory", false));
            difficulty.setAllowPVP(data.getBoolean("allow-pvp", true));
            difficulty.setAllowHealthRegen(data.getBoolean("allow-natural-regen", true));
            difficulty.setEffectsOnAttack(data.getBoolean("effects-when-attacked", true));
            difficulty.setPrefix(ChatColor.translateAlternateColorCodes('&', data.getString("prefix", key)));
            if(data.isSet("commands-not-allowed-on-difficulty")) difficulty.setDisabledCommands(data.getStringList("commands-not-allowed-on-difficulty"));
            if(data.isSet("extra-damage-for-certain-armor-types")) {
                HashMap<ArmorTypes, Integer> armorTypes = new HashMap<>();
                for(String armorType : data.getConfigurationSection("extra-damage-for-certain-armor-types").getKeys(false))
                    try {
                        armorTypes.put(ArmorTypes.valueOf(armorType), data.getInt("extra-damage-for-certain-armor-types." + armorType, 1));
                    } catch (Exception ignored) { }
                difficulty.setArmorDamageMultiplier(armorTypes);
            }
            if(data.isSet("mobs-ignore-player")) difficulty.setIgnoredMobs(data.getStringList("mobs-ignore-player"));
            DIFFICULTY_LIST.put(difficulty.getDifficultyName(), difficulty);

            ConfigurationSection enchantData = data.getConfigurationSection("enchanting");
            difficulty.setMaxEnchants(enchantData.getInt("max-enchants", 2));
            difficulty.setMaxEnchantLevel(enchantData.getInt("max-level", 1));
            difficulty.setChanceToHaveArmor(enchantData.getDouble("chance-to-have-armor", 15.0));
            difficulty.setChanceToEnchant(enchantData.getDouble("chance-to-enchant-a-piece", 30.0));
            difficulty.setArmorDropChance(enchantData.getDouble("armor-drop-chance", 15.0));
            difficulty.setWeaponDropChance(enchantData.getDouble("weapon-drop-chance", 10.0));
            HashMap<EquipmentItems, Double> equipmentValues = new HashMap<>();
            for(EquipmentItems item : EquipmentItems.values())
                equipmentValues.put(item, enchantData.getDouble(item.name().toLowerCase() + "-chance", 1.0));
            difficulty.setEnchantChances(equipmentValues);
        }

        TreeMap<Integer, String> tm = new TreeMap<>(tmpMap);
        String lastKey = null;
        for (int key : tm.keySet()) {
            String thisKey = tmpMap.get(key).replace(" ", "_");
            DIFFICULTY_LIST_SORTED.add(thisKey);
            if(tmpMap.size() == DIFFICULTY_LIST_SORTED.size())
                DIFFICULTY_LIST.get(thisKey).setUntil(Integer.MAX_VALUE);
            if(lastKey != null)
                DIFFICULTY_LIST.get(lastKey).setUntil(key - 1);
            lastKey = thisKey;
        }
    }
}