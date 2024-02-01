package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.Random;

public class HungerListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    private boolean shouldDisable;

    public HungerListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler
    public void onHungerDrain(FoodLevelChangeEvent e) {
        if(shouldDisable) return;
        if(MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getEntity()))
            if(((Player) e.getEntity()).getFoodLevel() > e.getFoodLevel())
                if(new Random().nextDouble() > MAIN_MANAGER.getDifficultyManager().getDifficulty(e.getEntity().getUniqueId()).hungerDrainChance)
                    e.setCancelled(true);
    }

    @Override
    public void reloadConfig() {
        shouldDisable = true;
        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties() )
            if (difficulty.hungerDrainChance < 1.0) {
                shouldDisable = false;
                break;
            }
    }
}
