package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MobSpawnListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    public MobSpawnListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobSpawn(CreatureSpawnEvent e) {
        if(!config.getStringList("custom-mob-items-spawn-chance.includes-mobs").contains(e.getEntity().getType().toString())) return;

        List<CreatureSpawnEvent.SpawnReason> naturalReasons = new ArrayList<>(Arrays.asList(CreatureSpawnEvent.SpawnReason.DEFAULT, CreatureSpawnEvent.SpawnReason.NATURAL));
        List<CreatureSpawnEvent.SpawnReason> spawnReasons = new ArrayList<>(Arrays.asList(CreatureSpawnEvent.SpawnReason.SPAWNER_EGG, CreatureSpawnEvent.SpawnReason.SPAWNER, CreatureSpawnEvent.SpawnReason.DISPENSE_EGG));
        if(customArmorSpawnChance && naturalReasons.contains(e.getSpawnReason())) {
            Bukkit.getScheduler().runTaskAsynchronously(m, () -> Bukkit.getScheduler().runTask(m, () -> {
                Player closestPlayer = null;
                double distance = 1024.0;
                List<Player> onlinePlayers = Bukkit.getWorld(e.getEntity().getWorld().getUID()).getPlayers();
                for(Player pl : onlinePlayers)
                    if(e.getEntity().getLocation().distance(pl.getLocation()) < distance) {
                        distance = e.getEntity().getLocation().distance(pl.getLocation());
                        closestPlayer = pl;
                    }
                if(closestPlayer == null)
                    return;
                if(!playerList.containsKey(closestPlayer.getUniqueId()))
                    addPlayerData(closestPlayer.getUniqueId());

                String diff = calcDifficulty(closestPlayer.getUniqueId());
                double chanceToEnchant = (calcPercentage(closestPlayer.getUniqueId(), "chance-to-enchant-a-piece") / 100.0 / 100.0);
                EntityEquipment eq = e.getEntity().getEquipment();
                if(new Random().nextDouble() < (calcPercentage(closestPlayer.getUniqueId(), "weapon-chance") / 100.0 / 100.0)) {
                    if (ranged.contains(eq.getItemInMainHand().getType())) {
                        ItemStack item = new ItemStack(eq.getItemInMainHand().getType());
                        eq.setItemInMainHand(calcEnchant(item, "bow", diff, chanceToEnchant));
                        eq.setItemInMainHandDropChance(((float)calcPercentage(closestPlayer.getUniqueId(), "weapon-drop-chance") / 100 / 100));
                    } else {
                        int count = 0;
                        int rnd = new Random().nextInt(chancePerWeapon.get("total") + 1);
                        for (String customSpawnWeapon : customSpawnWeapons) {
                            count += chancePerWeapon.get(customSpawnWeapon);
                            if (rnd <= count) {
                                ItemStack item = new ItemStack(Material.getMaterial(customSpawnWeapon));
                                e.getEntity().setCanPickupItems(true);
                                eq.setItemInMainHand(calcEnchant(item, "weapon", diff, chanceToEnchant));
                                eq.setItemInMainHandDropChance(((float)calcPercentage(closestPlayer.getUniqueId(), "weapon-drop-chance") / 100 / 100));
                                break;
                            }
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
                            float armorDropChance = (float) calcPercentage(closestPlayer.getUniqueId(), "armor-drop-chance") / 100 / 100;
                            if (randomChance < (calcPercentage(closestPlayer.getUniqueId(), "helmet-chance") / 100.0 / 100.0)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem.toUpperCase() + "_HELMET"));
                                e.getEntity().setCanPickupItems(true);
                                eq.setHelmet(calcEnchant(item, "helmet", diff, chanceToEnchant));
                                eq.setHelmetDropChance(armorDropChance);
                            }
                            if (randomChance < (calcPercentage(closestPlayer.getUniqueId(), "chest-chance") / 100.0 / 100.0)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem.toUpperCase() + "_CHESTPLATE"));
                                e.getEntity().setCanPickupItems(true);
                                eq.setChestplate(calcEnchant(item, "chestplate", diff, chanceToEnchant));
                                eq.setChestplateDropChance(armorDropChance);
                            }
                            if (randomChance < (calcPercentage(closestPlayer.getUniqueId(), "leggings-chance") / 100.0 / 100.0)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem.toUpperCase() + "_LEGGINGS"));
                                e.getEntity().setCanPickupItems(true);
                                eq.setLeggings(calcEnchant(item, "leggings", diff, chanceToEnchant));
                                eq.setLeggingsDropChance(armorDropChance);
                            }
                            if (randomChance < (calcPercentage(closestPlayer.getUniqueId(), "boots-chance") / 100.0 / 100.0)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem.toUpperCase() + "_BOOTS"));
                                e.getEntity().setCanPickupItems(true);
                                eq.setBoots(calcEnchant(item, "boots", diff, chanceToEnchant));
                                eq.setBootsDropChance(armorDropChance);
                            }
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
        } else if(spawnReasons.contains(e.getSpawnReason())) {
            if(config.getBoolean("plugin-support.no-changes-to-spawned-mobs", false))
                ignoreMobs.add(e.getEntity().getEntityId());
        }
    }
}
