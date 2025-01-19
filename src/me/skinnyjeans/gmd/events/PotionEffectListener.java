package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashSet;

public class PotionEffectListener extends BaseListener {

    private final HashSet<PotionEffectType> EFFECTS = new HashSet<>(Arrays.asList(PotionEffectType.SLOWNESS, PotionEffectType.NAUSEA,
            PotionEffectType.WITHER,PotionEffectType.POISON,PotionEffectType.BLINDNESS, PotionEffectType.WEAKNESS,PotionEffectType.HUNGER));
    private static final HashSet<EntityPotionEffectEvent.Cause> EFFECT_CAUSES = new HashSet<>(Arrays.asList(EntityPotionEffectEvent.Cause.ATTACK, EntityPotionEffectEvent.Cause.ARROW, EntityPotionEffectEvent.Cause.POTION_SPLASH));

    private boolean shouldDisable;

    public PotionEffectListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent e) {
        if(shouldDisable) return;
        if(MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getEntity()))
            if(EFFECT_CAUSES.contains(e.getCause()) && EFFECTS.contains(e.getModifiedType()))
                if(!MAIN_MANAGER.getDifficultyManager().getDifficulty((Player) e.getEntity()).effectsWhenAttacked)
                    e.setCancelled(true);
    }

    @Override
    public void reloadConfig() {
        shouldDisable = true;
        for(Difficulty difficulty : MAIN_MANAGER.getDifficultyManager().getDifficulties() )
            if (!difficulty.effectsWhenAttacked) {
                shouldDisable = false;
                break;
            }
    }
}
