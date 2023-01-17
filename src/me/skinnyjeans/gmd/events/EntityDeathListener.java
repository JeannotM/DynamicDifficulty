package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Random;
import java.util.UUID;

public class EntityDeathListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    public EntityDeathListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKill(EntityDeathEvent e) {
        if(e.getEntity().getKiller() == null) return;
        if(!MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getEntity().getKiller())) return;
        if(MAIN_MANAGER.getEntityManager().isEntityIgnored(e.getEntity())) {
            MAIN_MANAGER.getEntityManager().ignoredEntityKilled(e.getEntity());
            return;
        }

        UUID uuid = e.getEntity().getKiller().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () -> {
            if (MAIN_MANAGER.getEntityManager().hasEntityPoints(e.getEntityType()))
                MAIN_MANAGER.getPlayerManager().addAffinity(uuid, MAIN_MANAGER.getEntityManager().getEntityPoints(e.getEntityType()));
        });

        boolean isPlayer = e.getEntity() instanceof Player;
        if (!isPlayer && !MAIN_MANAGER.getEntityManager().isEntityDisabled(e.getEntity())) {
            if(e.getEntity().getCanPickupItems()) return;
            e.setDroppedExp((int) (e.getDroppedExp() * MAIN_MANAGER.getDifficultyManager().getDifficulty(uuid).getExperienceMultiplier() / 100.0));
            double DoubleLoot = MAIN_MANAGER.getDifficultyManager().getDifficulty(uuid).getDoubleLoot();
            if (DoubleLoot != 0.0 && new Random().nextDouble() < DoubleLoot / 100.0)
                for (int i = 0; i < e.getDrops().size(); i++)
                    Bukkit.getWorld(e.getEntity().getWorld().getUID()).dropItemNaturally(e.getEntity().getLocation(), e.getDrops().get(i));
        }
    }
}
