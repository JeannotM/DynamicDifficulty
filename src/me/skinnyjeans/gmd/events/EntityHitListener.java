package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EntityHitListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    private boolean calculateExtraArmorDamage;
    private int affinityPerHeart;
    private int onPlayerHit;

    public EntityHitListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent e) {
        Entity prey = e.getEntity();
        Entity hunter = e.getDamager();
        if (prey instanceof Player) {
            MAIN_MANAGER.getPlayerManager().isPlayerValid(prey);
            if (!(hunter instanceof Player) && MAIN_MANAGER.getEntityManager().isEntityValid(hunter)) {
                if (((Player)prey).isBlocking()) return;

                UUID uuid = prey.getUniqueId();
                MAIN_MANAGER.getPlayerManager().addAffinity(uuid, onPlayerHit);
                double dam;
                int damageByArmor = 0;

                if(calculateExtraArmorDamage) {
                    for(ItemStack x : ((Player)prey).getInventory().getArmorContents()) {
                        String s = "nothing";
                        if(x != null)
                            s = x.getType().toString().split("_")[0].toLowerCase();
                        int dmg = MAIN_MANAGER.getDifficultyManager().getDifficulty(uuid).getArmorDamageMultiplier(s);
                        damageByArmor += ((dmg == -505) ? 0 : dmg);
                    }
                }

                if(e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                    dam = e.getFinalDamage() * (MAIN_MANAGER.getDifficultyManager().getDifficulty(uuid).getDamageByRangedMobs() + damageByArmor) / 100.0;
                } else {
                    dam = e.getFinalDamage() * (MAIN_MANAGER.getDifficultyManager().getDifficulty(uuid).getDamageByMobs() + damageByArmor) / 100.0;
                }

                e.setDamage(dam);

                if(dam != 0)
                    MAIN_MANAGER.getPlayerManager().addAffinity(uuid, affinityPerHeart * (int)Math.ceil(dam / 2));

            } else if(hunter instanceof Player) {
                MAIN_MANAGER.getPlayerManager().isPlayerValid(hunter);
                if (!MAIN_MANAGER.getDifficultyManager().getDifficulty(hunter.getUniqueId()).getAllowPVP()) {
                    if(data.getLang().isSet("in-game.attacker-no-pvp") && data.getLang().getString("in-game.attacker-no-pvp").length() != 0)
                        hunter.sendMessage(data.getLang().getString("in-game.attacker-no-pvp").replaceAll("%user%", ((Player) prey).getDisplayName()));
                    e.setCancelled(true);
                } else if(!MAIN_MANAGER.getDifficultyManager().getDifficulty(prey.getUniqueId()).getAllowPVP()) {
                    if(data.getLang().isSet("in-game.attackee-no-pvp") && data.getLang().getString("in-game.attackee-no-pvp").length() != 0)
                        hunter.sendMessage(data.getLang().getString("in-game.attackee-no-pvp").replaceAll("%user%", ((Player) prey).getDisplayName()));
                    e.setCancelled(true);
                }
            }
        } else if (hunter instanceof Player && MAIN_MANAGER.getEntityManager().isEntityValid(prey)) {
            double dam = e.getFinalDamage() * MAIN_MANAGER.getDifficultyManager().getDifficulty(hunter.getUniqueId()).getDamageOnMobs() / 100.0;
            e.setDamage(dam);
            if(!mobsOverrideIgnore.contains(prey.getEntityId()))
                mobsOverrideIgnore.add(prey.getEntityId());
        }
    }


    @Override
    public void reloadConfig() {
        calculateExtraArmorDamage = true;
        affinityPerHeart = 0;
        onPlayerHit = 0;
    }
}
