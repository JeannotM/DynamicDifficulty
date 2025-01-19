package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class CreeperExplodeListener extends BaseListener {

    public boolean shouldDisable;
    public final static HashSet<EntityType> EXPLODABLES = new HashSet<>(Arrays.asList(EntityType.CREEPER, EntityType.FIREBALL, EntityType.WITHER_SKULL));

    public CreeperExplodeListener(MainManager mainManager) { MAIN_MANAGER = mainManager; }

    @EventHandler
    public void onCreeperExplosion(EntityExplodeEvent e) {
        if(shouldDisable) return;

        if(EXPLODABLES.contains(e.getEntityType())) {
            Player closestPlayer = null;
            double distance = 32.0;
            List<Player> onlinePlayers = e.getEntity().getWorld().getPlayers();
            Location entityLocation = e.getLocation();
            for(Player pl : onlinePlayers) {
                double playerDistance = entityLocation.distance(pl.getLocation());
                if(playerDistance < distance) {
                    distance = playerDistance;
                    closestPlayer = pl;
                }
            }

            if(MAIN_MANAGER.getPlayerManager().isPlayerValid(closestPlayer)
                && MAIN_MANAGER.getDifficultyManager().getDifficulty(closestPlayer).preventEntityExplosionBlockDamage) {
                e.blockList().clear();
            }
        }
    }

    @Override
    public void reloadConfig() {
        shouldDisable = true;

        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties())
            if (difficulty.preventEntityExplosionBlockDamage) {
                shouldDisable = false;
                break;
            }
    }
}
