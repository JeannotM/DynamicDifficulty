package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.Random;

public class ItemDamageListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    public ItemDamageListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDamage(PlayerItemDamageEvent e) {
        if(!MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getPlayer())) return;

        if(new Random().nextDouble() < MAIN_MANAGER.getDifficultyManager().getDifficulty(e.getPlayer().getUniqueId()).getDoubleDurabilityDamageChance() / 100.0)
            e.setDamage(e.getDamage() * 2);
    }

    @Override
    public void reloadConfig() {
        boolean shouldDisable = true;
        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties() )
            if (difficulty.getDoubleDurabilityDamageChance() != 0) {
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
