package me.skinnyjeans.gmd.events;

import me.skinnyjeans.gmd.managers.MainManager;
import me.skinnyjeans.gmd.models.BaseListener;
import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InventoryListener extends BaseListener {

    private final MainManager MAIN_MANAGER;
    private final EnumSet<Material> ITEMS_WITH_VALUES = EnumSet.of(Material.PINK_WOOL, Material.MAGENTA_WOOL, Material.PURPLE_WOOL,
    Material.BLUE_WOOL,Material.CYAN_WOOL,Material.LIGHT_BLUE_WOOL);

    public InventoryListener(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getCurrentItem() == null) return;
        if(e.getCurrentItem().getItemMeta() == null) return;

        if (e.getView().getTitle().equals("DynamicDifficulty - Players")) {
            Player whoClicked = (Player) e.getWhoClicked();
            if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                UUID uuid = UUID.fromString(e.getCurrentItem().getItemMeta().getLore().get(0));
                MAIN_MANAGER.getInventoryManager().openPlayerInventory(whoClicked, uuid);
            } else if (e.getCurrentItem().getType() == Material.IRON_INGOT) {
                int page = Integer.parseInt(whoClicked.getOpenInventory().getItem(4).getItemMeta()
                        .getDisplayName().replace("[^0-9-]+", ""));

                if(page > 1) MAIN_MANAGER.getInventoryManager().openInventory(whoClicked, page - 1);
            } else if (e.getCurrentItem().getType() == Material.GOLD_INGOT) {
                int page = Integer.parseInt(whoClicked.getOpenInventory().getItem(4).getItemMeta()
                        .getDisplayName().replace("[^0-9-]+", ""));

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

    //    public void openPlayersInventory(Player user, int page) {
//        Inventory tmp = inventorySettings.get("player");
//        if(Bukkit.getOnlinePlayers().size() < 45) {
//            int i = 0;
//            for(Player player : Bukkit.getOnlinePlayers()) {
//                UUID uuid = player.getUniqueId();
//                ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
//                setPlayerHead(skull, player);
//                setItemStackName(skull, player.getName());
//                String c1 = ChatColor.BOLD+""+ChatColor.DARK_GREEN;
//                String c2 = ChatColor.BOLD+""+ChatColor.GREEN;
//                Minecrafter pl = playerList.get(uuid);
//                setLore(skull, new ArrayList<>(Arrays.asList(c1+"Affinity: "+c2+pl.getAffinity(),c1+"Min Affinity: "+c2+pl.getMinAffinity(),c1+"Max Affinity: "+c2+pl.getMaxAffinity())));
//                tmp.setItem(i++, skull);
//            }
//        } else {
//            int curr = page * 45;
//            Bukkit.getConsoleSender().sendMessage("page: "+page + " curr: "+curr);
//            Player[] pl = Bukkit.getOnlinePlayers().toArray(new Player[0]);
//            ItemStack goldIngot = new ItemStack(Material.GOLD_INGOT);
//            setLore(goldIngot, new ArrayList<>(Collections.singletonList(String.valueOf(page > 0 ? page - 1 : 0))));
//            setItemStackName(goldIngot, ChatColor.AQUA+""+ChatColor.BOLD+"Previous page");
//            ItemStack chestPlate = new ItemStack(Material.IRON_CHESTPLATE);
//            setLore(chestPlate, new ArrayList<>(Collections.singletonList(String.valueOf(page))));
//            setItemStackName(chestPlate, ChatColor.AQUA+""+ChatColor.BOLD+"Current page");
//            ItemStack ironIngot = new ItemStack(Material.IRON_INGOT);
//            setLore(ironIngot, new ArrayList<>(Collections.singletonList(String.valueOf(page + 1))));
//            setItemStackName(ironIngot, ChatColor.AQUA+""+ChatColor.BOLD+"Next page");
//            tmp.setItem(3, goldIngot);
//            tmp.setItem(4, chestPlate);
//            tmp.setItem(5, ironIngot);
//            for(int i=0;i<45;i++) {
//                if(i + curr < pl.length) {
//                    String name = pl[i + curr].getName();
//                    UUID uuid = pl[i + curr].getUniqueId();
//                    ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
//                    setItemStackName(skull, name);
//                    setPlayerHead(skull, Bukkit.getServer().getPlayer(name));
//                    String c1 = ChatColor.BOLD+""+ChatColor.DARK_GREEN;
//                    String c2 = ChatColor.BOLD+""+ChatColor.GREEN;
//                    Minecrafter p = playerList.get(uuid);
//                    setLore(skull, new ArrayList<>(Arrays.asList(c1+"Affinity: "+c2+p.getAffinity(),c1+"Min Affinity: "+c2+p.getMinAffinity(),c1+"Max Affinity: "+c2+p.getMaxAffinity())));
//                    tmp.setItem(i+9, skull);
//                } else {
//                    break;
//                }
//            }
//        }
//        user.openInventory(tmp);
//    }
//
//    public void createIndividualPlayerInventories() {
//        Inventory inv = Bukkit.createInventory(null, 54, "DynamicDifficulty - Players");
//        inventorySettings.put("player", inv);
//
//        inv = Bukkit.createInventory(null, 27, "DynamicDifficulty - Individual Player");
//        List<String> settings = new ArrayList<>(Arrays.asList("Affinity","Min Affinity","Max Affinity"));
//        List<String> changeSettings = new ArrayList<>(Arrays.asList("","-100","-10","-1", "", "+1", "+10", "+100", "Default"));
//        List<String> woolColors = new ArrayList<>(Arrays.asList("LIME", "PINK", "MAGENTA", "PURPLE", "", "BLUE", "CYAN", "LIGHT_BLUE", "RED"));
//        for(int i=0;i<27;i++) {
//            if(i % 9 != 4) {
//                ItemStack wool;
//                if (i % 9 == 0) {
//                    wool = new ItemStack(Material.LIME_WOOL, 1);
//                    int tmp = 0;
//                    if(i != 0) { tmp = i / 9; }
//                    setItemStackName(wool, settings.get(tmp));
//                } else {
//                    if(woolColors.get((i % 9)).equals("")) continue;
//                    wool = new ItemStack(Material.getMaterial(woolColors.get((i % 9)) + "_WOOL"), 1);
//                    setItemStackName(wool, changeSettings.get(i % 9));
//                }
//                inv.setItem(i, wool);
//            }
//        }
//        inventorySettings.put("iplayer", inv);
//    }
//
//    public void setItemStackName(ItemStack renamed, String customName) {
//        ItemMeta renamedMeta = renamed.getItemMeta();
//        renamedMeta.setDisplayName(customName);
//        renamed.setItemMeta(renamedMeta);
//    }
//
//    public void setPlayerHead(ItemStack skull, Player name) {
//        SkullMeta meta = (SkullMeta) skull.getItemMeta();
//        meta.setOwningPlayer(name);
//    }
//
//    public void setLore(ItemStack item, List<String> loreSet) {
//        ItemMeta meta = item.getItemMeta();
//        meta.setLore(loreSet);
//        item.setItemMeta(meta);
//    }

//    @EventHandler
//    public void onInventoryClick(InventoryClickEvent e) {
//        if (e.getCurrentItem() == null) return;
//        if(!e.getView().getTitle().contains("DynamicDifficulty")) return;
//        if(e.getCurrentItem().getItemMeta() == null) return;
//
//        if(e.getView().getTitle().equals("DynamicDifficulty - Players")) {
//            ItemStack item = e.getCurrentItem();
//            e.getWhoClicked().closeInventory();
//            if (e.getSlot() == 5 && item.getType().toString().equalsIgnoreCase("IRON_INGOT")) {
//                openPlayersInventory((Player)e.getWhoClicked(), Integer.parseInt(item.getItemMeta().getLore().get(0)));
//            } else if (e.getSlot() == 3 && item.getType().toString().equalsIgnoreCase("GOLD_INGOT")) {
//                openPlayersInventory((Player)e.getWhoClicked(), Integer.parseInt(item.getItemMeta().getLore().get(0)));
//            } else if (e.getSlot() != 4) {
//                Inventory tmp = inventorySettings.get("iplayer");
//                tmp.setItem(13, item);
//                e.getWhoClicked().openInventory(tmp);
//            }
//        } else if (e.getView().getTitle().equals("DynamicDifficulty - Individual Player")) {
//            UUID uuid = Bukkit.getPlayer(e.getInventory().getItem(13).getItemMeta().getDisplayName()).getUniqueId();
//            ArrayList<String> allowed = new ArrayList<>(Arrays.asList("PINK_WOOL", "MAGENTA_WOOL", "PURPLE_WOOL", "LIME_WOOL", "BLUE_WOOL", "CYAN_WOOL", "LIGHT_BLUE_WOOL", "RED_WOOL"));
//            if(allowed.contains(e.getCurrentItem().getType().toString()) && e.getSlot() % 9 != 0 && e.getSlot() % 9 != 4) {
//                if (e.getCurrentItem().getType().toString().equals("RED_WOOL")) {
//                    Minecrafter pl = playerList.get(uuid);
//                    if(e.getSlot() / 9 < 1) { pl.setAffinity(startAffinity); }
//                    else if(e.getSlot() / 9 < 2) { pl.setMinAffinity(data.getConfig().getInt("starting-min-affinity", -1)); }
//                    else if(e.getSlot() / 9 < 3) { pl.setMaxAffinity(data.getConfig().getInt("starting-max-affinity", -1)); }
//                } else {
//                    int add = Integer.parseInt(e.getCurrentItem().getItemMeta().getDisplayName());
//                    if(e.getSlot() / 9 < 1) { addAmountOfAffinity(uuid, add); }
//                    else if(e.getSlot() / 9 < 2) { addAmountOfMinAffinity(uuid, add); }
//                    else if(e.getSlot() / 9 < 3) { addAmountOfMaxAffinity(uuid, (add * -1)); }
//                }
//                ItemStack item = e.getInventory().getItem(13);
//                String c1 = ChatColor.BOLD+""+ChatColor.DARK_GREEN;
//                String c2 = ChatColor.BOLD+""+ChatColor.GREEN;
//                Minecrafter p = playerList.get(uuid);
//                setLore(item, new ArrayList<>(Arrays.asList(c1+"Affinity: "+c2+p.getAffinity(),c1+"Min Affinity: "+c2+p.getMinAffinity(),c1+"Max Affinity: "+c2+p.getMaxAffinity())));
//                e.getInventory().setItem(13, item);
//            }
//        }
//        e.setCancelled(true);
//    }
}
