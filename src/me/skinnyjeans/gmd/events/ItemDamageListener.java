package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.Random;

public class ItemDamageListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    private boolean shouldDisable;

    public ItemDamageListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        if(shouldDisable) return;
        if(MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getPlayer()))
            if(new Random().nextDouble() < MAIN_MANAGER.getDifficultyManager().getDifficulty(e.getPlayer().getUniqueId()).doubleDurabilityDamageChance)
                e.setDamage(e.getDamage() * 2);
    }

    @Override
    public void reloadConfig() {
        shouldDisable = true;
        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties())
            if (difficulty.doubleDurabilityDamageChance != 0.0) {
                shouldDisable = false;
                break;
            }
    }
}
