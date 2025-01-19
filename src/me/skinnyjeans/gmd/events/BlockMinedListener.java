package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

public class BlockMinedListener extends BaseListener {

    private final HashMap<Material, Integer> BLOCKS = new HashMap<Material, Integer>();
    private final static HashMap<Material, Material> ORE_BLOCKS = new HashMap<Material, Material>() {{
        put(Material.COAL_ORE, Material.COAL);
        put(Material.LAPIS_ORE, Material.LAPIS_LAZULI);
        put(Material.DIAMOND_ORE, Material.DIAMOND);
        put(Material.EMERALD_ORE, Material.EMERALD);
        put(Material.REDSTONE_ORE, Material.REDSTONE);
        put(Material.NETHER_QUARTZ_ORE, Material.QUARTZ);
    }};

    private boolean silkTouchAllowed;

    public BlockMinedListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;

        try {
            // 1.16 20w11a
            ORE_BLOCKS.put(Material.valueOf("NETHER_GOLD_ORE"), Material.valueOf("GOLD_NUGGET"));

            // 1.17 21w10a
            ORE_BLOCKS.put(Material.valueOf("DEEPSLATE_COAL_ORE"), Material.valueOf("COAL"));
            ORE_BLOCKS.put(Material.valueOf("DEEPSLATE_LAPIS_ORE"), Material.valueOf("LAPIS_LAZULI"));
            ORE_BLOCKS.put(Material.valueOf("DEEPSLATE_DIAMOND_ORE"), Material.valueOf("DIAMOND"));
            ORE_BLOCKS.put(Material.valueOf("DEEPSLATE_EMERALD_ORE"), Material.valueOf("EMERALD"));
            ORE_BLOCKS.put(Material.valueOf("DEEPSLATE_REDSTONE_ORE"), Material.valueOf("REDSTONE"));
            ORE_BLOCKS.put(Material.valueOf("COPPER_ORE"), Material.valueOf("RAW_COPPER"));
            ORE_BLOCKS.put(Material.valueOf("DEEPSLATE_COPPER_ORE"), Material.valueOf("RAW_COPPER"));
        } catch (Exception ignored) { }
    }

    @EventHandler
    public void onMined(BlockBreakEvent e) {
        if(BLOCKS.isEmpty()) return;
        if(!MAIN_MANAGER.getPlayerManager().isPlayerValid(e.getPlayer())) return;
        final ItemStack tool = e.getPlayer().getInventory().getItemInMainHand();
        final boolean hasSilkTouch = tool.containsEnchantment(Enchantment.SILK_TOUCH);
        final Material type = e.getBlock().getType();

        Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () -> {
            if(BLOCKS.containsKey(type))
                if(silkTouchAllowed || !hasSilkTouch)
                    MAIN_MANAGER.getPlayerManager().addAffinity(e.getPlayer(), BLOCKS.get(type));
        });

        if (ORE_BLOCKS.containsKey(type) && e.getBlock().isPreferredTool(tool)) {
            if (! hasSilkTouch && new Random().nextDouble() < MAIN_MANAGER
                    .getDifficultyManager().getDifficulty(e.getPlayer()).doubleLootChance)
                e.getPlayer().getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(ORE_BLOCKS.get(type)));
        }
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
