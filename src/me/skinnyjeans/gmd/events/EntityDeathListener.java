package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class EntityDeathListener extends BaseListener {

    private final MainManager MAIN_MANAGER;

    private final HashMap<EntityType, Integer> MOBS = new HashMap<>();

    private int onPVPKill;

    public EntityDeathListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKill(EntityDeathEvent e) {
        if(e.getEntity().getKiller() != null) return;
        if(MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getEntity().getKiller())) return;
        UUID uuid = e.getEntity().getKiller().getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () -> {
            if (e.getEntity() instanceof Player) {
                MAIN_MANAGER.getPlayerManager().addAffinity(uuid, onPVPKill);
            } else if (MOBS.containsKey(e.getEntityType())) {
                MAIN_MANAGER.getPlayerManager().addAffinity(uuid, MOBS.get(e.getEntityType()));
            }
        });

        if (!(e.getEntity() instanceof Player) && !MAIN_MANAGER.getEntityManager().isEntityDisabled(e.getEntity())) {
            e.setDroppedExp((int) (e.getDroppedExp() * MAIN_MANAGER.getDifficultyManager().getDifficulty(uuid).getExperienceMultiplier() / 100.0));
            double DoubleLoot = MAIN_MANAGER.getDifficultyManager().getDifficulty(uuid).getDoubleLoot();
            if (DoubleLoot != 0.0 && new Random().nextDouble() < DoubleLoot / 100.0 && !e.getEntity().getCanPickupItems())
                for (int i = 0; i < e.getDrops().size(); i++)
                    Bukkit.getWorld(e.getEntity().getWorld().getUID()).dropItemNaturally(e.getEntity().getLocation(), e.getDrops().get(i));
        }
    }

    @Override
    public void reloadConfig() {
        FileConfiguration config = MAIN_MANAGER.getDataManager().getConfig();
        onPVPKill = config.getInt("pvp-kill", 20);

        MOBS.clear();
        for(Object key : config.getList("mobs-count-as-pve").toArray()) {
            String[] sep = key.toString().replaceAll("[{|}]","").split("=");
            if(EntityType.valueOf(sep[0]) != null) {
                int value = (sep.length > 1) ? Integer.parseInt(sep[1]) : config.getInt("pve-kill", 2);
                MOBS.put(EntityType.valueOf(sep[0]), value);
            }
        }

        if(MOBS.isEmpty() && onPVPKill == 0) {
            BlockBreakEvent.getHandlerList().unregister(MAIN_MANAGER.getPlugin());
        } else if (!HandlerList.getRegisteredListeners(MAIN_MANAGER.getPlugin()).contains(this)) {
            Bukkit.getPluginManager().registerEvents(this, MAIN_MANAGER.getPlugin());
        }
    }
}
