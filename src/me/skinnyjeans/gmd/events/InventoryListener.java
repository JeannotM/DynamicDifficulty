package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

public class InventoryListener extends BaseListener {

    private final MainManager MAIN_MANAGER;
    private final EnumSet<Material> ITEMS_WITH_VALUES = EnumSet.of(Material.PINK_WOOL, Material.MAGENTA_WOOL, Material.PURPLE_WOOL,
    Material.BLUE_WOOL,Material.CYAN_WOOL,Material.LIGHT_BLUE_WOOL);

    public InventoryListener(MainManager mainManager) { MAIN_MANAGER = mainManager; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getCurrentItem() == null) return;
        if(e.getCurrentItem().getItemMeta() == null) return;
        if(!e.getView().getTitle().contains("DynamicDifficulty")) return;

        if (e.getView().getTitle().equals("DynamicDifficulty - Players")) {
            Player whoClicked = (Player) e.getWhoClicked();
            if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                UUID uuid = UUID.fromString(e.getCurrentItem().getItemMeta().getLore().get(0));
                MAIN_MANAGER.getInventoryManager().openPlayerInventory(whoClicked, uuid);
            } else if (e.getCurrentItem().getType() == Material.IRON_INGOT) {
                int page = Integer.parseInt(whoClicked.getOpenInventory().getItem(4).getItemMeta()
                        .getDisplayName().replaceAll("[^0-9-]+", ""));

                if(page > 1) MAIN_MANAGER.getInventoryManager().openInventory(whoClicked, page - 1);
            } else if (e.getCurrentItem().getType() == Material.GOLD_INGOT) {
                int page = Integer.parseInt(whoClicked.getOpenInventory().getItem(4).getItemMeta()
                        .getDisplayName().replaceAll("[^0-9-]+", ""));

                MAIN_MANAGER.getInventoryManager().openInventory(whoClicked, page + 1);
            }
        } else if (e.getView().getTitle().equals("DynamicDifficulty - Individual Player")) {
            if(ITEMS_WITH_VALUES.contains(e.getCurrentItem().getType())) {
                int affinityType = e.getSlot() / 9;
                int value = Integer.parseInt(e.getCurrentItem().getItemMeta().getDisplayName());
                UUID uuid = UUID.fromString(e.getCurrentItem().getItemMeta().getLore().get(0));
                if(affinityType < 2) {
                    MAIN_MANAGER.getPlayerManager().addAffinity(uuid, value);
                } else if (affinityType < 3) {
                    MAIN_MANAGER.getPlayerManager().addMinAffinity(uuid, value);
                } else if (affinityType < 4) {
                    MAIN_MANAGER.getPlayerManager().addMaxAffinity(uuid, value);
                }
                MAIN_MANAGER.getInventoryManager().updatePlayerInventory((Player) e.getWhoClicked(), uuid);
            } else if (e.getCurrentItem().getType() == Material.IRON_INGOT) {
                Player whoClicked = (Player) e.getWhoClicked();
                MAIN_MANAGER.getInventoryManager().openInventory(whoClicked, 1);
            } else if (e.getCurrentItem().getType() == Material.RED_WOOL) {
                UUID uuid = UUID.fromString(e.getCurrentItem().getItemMeta().getLore().get(0));
                MAIN_MANAGER.getAffinityManager().resetAffinity(uuid);
            }
        }

        e.setCancelled(true);
    }
}
