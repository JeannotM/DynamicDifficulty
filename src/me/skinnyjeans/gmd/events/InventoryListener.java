package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.utils.StaticInfo;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InventoryListener extends BaseListener {

    private static final HashMap<Material, Integer> ITEMS_WITH_VALUES = new HashMap<Material, Integer>(){{
        put(Material.PINK_WOOL, -100);
        put(Material.MAGENTA_WOOL, -10);
        put(Material.PURPLE_WOOL, -1);
        put(Material.BLUE_WOOL, 1);
        put(Material.CYAN_WOOL, 10);
        put(Material.LIGHT_BLUE_WOOL, 100);
    }};

    public InventoryListener(MainManager mainManager) { MAIN_MANAGER = mainManager; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getCurrentItem() == null) return;
        if(e.getCurrentItem().getItemMeta() == null) return;

        String title = e.getView().getTitle();
        if(!title.contains("DynamicDifficulty")) return;

        if (title.equals("DynamicDifficulty - Players")) {
            Player whoClicked = (Player) e.getWhoClicked();
            if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                UUID uuid = UUID.fromString(e.getCurrentItem().getItemMeta().getLore().get(0));
                MAIN_MANAGER.getInventoryManager().openPlayerInventory(whoClicked, uuid);
            } else if (e.getCurrentItem().getType() == Material.IRON_INGOT) {
                int page = Integer.parseInt(getSlot(e, 4).getItemMeta()
                        .getDisplayName().replaceAll("[^0-9-]+", ""));

                if(page > 1) MAIN_MANAGER.getInventoryManager().openInventory(whoClicked, page - 1);
            } else if (e.getCurrentItem().getType() == Material.GOLD_INGOT) {
                int page = Integer.parseInt(getSlot(e, 4).getItemMeta()
                        .getDisplayName().replaceAll("[^0-9-]+", ""));

                MAIN_MANAGER.getInventoryManager().openInventory(whoClicked, page + 1);
            }
        } else if (title.equals(StaticInfo.INDIVIDUAL_PLAYER_INVENTORY)) {
            if(ITEMS_WITH_VALUES.containsKey(e.getCurrentItem().getType())) {
                int affinityType = e.getSlot() / 9;
                int value = ITEMS_WITH_VALUES.get(e.getCurrentItem().getType());
                UUID uuid = UUID.fromString(getSlot(e, 4).getItemMeta().getLore().get(0));
                if (affinityType < 2) {
                    if(MAIN_MANAGER.getCommandManager().hasAnyPermission(e.getWhoClicked(), "set", "other"))
                        MAIN_MANAGER.getPlayerManager().addAffinity(uuid, value);
                } else if (affinityType < 3) {
                    if(MAIN_MANAGER.getCommandManager().hasAnyPermission(e.getWhoClicked(), "setmin", "other"))
                        MAIN_MANAGER.getPlayerManager().addMinAffinity(uuid, value);
                } else if (affinityType < 4) {
                    if(MAIN_MANAGER.getCommandManager().hasAnyPermission(e.getWhoClicked(), "setmax", "other"))
                        MAIN_MANAGER.getPlayerManager().addMaxAffinity(uuid, value);
                }
                MAIN_MANAGER.getInventoryManager().updatePlayerInventory((Player) e.getWhoClicked(), uuid);
            } else if (e.getCurrentItem().getType() == Material.IRON_INGOT) {
                Player whoClicked = (Player) e.getWhoClicked();
                MAIN_MANAGER.getInventoryManager().openInventory(whoClicked, 1);
            } else if (e.getCurrentItem().getType() == Material.RED_WOOL) {
                UUID uuid = UUID.fromString(getSlot(e, 4).getItemMeta().getLore().get(0));
                if(MAIN_MANAGER.getCommandManager().hasAnyPermission(e.getWhoClicked(), "set", "other"))
                    MAIN_MANAGER.getAffinityManager().resetAffinity(uuid);
                MAIN_MANAGER.getInventoryManager().updatePlayerInventory((Player) e.getWhoClicked(), uuid);
            }
        } else if (title.equals(StaticInfo.DIFFICULTIES_INVENTORY)) {
            MAIN_MANAGER.getInventoryManager().openDifficultyInventory((Player) e.getWhoClicked(),
                    e.getCurrentItem().getItemMeta().getDisplayName());
        } else if (title.equals(StaticInfo.DIFFICULTY_INVENTORY) && e.getSlot() == 0) {
            MAIN_MANAGER.getInventoryManager().openBaseDifficultyInventory((Player) e.getWhoClicked());
        }
        e.setCancelled(true);
    }

    private ItemStack getSlot(InventoryClickEvent e, int slot) { return e.getWhoClicked().getOpenInventory().getItem(slot); }
}
