package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    public CommandListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void beforeCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if(p.isOp()) return;
        MAIN_MANAGER.getPlayerManager().isPlayerValid(p);

        List<String> list = difficultyList.get(calcDifficulty(p.getUniqueId())).getDisabledCommands();
        StringBuilder cmd = new StringBuilder();
        String[] args = e.getMessage().replace("/","").split(" ");
        if(!list.isEmpty())
            for(String arg : args) {
                if(cmd.length() != 0)
                    cmd.append(" ");
                cmd.append(arg);
                if(list.contains(cmd.toString())) {
                    e.setCancelled(true);
                    if(data.getLang().isSet("in-game.command-not-allowed") && data.getLang().getString("in-game.command-not-allowed").length() != 0)
                        e.getPlayer().sendMessage(data.getLang().getString("in-game.command-not-allowed"));
                    return;
                }
            }
    }
}
