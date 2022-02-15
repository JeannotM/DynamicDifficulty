package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class EntityDeathListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    private int onPVPKill;

    public EntityDeathListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKill(EntityDeathEvent e) {
        if(e.getEntity().getKiller() != null) return;
        if(MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getEntity().getKiller())) return;
        UUID uuid = e.getEntity().getKiller().getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(m, () -> {
            if (e.getEntity() instanceof Player) {
                addAmountOfAffinity(uuid, onPVPKill);
            } else if (mobsPVE.get(e.getEntityType().toString()) != null) {
                addAmountOfAffinity(uuid, mobsPVE.get(e.getEntityType().toString()));
                if(ignoreMobs.contains(e.getEntity().getEntityId()))
                    ignoreMobs.remove(ignoreMobs.indexOf(e.getEntity().getEntityId()));
            }
        });

        if (!(e.getEntity() instanceof Player) && !disabledMobs.contains(e.getEntityType().toString())) {
            e.setDroppedExp((int) (e.getDroppedExp() * calcPercentage(uuid, "experience-multiplier") / 100.0));
            double DoubleLoot = calcPercentage(uuid, "double-loot-chance");
            if (DoubleLoot != 0.0 && new Random().nextDouble() < DoubleLoot / 100.0 && !e.getEntity().getCanPickupItems())
                for (int i = 0; i < e.getDrops().size(); i++)
                    Bukkit.getWorld(e.getEntity().getWorld().getUID()).dropItemNaturally(e.getEntity().getLocation(), e.getDrops().get(i));
        }
    }

    @Override
    public void reloadConfig() {

    }
}
