package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.EnumSet;

public class PlayerDeathListener extends BaseListener {

    private final MainManager MAIN_MANAGER;
    private final EnumSet<EntityDamageEvent.DamageCause> COUNTS_AS_SUICIDE;
    private boolean preventAffinityLossOnSuicide;
    private int onDeath;

    public PlayerDeathListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
        COUNTS_AS_SUICIDE = EnumSet.of(EntityDamageEvent.DamageCause.FALL, EntityDamageEvent.DamageCause.SUFFOCATION, EntityDamageEvent.DamageCause.SUICIDE);

        reloadConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        if(!MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getEntity())) return;

        if(MAIN_MANAGER.getDifficultyManager().getDifficulty(e.getEntity().getUniqueId()).getKeepInventory()) {
            e.setKeepInventory(true);
            e.setKeepLevel(true);
            e.setNewTotalExp(0);
            e.getDrops().clear();
        }

        Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () -> {
            if(preventAffinityLossOnSuicide)
                if(COUNTS_AS_SUICIDE.contains(e.getEntity().getLastDamageCause()))
                    return;
            MAIN_MANAGER.getPlayerManager().addAffinity(e.getEntity().getUniqueId(), onDeath);
        });
    }

    @Override
    public void reloadConfig() {
        preventAffinityLossOnSuicide = MAIN_MANAGER.getDataManager().getConfig().getBoolean("prevent-affinity-loss-on-suicide", true);
        onDeath = MAIN_MANAGER.getDataManager().getConfig().getInt("death", -100);
    }
}
