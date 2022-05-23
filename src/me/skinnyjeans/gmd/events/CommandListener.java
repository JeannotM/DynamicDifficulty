package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    private String commandNotAllowed;
    private boolean shouldDisable;

    public CommandListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void beforeCommand(PlayerCommandPreprocessEvent e) {
        if(shouldDisable) return;
        Player p = e.getPlayer();
        if(p.isOp()) return;
        MAIN_MANAGER.getPlayerManager().isPlayerValid(p);

        List<String> list = MAIN_MANAGER.getDifficultyManager().getDifficulty(p.getUniqueId()).getDisabledCommands();
        StringBuilder cmd = new StringBuilder();
        String[] args = e.getMessage().replace("/","").split(" ");
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

        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties() )
            if (difficulty.getDisabledCommands().size() != 0) {
                shouldDisable = false;
                break;
            }

        commandNotAllowed = MAIN_MANAGER.getDataManager().getLanguageString("in-game.command-not-allowed", false);
    }
}
