package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DifficultyManager {

    private final MainManager MAIN_MANAGER;

    private final HashMap<UUID, Difficulty> PLAYER_LIST = new HashMap<>();
    private final HashMap<String, Difficulty> DIFFICULTY_LIST = new HashMap<>();
    private final ArrayList<String> DIFFICULTY_LIST_SORTED = new ArrayList<>();

    private BukkitTask calculateTimer = null;
    private boolean exactPercentage = true;
    private boolean calculateHealth = false;
    private DifficultyTypes DifficultyType;

    public DifficultyManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;

        if(MAIN_MANAGER.getDataManager().getConfig().getBoolean("toggle-settings.force-hard-difficulty", true))
            Bukkit.getWorlds().forEach(world -> {
                if(!MAIN_MANAGER.getDataManager().isWorldDisabled(world.getName()))
                    world.setDifficulty(org.bukkit.Difficulty.HARD);
            });
    }

    public ArrayList<Difficulty> getDifficulties() { return new ArrayList<>(DIFFICULTY_LIST.values()); }

    public ArrayList<String> getDifficultyNames() { return DIFFICULTY_LIST_SORTED; }

    public Difficulty getDifficulty(String name) { return DIFFICULTY_LIST.get(name); }

    public Difficulty getDifficulty(UUID uuid) { return PLAYER_LIST.getOrDefault(uuid, DIFFICULTY_LIST.get(DIFFICULTY_LIST_SORTED.get(0))); }

    public DifficultyTypes getType() { return DifficultyType; }

    public Difficulty calcDifficulty(int affinity) {
        for (String difficulty : DIFFICULTY_LIST_SORTED)
            if(affinity < DIFFICULTY_LIST.get(difficulty).difficultyUntil)
                return DIFFICULTY_LIST.get(difficulty);

        return DIFFICULTY_LIST.get(DIFFICULTY_LIST_SORTED.get(0));
    }

    public void calculateAllPlayers() {
        MAIN_MANAGER.getPlayerManager().getPlayerList().forEach((key, value) -> calculateDifficulty(key));
    }

    public String getProgress(UUID uuid) {
        int a = getDifficulty(getDifficulty(uuid).getDifficultyName()).getAffinity();
        int b = getNextDifficulty(uuid).getAffinity();

        if(a == b) return "100.0%";
        return Math.round(1000.0 * Math.abs(1.0 - (100.0 / (a - b) * (MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid).affinity - b)) / 100.0)) / 10.0 + "%";
    }

    public Difficulty getNextDifficulty(UUID uuid) {
        int index = DIFFICULTY_LIST_SORTED.indexOf(getDifficulty(uuid).getDifficultyName());
        if (index != DIFFICULTY_LIST_SORTED.size() - 1 || index == -1) index++;

        return DIFFICULTY_LIST.get(DIFFICULTY_LIST_SORTED.get(index));
    }

    public void calculateDifficulty(UUID uuid) {
        Difficulty oldDifficulty = getDifficulty(uuid);
        Difficulty difficulty = calculateDifficulty(MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid));
        PLAYER_LIST.put(uuid, difficulty);

        handleDifficultyChange(uuid, oldDifficulty, difficulty);
    }

    private void handleDifficultyChange(UUID uuid, Difficulty oldDifficulty, Difficulty newDifficulty) {
        if (oldDifficulty.getDifficultyName().equals(newDifficulty.getDifficultyName())) return;

        int oldDifficultyIndex = DIFFICULTY_LIST_SORTED.indexOf(oldDifficulty.getDifficultyName());
        int newDifficultyIndex = DIFFICULTY_LIST_SORTED.indexOf(newDifficulty.getDifficultyName());

        boolean directionIsUp = oldDifficultyIndex < newDifficultyIndex;
        ArrayList<String> difficultiesInBetween = new ArrayList<>();

        if (directionIsUp) {
            for (int i = oldDifficultyIndex + 1; i <= newDifficultyIndex; i++)
                difficultiesInBetween.add(DIFFICULTY_LIST_SORTED.get(i));
        } else {
            for (int i = oldDifficultyIndex - 1; i >= newDifficultyIndex; i--)
                difficultiesInBetween.add(DIFFICULTY_LIST_SORTED.get(i));
        }

        ArrayList<String> allCommands = new ArrayList<>();

        for (String difficultyName : difficultiesInBetween) {
            Difficulty difficulty = DIFFICULTY_LIST.get(difficultyName);
            List<String> commands = directionIsUp
                    ? difficulty.commandsOnSwitchFromPrev
                    : difficulty.commandsOnSwitchFromNext;

            allCommands.addAll(commands);
        }

        MAIN_MANAGER.getCommandManager().dispatchCommandsIfOnline(uuid, allCommands);
    }

    public Difficulty calculateDifficulty(Minecrafter affinity) {
        Difficulty first = calcDifficulty(affinity.affinity);
        Difficulty second = DIFFICULTY_LIST.get(getNextDifficulty(affinity.uuid).difficultyName);

        Difficulty difficulty = new Difficulty(first.difficultyName);

        int a = first.affinityRequirement;
        int b = second.affinityRequirement;
        double c = (a == b || !exactPercentage) ? 0.0 : Math.abs(1.0 - (1.0 / (a - b) * (affinity.affinity - b)));

        difficulty.doubleLootChance = calculatePercentage(first.doubleLootChance, second.doubleLootChance, c);
        difficulty.hungerDrainChance = calculatePercentage(first.hungerDrainChance, second.hungerDrainChance, c);
        difficulty.damageDoneByMobs = calculatePercentage(first.damageDoneByMobs, second.damageDoneByMobs, c);
        difficulty.damageDoneOnMobs = calculatePercentage(first.damageDoneOnMobs, second.damageDoneOnMobs, c);
        difficulty.damageDoneOnTamed = calculatePercentage(first.damageDoneOnTamed, second.damageDoneOnTamed, c);
        difficulty.armorDropChance = calculatePercentage(first.armorDropChance, second.armorDropChance, c);
        difficulty.chanceToHaveArmor = calculatePercentage(first.chanceToHaveArmor, second.chanceToHaveArmor, c);
        difficulty.chanceToHaveWeapon = calculatePercentage(first.chanceToHaveWeapon, second.chanceToHaveWeapon, c);
        difficulty.damageByRangedMobs = calculatePercentage(first.damageByRangedMobs, second.damageByRangedMobs, c);
        difficulty.damagePerArmorPoint = calculatePercentage(first.damagePerArmorPoint, second.damagePerArmorPoint, c);
        difficulty.experienceMultiplier = calculatePercentage(first.experienceMultiplier, second.experienceMultiplier, c);
        difficulty.doubleDurabilityDamageChance = calculatePercentage(first.doubleDurabilityDamageChance, second.doubleDurabilityDamageChance, c);
        difficulty.maxEnchants = calculatePercentage(first.maxEnchants, second.maxEnchants, c);
        difficulty.maxEnchantLevel = calculatePercentage(first.maxEnchantLevel, second.maxEnchantLevel, c);
        difficulty.chanceToEnchant = calculatePercentage(first.chanceToEnchant, second.chanceToEnchant, c);
        difficulty.weaponDropChance = calculatePercentage(first.weaponDropChance, second.weaponDropChance, c);
        difficulty.minimumStarvationHealth = calculatePercentage(first.minimumStarvationHealth, second.minimumStarvationHealth, c);
        difficulty.maximumHealth = calculatePercentage(first.maximumHealth, second.maximumHealth, c);
        difficulty.chanceCancelDeath = calculatePercentage(first.chanceCancelDeath, second.chanceCancelDeath, c);

        for(EquipmentItems item : EquipmentItems.values())
            difficulty.armorChance.put(item,
                    calculatePercentage(first.getArmorChance(item), second.getArmorChance(item), c));

        for(ArmorTypes item : ArmorTypes.values())
            difficulty.armorDamageMultipliers.put(item,
                    calculatePercentage(first.getArmorDamageMultipliers(item), second.getArmorDamageMultipliers(item), c));

        difficulty.mythicMobProfiles = first.mythicMobProfiles;
        difficulty.allowHealthRegen = first.allowHealthRegen;
        difficulty.prefix = first.prefix;
        difficulty.allowPVP = first.allowPVP;
        difficulty.keepInventory = first.keepInventory;
        difficulty.effectsWhenAttacked = first.effectsWhenAttacked;
        difficulty.disabledCommands = first.disabledCommands;
        difficulty.mobsIgnoredPlayers = first.mobsIgnoredPlayers;
        difficulty.commandsOnSwitchFromNext = first.commandsOnSwitchFromNext;
        difficulty.commandsOnSwitchFromPrev = first.commandsOnSwitchFromPrev;
        difficulty.commandsOnJoin = first.commandsOnJoin;

        if (calculateHealth && Bukkit.getOfflinePlayer(affinity.uuid).isOnline()) {
            Player player = Bukkit.getPlayer(affinity.uuid);
            if(player != null && player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(difficulty.maximumHealth);
            }
        }

        return difficulty;
    }

    private static int calculatePercentage(int value1, int value2, double percentage) {
        if(value1 == value2) return value1;
        return (int) Math.round(value1 - ((value1 - value2) * percentage));
    }

    private static double calculatePercentage(double value1, double value2, double percentage) {
        if(value1 == value2) return value1;
        return value1 - ((value1 - value2) * percentage);
    }

    public void reloadConfig() {
        DIFFICULTY_LIST.clear();
        DIFFICULTY_LIST_SORTED.clear();
        calculateHealth = false;

        String type = MAIN_MANAGER.getDataManager().getConfig().getString("toggle-settings.difficulty-type", "player").toLowerCase();
        exactPercentage = MAIN_MANAGER.getDataManager().getConfig().getBoolean("toggle-settings.advanced.exact-percentage", true);
        DifficultyType = DifficultyTypes.valueOf(type);

        HashMap<Integer, String> tmpMap = new HashMap<Integer, String>();
        for(String key : MAIN_MANAGER.getDataManager().getConfig().getConfigurationSection("difficulty").getKeys(false)) {
            ConfigurationSection config = MAIN_MANAGER.getDataManager().getConfig().getConfigurationSection("difficulty." + key);

            if(! config.getBoolean("enabled", true)) continue;
            Difficulty difficulty = new Difficulty(key.replace(" ", "_"));

            difficulty.affinityRequirement = config.getInt("affinity-required", 0);

            // Don't want to interrupt the current player base lol. So we have to deal with the / 100.0
            difficulty.damageDoneByMobs = config.getDouble("damage-done-by-mobs", 100.0) / 100.0;
            difficulty.damageDoneOnMobs = config.getDouble("damage-done-on-mobs", 100.0) / 100.0;
            difficulty.damageDoneOnTamed = config.getDouble("damage-done-on-tamed", 100.0) / 100.0;
            difficulty.hungerDrainChance = config.getDouble("hunger-drain-chance", 100.0) / 100.0;
            difficulty.damageByRangedMobs = config.getDouble("damage-done-by-ranged-mobs", 100.0) / 100.0;
            difficulty.experienceMultiplier = config.getDouble("experience-multiplier", 100.0) / 100.0;
            difficulty.doubleLootChance = config.getDouble("double-loot-chance", 100.0) / 100.0;
            difficulty.doubleDurabilityDamageChance = config.getDouble("double-durability-damage-chance", 100.0) / 100.0;
            difficulty.chanceCancelDeath = config.getDouble("chance-cancel-death", 0.0) / 100.0;
            difficulty.damagePerArmorPoint = config.getDouble("extra-damage-per-armor-point", 0.0) / 100.0;
            difficulty.chanceToHaveArmor = config.getDouble("enchanting.chance-to-have-armor", 15.0) / 100.0;
            difficulty.chanceToEnchant = config.getDouble("enchanting.chance-to-enchant-a-piece", 30.0) / 100.0;
            difficulty.armorDropChance = config.getDouble("enchanting.armor-drop-chance", 15.0) / 100.0;
            difficulty.weaponDropChance = config.getDouble("enchanting.weapon-drop-chance", 10.0) / 100.0;
            difficulty.chanceToHaveWeapon = config.getDouble("enchanting.weapon-chance", 5.0) / 100.0;

            difficulty.maximumHealth = config.getInt("maximum-health", 20);
            if(difficulty.maximumHealth != 20 && difficulty.maximumHealth > 0) {
                calculateHealth = true;
            }

            difficulty.minimumStarvationHealth = config.getInt("minimum-health-starvation", 0);
            difficulty.keepInventory = config.getBoolean("keep-inventory", false);
            difficulty.allowPVP = config.getBoolean("allow-pvp", true);
            difficulty.allowHealthRegen = config.getBoolean("allow-natural-regen", true);
            difficulty.effectsWhenAttacked = config.getBoolean("effects-when-attacked", true);

            difficulty.prefix = ChatColor.translateAlternateColorCodes('&', config.getString("prefix", key));

            if(config.isSet("commands-not-allowed-on-difficulty"))
                difficulty.disabledCommands = config.getStringList("commands-not-allowed-on-difficulty");

            if(config.isSet("extra-damage-for-certain-armor-types")) {
                difficulty.armorDamageMultipliers = new HashMap<ArmorTypes, Double>();
                for(String armorType : config.getConfigurationSection("extra-damage-for-certain-armor-types").getKeys(false))
                    try {
                        difficulty.armorDamageMultipliers
                                .put(ArmorTypes.valueOf(armorType),
                                        config.getDouble("extra-damage-for-certain-armor-types." + armorType, 1.0) / 100.0);
                    } catch (Exception ignored) { }
            }
            if(config.isSet("mobs-ignore-player")) difficulty.mobsIgnoredPlayers = config.getStringList("mobs-ignore-player");

            difficulty.maxEnchants = config.getInt("enchanting.max-enchants", 2);
            difficulty.maxEnchantLevel = config.getInt("enchanting.max-level", 1);
            for(EquipmentItems item : EquipmentItems.values())
                difficulty.armorChance.put(item, config.getDouble("enchanting." + item.name().toLowerCase() + "-chance", 1.0)
                        / 100.0);

            if (config.isSet("mythic-mobs")) {
                ConfigurationSection mythicMobConfig = config.getConfigurationSection("mythic-mobs");
                difficulty.mythicMobProfiles = new ArrayList<>();
                for (String mythicMobKey : mythicMobConfig.getKeys(false))
                    difficulty.mythicMobProfiles.add(new MythicMobProfile(mythicMobKey, mythicMobConfig));
            }

            if (config.isSet("execute")) {
                ConfigurationSection commands = config.getConfigurationSection("execute");
                difficulty.commandsOnJoin = commands.getStringList("on-join");
                difficulty.commandsOnSwitchFromPrev = commands.getStringList("on-switch-from-previous");
                difficulty.commandsOnSwitchFromNext = commands.getStringList("on-switch-from-next");
            }

            DIFFICULTY_LIST.put(difficulty.getDifficultyName(), difficulty);
            tmpMap.put(difficulty.getAffinity(), difficulty.getDifficultyName());
        }

        TreeMap<Integer, String> tm = new TreeMap<>(tmpMap);
        String lastKey = null;
        for (int key : tm.keySet()) {
            String thisKey = tmpMap.get(key).replace(" ", "_");
            DIFFICULTY_LIST_SORTED.add(thisKey);
            if(tmpMap.size() == DIFFICULTY_LIST_SORTED.size())
                DIFFICULTY_LIST.get(thisKey).difficultyUntil = Integer.MAX_VALUE;
            if(lastKey != null)
                DIFFICULTY_LIST.get(lastKey).difficultyUntil = key;
            lastKey = thisKey;
        }

        if(calculateTimer != null) { calculateTimer.cancel(); }

        int updateTime = MAIN_MANAGER.getDataManager().getConfig().getInt("toggle-settings.update-time-ticks", 1200);
        if(updateTime < 20) { updateTime = 1200; }

        calculateTimer = Bukkit.getScheduler().runTaskTimerAsynchronously(MAIN_MANAGER.getPlugin(), this::calculateAllPlayers, 20 * 5, updateTime);
    }
}