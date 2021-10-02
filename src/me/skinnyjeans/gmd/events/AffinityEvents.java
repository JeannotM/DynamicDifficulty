package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.Main;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AffinityEvents extends Affinity implements Listener {

    public AffinityEvents(Main ma) { super(ma); }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        if(!disabledWorlds.contains(e.getEntity().getWorld().getName())) {
            if(onDeath != 0)
                addAmountOfAffinity(e.getEntity().getUniqueId(), onDeath);
            if(difficultyList.get(calcDifficulty(e.getEntity().getUniqueId())).getKeepInventory()) {
                e.setKeepInventory(true);
                e.setKeepLevel(true);
                e.getDrops().clear();
            }
            if(calcMaxAffinity)
                playerList.get(e.getEntity().getUniqueId())
                        .addMaxAffinity(data.getConfig().getInt("calculating-affinity.max-affinity-changes.death"), maxAffinityLimit);
            if(calcMinAffinity)
                playerList.get(e.getEntity().getUniqueId())
                        .addMinAffinity(data.getConfig().getInt("calculating-affinity.min-affinity-changes.death"), minAffinityLimit);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKill(EntityDeathEvent e) {
        if(!disabledWorlds.contains(e.getEntity().getWorld().getName())) {
            try {
                if(e.getEntity().getKiller() instanceof Player) {
                    UUID uuid = e.getEntity().getKiller().getUniqueId();
                    if (onPVPKill != 0 && e.getEntity() instanceof Player) {
                        addAmountOfAffinity(uuid, onPVPKill);
                        if(calcMaxAffinity)
                            playerList.get(uuid)
                                    .addMaxAffinity(data.getConfig().getInt("calculating-affinity.max-affinity-changes.pvp-kill"), maxAffinityLimit);
                        if(calcMinAffinity)
                            playerList.get(uuid)
                                    .addMinAffinity(data.getConfig().getInt("calculating-affinity.min-affinity-changes.pvp-kill"), minAffinityLimit);
                    } else if (mobsPVE.get(e.getEntityType().toString()) != null) {
                        addAmountOfAffinity(uuid, mobsPVE.get(e.getEntityType().toString()));
                        if(calcMaxAffinity)
                            playerList.get(uuid)
                                    .addMaxAffinity(data.getConfig().getInt("calculating-affinity.max-affinity-changes.pve-kill"), maxAffinityLimit);
                        if(calcMinAffinity)
                            playerList.get(uuid)
                                    .addMinAffinity(data.getConfig().getInt("calculating-affinity.min-affinity-changes.pve-kill"), minAffinityLimit);
                    }

                    if (!(e.getEntity() instanceof Player) && !disabledMobs.contains(e.getEntityType().toString())) {
                        e.setDroppedExp((int) (e.getDroppedExp() * calcPercentage(uuid, "experience-multiplier") / 100.0));
                        double DoubleLoot = calcPercentage(uuid, "double-loot-chance");
                        if (DoubleLoot != 0.0 && new Random().nextDouble() < DoubleLoot / 100.0 && !e.getEntity().getCanPickupItems())
                            for (int i = 0; i < e.getDrops().size(); i++)
                                Bukkit.getWorld(e.getEntity().getWorld().getUID()).dropItemNaturally(e.getEntity().getLocation(), e.getDrops().get(i));
                    }
                }
            } catch (NullPointerException er) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Dynamic Difficulty] NullPointerException. A plugin might be causing issues");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMined(BlockBreakEvent e) {
        if(!disabledWorlds.contains(e.getPlayer().getWorld().getName()))
            if (blocks.get(e.getBlock().getBlockData().getMaterial().name()) != null)
                if(!e.getPlayer().getItemOnCursor().containsEnchantment(Enchantment.SILK_TOUCH) || data.getConfig().getBoolean("silk-touch-allowed"))
                    if(e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                        addAmountOfAffinity(e.getPlayer().getUniqueId(), blocks.get(e.getBlock().getBlockData().getMaterial().name()));
                        if(calcMaxAffinity)
                            playerList.get(e.getPlayer().getUniqueId())
                                    .addMaxAffinity(data.getConfig().getInt("calculating-affinity.max-affinity-changes.block-mined"), maxAffinityLimit);
                        if(calcMinAffinity)
                            playerList.get(e.getPlayer().getUniqueId())
                                    .addMinAffinity(data.getConfig().getInt("calculating-affinity.min-affinity-changes.block-mined"), minAffinityLimit);
                    }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent e) {
        if(!disabledWorlds.contains(e.getEntity().getWorld().getName())) {
            Entity prey = e.getEntity();
            Entity hunter = e.getDamager();
            if (((HumanEntity)prey).isBlocking()) { return; }
            try {
                if (prey instanceof Player) {
                    if (!(hunter instanceof Player) && !disabledMobs.contains(hunter.getType().toString()) && !ignoreMobs.contains(hunter.getEntityId())) {
                        UUID uuid = prey.getUniqueId();
                        addAmountOfAffinity(uuid, onPlayerHit);
                        double dam = e.getFinalDamage() * calcPercentage(uuid, "damage-done-by-mobs") / 100.0;
                        e.setDamage(dam);

                        if(calcMaxAffinity)
                            playerList.get(prey.getUniqueId())
                                    .addMaxAffinity(data.getConfig().getInt("calculating-affinity.max-affinity-changes.player-hit"), maxAffinityLimit);
                        if(calcMinAffinity)
                            playerList.get(prey.getUniqueId())
                                    .addMinAffinity(data.getConfig().getInt("calculating-affinity.min-affinity-changes.player-hit"), minAffinityLimit);
                    }
                } else if (hunter instanceof Player && !disabledMobs.contains(prey.getType().toString()) && !ignoreMobs.contains(prey.getEntityId())) {
                    double dam = e.getFinalDamage() * calcPercentage(hunter.getUniqueId(), "damage-done-on-mobs") / 100.0;
                    e.setDamage(dam);
                    if(!mobsOverrideIgnore.contains(prey.getEntityId()))
                        mobsOverrideIgnore.add(prey.getEntityId());
                }
            } catch (NullPointerException er) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[Dynamic Difficulty] NullPointerException. A plugin might be causing issues");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobSpawn(CreatureSpawnEvent e) {
        if(!data.getConfig().getStringList("custom-mob-items-spawn-chance.includes-mobs").contains(e.getEntity().getType().toString()))
            return;

        List<SpawnReason> naturalReasons = new ArrayList<>(Arrays.asList(SpawnReason.DEFAULT, SpawnReason.NATURAL));
        List<SpawnReason> spawnReasons = new ArrayList<>(Arrays.asList(SpawnReason.SPAWNER_EGG, SpawnReason.SPAWNER, SpawnReason.DISPENSE_EGG));
        if(customArmorSpawnChance && (naturalReasons.contains(e.getSpawnReason()))) {
            Bukkit.getScheduler().runTaskAsynchronously(m, () -> Bukkit.getScheduler().runTask(m, () -> {
                Player closestPlayer = null;
                double distance = 512.0;
                for(Player pl : Bukkit.getWorld(e.getEntity().getWorld().getName()).getPlayers()) {
                    if(e.getEntity().getLocation().distance(pl.getLocation()) < distance) {
                        distance = e.getEntity().getLocation().distance(pl.getLocation());
                        closestPlayer = pl;
                    }
                }
                String diff = calcDifficulty(closestPlayer.getUniqueId());
                int rand = new Random().nextInt(chancePerArmor.get("total") + 1) -1;
                int count = 0;
                ArrayList<String> array = new ArrayList<>(Arrays.asList("leather", "gold", "chain", "iron", "diamond", "netherite"));
                double chanceToEnchant = (calcPercentage(closestPlayer.getUniqueId(), "chance-to-enchant-a-piece") / 100.0 / 100.0);
                if(new Random().nextDouble() < (calcPercentage(closestPlayer.getUniqueId(), "weapon-chance") / 100.0 / 100.0)) {
                    ArrayList<Material> ranged = new ArrayList<>(Arrays.asList(Material.BOW, Material.CROSSBOW));
                    if (ranged.contains(e.getEntity().getEquipment().getItemInMainHand().getType())) {
                        ItemStack item = new ItemStack(e.getEntity().getEquipment().getItemInMainHand().getType());
                        e.getEntity().getEquipment().setItemInMainHand(calcEnchant(item, "bow", diff, chanceToEnchant));
                    } else {
                        for (int i = 0; i < customSpawnWeapons.size(); i++) {
                            int thisCount = count + chancePerArmor.get(customSpawnWeapons.get(i));
                            if (count < rand && thisCount >= rand) {
                                ItemStack item = new ItemStack(Material.getMaterial(customSpawnWeapons.get(i)));
                                e.getEntity().getEquipment().setItemInMainHand(calcEnchant(item, "weapon", diff, chanceToEnchant));
                            }
                        }
                        count = 0;
                    }
                }
                if(new Random().nextDouble() < (calcPercentage(closestPlayer.getUniqueId(), "chance-to-have-armor") / 100.0 / 100.0)) {
                    for(int i=0;i<6;i++) {
                        int thisCount = count + chancePerArmor.get(array.get(i));
                        if(count < rand && thisCount >= rand) {
                            String thisItem = array.get(i);
                            double randomChance = new Random().nextDouble();
                            float armorDropChance = (float) calcPercentage(closestPlayer.getUniqueId(), "armor-drop-chance") / 100 / 100;
                            if(randomChance < (calcPercentage(closestPlayer.getUniqueId(), "helmet-chance") / 100.0 / 100.0)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem.toUpperCase() + "_HELMET"));
                                e.getEntity().getEquipment().setHelmet(calcEnchant(item, "helmet", diff, chanceToEnchant));
                                e.getEntity().getEquipment().setHelmetDropChance(armorDropChance);
                            }
                            if(randomChance < (calcPercentage(closestPlayer.getUniqueId(), "chest-chance") / 100.0 / 100.0)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem.toUpperCase() + "_CHESTPLATE"));
                                e.getEntity().getEquipment().setChestplate(calcEnchant(item, "chestplate", diff, chanceToEnchant));
                                e.getEntity().getEquipment().setChestplateDropChance(armorDropChance);
                            }
                            if(randomChance < (calcPercentage(closestPlayer.getUniqueId(), "leggings-chance") / 100.0 / 100.0)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem.toUpperCase() + "_LEGGINGS"));
                                e.getEntity().getEquipment().setLeggings(calcEnchant(item, "leggings", diff, chanceToEnchant));
                                e.getEntity().getEquipment().setLeggingsDropChance(armorDropChance);
                            }
                            if(randomChance < (calcPercentage(closestPlayer.getUniqueId(), "boots-chance") / 100.0 / 100.0)) {
                                ItemStack item = new ItemStack(Material.getMaterial(thisItem.toUpperCase() + "_BOOTS"));
                                e.getEntity().getEquipment().setBoots(calcEnchant(item, "boots", diff, chanceToEnchant));
                                e.getEntity().getEquipment().setBootsDropChance(armorDropChance);
                            }
                            break;
                        }
                        count += chancePerArmor.get(array.get(i));
                    }
                } else {
                    e.getEntity().getEquipment().setHelmet(null);
                    e.getEntity().getEquipment().setChestplate(null);
                    e.getEntity().getEquipment().setLeggings(null);
                    e.getEntity().getEquipment().setBoots(null);
                }
            }));
        } else if(spawnReasons.contains(e.getSpawnReason())) {
            if(data.getConfig().getBoolean("plugin-support.no-changes-to-spawned-mobs", false))
                ignoreMobs.add(e.getEntity().getEntityId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHungerDrain(FoodLevelChangeEvent e) {
        try {
            if(e.getEntity() instanceof Player) {
                double drainChance = calcPercentage(e.getEntity().getUniqueId(), "hunger-drain-chance");
                if(drainChance >= 0.0 && drainChance < 100.0)
                    if(new Random().nextDouble() > (drainChance / 100.0))
                        e.setCancelled(true);
            }
        }catch(NullPointerException er){ }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionEffect(EntityPotionEffectEvent e) {
        try {
            if(e.getEntity() instanceof Player)
                if(effectCauses.contains(e.getCause()))
                    if(effects.contains(e.getModifiedType()))
                        if(!difficultyList.get(calcDifficulty(e.getEntity().getUniqueId())).getEffectsOnAttack())
                            e.setCancelled(true);
        }catch(NullPointerException er){ }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpot(EntityTargetLivingEntityEvent e) {
        try {
            if(e.getTarget() instanceof Player)
                if(difficultyList.get(calcDifficulty(e.getTarget().getUniqueId())).getIgnoredMobs().contains(e.getEntity().getType().toString()))
                    if(!mobsOverrideIgnore.contains(e.getEntity().getEntityId()) && !ignoreMobs.contains(e.getEntity().getEntityId()))
                        e.setCancelled(true);
        }catch(NullPointerException er){ }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Player usr = e.getPlayer();
        if(!playerList.containsKey(uuid)) {
            SQL.getAffinityValues(uuid.toString(), r -> {
                Minecrafter mc = new Minecrafter(uuid, usr.getName());
                if (r.get(0) == -1) {
                    mc.setAffinity(startAffinity);
                    mc.setMaxAffinity(-1);
                    mc.setMinAffinity(-1);
                    playerList.put(uuid, mc);
                    SQL.updatePlayer(uuid.toString(), startAffinity, -1, -1);
                } else {
                    mc.setAffinity(r.get(0));
                    mc.setMaxAffinity(r.get(1));
                    mc.setMinAffinity(r.get(2));
                    playerList.put(uuid, mc);
                }
                playersUUID.put(usr.getName(), uuid);
            });
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Minecrafter pl = playerList.get(uuid);
        SQL.updatePlayer(uuid.toString(), pl.getAffinity(), pl.getMaxAffinity(), pl.getMinAffinity());
        if(data.getConfig().getBoolean("plugin-support.unload-leaving-player", false)) {
            playerList.remove(uuid);
            playersUUID.remove(e.getPlayer().getName());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null)
            return;

        if(equipmentCheck.equalsIgnoreCase("on-equip") && (calcMinAffinity || calcMaxAffinity)) {
            if(calcMaxAffinity) {
                if(maxAffinityListItems.contains(e.getCurrentItem().getType().toString())) {
                    Minecrafter mc = playerList.get(e.getWhoClicked().getUniqueId());
                    if(mc.getMaxAffinity() == -1)
                        mc.setMaxAffinity(maxAffinity);
                    mc.addMaxAffinity(maxAffinityItems.get(e.getCurrentItem().getType().toString()), maxAffinityLimit);
                }
            }

            if(calcMinAffinity) {
                if(minAffinityListItems.contains(e.getCurrentItem().getType().toString())) {
                    Minecrafter mc = playerList.get(e.getWhoClicked().getUniqueId());
                    if(mc.getMinAffinity() == -1)
                        mc.setMinAffinity(minAffinity);
                    mc.addMinAffinity(minAffinityItems.get(e.getCurrentItem().getType().toString()), minAffinityLimit);
                }
            }
        }

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
                Minecrafter pl = playerList.get(uuid);
                if (e.getCurrentItem().getType().toString().equals("RED_WOOL")) {
                    if(e.getSlot() / 9 < 1) { pl.setAffinity(startAffinity); }
                    else if(e.getSlot() / 9 < 2) { pl.setMinAffinity(-1); }
                    else if(e.getSlot() / 9 < 3) {pl.setMaxAffinity(-1); }
                } else {
                    int add = Integer.parseInt(e.getCurrentItem().getItemMeta().getDisplayName());
                    if(e.getSlot() / 9 < 1) { pl.setAffinity(calcAffinity(add)); }
                    else if(e.getSlot() / 9 < 2) { pl.setMinAffinity(calcAffinity(add)); }
                    else if(e.getSlot() / 9 < 3) {pl.setMaxAffinity(calcAffinity(add)); }
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
