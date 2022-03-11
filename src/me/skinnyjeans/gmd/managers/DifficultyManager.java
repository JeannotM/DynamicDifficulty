package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Field;
import java.util.*;

import static me.skinnyjeans.gmd.models.DifficultyItemStates.*;

public class DifficultyManager {

    private final MainManager MAIN_MANAGER;

    private final HashMap<NamespacedKey, Difficulty> BIOME_LIST = new HashMap<>();
    private final HashMap<UUID, Difficulty> PLAYER_LIST = new HashMap<>();
    private final HashMap<String, Difficulty> DIFFICULTY_LIST = new HashMap<>();
    private final ArrayList<String> DIFFICULTY_LIST_SORTED = new ArrayList<>();
    private final EnumSet<DifficultyItemStates> CUSTOM_VALUES = EnumSet.of(
        DamageByMobs, DamageOnMobs, HungerDrain, DoubleLoot, MaxEnchants, MaxEnchantLevel, DamageByRangedMobs, ExperienceMultiplier,
        DoubleDurabilityDamageChance, ArmorDropChance, WeaponDropChance, ChanceToEnchant, ChanceToHaveArmor
    );

    private Difficulty world;
    private DifficultyTypes DifficultyType;

    public DifficultyManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;

        reloadConfig();
        Bukkit.getScheduler().runTaskTimerAsynchronously(MAIN_MANAGER.getPlugin(), this::calculateAllPlayers, 20 * 60, 20 * 60);
    }

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

        Field[] fields = Difficulty.class.getFields();
        for(Field field : fields)
            try {
                if(field.getType() == Integer.class) {
                    difficulty.getClass().getField(field.getName()).set(field, ((int) difficulty.getClass().getField(field.getName()).get(difficulty)) * c);
                } else if(field.getType() == Double.class) {
                    difficulty.getClass().getField(field.getName()).set(field, ((double) difficulty.getClass().getField(field.getName()).get(difficulty)) * c);
                } else {
                    difficulty.getClass().getField(field.getName()).set(field, difficulty.getClass().getField(field.getName()).get(difficulty));
                }
            } catch(Exception e) { e.printStackTrace(); }

        return difficulty;
    }

    public void reloadConfig() {
        String type = MAIN_MANAGER.getDataManager().getConfig().getString("difficulty-modifiers.type", "player");
        DifficultyType = DifficultyTypes.valueOf(type.substring(0, 1).toUpperCase() + type.substring(1));
    }
}