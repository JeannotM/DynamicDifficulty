package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    public PlayerJoinListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) { MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getPlayer()); }
}
