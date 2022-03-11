package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;

public class BlockMinedListener extends BaseListener {

    private final MainManager MAIN_MANAGER;
    private final HashMap<Material, Integer> BLOCKS = new HashMap<>();

    private boolean silkTouchAllowed;

    public BlockMinedListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;

        reloadConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMined(BlockBreakEvent e) {
        if(!MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getPlayer())) return;

        Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () -> {
            if(BLOCKS.containsKey(e.getBlock().getType()))
                if(!e.getPlayer().getItemOnCursor().containsEnchantment(Enchantment.SILK_TOUCH) || silkTouchAllowed)
                    MAIN_MANAGER.getPlayerManager().addAffinity(e.getPlayer().getUniqueId(), BLOCKS.get(e.getBlock().getType()));
        });
    }

    @Override
    public void reloadConfig() {
        BLOCKS.clear();

        silkTouchAllowed = MAIN_MANAGER.getDataManager().getConfig().getBoolean("silk-touch-allowed", false);
        ConfigurationSection config = MAIN_MANAGER.getDataManager().getConfig().getConfigurationSection("blocks");

        int blockMined = MAIN_MANAGER.getDataManager().getConfig().getInt("block-mined", 2);

        for(String key : config.getKeys(false))
           BLOCKS.put(Material.valueOf(key), config.getInt(key, blockMined));

        if(BLOCKS.isEmpty()) {
            BlockBreakEvent.getHandlerList().unregister(MAIN_MANAGER.getPlugin());
        } else if (HandlerList.getRegisteredListeners(MAIN_MANAGER.getPlugin()).contains(this)) {
            Bukkit.getPluginManager().registerEvents(this, MAIN_MANAGER.getPlugin());
        }
    }
}
