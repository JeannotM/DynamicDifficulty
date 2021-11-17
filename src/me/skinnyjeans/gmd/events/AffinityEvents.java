package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.models.Difficulty;
import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AffinityEvents extends Affinity implements Listener {

    public AffinityEvents(Main ma) {
        super(ma);
        Bukkit.getScheduler().runTaskAsynchronously(ma, () -> checkEvents());
    }

    protected void checkEvents() {
        int death = onDeath;
        int ignoredMobs = 0;
        int durabilty = 0;
        int effects = 0;
        int food = 0;

        for(String d : difficulties) {
            Difficulty diff = difficultyList.get(d);

            if(diff.getKeepInventory())
                death++;
            if(diff.getDoubleDurabilityDamageChance() != 0)
                durabilty++;
            if(!diff.getEffectsOnAttack())
                effects++;
            if(diff.getHungerDrain() != 100)
                food++;
            if(diff.getIgnoredMobs() != null && !diff.getIgnoredMobs().isEmpty())
                ignoredMobs++;
        }

        if(blocks.size() == 0)
            BlockBreakEvent.getHandlerList().unregister(m);
        if(death == 0)
            PlayerDeathEvent.getHandlerList().unregister(m);
        if(!customArmorSpawnChance && !config.getBoolean("plugin-support.no-changes-to-spawned-mobs"))
            SpawnerSpawnEvent.getHandlerList().unregister(m);
        if(durabilty == 0)
            PlayerItemDamageEvent.getHandlerList().unregister(m);
        if(effects == 0)
            EntityPotionEffectEvent.getHandlerList().unregister(m);
        if(food == 0)
            FoodLevelChangeEvent.getHandlerList().unregister(m);
        if(ignoredMobs == 0) {
            EntityTargetLivingEntityEvent.getHandlerList().unregister(m);
        } else {
            emptyHitMobsList();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        if(!disabledWorlds.contains(e.getEntity().getWorld().getName())) {
            if(!playerList.containsKey(e.getEntity().getUniqueId()))
                addPlayerData(e.getEntity().getUniqueId());

            if(onDeath != 0)
                addAmountOfAffinity(e.getEntity().getUniqueId(), onDeath);
            if(difficultyList.get(calcDifficulty(e.getEntity().getUniqueId())).getKeepInventory()) {
                e.setKeepInventory(true);
                e.setKeepLevel(true);
                e.getDrops().clear();
            }
            if(calcMaxAffinity)
                addAmountOfMaxAffinity(e.getEntity().getUniqueId(), config.getInt("calculating-affinity.max-affinity-changes.death"));
            if(calcMinAffinity)
                addAmountOfMinAffinity(e.getEntity().getUniqueId(), config.getInt("calculating-affinity.min-affinity-changes.death"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDamage(PlayerItemDamageEvent e) {
        if(!disabledWorlds.contains(e.getPlayer().getWorld().getName())) {
            UUID uuid = e.getPlayer().getUniqueId();
            if(!playerList.containsKey(uuid))
                addPlayerData(uuid);

            if(new Random().nextDouble() < calcPercentage(uuid, "double-durability-damage") / 100)
                e.setDamage(e.getDamage() * 2);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKill(EntityDeathEvent e) {
        if(!disabledWorlds.contains(e.getEntity().getWorld().getName())) {
            if(e.getEntity().getKiller() != null && e.getEntity().getKiller() instanceof Player) {
                UUID uuid = e.getEntity().getKiller().getUniqueId();
                if(!playerList.containsKey(uuid))
                    addPlayerData(uuid);

                Bukkit.getScheduler().runTaskAsynchronously(m, () -> Bukkit.getScheduler().runTask(m, () -> {
                    if (e.getEntity() instanceof Player) {
                        addAmountOfAffinity(uuid, onPVPKill);
                        if(calcMaxAffinity)
                            addAmountOfMaxAffinity(uuid, config.getInt("calculating-affinity.max-affinity-changes.pvp-kill"));
                        if(calcMinAffinity)
                            addAmountOfMinAffinity(uuid, config.getInt("calculating-affinity.min-affinity-changes.pvp-kill"));
                    } else if (mobsPVE.get(e.getEntityType().toString()) != null) {
                        addAmountOfAffinity(uuid, mobsPVE.get(e.getEntityType().toString()));
                        if(calcMaxAffinity)
                            addAmountOfMaxAffinity(uuid, config.getInt("calculating-affinity.max-affinity-changes.pve-kill"));
                        if(calcMinAffinity)
                            addAmountOfMinAffinity(uuid, config.getInt("calculating-affinity.min-affinity-changes.pve-kill"));
                        if(ignoreMobs.contains(e.getEntity().getEntityId()))
                            ignoreMobs.remove(ignoreMobs.indexOf(e.getEntity().getEntityId()));
                    }
                }));

                if (!(e.getEntity() instanceof Player) && !disabledMobs.contains(e.getEntityType().toString())) {
                    e.setDroppedExp((int) (e.getDroppedExp() * calcPercentage(uuid, "experience-multiplier") / 100.0));
                    double DoubleLoot = calcPercentage(uuid, "double-loot-chance");
                    if (DoubleLoot != 0.0 && new Random().nextDouble() < DoubleLoot / 100.0 && !e.getEntity().getCanPickupItems())
                        for (int i = 0; i < e.getDrops().size(); i++)
                            Bukkit.getWorld(e.getEntity().getWorld().getUID()).dropItemNaturally(e.getEntity().getLocation(), e.getDrops().get(i));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMined(BlockBreakEvent e) {
        if(!playerList.containsKey(e.getPlayer().getUniqueId()))
            addPlayerData(e.getPlayer().getUniqueId());

        Bukkit.getScheduler().runTaskAsynchronously(m, () -> Bukkit.getScheduler().runTask(m, () -> {
            if(!disabledWorlds.contains(e.getPlayer().getWorld().getName()))
                if (blocks.get(e.getBlock().getBlockData().getMaterial().name()) != null)
                    if(!e.getPlayer().getItemOnCursor().containsEnchantment(Enchantment.SILK_TOUCH) || config.getBoolean("silk-touch-allowed"))
                        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                            addAmountOfAffinity(e.getPlayer().getUniqueId(), blocks.get(e.getBlock().getBlockData().getMaterial().name()));
                            if(calcMaxAffinity)
                                addAmountOfMaxAffinity(e.getPlayer().getUniqueId(), config.getInt("calculating-affinity.max-affinity-changes.block-mined"));
                            if(calcMinAffinity)
                                addAmountOfMinAffinity(e.getPlayer().getUniqueId(), config.getInt("calculating-affinity.min-affinity-changes.block-mined"));
                        }
        }));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent e) {
        if(!disabledWorlds.contains(e.getEntity().getWorld().getName())) {
            Entity prey = e.getEntity();
            Entity hunter = e.getDamager();
            if (prey instanceof Player) {
                if(!playerList.containsKey(prey.getUniqueId()))
                    addPlayerData(prey.getUniqueId());
                if (!(hunter instanceof Player) && !disabledMobs.contains(hunter.getType().toString()) && !ignoreMobs.contains(hunter.getEntityId())) {
                    if (((HumanEntity)prey).isBlocking()) { return; }

                    UUID uuid = prey.getUniqueId();
                    addAmountOfAffinity(uuid, onPlayerHit);
                    double dam;
                    int damageByArmor = 0;

                    if(calculateExtraArmorDamage) {
                        for(ItemStack x : ((Player)prey).getInventory().getArmorContents()) {
                            String s = "nothing";
                            if(x != null)
                                s = x.getType().toString().split("_")[0].toLowerCase();
                            int dmg = difficultyList.get(calcDifficulty(uuid)).getArmorDamageMultiplier(s);
                            damageByArmor += ((dmg == -505) ? 0 : dmg);
                        }
                    }

                    if(e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                        dam = e.getFinalDamage() * (calcPercentage(uuid, "damage-done-by-ranged-mobs") + damageByArmor) / 100.0;
                    } else {
                        dam = e.getFinalDamage() * (calcPercentage(uuid, "damage-done-by-mobs") + damageByArmor) / 100.0;
                    }

                    e.setDamage(dam);
                    if(calcMaxAffinity)
                        addAmountOfMaxAffinity(prey.getUniqueId(), config.getInt("calculating-affinity.max-affinity-changes.player-hit"));
                    if(calcMinAffinity)
                        addAmountOfMinAffinity(prey.getUniqueId(), config.getInt("calculating-affinity.min-affinity-changes.player-hit"));
                } else if(hunter instanceof Player) {
                    if(playerList.get(hunter.getUniqueId()) == null)
                        addPlayerData(hunter.getUniqueId());
                    if (!difficultyList.get(calcDifficulty(hunter.getUniqueId())).getAllowPVP()) {
                        if(data.getLang().getString("in-game.attacker-no-pvp") != null && !data.getLang().getString("in-game.attacker-no-pvp").equals(""))
                            hunter.sendMessage(config.getString("in-game.attacker-no-pvp").replaceAll("%user%", ((Player) prey).getDisplayName()));
                        e.setCancelled(true);
                    } else if(!difficultyList.get(calcDifficulty(prey.getUniqueId())).getAllowPVP()) {
                        if(data.getLang().getString("in-game.attackee-no-pvp") != null && !data.getLang().getString("in-game.attackee-no-pvp").equals(""))
                            hunter.sendMessage(config.getString("in-game.attackee-no-pvp").replaceAll("%user%", ((Player) prey).getDisplayName()));
                        e.setCancelled(true);
                    }
                }
            } else if (hunter instanceof Player && !disabledMobs.contains(prey.getType().toString()) && !ignoreMobs.contains(prey.getEntityId())) {
                double dam = e.getFinalDamage() * calcPercentage(hunter.getUniqueId(), "damage-done-on-mobs") / 100.0;
                e.setDamage(dam);
                if(!mobsOverrideIgnore.contains(prey.getEntityId()))
                    mobsOverrideIgnore.add(prey.getEntityId());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobSpawn(CreatureSpawnEvent e) {
        if(!config.getStringList("custom-mob-items-spawn-chance.includes-mobs").contains(e.getEntity().getType().toString()))
            return;

        List<SpawnReason> naturalReasons = new ArrayList<>(Arrays.asList(SpawnReason.DEFAULT, SpawnReason.NATURAL));
        List<SpawnReason> spawnReasons = new ArrayList<>(Arrays.asList(SpawnReason.SPAWNER_EGG, SpawnReason.SPAWNER, SpawnReason.DISPENSE_EGG));
        if(customArmorSpawnChance && naturalReasons.contains(e.getSpawnReason())) {
            Bukkit.getScheduler().runTaskAsynchronously(m, () -> Bukkit.getScheduler().runTask(m, () -> {
                Player closestPlayer = null;
                double distance = 1024.0;
                for(Player pl : Bukkit.getWorld(e.getEntity().getWorld().getUID()).getPlayers())
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHungerDrain(FoodLevelChangeEvent e) {
        if(e.getEntity() instanceof Player) {
            if(!playerList.containsKey(e.getEntity().getUniqueId()))
                addPlayerData(e.getEntity().getUniqueId());
            if(e.getEntity().getFoodLevel() > e.getFoodLevel())
                if(new Random().nextDouble() > (calcPercentage(e.getEntity().getUniqueId(), "hunger-drain-chance") / 100.0))
                    e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionEffect(EntityPotionEffectEvent e) {
        if(e.getEntity() instanceof Player) {
            if(!playerList.containsKey(e.getEntity().getUniqueId()))
                addPlayerData(e.getEntity().getUniqueId());
            if(effectCauses.contains(e.getCause()) && effects.contains(e.getModifiedType()))
                if(!difficultyList.get(calcDifficulty(e.getEntity().getUniqueId())).getEffectsOnAttack())
                    e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpot(EntityTargetLivingEntityEvent e) {
        if(e.getTarget() instanceof Player) {
            if(!playerList.containsKey(e.getTarget().getUniqueId()))
                addPlayerData(e.getTarget().getUniqueId());
            if(difficultyList.get(calcDifficulty(e.getTarget().getUniqueId())).getIgnoredMobs().contains(e.getEntity().getType().toString()))
                if(!mobsOverrideIgnore.contains(e.getEntity().getEntityId()) && !ignoreMobs.contains(e.getEntity().getEntityId()))
                    e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if(!playerList.containsKey(e.getPlayer().getUniqueId()))
            addPlayerData(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Minecrafter pl = playerList.get(uuid);
        SQL.updatePlayer(uuid.toString(), pl.getAffinity(), pl.getMaxAffinity(), pl.getMinAffinity());
        if(config.getBoolean("plugin-support.unload-leaving-player", false)) {
            playerList.remove(uuid);
            playersUUID.remove(e.getPlayer().getName());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null)
            return;
        if(!e.getView().getTitle().contains("DynamicDifficulty"))
            return;
        if(e.getCurrentItem().getItemMeta() == null)
            return;

        if(e.getView().getTitle().equals("DynamicDifficulty - Players")) {
            ItemStack item = e.getCurrentItem();
            e.getWhoClicked().closeInventory();
            if (e.getSlot() == 5 && item.getType().toString().equalsIgnoreCase("IRON_INGOT")) {
                openPlayersInventory((Player)e.getWhoClicked(), Integer.parseInt(item.getItemMeta().getLore().get(0)));
            } else if (e.getSlot() == 3 && item.getType().toString().equalsIgnoreCase("GOLD_INGOT")) {
                openPlayersInventory((Player)e.getWhoClicked(), Integer.parseInt(item.getItemMeta().getLore().get(0)));
            } else if (e.getSlot() != 4) {
                Inventory tmp = inventorySettings.get("iplayer");
                tmp.setItem(13, item);
                e.getWhoClicked().openInventory(tmp);
            }
        } else if (e.getView().getTitle().equals("DynamicDifficulty - Individual Player")) {
            UUID uuid = Bukkit.getPlayer(e.getInventory().getItem(13).getItemMeta().getDisplayName()).getUniqueId();
            ArrayList<String> allowed = new ArrayList<>(Arrays.asList("PINK_WOOL", "MAGENTA_WOOL", "PURPLE_WOOL", "LIME_WOOL", "BLUE_WOOL", "CYAN_WOOL", "LIGHT_BLUE_WOOL", "RED_WOOL"));
            if(allowed.contains(e.getCurrentItem().getType().toString()) && e.getSlot() % 9 != 0 && e.getSlot() % 9 != 4) {
                if (e.getCurrentItem().getType().toString().equals("RED_WOOL")) {
                    Minecrafter pl = playerList.get(uuid);
                    if(e.getSlot() / 9 < 1) { pl.setAffinity(startAffinity); }
                    else if(e.getSlot() / 9 < 2) { pl.setMinAffinity(-1); }
                    else if(e.getSlot() / 9 < 3) { pl.setMaxAffinity(-1); }
                } else {
                    int add = Integer.parseInt(e.getCurrentItem().getItemMeta().getDisplayName());
                    if(e.getSlot() / 9 < 1) { addAmountOfAffinity(uuid, add); }
                    else if(e.getSlot() / 9 < 2) { addAmountOfMinAffinity(uuid, add); }
                    else if(e.getSlot() / 9 < 3) { addAmountOfMaxAffinity(uuid, (add * -1)); }
                }
                ItemStack item = e.getInventory().getItem(13);
                String c1 = ChatColor.BOLD+""+ChatColor.DARK_GREEN;
                String c2 = ChatColor.BOLD+""+ChatColor.GREEN;
                Minecrafter p = playerList.get(uuid);
                setLore(item, new ArrayList<>(Arrays.asList(c1+"Affinity: "+c2+p.getAffinity(),c1+"Min Affinity: "+c2+p.getMinAffinity(),c1+"Max Affinity: "+c2+p.getMaxAffinity())));
                e.getInventory().setItem(13, item);
            }
        }
        e.setCancelled(true);
    }
}
