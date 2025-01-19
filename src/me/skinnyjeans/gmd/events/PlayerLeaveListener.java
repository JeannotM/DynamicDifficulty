package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerLeaveListener extends BaseListener {

    public PlayerLeaveListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if(!MAIN_MANAGER.getPlayerManager().isPlayerValidNoWorld(e.getPlayer())) return;

        UUID uuid = e.getPlayer().getUniqueId();
        if (uuid == null) {
            uuid = UUID.fromString( "00000000-0000-0000-0000-000000000000" );
        }
        
        MAIN_MANAGER.getDataManager().updatePlayer(uuid);
        MAIN_MANAGER.getPlayerManager().unloadPlayer(uuid);
    }
}
