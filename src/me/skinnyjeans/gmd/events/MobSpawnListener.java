package me.skinnyjeans.gmd.events;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import me.skinnyjeans.gmd.models.EquipmentItems;
import me.skinnyjeans.gmd.models.MythicMobProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MobSpawnListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    private final HashSet<EntityType> AFFECTED_MOBS = new HashSet<>();
    private final HashSet<CreatureSpawnEvent.SpawnReason> NATURAL_SPAWN_REASONS = new HashSet<>(Arrays.asList(CreatureSpawnEvent.SpawnReason.DEFAULT, CreatureSpawnEvent.SpawnReason.NATURAL));
    private final HashSet<CreatureSpawnEvent.SpawnReason> UNNATURAL_SPAWN_REASONS = new HashSet<>(Arrays.asList(CreatureSpawnEvent.SpawnReason.SPAWNER_EGG, CreatureSpawnEvent.SpawnReason.SPAWNER, CreatureSpawnEvent.SpawnReason.DISPENSE_EGG));
    private final HashSet<Material> RANGED = new HashSet<>(Arrays.asList(Material.CROSSBOW, Material.BOW));
    private final HashMap<Material, Integer> CUSTOM_SPAWN_WEAPONS = new HashMap<>();
    private final HashMap<String, Integer> ARMOR_TYPES = new HashMap<>();
    private final HashMap<EquipmentItems, HashSet<NamespacedKey>> ENCHANTMENTS = new HashMap<>();
    private final HashMap<NamespacedKey, Integer> ENCHANTMENT_WEIGHT = new HashMap<>();
    private final Random random = new Random();

    private boolean overrideConflictingEnchants;
    private final boolean enableMythicMobs;
    private boolean customArmorSpawnChance;
    private boolean overrideEnchantLimit;
    private boolean changeSpawnedMobs;

    public MobSpawnListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
        enableMythicMobs = Bukkit.getPluginManager().getPlugin("MythicMobs") != null;

        if (enableMythicMobs)
            Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] MythicMobs found, enabled the connection!");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobSpawn(CreatureSpawnEvent e) {
        if(NATURAL_SPAWN_REASONS.contains(e.getSpawnReason())) {
            Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () -> Bukkit.getScheduler().runTask(MAIN_MANAGER.getPlugin(), () -> {
                Player closestPlayer = null;
                double distance = 512.0;
                List<Player> onlinePlayers = Bukkit.getWorld(e.getEntity().getWorld().getUID()).getPlayers();
                for(Player pl : onlinePlayers)
                    if(e.getEntity().getLocation().distance(pl.getLocation()) < distance) {
                        distance = e.getEntity().getLocation().distance(pl.getLocation());
                        closestPlayer = pl;
                    }
                if(closestPlayer == null || !MAIN_MANAGER.getPlayerManager().isPlayerValid(closestPlayer)) return;
                Difficulty difficulty = MAIN_MANAGER.getDifficultyManager().getDifficulty(closestPlayer.getUniqueId());

                if (enableMythicMobs) {
                    for (MythicMobProfile mythicMobProfile : difficulty.getMythicMobProfiles()) {
                        if (!mythicMobProfile.replacedWith.equals(e.getEntity().getType())) continue;
                        if (random.nextDouble() < mythicMobProfile.chanceToReplace / 100.0) {
                            MythicMob mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(mythicMobProfile.mythicMobName)
                                    .orElse(null);

                            if (mythicMob != null) {
                                mythicMob.spawn(BukkitAdapter.adapt(e.getEntity().getLocation()), 1);
                                e.setCancelled(true);
                                return;
                            }
                        }
                        break;
                    }
                }

                if (!customArmorSpawnChance || !AFFECTED_MOBS.contains(e.getEntity().getType())) {
                    return;
                }


                double chanceToEnchant = difficulty.getChanceToEnchant() / 100.0;
                EntityEquipment eq = e.getEntity().getEquipment();
                int rnd = random.nextInt(CUSTOM_SPAWN_WEAPONS.values().stream().mapToInt(i -> i).sum() + 1);
                if(random.nextDouble() < difficulty.getChanceToHaveWeapon() / 100.0)
                    if (RANGED.contains(eq.getItemInMainHand().getType())) {
                        ItemStack item = new ItemStack(eq.getItemInMainHand().getType());
                        eq.setItemInMainHand(calcEnchant(item, difficulty, EquipmentItems.BOW, chanceToEnchant));
                        eq.setItemInMainHandDropChance((float) (difficulty.getWeaponDropChance() / 100.0));
                    } else {
                        int count = 0;
                        for (Material weapon : CUSTOM_SPAWN_WEAPONS.keySet()) {
                            count += CUSTOM_SPAWN_WEAPONS.get(weapon);
                            if (rnd <= count) {
                                e.getEntity().setCanPickupItems(true);
                                eq.setItemInMainHand(calcEnchant(new ItemStack(weapon), difficulty, EquipmentItems.WEAPON, chanceToEnchant));
                                eq.setItemInMainHandDropChance((float) (difficulty.getWeaponDropChance() / 100.0));
                                break;
                            }
                        }
                    }

                if(random.nextDouble() < difficulty.getChanceToHaveArmor() / 100.0) {
                    int count = 0;
                    for (String thisItem : ARMOR_TYPES.keySet()) {
                        if (count >= rnd) {
                            double randomChance = random.nextDouble();
                            float armorDropChance = (float) ((float) difficulty.getArmorDropChance() / 100.0);
                            if (randomChance < difficulty.getEnchantChance(EquipmentItems.HELMET) / 100.0) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem + "_HELMET"));
                                eq.setHelmet(calcEnchant(item, difficulty, EquipmentItems.HELMET, chanceToEnchant));
                                eq.setHelmetDropChance(armorDropChance);
                            }
                            if (randomChance < difficulty.getEnchantChance(EquipmentItems.CHEST) / 100.0) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem + "_CHESTPLATE"));
                                eq.setChestplate(calcEnchant(item, difficulty, EquipmentItems.CHEST, chanceToEnchant));
                                eq.setChestplateDropChance(armorDropChance);
                            }
                            if (randomChance < difficulty.getEnchantChance(EquipmentItems.LEGGINGS) / 100.0) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem + "_LEGGINGS"));
                                eq.setLeggings(calcEnchant(item, difficulty, EquipmentItems.LEGGINGS, chanceToEnchant));
                                eq.setLeggingsDropChance(armorDropChance);
                            }
                            if (randomChance < difficulty.getEnchantChance(EquipmentItems.BOOTS) / 100.0) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem + "_BOOTS"));
                                eq.setBoots(calcEnchant(item, difficulty, EquipmentItems.BOOTS, chanceToEnchant));
                                eq.setBootsDropChance(armorDropChance);
                            }
                            e.getEntity().setCanPickupItems(true);
                            break;
                        }
                        count += ARMOR_TYPES.get(thisItem);
                    }
                } else {
                    eq.setHelmet(null);
                    eq.setChestplate(null);
                    eq.setLeggings(null);
                    eq.setBoots(null);
                }
            }));
        } else if(changeSpawnedMobs && UNNATURAL_SPAWN_REASONS.contains(e.getSpawnReason())) {
            MAIN_MANAGER.getEntityManager().ignoreEntity(e.getEntity());
        }
    }

    public ItemStack calcEnchant(ItemStack item, Difficulty difficulty, EquipmentItems piece, Double chanceToEnchant) {
        int maxEnchants = difficulty.getMaxEnchants();
        if(maxEnchants <= 0) return item;

        int count = random.nextInt(maxEnchants);
        for(int j = 0; j < count; j++) {
            Enchantment chosenEnchant = null;
            int currentAmount = 0;
            int chosenAmount = random.nextInt(ENCHANTMENT_WEIGHT.values().stream().mapToInt(i -> i).sum()) + 1;
            for(NamespacedKey key : ENCHANTMENTS.get(piece)) {
                if(chosenEnchant == null)
                    chosenEnchant = Enchantment.getByKey(key);

                if(currentAmount >= chosenAmount) {
                    chosenEnchant = Enchantment.getByKey(key);
                    break;
                }
                currentAmount += ENCHANTMENT_WEIGHT.get(key);
            }

            if(!overrideConflictingEnchants) {
                boolean disallow = false;
                for(Enchantment enchant : item.getEnchantments().keySet())
                    if(enchant.conflictsWith(chosenEnchant)) {
                        disallow = true;
                        break;
                    }
                if (disallow) continue;
            }

            int chosenLevel;
            int maxlvl = difficulty.getMaxEnchantLevel();

            if(chosenEnchant.getMaxLevel() == 1) {
                chosenLevel = chosenEnchant.getMaxLevel();
            } else if (overrideEnchantLimit) {
                chosenLevel = random.nextInt(maxlvl) + 1;
            } else if (maxlvl > chosenEnchant.getMaxLevel()) {
                chosenLevel = random.nextInt(chosenEnchant.getMaxLevel()) + 1;
            } else chosenLevel = random.nextInt(maxlvl);

            item.addUnsafeEnchantment(chosenEnchant, chosenLevel);
            if(random.nextDouble() > chanceToEnchant) break;
        }
        return item;
    }

    @Override
    public void reloadConfig() {
        AFFECTED_MOBS.clear();
        ARMOR_TYPES.clear();
        FileConfiguration config = MAIN_MANAGER.getDataManager().getConfig();
        ConfigurationSection customMobs = MAIN_MANAGER.getDataManager().getConfig().getConfigurationSection("custom-mob-items-spawn-chance");

        overrideConflictingEnchants = customMobs.getBoolean("override-enchant-conflicts", false);
        overrideEnchantLimit = customMobs.getBoolean("override-default-limits", false);
        customArmorSpawnChance = config.getBoolean("toggle-settings.advanced.custom-enchants-on-mobs", false);
        changeSpawnedMobs = config.getBoolean("toggle-settings.loot-changes-to-spawned-mobs", false);

        if(customArmorSpawnChance) {
            for(String armorType : customMobs.getConfigurationSection("armor-set-weight").getKeys(false))
                ARMOR_TYPES.put(armorType.toUpperCase(), customMobs.getInt("armor-set-weight." + armorType));

            for(String entity : customMobs.getStringList("includes-mobs"))
                if(EntityType.valueOf(entity) != null) AFFECTED_MOBS.add(EntityType.valueOf(entity));

            HashMap<EquipmentItems, String> itemSlot = new HashMap<EquipmentItems, String>() {{
                put(EquipmentItems.HELMET, "helmet-enchants-include");
                put(EquipmentItems.CHEST, "chestplate-enchants-include");
                put(EquipmentItems.LEGGINGS, "leggings-enchants-include");
                put(EquipmentItems.BOOTS, "boots-enchants-include");
                put(EquipmentItems.WEAPON, "weapon-enchants-include");
                put(EquipmentItems.BOW, "bow-enchants-include");
            }};

            for(EquipmentItems equipmentItem : itemSlot.keySet()) {
                HashSet<NamespacedKey> enchants = new HashSet<>();
                for(Object key : customMobs.getList(itemSlot.get(equipmentItem)).toArray())
                    try {
                        String[] sep = key.toString().replaceAll("[{|}]","").split("=");
                        NamespacedKey name = NamespacedKey.minecraft(sep[0]);
                        ENCHANTMENT_WEIGHT.put(name, (sep.length > 1 ? Integer.parseInt(sep[1]) : 1));
                        enchants.add(name);
                    } catch (Exception ignored) { }
                ENCHANTMENTS.put(equipmentItem, enchants);
            }

            for(Object key : customMobs.getStringList("weapons-include").toArray())
                try {
                    String[] sep = key.toString().replaceAll("[{|}]","").split("=");
                    CUSTOM_SPAWN_WEAPONS.put(Material.valueOf(sep[0]), (sep.length > 1 ? Integer.parseInt(sep[1]) : 1));
                } catch (Exception ignored) { }
        }
    }
}
