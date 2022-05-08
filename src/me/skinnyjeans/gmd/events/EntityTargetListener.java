package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EntityTargetListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    public EntityTargetListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpot(EntityTargetLivingEntityEvent e) {
        if(MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getTarget()))
            if(MAIN_MANAGER.getDifficultyManager().getDifficulty(e.getTarget().getUniqueId()).getIgnoredMobs().contains(e.getEntity().getType().toString()))
                if(MAIN_MANAGER.getEntityManager().isEntityIgnored(e.getEntity()))
                    e.setCancelled(true);
    }

    @Override
    public void reloadConfig() {
        boolean shouldDisable = true;
        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties() )
            if (difficulty.getIgnoredMobs().size() != 0) {
                shouldDisable = false;
                break;
            }

        if(shouldDisable) {
            BlockBreakEvent.getHandlerList().unregister(MAIN_MANAGER.getPlugin());
        } else if (!HandlerList.getRegisteredListeners(MAIN_MANAGER.getPlugin()).contains(this)) {
            Bukkit.getPluginManager().registerEvents(this, MAIN_MANAGER.getPlugin());
        }
    }
}
