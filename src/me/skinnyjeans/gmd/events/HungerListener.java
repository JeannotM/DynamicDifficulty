package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
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

        if(e.getEntity().getFoodLevel() > e.getFoodLevel())
            if(new Random().nextDouble() > MAIN_MANAGER.getDifficultyManager().getDifficulty(e.getEntity().getUniqueId()).getHungerDrain() / 100.0)
                e.setCancelled(true);
    }

    @Override
    public void reloadConfig() {
        boolean shouldDisable = true;
        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties() )
            if (difficulty.getHungerDrain() < 100) {
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
