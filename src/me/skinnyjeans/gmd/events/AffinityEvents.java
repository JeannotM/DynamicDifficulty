package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.models.Difficulty;
import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AffinityEvents extends Affinity implements Listener {

    public AffinityEvents(Main ma) {
        super(ma);
        Bukkit.getScheduler().runTaskAsynchronously(ma, this::checkEvents);
    }

    protected void checkEvents() {
        int death = onDeath;
        int disabledCommands = 0;
        int ignoredMobs = 0;
        int durabilty = 0;
        int effects = 0;
        int food = 0;

        for(String diff : difficulties) {
            Difficulty d = difficultyList.get(diff);

            if(d.getKeepInventory())
                death++;
            if(d.getDoubleDurabilityDamageChance() != 0)
                durabilty++;
            if(!d.getEffectsOnAttack())
                effects++;
            if(d.getHungerDrain() != 100)
                food++;
            if(d.getIgnoredMobs() != null && !d.getIgnoredMobs().isEmpty())
                ignoredMobs++;
            if(d.getDisabledCommands() != null && !d.getDisabledCommands().isEmpty())
                disabledCommands++;
        }

        if(blocks.size() == 0)
            BlockBreakEvent.getHandlerList().unregister(m);
        if(death == 0 && onDeath == 0 && !preventAffinityLossOnSuicide)
            PlayerDeathEvent.getHandlerList().unregister(m);
        if(!customArmorSpawnChance && !config.getBoolean("plugin-support.no-changes-to-spawned-mobs"))
            SpawnerSpawnEvent.getHandlerList().unregister(m);
        if(durabilty == 0)
            PlayerItemDamageEvent.getHandlerList().unregister(m);
        if(effects == 0)
            EntityPotionEffectEvent.getHandlerList().unregister(m);
        if(food == 0)
            FoodLevelChangeEvent.getHandlerList().unregister(m);
        if(disabledCommands == 0)
            PlayerCommandPreprocessEvent.getHandlerList().unregister(m);
        if(ignoredMobs == 0) {
            EntityTargetLivingEntityEvent.getHandlerList().unregister(m);
        } else {
            emptyHitMobsList();
        }
    }
}
