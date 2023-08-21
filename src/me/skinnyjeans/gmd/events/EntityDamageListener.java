package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    private boolean shouldDisable;

    public EntityDamageListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (shouldDisable) return;
        if (MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getEntity())) {
            Player player = (Player) e.getEntity();
            if (e.getCause() == EntityDamageEvent.DamageCause.STARVATION)
                if(player.getHealth() - e.getFinalDamage() < MAIN_MANAGER.getDifficultyManager()
                        .getDifficulty(player.getUniqueId()).minimumStarvationHealth)
                    e.setCancelled(true);
        }
    }

    @Override
    public void reloadConfig() {
        shouldDisable = true;

        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties())
            if (difficulty.minimumStarvationHealth != 0) {
                shouldDisable = false;
                break;
            }
    }
}
