package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandListener extends BaseListener {

    private String commandNotAllowed;
    private boolean shouldDisable;

    public CommandListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler
    public void beforeCommand(PlayerCommandPreprocessEvent e) {
        if(shouldDisable) return;
        Player p = e.getPlayer();
        if(p.isOp() || !MAIN_MANAGER.getPlayerManager().isPlayerValid(p)) return;
        List<String> list = MAIN_MANAGER.getDifficultyManager().getDifficulty(p).disabledCommands;
        StringBuilder cmd = new StringBuilder();
        String[] args = e.getMessage().replace("/","").toLowerCase().split(" ");
        if(list.size() != 0)
            for(String arg : args) {
                if(cmd.length() != 0)
                    cmd.append(" ");
                cmd.append(arg);
                if(list.contains(cmd.toString())) {
                    e.setCancelled(true);
                    if(commandNotAllowed.length() != 0) e.getPlayer().sendMessage(commandNotAllowed);
                    return;
                }
            }
    }

    @Override
    public void reloadConfig() {
        shouldDisable = true;
        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties())
            if (difficulty.disabledCommands.size() != 0) {
                shouldDisable = false;
                break;
            }

        commandNotAllowed = MAIN_MANAGER.getDataManager().getLanguageString("in-game.command-not-allowed", false);
    }
}
