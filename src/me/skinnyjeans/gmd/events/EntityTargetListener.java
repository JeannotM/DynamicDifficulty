package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EntityTargetListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    private boolean shouldDisable;

    public EntityTargetListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpot(EntityTargetLivingEntityEvent e) {
        if(shouldDisable) return;
        if(MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getTarget()))
            if(MAIN_MANAGER.getDifficultyManager().getDifficulty(e.getTarget().getUniqueId()).getIgnoredMobs().contains(e.getEntity().getType().toString()))
                if(!MAIN_MANAGER.getEntityManager().wasEntityAttacked(e.getEntity()))
                    e.setCancelled(true);
    }

    @Override
    public void reloadConfig() {
        shouldDisable = true;
        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties())
            if (difficulty.getIgnoredMobs().size() != 0) {
                shouldDisable = false;
                break;
            }
    }
}
