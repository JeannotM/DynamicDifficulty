package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    public PlayerJoinListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getPlayer())) {
            UUID uuid = e.getPlayer().getUniqueId();
            Difficulty difficulty = MAIN_MANAGER.getDifficultyManager().getDifficulty(uuid);
            Runnable afterJoinTask = () ->
                    MAIN_MANAGER.getCommandManager()
                            .dispatchCommandsIfOnline(uuid, difficulty.getCommandsOnJoin());

            Bukkit.getScheduler().runTaskLater(MAIN_MANAGER.getPlugin(), afterJoinTask, 1L);
        }
    }
}
