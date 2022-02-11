package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.Random;

public class HungerListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    public HungerListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHungerDrain(FoodLevelChangeEvent e) {
        if(!MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getEntity())) return;

        if(e.getEntity() instanceof Player)
            if(e.getEntity().getFoodLevel() > e.getFoodLevel())
                if(new Random().nextDouble() > (calcPercentage(e.getEntity().getUniqueId(), "hunger-drain-chance") / 100.0))
                    e.setCancelled(true);
    }
}
