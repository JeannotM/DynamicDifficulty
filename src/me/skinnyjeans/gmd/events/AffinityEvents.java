package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class AffinityEvents extends Affinity implements Listener {

    public AffinityEvents(Main ma) { super(ma); }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerRespawnEvent e) {
        if(!disabledWorlds.contains(e.getPlayer().getWorld().getName()) && onDeath != 0)
            addAmountOfAffinity(e.getPlayer().getUniqueId(), onDeath);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKill(EntityDeathEvent e) {
        if(!disabledWorlds.contains(e.getEntity().getWorld().getName())) {
            try {
                if(e.getEntity().getKiller() instanceof Player) {
                    UUID uuid = e.getEntity().getKiller().getUniqueId();
                    if (onPVEKill != 0 && e.getEntity() instanceof Player) {
                        addAmountOfAffinity(uuid, onPVPKill);
                    } else if (onPVPKill != 0 && mobsPVE.get(e.getEntityType().toString()) != null) {
                        addAmountOfAffinity(uuid, mobsPVE.get(e.getEntityType().toString()));
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
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[Dynamic Difficulty] NullPointerException. A plugin might be causing issues");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMined(BlockBreakEvent e) {
        if(!disabledWorlds.contains(e.getPlayer().getWorld().getName()))
            if (onMined != 0 && blocks.get(e.getBlock().getBlockData().getMaterial().name()) != null)
                if(!e.getPlayer().getItemOnCursor().containsEnchantment(Enchantment.SILK_TOUCH) || silkTouchAllowed)
                    if(e.getPlayer().getGameMode() != GameMode.CREATIVE)
                        addAmountOfAffinity(e.getPlayer().getUniqueId(), onMined);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent e) {
        if(!disabledWorlds.contains(e.getEntity().getWorld().getName())) {
            Entity prey = e.getEntity();
            Entity hunter = e.getDamager();
            if(prey instanceof Player) {
                if (((HumanEntity)prey).isBlocking()) { return; }
            }
            try {
                if (prey instanceof Player) {
                    if (!(hunter instanceof Player) && !disabledMobs.contains(hunter.getType().toString())) {
                        UUID uuid = prey.getUniqueId();
                        playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + onPlayerHit));
                        double dam = e.getFinalDamage() * calcPercentage(uuid, "damage-done-by-mobs") / 100.0;
                        e.setDamage(dam);
                    }
                } else if (hunter instanceof Player && !disabledMobs.contains(prey.getType().toString())) {
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
    public void onPotionEffect(EntityPotionEffectEvent e) {
        try {
            if(e.getEntity() instanceof Player)
                if(effectCauses.contains(e.getCause()))
                    if(effects.contains(e.getModifiedType()))
                        if(!effectsWhenAttacked.get(calcDifficulty(e.getEntity().getUniqueId())))
                            e.setCancelled(true);
        }catch(NullPointerException er){ return; }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpot(EntityTargetLivingEntityEvent e) {
        try {
            if(e.getTarget() instanceof Player)
                if(mobsIgnorePlayers.get(calcDifficulty(e.getTarget().getUniqueId())).contains(e.getEntity().getType().toString()))
                    if(!mobsOverrideIgnore.contains(e.getEntity().getEntityId()))
                        e.setCancelled(true);
        }catch(NullPointerException er){ return; }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        SQL.getAffinityValues(uuid.toString(), r -> {
            if (r.get(0) == -1){
                playerAffinity.put(uuid, startAffinity);
                playerMaxAffinity.put(uuid, -1);
                playerMinAffinity.put(uuid, -1);
                SQL.updatePlayer(uuid.toString(), startAffinity, -1, -1);
            } else {
                playerAffinity.put(uuid, r.get(0));
                playerMaxAffinity.put(uuid, r.get(1));
                playerMinAffinity.put(uuid, r.get(2));
            }
            playersUUID.put(e.getPlayer().getName(), e.getPlayer().getUniqueId());
        });
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        SQL.updatePlayer(uuid.toString(), playerAffinity.get(uuid), playerMaxAffinity.get(uuid), playerMinAffinity.get(uuid));
        if(getVariable("unload-player") == 0) {
            playerAffinity.remove(uuid);
            playerMaxAffinity.remove(uuid);
            playerMinAffinity.remove(uuid);
            playersUUID.remove(e.getPlayer().getName());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
//        if(calculateMinAffinity) {
//
//        }
//        if(armorWorn.containsKey(e.getCurrentItem().getType().toString())) {
//            if (e.getSlotType().equals(InventoryType.SlotType.ARMOR)) {
//            }
//        }

        if(!e.getView().getTitle().contains("DynamicDifficulty"))
            return;
        if (e.getCurrentItem() == null)
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
                    if(e.getSlot() / 9 < 1) { playerAffinity.replace(uuid, startAffinity); }
                    else if(e.getSlot() / 9 < 2) { playerMinAffinity.replace(uuid, -1); }
                    else if(e.getSlot() / 9 < 3) { playerMaxAffinity.replace(uuid, -1); }
                } else {
                    int add = Integer.parseInt(e.getCurrentItem().getItemMeta().getDisplayName());
                    if(e.getSlot() / 9 < 1) { playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + add)); }
                    else if(e.getSlot() / 9 < 2) { playerMinAffinity.replace(uuid, calcAffinity(null, playerMinAffinity.get(uuid) + add)); }
                    else if(e.getSlot() / 9 < 3) { playerMaxAffinity.replace(uuid, calcAffinity(null, playerMaxAffinity.get(uuid) + add)); }
                }
                ItemStack item = e.getInventory().getItem(13);
                String c1 = ChatColor.BOLD+""+ChatColor.DARK_GREEN;
                String c2 = ChatColor.BOLD+""+ChatColor.GREEN;
                setLore(item, new ArrayList<>(Arrays.asList(c1+"Affinity: "+c2+playerAffinity.get(uuid),c1+"Min Affinity: "+c2+playerMinAffinity.get(uuid),c1+"Max Affinity: "+c2+playerMaxAffinity.get(uuid))));
                e.getInventory().setItem(13, item);
            }
        }
        e.setCancelled(true);
    }
}
