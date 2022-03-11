package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

    private boolean overrideConflictingEnchants;
    private boolean customArmorSpawnChance;
    private boolean changeSpawnedMobs;

    public MobSpawnListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobSpawn(CreatureSpawnEvent e) {
        if(!AFFECTED_MOBS.contains(e.getEntity().getType())) return;

        if(customArmorSpawnChance && NATURAL_SPAWN_REASONS.contains(e.getSpawnReason())) {
            Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () -> Bukkit.getScheduler().runTask(MAIN_MANAGER.getPlugin(), () -> {
                Player closestPlayer = null;
                double distance = 1024.0;
                List<Player> onlinePlayers = Bukkit.getWorld(e.getEntity().getWorld().getUID()).getPlayers();
                for(Player pl : onlinePlayers)
                    if(e.getEntity().getLocation().distance(pl.getLocation()) < distance) {
                        distance = e.getEntity().getLocation().distance(pl.getLocation());
                        closestPlayer = pl;
                    }
                if(closestPlayer == null) return;
                MAIN_MANAGER.getPlayerManager().isPlayerValid(closestPlayer);

                Difficulty difficulty = MAIN_MANAGER.getDifficultyManager().getDifficulty(closestPlayer.getUniqueId());
                double chanceToEnchant = difficulty.getChanceToEnchant() / 100.0;
                EntityEquipment eq = e.getEntity().getEquipment();
                if(new Random().nextDouble() < difficulty.getWeaponDropChance() / 100.0)
                    if (RANGED.contains(eq.getItemInMainHand().getType())) {
                        ItemStack item = new ItemStack(eq.getItemInMainHand().getType());
                        eq.setItemInMainHand(calcEnchant(item, "bow", diff, chanceToEnchant));
                        eq.setItemInMainHandDropChance((float) (difficulty.getWeaponDropChance() / 100.0));
                    } else {
                        int count = 0;
                        int rnd = new Random().nextInt(chancePerWeapon.get("total") + 1);
                        for (String customSpawnWeapon : customSpawnWeapons) {
                            count += chancePerWeapon.get(customSpawnWeapon);
                            if (rnd <= count) {
                                ItemStack item = new ItemStack(Material.getMaterial(customSpawnWeapon));
                                e.getEntity().setCanPickupItems(true);
                                eq.setItemInMainHand(calcEnchant(item, "weapon", diff, chanceToEnchant));
                                eq.setItemInMainHandDropChance((float) (difficulty.getWeaponDropChance() / 100.0));
                                break;
                            }
                        }
                    }

                if(new Random().nextDouble() < calcPercentage(closestPlayer.getUniqueId(), "chance-to-have-armor") / 100.0 / 100.0) {
                    ArrayList<String> array = new ArrayList<>(Arrays.asList("leather", "golden", "chainmail", "iron", "diamond", "netherite"));
                    int rnd = new Random().nextInt(chancePerArmor.get("total") + 1);
                    int count = 0;
                    for (String thisItem : array) {
                        if (count >= rnd) {
                            double randomChance = new Random().nextDouble();
                            float armorDropChance = (float) calcPercentage(closestPlayer.getUniqueId(), "armor-drop-chance") / 100 / 100.0;
                            if (randomChance < (calcPercentage(closestPlayer.getUniqueId(), "helmet-chance") / 100.0 / 100.0)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem.toUpperCase() + "_HELMET"));
                                eq.setHelmet(calcEnchant(item, "helmet", diff, chanceToEnchant));
                                eq.setHelmetDropChance(armorDropChance);
                            }
                            if (randomChance < (calcPercentage(closestPlayer.getUniqueId(), "chest-chance") / 100.0 / 100.0)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem.toUpperCase() + "_CHESTPLATE"));
                                eq.setChestplate(calcEnchant(item, "chestplate", diff, chanceToEnchant));
                                eq.setChestplateDropChance(armorDropChance);
                            }
                            if (randomChance < (calcPercentage(closestPlayer.getUniqueId(), "leggings-chance") / 100.0 / 100.0)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem.toUpperCase() + "_LEGGINGS"));
                                eq.setLeggings(calcEnchant(item, "leggings", diff, chanceToEnchant));
                                eq.setLeggingsDropChance(armorDropChance);
                            }
                            if (randomChance < (calcPercentage(closestPlayer.getUniqueId(), "boots-chance") / 100.0 / 100.0)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem.toUpperCase() + "_BOOTS"));
                                eq.setBoots(calcEnchant(item, "boots", diff, chanceToEnchant));
                                eq.setBootsDropChance(armorDropChance);
                            }
                            e.getEntity().setCanPickupItems(true);
                            break;
                        }
                        count += chancePerArmor.get(thisItem);
                    }
                } else {
                    eq.setHelmet(null);
                    eq.setChestplate(null);
                    eq.setLeggings(null);
                    eq.setBoots(null);
                }
            }));
        } else if(UNNATURAL_SPAWN_REASONS.contains(e.getSpawnReason())) {
            if(changeSpawnedMobs)
                ignoreMobs.add(e.getEntity().getEntityId());
        }
    }

    public ItemStack calcEnchant(ItemStack item, Difficulty difficulty, Double chanceToEnchant) {
        for(int j=0;j<new Random().nextInt(difficulty.getMaxEnchants());j++) {
            Enchantment chosenEnchant = null;
            int currentAmount = 0;
            int chosenAmount = 0;
            List<String> enchants = enchantmentList.get(piece);
            for(String s : enchants) {
                String[] spl = s.split(":");
                if(s.startsWith("total")) {
                    chosenAmount = new Random().nextInt(Integer.parseInt(spl[1])) + 1;
                    continue;
                }

                if(chosenEnchant == null)
                    chosenEnchant = Enchantment.getByKey(NamespacedKey.minecraft(spl[0]));

                if(currentAmount >= chosenAmount) {
                    chosenEnchant = Enchantment.getByKey(NamespacedKey.minecraft(spl[0]));
                    break;
                }
                currentAmount += Integer.parseInt(spl[1]);
            }

            if(!config.getBoolean("custom-mob-items-spawn-chance.override-enchant-conflicts", false)) {
                boolean allowed = true;
                for(List<Enchantment> enchantList : enchantmentConflict)
                    if(allowed) {
                        if(enchantList.contains(chosenEnchant))
                            for(Enchantment currentEnchant : enchantList)
                                if(item.containsEnchantment(currentEnchant)) {
                                    allowed = false;
                                    break;
                                }
                    } else {
                        break;
                    }
                if (!allowed)
                    continue;
            }

            int chosenLevel;
            int maxlvl = Math.round(difficultyList.get(diff).getMaxEnchantLevel());
            if(chosenEnchant.getMaxLevel() == 1) {
                chosenLevel = chosenEnchant.getMaxLevel();
            } else if (config.getBoolean("custom-mob-items-spawn-chance.override-default-limits", false)) {
                chosenLevel = new Random().nextInt(Math.round(difficultyList.get(diff).getMaxEnchantLevel())) + 1;
            } else if (maxlvl > chosenEnchant.getMaxLevel()) {
                chosenLevel = new Random().nextInt(chosenEnchant.getMaxLevel()) + 1;
            } else {
                chosenLevel = new Random().nextInt(maxlvl);
            }
            item.addUnsafeEnchantment(chosenEnchant, chosenLevel);
            if(new Random().nextDouble() > chanceToEnchant)
                break;
        }
        return item;
    }

    @Override
    public void reloadConfig() {
        AFFECTED_MOBS.clear();

        overrideConflictingEnchants = MAIN_MANAGER.getDataManager().getConfig().getBoolean("custom-mob-items-spawn-chance.override-enchant-conflicts", false)
        customArmorSpawnChance = MAIN_MANAGER.getDataManager().getConfig().getBoolean("advanced-features.custom-mob-items-spawn-chance", false);
        changeSpawnedMobs = MAIN_MANAGER.getDataManager().getConfig().getBoolean("plugin-support.no-changes-to-spawned-mobs", false);

        for(String entity : MAIN_MANAGER.getDataManager().getConfig().getStringList("custom-mob-items-spawn-chance.includes-mobs"))
            AFFECTED_MOBS.add(EntityType.valueOf(entity));
    }
}
