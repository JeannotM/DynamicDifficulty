package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Random;
import java.util.UUID;

public class EntityDeathListener extends BaseListener {

    public EntityDeathListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler
    public void onKill(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if(!MAIN_MANAGER.getPlayerManager().isPlayerValid(entity.getKiller())) return;
        if(MAIN_MANAGER.getEntityManager().isEntityIgnored(entity)) {
            MAIN_MANAGER.getEntityManager().ignoredEntityKilled(entity);
            return;
        }


        Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () -> {
            if (MAIN_MANAGER.getEntityManager().hasEntityPoints(e.getEntityType()))
                MAIN_MANAGER.getPlayerManager().addAffinity(entity.getKiller(), MAIN_MANAGER.getEntityManager().getEntityPoints(e.getEntityType()));
        });

        boolean isPlayer = entity instanceof Player;
        if (!isPlayer && !MAIN_MANAGER.getEntityManager().isEntityDisabled(entity)) {
            if(entity.getCanPickupItems()) return;

            e.setDroppedExp((int) (e.getDroppedExp() * MAIN_MANAGER.getDifficultyManager().getDifficulty(entity.getKiller()).experienceMultiplier));
            if (new Random().nextDouble() < MAIN_MANAGER.getDifficultyManager().getDifficulty(entity.getKiller()).doubleLootChance) {
                World world = Bukkit.getWorld(entity.getWorld().getUID());
                if (world != null) {
                    int itemsDropped = e.getDrops().size();
                    for (int i = 0; i < itemsDropped; i++) {
                        world.dropItemNaturally(
                                entity.getLocation(),
                                e.getDrops().get(i));
                    }
                }
            }
        }
    }
}
