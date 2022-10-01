package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;

public class BlockMinedListener extends BaseListener {

    private final MainManager MAIN_MANAGER;
    private final HashMap<Material, Integer> BLOCKS = new HashMap<>();

    private boolean silkTouchAllowed;

    public BlockMinedListener(MainManager mainManager) { MAIN_MANAGER = mainManager; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMined(BlockBreakEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () -> {
            if(BLOCKS.size() == 0) return;
            if(!MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getPlayer())) return;

            if(BLOCKS.containsKey(e.getBlock().getType()))
                if(!e.getPlayer().getItemOnCursor().containsEnchantment(Enchantment.SILK_TOUCH) || silkTouchAllowed)
                    MAIN_MANAGER.getPlayerManager().addAffinity(e.getPlayer().getUniqueId(), BLOCKS.get(e.getBlock().getType()));
        });
    }

    @Override
    public void reloadConfig() {
        BLOCKS.clear();
        silkTouchAllowed = MAIN_MANAGER.getDataManager().getConfig().getBoolean("silk-touch-allowed", false);
        ConfigurationSection config = MAIN_MANAGER.getDataManager().getConfig();

        int blockMined = config.getInt("block-mined", 2);

        for(Object key : config.getList("blocks").toArray())
            try {
                String[] sep = key.toString().replaceAll("[{|}]","").split("=");
                int value = (sep.length > 1) ? Integer.parseInt(sep[1]) : blockMined;
                BLOCKS.put(Material.valueOf(sep[0]), value);
            } catch (Exception ignored) { }
    }
}
