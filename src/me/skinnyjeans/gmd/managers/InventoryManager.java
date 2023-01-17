package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.models.ArmorTypes;
import me.skinnyjeans.gmd.models.Difficulty;
import me.skinnyjeans.gmd.models.EquipmentItems;
import me.skinnyjeans.gmd.models.Minecrafter;
import me.skinnyjeans.gmd.utils.Formatter;
import me.skinnyjeans.gmd.utils.StaticInfo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class InventoryManager {

    private final MainManager MAIN_MANAGER;

    private Inventory difficultyInventory;
    private HashMap<String, Inventory> difficultyInventories = new HashMap<String, Inventory>();
    private Inventory baseInventory;
    private Inventory basePlayerInventory;
    private String affinity, minAffinity, maxAffinity;

    private final HashMap<Material, String> MATERIAL_NAMES = new HashMap<Material, String>(){{
        put(Material.GOLD_INGOT, "Next Page >");
        put(Material.IRON_INGOT, "< Previous Page");
        put(Material.RED_WOOL, "Reset Affinity");
        put(Material.IRON_CHESTPLATE, "Current Page: %number%");
        put(Material.PINK_WOOL, "-100");
        put(Material.MAGENTA_WOOL, "-10");
        put(Material.PURPLE_WOOL, "-1");
        put(Material.BLUE_WOOL, "+1");
        put(Material.CYAN_WOOL, "+10");
        put(Material.LIGHT_BLUE_WOOL, "+100");
    }};

    private final HashMap<Integer, Material> INVENTORY_SLOTS = new HashMap<Integer, Material>(){{
        put(1, Material.PINK_WOOL);
        put(2, Material.MAGENTA_WOOL);
        put(3, Material.PURPLE_WOOL);
        put(5, Material.BLUE_WOOL);
        put(6, Material.CYAN_WOOL);
        put(7, Material.LIGHT_BLUE_WOOL);
    }};

    public InventoryManager(MainManager mainManager) { MAIN_MANAGER = mainManager; }

    public void createBaseInventory() {
        Inventory inventory = Bukkit.createInventory(null, 54, StaticInfo.PLAYERS_INVENTORY);
        ItemStack prevPage = createItem(Material.IRON_INGOT);
        ItemStack currentPage = createItem(Material.IRON_CHESTPLATE);
        ItemStack nextPage = createItem(Material.GOLD_INGOT);

        inventory.setItem(3, prevPage);
        inventory.setItem(4, currentPage);
        inventory.setItem(5, nextPage);

        baseInventory = inventory;
    }

    public void createBasePlayerInventory() {
        Inventory inventory = Bukkit.createInventory(null, 36, StaticInfo.INDIVIDUAL_PLAYER_INVENTORY);

        for(int i = 9; i < 36; i++)
            if(INVENTORY_SLOTS.containsKey(i % 9))
                inventory.setItem(i, createItem(INVENTORY_SLOTS.get(i % 9)));

        inventory.setItem(3, createItem(Material.IRON_INGOT));
        inventory.setItem(5, createItem(Material.RED_WOOL));

        basePlayerInventory = inventory;
    }

    public void createDifficultiesInventory() {
        difficultyInventory = Bukkit.createInventory(null, 54, StaticInfo.DIFFICULTIES_INVENTORY);
        int i = 0;

        List<Difficulty> difficulties = MAIN_MANAGER.getDifficultyManager().getDifficulties();
        for(Difficulty difficulty : difficulties) {
            if (i >= 53) break;
            difficultyInventory.setItem(i++, createDifficultyPlayerHead("&r" + difficulty.getDifficultyName(), difficulty.getAffinity()));
            Inventory inventory = Bukkit.createInventory(null, 36, StaticInfo.DIFFICULTY_INVENTORY);
            inventory.setItem(0,  createDifficultyPlayerHead(MAIN_MANAGER.getDataManager().getLanguageString("command.player-gui.previous-page")));
            inventory.setItem(4,  createDifficultyPlayerHead("Name", difficulty.getDifficultyName()));
            inventory.setItem(9,  createDifficultyPlayerHead("Prefix", difficulty.getPrefix()));
            inventory.setItem(10, createDifficultyPlayerHead("Affinity Required", difficulty.getAffinity()));
            inventory.setItem(11, createDifficultyPlayerHead("Damage Done By Mobs", difficulty.getDamageByMobs()));
            inventory.setItem(12, createDifficultyPlayerHead("Damage Done On Mobs", difficulty.getDamageOnMobs()));
            inventory.setItem(13, createDifficultyPlayerHead("Damage Done On Tamed", difficulty.getDamageOnTamed()));
            inventory.setItem(14, createDifficultyPlayerHead("Experience Multiplier", difficulty.getExperienceMultiplier()));
            inventory.setItem(15, createDifficultyPlayerHead("Hunger Drain", difficulty.getHungerDrain()));
            inventory.setItem(16, createDifficultyPlayerHead("Double Loot Chance", difficulty.getDoubleLoot()));
            inventory.setItem(17, createDifficultyPlayerHead("Max Enchants", difficulty.getMaxEnchants()));
            inventory.setItem(18, createDifficultyPlayerHead("Max Enchant Level", difficulty.getMaxEnchantLevel()));
            inventory.setItem(19, createDifficultyPlayerHead("Damage Done On Ranged", difficulty.getDamageByRangedMobs()));
            inventory.setItem(20, createDifficultyPlayerHead("Double Durability Damage Chance", difficulty.getDoubleDurabilityDamageChance()));
            inventory.setItem(21, createDifficultyPlayerHead("Armor Damage Multiplier", difficulty.getArmorDamageMultiplier().values()));
            inventory.setItem(22, createDifficultyPlayerHead("PVP Allowed", difficulty.getAllowPVP()));
            inventory.setItem(23, createDifficultyPlayerHead("Keep Inventory", difficulty.getKeepInventory()));
            inventory.setItem(24, createDifficultyPlayerHead("Health Regen", difficulty.getAllowHealthRegen()));
            inventory.setItem(25, createDifficultyPlayerHead("Effects When Attacked", difficulty.getEffectsOnAttack()));
            inventory.setItem(26, createDifficultyPlayerHead("Armor Drop Chance", difficulty.getArmorDropChance()));
            inventory.setItem(27, createDifficultyPlayerHead("Armor Enchant Chance", difficulty.getChanceToEnchant()));
            inventory.setItem(28, createDifficultyPlayerHead("Armor Chance For Mobs", difficulty.getChanceToHaveArmor()));
            inventory.setItem(30, createDifficultyPlayerHead("Weapon Drop Chance", difficulty.getWeaponDropChance()));
            inventory.setItem(31, createDifficultyPlayerHead("Ignored Mobs", difficulty.getIgnoredMobs()));
            inventory.setItem(32, createDifficultyPlayerHead("Disabled Commands", difficulty.getDisabledCommands()));
            inventory.setItem(33, createDifficultyPlayerHead("Enchant Chances", difficulty.getEnchantChances()));
            difficultyInventories.put(difficulty.getDifficultyName(), inventory);
        }
    }

    public ItemStack createDifficultyPlayerHead(String name, Object ...values) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();

        if(meta == null) return item;
        meta.setDisplayName(Formatter.format("&f&l" + name));
        meta.setLore(Formatter.list(values));
        item.setItemMeta(meta);
        return item;
    }

    public void openDifficultyInventory(Player player, String difficulty) {
        if (difficultyInventories.containsKey(difficulty))
            player.openInventory(difficultyInventories.get(difficulty));
    }
    public void openBaseDifficultyInventory(Player player) { player.openInventory(difficultyInventory); }
    public void openInventory(Player player, int page) {
        Bukkit.getScheduler().runTaskAsynchronously(MAIN_MANAGER.getPlugin(), () -> {
            Inventory inventory = baseInventory;
            Minecrafter[] players = MAIN_MANAGER.getPlayerManager().getPlayerList().values().toArray(new Minecrafter[0]);
            int iterator = (page - 1) * 45;
            if(players.length - iterator <= 0) return;

            ItemMeta itemMeta = inventory.getItem(4).getItemMeta();
            itemMeta.setDisplayName(itemMeta.getDisplayName().replace("%number%", String.valueOf(page)));
            inventory.getItem(4).setItemMeta(itemMeta);
            int iteratorLimit = (players.length % 45);

            for(int i = 0; i < iteratorLimit; i++)
                inventory.setItem(i + 9, createPlayerHead(players[iterator + i]));

            Bukkit.getScheduler().runTask(MAIN_MANAGER.getPlugin(), () ->
                    player.openInventory(inventory));
        });
    }

    public void openPlayerInventory(Player player, UUID uuid) {
        Inventory inventory = basePlayerInventory;
        inventory.setItem(4, createPlayerHead(uuid));
        player.openInventory(inventory);
    }

    public void updatePlayerInventory(Player player, UUID uuid) {
        player.getOpenInventory().setItem(4, createPlayerHead(uuid));
    }

    public ItemStack createPlayerHead(UUID uuid) { return createPlayerHead(MAIN_MANAGER.getPlayerManager().getPlayerAffinity(uuid)); }
    public ItemStack createPlayerHead(Minecrafter data) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(data.getName());
        try { meta.setOwningPlayer(Bukkit.getOfflinePlayer(data.getUUID()));
        } catch (Exception ignored) { }
        meta.setLore(Arrays.asList(
            data.getUUID().toString(),
            affinity.replace("%number%", String.valueOf(data.getAffinity())),
            minAffinity.replace("%number%", String.valueOf(data.getMinAffinity())),
            maxAffinity.replace("%number%", String.valueOf(data.getMaxAffinity()))
        ));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createItem(Material type) {
        ItemStack item = new ItemStack(type);
        if(MATERIAL_NAMES.containsKey(type)) {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(MATERIAL_NAMES.get(type));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void reloadConfig() {
        MATERIAL_NAMES.put(Material.GOLD_INGOT, MAIN_MANAGER.getDataManager().getLanguageString("command.player-gui.next-page"));
        MATERIAL_NAMES.put(Material.RED_WOOL, MAIN_MANAGER.getDataManager().getLanguageString("command.player-gui.reset-values"));
        MATERIAL_NAMES.put(Material.IRON_INGOT, MAIN_MANAGER.getDataManager().getLanguageString("command.player-gui.previous-page"));
        MATERIAL_NAMES.put(Material.IRON_CHESTPLATE, MAIN_MANAGER.getDataManager().getLanguageString("command.player-gui.current-page"));

        affinity = MAIN_MANAGER.getDataManager().getLanguageString("command.player-gui.affinity");
        minAffinity = MAIN_MANAGER.getDataManager().getLanguageString("command.player-gui.min-affinity");
        maxAffinity = MAIN_MANAGER.getDataManager().getLanguageString("command.player-gui.max-affinity");

        createBaseInventory();
        createBasePlayerInventory();
        createDifficultiesInventory();
    }
}
