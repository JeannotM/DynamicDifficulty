package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerLeaveListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    private boolean unloadLeavingPlayers;

    public PlayerLeaveListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if(!MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getPlayer())) return;

        UUID uuid = e.getPlayer().getUniqueId();
        Minecrafter pl = playerList.get(uuid);
        SQL.updatePlayer(uuid.toString(), pl.getAffinity(), pl.getMaxAffinity(), pl.getMinAffinity());
        if(unloadLeavingPlayers) {
            playerList.remove(uuid);
            playersUUID.remove(e.getPlayer().getName());
        }
    }

    @Override
    public void reloadConfig() {
        unloadLeavingPlayers = MAIN_MANAGER.getDataManager().getConfig().getBoolean("plugin-support.unload-leaving-player", false);
    }
}
