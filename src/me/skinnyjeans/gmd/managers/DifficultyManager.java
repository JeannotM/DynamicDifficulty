package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.util.*;

public class DifficultyManager {

    private final MainManager MAIN_MANAGER;

    private final HashMap<NamespacedKey, Difficulty> BIOME_LIST = new HashMap<>();
    private final HashMap<UUID, Difficulty> PLAYER_LIST = new HashMap<>();
    private final HashMap<String, Difficulty> DIFFICULTY_LIST = new HashMap<>();
    private final ArrayList<String> DIFFICULTY_LIST_SORTED = new ArrayList<>();

    private Difficulty world;
    private DifficultyTypes DifficultyType;

    public DifficultyManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;

        reloadConfig();
        Bukkit.getScheduler().runTaskTimerAsynchronously(MAIN_MANAGER.getPlugin(), this::calculateAllPlayers, 20 * 60, 20 * 60);
    }

    public ArrayList<Difficulty> getDifficulties() { return new ArrayList<>(DIFFICULTY_LIST.values()); }

    public Difficulty getDifficulty(UUID uuid) {
        switch(DifficultyType) {
            case World:
                return PLAYER_LIST.get(uuid);
            case Biome:
                return BIOME_LIST.get(uuid);
            default:
                return PLAYER_LIST.get(uuid);
        }
    }

    public DifficultyTypes getType() { return DifficultyType; }

    public Difficulty calcDifficulty(int affinity) {
        for (Difficulty difficulty : DIFFICULTY_LIST.values())
            if(difficulty.getAffinity() >= affinity && difficulty.getUntil() <= affinity)
                return difficulty;
        return DIFFICULTY_LIST.get(DIFFICULTY_LIST_SORTED.get(0));

//        if(randomizer) return difficulties.get(new Random().nextInt(difficulties.size() - 1));
//        if(biomeList.containsKey(name)) {
//            int af = biomeList.get(name).getAffinity();
//            int size = difficulties.size();
//            for (int i = 0; i < size; i++)
//                if(af <= difficultyList.get(difficulties.get(i)).getUntil())
//                    return difficulties.get(i);
//        }
//        return difficulties.get(0);
    }

    public void calculateAllPlayers() {
        MAIN_MANAGER.getPlayerManager().getPlayerList().forEach((key, value) -> calculateDifficulty(key));
        world = calculateDifficulty(MAIN_MANAGER.getPlayerManager().getPlayerAffinity(null));
    }

    public void calculateDifficulty(UUID uuid) {
        Difficulty difficulty = calculateDifficulty(MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid));
        PLAYER_LIST.put(uuid, difficulty);
    }

    public Difficulty calculateDifficulty(Minecrafter affinity) {
        Difficulty first = calcDifficulty(affinity.getAffinity());
        int index = DIFFICULTY_LIST_SORTED.indexOf(first.getDifficultyName());
        if(index != DIFFICULTY_LIST_SORTED.size() - 1) index++;
        Difficulty second = DIFFICULTY_LIST.get(DIFFICULTY_LIST_SORTED.get(index));

        Difficulty difficulty = new Difficulty("");

        int a = first.getAffinity();
        int b = second.getAffinity();
        double c = (100.0 / (a - b) * (affinity.getAffinity() - b)) / 100.0 + 1.0;

        difficulty.setDamageByMobs(multiplyInt(first.getDamageByMobs(), c));
        difficulty.setDamageOnMobs(multiplyInt(first.getDamageOnMobs(), c));
        difficulty.setExperienceMultiplier(multiplyInt(first.getExperienceMultiplier(), c));
        difficulty.setHungerDrain(multiplyInt(first.getHungerDrain(), c));
        difficulty.setDoubleLoot(multiplyInt(first.getDoubleLoot(), c));
        difficulty.setMaxEnchants(multiplyInt(first.getMaxEnchants(), c));
        difficulty.setMaxEnchantLevel(multiplyInt(first.getMaxEnchantLevel(), c));
        difficulty.setDamageByRangedMobs(multiplyInt(first.getDamageByRangedMobs(), c));
        difficulty.setDoubleDurabilityDamageChance(multiplyInt(first.getDoubleDurabilityDamageChance(), c));
        difficulty.setArmorDamageMultiplier(first.getArmorDamageMultiplier());
        difficulty.setAllowPVP(first.getAllowPVP());
        difficulty.setKeepInventory(first.getKeepInventory());
        difficulty.setEffectsOnAttack(first.getEffectsOnAttack());
        difficulty.setDisabledCommands(first.getDisabledCommands());
        difficulty.setIgnoredMobs(first.getIgnoredMobs());
        difficulty.setEnchantChances(first.getEnchantChance());
        difficulty.setArmorDropChance(first.getArmorDropChance() * c);
        difficulty.setWeaponDropChance(first.getWeaponDropChance() * c);
        difficulty.setChanceToEnchant(first.getChanceToEnchant() * c);
        difficulty.setChanceToHaveArmor(first.getChanceToHaveArmor() * c);

        return difficulty;
    }

    private int multiplyInt(int value, double times) { return (int) Math.round(value * times); }

    public void reloadConfig() {
        String type = MAIN_MANAGER.getDataManager().getConfig().getString("difficulty-modifiers.type", "player");
        DifficultyType = DifficultyTypes.valueOf(type.substring(0, 1).toUpperCase() + type.substring(1));
    }
}