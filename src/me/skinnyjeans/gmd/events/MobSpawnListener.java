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
import org.bukkit.Location;
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

    private final HashSet<EntityType> AFFECTED_MOBS = new HashSet<>();
    private static final HashSet<CreatureSpawnEvent.SpawnReason> NATURAL_SPAWN_REASONS = new HashSet<>(Arrays.asList(CreatureSpawnEvent.SpawnReason.DEFAULT, CreatureSpawnEvent.SpawnReason.NATURAL));
    private static final HashSet<CreatureSpawnEvent.SpawnReason> UNNATURAL_SPAWN_REASONS = new HashSet<>(Arrays.asList(CreatureSpawnEvent.SpawnReason.SPAWNER_EGG, CreatureSpawnEvent.SpawnReason.SPAWNER, CreatureSpawnEvent.SpawnReason.DISPENSE_EGG));
    private static final HashSet<Material> RANGED = new HashSet<>(Arrays.asList(Material.CROSSBOW, Material.BOW));
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
    private boolean armorOnMobs;
    private int totalArmorTypeCount;

    public MobSpawnListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
        enableMythicMobs = Bukkit.getPluginManager().getPlugin("MythicMobs") != null;

        if (enableMythicMobs)
            Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] MythicMobs found, enabled the connection!");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMobSpawn(CreatureSpawnEvent e) {
        if(NATURAL_SPAWN_REASONS.contains(e.getSpawnReason())) {
            Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () -> Bukkit.getScheduler().runTask(MAIN_MANAGER.getPlugin(), () -> {
                Player closestPlayer = null;
                double distance = 256.0;
                List<Player> onlinePlayers = e.getEntity().getWorld().getPlayers();
                Location entityLocation = e.getEntity().getLocation();
                for(Player pl : onlinePlayers) {
                    double playerDistance = entityLocation.distance(pl.getLocation());
                    if(playerDistance < distance) {
                        distance = playerDistance;
                        closestPlayer = pl;
                    }
                }

                if(closestPlayer == null || !MAIN_MANAGER.getPlayerManager().isPlayerValid(closestPlayer)) return;
                Difficulty difficulty = MAIN_MANAGER.getDifficultyManager().getDifficulty(closestPlayer.getUniqueId());

                if (enableMythicMobs) {
                    for (MythicMobProfile mythicMobProfile : difficulty.mythicMobProfiles) {
                        if (!mythicMobProfile.replacedWith.equals(e.getEntityType())) continue;
                        if (random.nextDouble() <= mythicMobProfile.chanceToReplace) {
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

                if (!armorOnMobs || !AFFECTED_MOBS.contains(e.getEntity().getType())) return;

                EntityEquipment equipment = e.getEntity().getEquipment();
                int rnd;

                if (RANGED.contains(equipment.getItemInMainHand().getType())) {
                    if (random.nextDouble() <= difficulty.getArmorChance(EquipmentItems.BOW)) {
                        ItemStack item = new ItemStack(equipment.getItemInMainHand().getType());
                        equipment.setItemInMainHand(calcEnchant(item, difficulty, EquipmentItems.BOW));
                        equipment.setItemInMainHandDropChance((float) difficulty.weaponDropChance);
                    }
                } else {
                    equipment.setItemInMainHand(null);
                    if(random.nextDouble() <= difficulty.getArmorChance(EquipmentItems.WEAPON)) {
                        rnd = random.nextInt(CUSTOM_SPAWN_WEAPONS.values().stream().mapToInt(i -> i).sum() + 1);
                        int count = 0;
                        for (Material weapon : CUSTOM_SPAWN_WEAPONS.keySet()) {
                            if (rnd <= count) {
                                e.getEntity().setCanPickupItems(true);
                                equipment.setItemInMainHand(calcEnchant(new ItemStack(weapon), difficulty, EquipmentItems.WEAPON));
                                equipment.setItemInMainHandDropChance((float) difficulty.weaponDropChance);
                                break;
                            }
                            count += CUSTOM_SPAWN_WEAPONS.get(weapon);
                        }
                    }
                }

                equipment.setHelmet(null);
                equipment.setChestplate(null);
                equipment.setLeggings(null);
                equipment.setBoots(null);

                if(random.nextDouble() <= difficulty.chanceToHaveArmor) {
                    rnd = random.nextInt(totalArmorTypeCount);
                    int count = 0;
                    for (String thisItem : ARMOR_TYPES.keySet()) {
                        if (rnd <= count) {
                            float armorDropChance = (float) difficulty.armorDropChance;

                            if (random.nextDouble() <= difficulty.getArmorChance(EquipmentItems.HELMET)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem + "_HELMET"));
                                equipment.setHelmet(calcEnchant(item, difficulty, EquipmentItems.HELMET));
                                equipment.setHelmetDropChance(armorDropChance);
                            }
                            if (random.nextDouble() <= difficulty.getArmorChance(EquipmentItems.CHEST)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem + "_CHESTPLATE"));
                                equipment.setChestplate(calcEnchant(item, difficulty, EquipmentItems.CHEST));
                                equipment.setChestplateDropChance(armorDropChance);
                            }
                            if (random.nextDouble() <= difficulty.getArmorChance(EquipmentItems.LEGGINGS)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem + "_LEGGINGS"));
                                equipment.setLeggings(calcEnchant(item, difficulty, EquipmentItems.LEGGINGS));
                                equipment.setLeggingsDropChance(armorDropChance);
                            }
                            if (random.nextDouble() <= difficulty.getArmorChance(EquipmentItems.BOOTS)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem + "_BOOTS"));
                                equipment.setBoots(calcEnchant(item, difficulty, EquipmentItems.BOOTS));
                                equipment.setBootsDropChance(armorDropChance);
                            }
                            e.getEntity().setCanPickupItems(true);
                            break;
                        }
                        count += ARMOR_TYPES.get(thisItem);
                    }
                }
            }));
        } else if(changeSpawnedMobs && UNNATURAL_SPAWN_REASONS.contains(e.getSpawnReason())) {
            MAIN_MANAGER.getEntityManager().ignoreEntity(e.getEntity());
        }
    }

    public ItemStack calcEnchant(ItemStack item, Difficulty difficulty, EquipmentItems piece) {
        if(!customArmorSpawnChance || difficulty.maxEnchants <= 0 || random.nextDouble() > difficulty.chanceToEnchant) return item;

        int count = random.nextInt(difficulty.maxEnchants);
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

            if(chosenEnchant.getMaxLevel() == 1) {
                chosenLevel = chosenEnchant.getMaxLevel();
            } else if (overrideEnchantLimit) {
                chosenLevel = random.nextInt(difficulty.maxEnchantLevel) + 1;
            } else if (difficulty.maxEnchantLevel > chosenEnchant.getMaxLevel()) {
                chosenLevel = random.nextInt(chosenEnchant.getMaxLevel()) + 1;
            } else chosenLevel = random.nextInt(difficulty.maxEnchantLevel);

            item.addUnsafeEnchantment(chosenEnchant, chosenLevel);
            if(random.nextDouble() > difficulty.chanceToEnchant) break;
        }
        return item;
    }

    @Override
    public void reloadConfig() {
        CUSTOM_SPAWN_WEAPONS.clear();
        ENCHANTMENT_WEIGHT.clear();
        AFFECTED_MOBS.clear();
        ENCHANTMENTS.clear();
        ARMOR_TYPES.clear();
        FileConfiguration config = MAIN_MANAGER.getDataManager().getConfig();
        ConfigurationSection customMobs = MAIN_MANAGER.getDataManager().getConfig().getConfigurationSection("custom-mob-items-spawn-chance");

        overrideConflictingEnchants = customMobs.getBoolean("override-enchant-conflicts", false);
        overrideEnchantLimit = customMobs.getBoolean("override-default-limits", false);
        customArmorSpawnChance = config.getBoolean("toggle-settings.advanced.custom-enchants-on-mobs", true);
        changeSpawnedMobs = config.getBoolean("toggle-settings.loot-changes-to-spawned-mobs", false);
        armorOnMobs = config.getBoolean("toggle-settings.advanced.armor-on-mobs", true);

        if(customArmorSpawnChance) {
            for(String armorType : customMobs.getConfigurationSection("armor-set-weight").getKeys(false))
                ARMOR_TYPES.put(armorType.toUpperCase(), customMobs.getInt("armor-set-weight." + armorType));

            totalArmorTypeCount = ARMOR_TYPES.values().stream().mapToInt(i -> i).sum() + 1;

            for(String entity : customMobs.getStringList("includes-mobs"))
                if(EntityType.valueOf(entity) != null)
                    AFFECTED_MOBS.add(EntityType.valueOf(entity));

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

            for(Object key : customMobs.getList("weapons-include").toArray())
                try {
                    String[] sep = key.toString().replaceAll("[{|}]","").split("=");
                    CUSTOM_SPAWN_WEAPONS.put(Material.valueOf(sep[0]), (sep.length > 1 ? Integer.parseInt(sep[1]) : 1));
                } catch (Exception ignored) { }
        }
    }
}
