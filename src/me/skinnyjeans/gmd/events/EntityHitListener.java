package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class EntityHitListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    private boolean calculateExtraArmorDamage;
    private boolean allowTamedWolves;
    private int affinityPerHeart;
    private int onPlayerHit;
    private String notAttackOthers;
    private String notAttackPerson;

    public EntityHitListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent e) {
        Entity hunter, prey;

        if(allowTamedWolves && e.getEntity() instanceof Wolf) {
            prey = (((Wolf) e.getDamager()).getOwner() == null) ? e.getDamager() : (Entity) ((Wolf) e.getDamager()).getOwner();
        } else prey = e.getEntity();

        if (e.getDamager() instanceof Arrow) {
            hunter = (Entity) ((Projectile) e.getDamager()).getShooter();
        } else if (allowTamedWolves && e.getDamager() instanceof Wolf) {
            hunter = (((Wolf) e.getDamager()).getOwner() == null) ? e.getDamager() : (Entity) ((Wolf) e.getDamager()).getOwner();
        } else hunter = e.getDamager();

        if (prey instanceof Player && MAIN_MANAGER.getPlayerManager().isPlayerValid(prey)) {
            if (((Player) prey).isBlocking()) return;

            if (hunter instanceof Player && MAIN_MANAGER.getPlayerManager().isPlayerValid(hunter)) {
                HashMap<String, String> entry = new HashMap<String, String>() {{ put("%user%", ((Player) prey).getDisplayName()); }};
                if (!MAIN_MANAGER.getDifficultyManager().getDifficulty(hunter.getUniqueId()).getAllowPVP()) {
                    if(notAttackOthers.length() != 0) prey.sendMessage(MAIN_MANAGER.getDataManager().replaceString(notAttackOthers, entry));
                    e.setCancelled(true);
                } else if(!MAIN_MANAGER.getDifficultyManager().getDifficulty(prey.getUniqueId()).getAllowPVP()) {
                    if(notAttackPerson.length() != 0) prey.sendMessage(MAIN_MANAGER.getDataManager().replaceString(notAttackPerson, entry));
                    e.setCancelled(true);
                }
            } else {
                UUID uuid = prey.getUniqueId();
                Difficulty difficulty = MAIN_MANAGER.getDifficultyManager().getDifficulty(uuid);
                int damageByArmor = 0;

                if(calculateExtraArmorDamage && e.getEntity() instanceof Player)
                    for(ItemStack x : ((Player) prey).getInventory().getArmorContents()) {
                        String s = x == null ? "nothing" : x.getType().toString().split("_")[0].toLowerCase();
                        int dmg = difficulty.getArmorDamageMultiplier(s);
                        damageByArmor += dmg == -505 ? 0 : dmg;
                    }

                double damage;
                if (allowTamedWolves && e.getEntity() instanceof Wolf) {
                    damage = (e.getFinalDamage() * difficulty.getDamageOnTamed()) / 100;
                } else {
                    boolean isProjectile = e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE);
                    damage = (e.getFinalDamage() * (isProjectile ? difficulty.getDamageByRangedMobs() : difficulty.getDamageByMobs()) + damageByArmor) / 100;
                }

                e.setDamage(damage);

                Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () -> {
                    int removePoints = -(damage == 0 ? 0 : affinityPerHeart * (int) Math.ceil(damage / 2)) + onPlayerHit;
                    MAIN_MANAGER.getPlayerManager().addAffinity(uuid, removePoints);
                });
            }
        } else if (hunter instanceof Player && MAIN_MANAGER.getPlayerManager().isPlayerValid(hunter)) {
            double damage = e.getFinalDamage() * MAIN_MANAGER.getDifficultyManager().getDifficulty(hunter.getUniqueId()).getDamageOnMobs() / 100.0;
            MAIN_MANAGER.getEntityManager().entityHit(prey);
            e.setDamage(damage);
        }
    }

    @Override
    public void reloadConfig() {
        FileConfiguration config = MAIN_MANAGER.getDataManager().getConfig();

        calculateExtraArmorDamage = false;
        allowTamedWolves = config.getBoolean("toggle-settings.allow-tamed-wolves-in-calculations", true);
        affinityPerHeart = config.getInt("affinity-per-heart-loss", -1);
        onPlayerHit = config.getInt("player-hit", -1);

        notAttackOthers = MAIN_MANAGER.getDataManager().getLanguageString("in-game.attacker-no-pvp", false);
        notAttackPerson = MAIN_MANAGER.getDataManager().getLanguageString("in-game.attackee-no-pvp", false);

        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties() )
            if (difficulty.getArmorDamageMultiplier().size() != 0) {
                calculateExtraArmorDamage = true;
                break;
            }
    }
}
