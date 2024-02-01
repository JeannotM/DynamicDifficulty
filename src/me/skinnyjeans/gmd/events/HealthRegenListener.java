package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.EnumSet;

public class HealthRegenListener extends BaseListener {

    private final MainManager MAIN_MANAGER;
    private final EnumSet<EntityRegainHealthEvent.RegainReason> CANCEL_REGEN = EnumSet.of(
            EntityRegainHealthEvent.RegainReason.REGEN, EntityRegainHealthEvent.RegainReason.SATIATED);

    private boolean shouldDisable;

    public HealthRegenListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler
    public void onHealthRegen(EntityRegainHealthEvent e) {
        if(shouldDisable) return;
        if(MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getEntity()))
            if(CANCEL_REGEN.contains(e.getRegainReason()))
                e.setCancelled(true);
    }

    @Override
    public void reloadConfig() {
        shouldDisable = true;
        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties() )
            if (!difficulty.allowHealthRegen) {
                shouldDisable = false;
                break;
            }
    }
}
