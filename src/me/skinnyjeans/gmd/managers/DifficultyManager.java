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
    private boolean customPrefixAllowed;

    public DifficultyManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
        Bukkit.getScheduler().runTaskTimerAsynchronously(MAIN_MANAGER.getPlugin(), this::calculateAllPlayers, 20 * 60, 20 * 60);
    }

    public ArrayList<Difficulty> getDifficulties() { return new ArrayList<>(DIFFICULTY_LIST.values()); }

    public ArrayList<String> getDifficultyNames() { return DIFFICULTY_LIST_SORTED; }

    public Difficulty getDifficulty(String name) { return DIFFICULTY_LIST.get(name); }

    public Difficulty getDifficulty(UUID uuid) {
        return PLAYER_LIST.get(uuid);
    }

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

    public String getPrefix(int value) {
        Difficulty difficulty = calcDifficulty(value);
        return (customPrefixAllowed) ? difficulty.getPrefix() : difficulty.getDifficultyName();
    }

    public String getPrefix(UUID uuid) {
        Difficulty difficulty = getDifficulty(uuid);
        return (customPrefixAllowed) ? difficulty.getPrefix() : difficulty.getDifficultyName();
    }

    public void calculateDifficulty(UUID uuid) {
        Difficulty difficulty = calculateDifficulty(MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid));
        PLAYER_LIST.put(uuid, difficulty);
    }

    public Difficulty calculateDifficulty(Minecrafter affinity) {
        Difficulty first = calcDifficulty(affinity.getAffinity());
        int index = DIFFICULTY_LIST_SORTED.indexOf(first.getDifficultyName());
        if (index != DIFFICULTY_LIST_SORTED.size() - 1) index++;
        Difficulty second = DIFFICULTY_LIST.get(DIFFICULTY_LIST_SORTED.get(index));

        Difficulty difficulty = new Difficulty("");

        int a = first.getAffinity();
        int b = second.getAffinity();
//                            (100.0 / (-200) * (-100) / 100.0) = 0.5
//                            (100.0 / (400 - 600) * (500 - 600) / 100.0) = 0.5
        double c = Math.abs((100.0 / (a - b) * (affinity.getAffinity() - b)) / 100.0);

        difficulty.setDoubleLoot(calculatePercentage(first.getDoubleLoot(), second.getDoubleLoot(), c));
        difficulty.setHungerDrain(calculatePercentage(first.getHungerDrain(), second.getHungerDrain(), c));
        difficulty.setMaxEnchants(calculatePercentage(first.getMaxEnchants(), second.getMaxEnchants(), c));
        difficulty.setDamageByMobs(calculatePercentage(first.getDamageByMobs(), second.getDamageByMobs(), c));
        difficulty.setDamageOnMobs(calculatePercentage(first.getDamageOnMobs(), second.getDamageOnMobs(), c));
        difficulty.setArmorDropChance(calculatePercentage(first.getArmorDropChance(), second.getArmorDropChance(), c));
        difficulty.setMaxEnchantLevel(calculatePercentage(first.getMaxEnchantLevel(), second.getMaxEnchantLevel(), c));
        difficulty.setChanceToEnchant(calculatePercentage(first.getChanceToEnchant(), second.getChanceToEnchant(), c));
        difficulty.setWeaponDropChance(calculatePercentage(first.getWeaponDropChance(), second.getWeaponDropChance(), c));
        difficulty.setChanceToHaveArmor(calculatePercentage(first.getChanceToHaveArmor(), second.getChanceToHaveArmor(), c));
        difficulty.setDamageByRangedMobs(calculatePercentage(first.getDamageByRangedMobs(), second.getDamageByRangedMobs(), c));
        difficulty.setExperienceMultiplier(calculatePercentage(first.getExperienceMultiplier(), second.getExperienceMultiplier(), c));
        difficulty.setDoubleDurabilityDamageChance(calculatePercentage(first.getDoubleDurabilityDamageChance(), second.getDoubleDurabilityDamageChance(), c));

//        difficulty.setDamageByMobs(multiplyInt(first.getDamageByMobs(), c));
//        difficulty.setDamageOnMobs(multiplyInt(first.getDamageOnMobs(), c));
//        difficulty.setExperienceMultiplier(multiplyInt(first.getExperienceMultiplier(), c));
//        difficulty.setHungerDrain(multiplyInt(first.getHungerDrain(), c));
//        difficulty.setDoubleLoot(multiplyInt(first.getDoubleLoot(), c));
//        difficulty.setMaxEnchants(multiplyInt(first.getMaxEnchants(), c));
//        difficulty.setMaxEnchantLevel(multiplyInt(first.getMaxEnchantLevel(), c));
//        difficulty.setDamageByRangedMobs(multiplyInt(first.getDamageByRangedMobs(), c));
//        difficulty.setDoubleDurabilityDamageChance(multiplyInt(first.getDoubleDurabilityDamageChance(), c));
//        difficulty.setArmorDropChance(first.getArmorDropChance() * c);
//        difficulty.setWeaponDropChance(first.getWeaponDropChance() * c);
//        difficulty.setChanceToEnchant(first.getChanceToEnchant() * c);
//        difficulty.setChanceToHaveArmor(first.getChanceToHaveArmor() * c);

        difficulty.setArmorDamageMultiplier(first.getArmorDamageMultiplier());
        difficulty.setAllowPVP(first.getAllowPVP());
        difficulty.setKeepInventory(first.getKeepInventory());
        difficulty.setEffectsOnAttack(first.getEffectsOnAttack());
        difficulty.setDisabledCommands(first.getDisabledCommands());
        difficulty.setIgnoredMobs(first.getIgnoredMobs());
        difficulty.setEnchantChances(first.getEnchantChance());

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
        return (int) (value1 + Math.round(Math.abs(100.0 / Math.abs(value1 - value2) * percentage)));
    }

    private double calculatePercentage(double value1, double value2, double percentage) {
        if(value1 == value2) return value1;
        return value1 + Math.abs(100.0 / Math.abs(value1 - value2) * percentage);
    }

    private int multiplyInt(int value, double times) { return (int) Math.round(value * times); }

    public void reloadConfig() {
        customPrefixAllowed = MAIN_MANAGER.getDataManager().getConfig().getBoolean("plugin-support.use-prefix", true);
        String type = MAIN_MANAGER.getDataManager().getConfig().getString("difficulty-modifiers.type", "player");
        DifficultyType = DifficultyTypes.valueOf(type.substring(0, 1).toUpperCase() + type.substring(1));

//        ConfigurationSection difficulties = MAIN_MANAGER.getDataManager().getConfig().getConfigurationSection("difficulty");
//        DIFFICULTY_LIST_SORTED
        HashMap<Integer, String> tmpMap = new HashMap<>();
        for(String key : MAIN_MANAGER.getDataManager().getConfig().getConfigurationSection("difficulty").getKeys(false)) {
            ConfigurationSection data = MAIN_MANAGER.getDataManager().getConfig().getConfigurationSection("difficulty." + key);
            Difficulty difficulty = new Difficulty(key.replace(" ", "_"));

            difficulty.setAffinity(data.getInt("affinity-required", 0));
            tmpMap.put(difficulty.getAffinity(), key.replace(" ", "_"));
            difficulty.setDamageByMobs(data.getInt("damage-done-by-mobs", 100));
            difficulty.setDamageOnMobs(data.getInt("damage-done-on-mobs", 100));
            difficulty.setHungerDrain(data.getInt("hunger-drain-chance", 100));
            difficulty.setDamageByRangedMobs(data.getInt("damage-done-by-ranged-mobs", 100));
            difficulty.setDoubleDurabilityDamageChance(data.getInt("double-durability-damage-chance", 0));
            difficulty.setExperienceMultiplier(data.getInt("experience-multiplier", 100));
            difficulty.setDoubleLoot(data.getInt("double-loot-chance", 1));
            difficulty.setKeepInventory(data.getBoolean("keep-inventory", false));
            difficulty.setAllowPVP(data.getBoolean("allow-pvp", true));
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
            DIFFICULTY_LIST.put(key, difficulty);
//            if(MAIN_MANAGER.getDataManager().getConfig().getBoolean("toggle-settings.advanced.custom-mob-items-spawn-chance", false)) {
//                ConfigurationSection enchantData = MAIN_MANAGER.getDataManager().getConfig().getConfigurationSection("custom-mob-items-spawn-chance.difficulties." + key);
//
//                difficulty.setMaxEnchants(enchantData.getInt("max-enchants", 2));
//                difficulty.setMaxEnchantLevel(enchantData.getInt("max-level", 1));
//                difficulty.setChanceToHaveArmor(enchantData.getDouble("chance-to-have-armor", 15));
//                difficulty.setChanceToEnchant(enchantData.getDouble("chance-to-enchant-a-piece", 30.0));
//                difficulty.setArmorDropChance(enchantData.getDouble("armor-drop-chance", 15.0));
//                difficulty.setWeaponDropChance(enchantData.getDouble("weapon-drop-chance", 10.0));
//                HashMap<EquipmentItems, Double> equipmentValues = new HashMap<>();
//                for(EquipmentItems item : EquipmentItems.values())
//                    equipmentValues.put(item, enchantData.getDouble(item.name().toLowerCase() + "-chance", 1.0));
//                difficulty.setEnchantChances(equipmentValues);
//            }
        }
        TreeMap<Integer, String> tm = new TreeMap<>(tmpMap);
        String lastKey = null;
        for (int key : tm.keySet()) {
            String thisKey = tmpMap.get(key).replace(" ", "_");
            DIFFICULTY_LIST_SORTED.add(thisKey);
            if(tmpMap.size() == DIFFICULTY_LIST_SORTED.size())
                DIFFICULTY_LIST.get(thisKey).setUntil(MAIN_MANAGER.getAffinityManager().withinServerLimits(Integer.MAX_VALUE));
            if(lastKey != null)
                DIFFICULTY_LIST.get(lastKey).setUntil(key - 1);
            lastKey = thisKey;
        }
    }
}