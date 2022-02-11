package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.Random;
import java.util.UUID;

public class ItemDamageListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    public ItemDamageListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDamage(PlayerItemDamageEvent e) {
        if(!MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getPlayer())) return;

        UUID uuid = e.getPlayer().getUniqueId();

        if(new Random().nextDouble() < calcPercentage(uuid, "double-durability-damage") / 100)
            e.setDamage(e.getDamage() * 2);
    }
}
