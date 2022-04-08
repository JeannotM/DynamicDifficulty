package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;

public class PotionEffectListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    private final HashSet<PotionEffectType> EFFECTS;
    private final EnumSet<EntityPotionEffectEvent.Cause> EFFECT_CAUSES;

    public PotionEffectListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;

        EFFECTS = new HashSet<>(Arrays.asList(PotionEffectType.WITHER,PotionEffectType.POISON,PotionEffectType.BLINDNESS,
                PotionEffectType.WEAKNESS,PotionEffectType.SLOW,PotionEffectType.CONFUSION,PotionEffectType.HUNGER));

        EFFECT_CAUSES = EnumSet.of(EntityPotionEffectEvent.Cause.ATTACK, EntityPotionEffectEvent.Cause.ARROW, EntityPotionEffectEvent.Cause.POTION_SPLASH);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionEffect(EntityPotionEffectEvent e) {
        if(!MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getEntity())) return;

        if(EFFECT_CAUSES.contains(e.getCause()) && EFFECTS.contains(e.getModifiedType()))
            if(!MAIN_MANAGER.getDifficultyManager().getDifficulty(e.getEntity().getUniqueId()).getEffectsOnAttack())
                e.setCancelled(true);
    }

    @Override
    public void reloadConfig() {
        boolean shouldDisable = true;
        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties() )
            if (!difficulty.getEffectsOnAttack()) {
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
