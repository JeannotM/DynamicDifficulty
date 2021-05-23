package me.skinnyjeans.gmd;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerAffinity extends Affinity {

	public PlayerAffinity(Main m) { super(m); }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDeath(PlayerRespawnEvent e) {
		if(!(disabledWorlds.contains(e.getPlayer().getWorld().getName())) || !(multiverseEnabled)){
			if (onDeath != 0) {
				UUID uuid = e.getPlayer().getUniqueId();
				playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + onDeath));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onKill(EntityDeathEvent e) {
		if(!(disabledWorlds.contains(e.getEntity().getWorld().getName())) || !(multiverseEnabled)) {
			try {
				if ((onPVPKill != 0 || onPVEKill != 0) && e.getEntity().getKiller() instanceof Player) {
					UUID uuid = e.getEntity().getKiller().getUniqueId();
					if (e.getEntity() instanceof Player) {
						playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + onPVPKill));
					} else if (mobsPVE.get(e.getEntityType().toString()) != null) {
						playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + mobsPVE.get(e.getEntityType().toString())));
					}
				}

				if (!(e.getEntity() instanceof Player) && !(e.getEntity() instanceof EnderDragon) && !(e.getEntity() instanceof Wither) && e.getEntity().getKiller() instanceof Player) {
					UUID uuid = e.getEntity().getKiller().getUniqueId();
					e.setDroppedExp((int) (e.getDroppedExp() * calcPercentage(uuid, "experience-multiplier") / 100.0));
					double DoubleLoot = calcPercentage(uuid, "double-loot-chance");
					if (DoubleLoot != 0.0 && new Random().nextDouble() < DoubleLoot / 100.0 && !e.getEntity().getCanPickupItems()) {
						for (int i = 0; i < e.getDrops().size(); i++) {
							Bukkit.getWorld(e.getEntity().getWorld().getUID()).dropItemNaturally(e.getEntity().getLocation(), e.getDrops().get(i));
						}
					}
				}
			} catch (NullPointerException error) {
				Bukkit.getConsoleSender().sendMessage("NullPointerException. Enemytype Attack: " + e.getEntity().getKiller().getType() + " Enemytype Hit: " + e.getEntity().getType());
				// Ugly tmp fix
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onMined(BlockBreakEvent e) {
		if(!(disabledWorlds.contains(e.getPlayer().getWorld().getName())) || !(multiverseEnabled)) {
			if (onMined != 0 && blocks.contains(e.getBlock().getBlockData().getMaterial().name()) && (!e.getPlayer().getItemOnCursor().containsEnchantment(Enchantment.SILK_TOUCH) || silkTouchAllowed) && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
				UUID uuid = e.getPlayer().getUniqueId();
				playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + onMined));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onHit(EntityDamageByEntityEvent e) {
		if(!(disabledWorlds.contains(e.getEntity().getWorld().getName())) || !(multiverseEnabled)) {
			Entity prey = e.getEntity();
			Entity hunter = e.getDamager();
			try {
				if (prey instanceof Player) {
					if (!(hunter instanceof Player) && !(hunter instanceof EnderDragon) && !(hunter instanceof Wither)) {
						UUID uuid = prey.getUniqueId();
						playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + onPlayerHit));
						double dam = e.getFinalDamage() * calcPercentage(uuid, "damage-done-by-mobs") / 100.0;
						e.setDamage(dam);
					}
					if (!(hunter instanceof Player) && (hunter instanceof LivingEntity || hunter instanceof Arrow) && effectsWhenAttacked.get(calcDifficulty(prey.getUniqueId())))
						for (PotionEffectType effect : effects) {
							if (((LivingEntity) prey).hasPotionEffect(effect))
								((LivingEntity) prey).removePotionEffect(effect);
						}
				} else if (hunter instanceof Player && !(prey instanceof Player) && !(prey instanceof EnderDragon) && !(prey instanceof Wither)) {
					double dam = e.getFinalDamage() * calcPercentage(hunter.getUniqueId(), "damage-done-on-mobs") / 100.0;
					e.setDamage(dam);
				}
			} catch (NullPointerException error) {
				Bukkit.getConsoleSender().sendMessage("NullPointerException. Enemytype Attack: " + hunter + " Enemytype Hit: " + prey);
				// Ugly tmp fix
			}
		}
	}

	// To increase/decrease players score every few minutes
	@Override
    public void onInterval() {
        Bukkit.getOnlinePlayers().forEach(name -> {
            UUID uuid = name.getUniqueId();
            playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + onInterval));
        });
    }
}
