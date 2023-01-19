package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.hooks.Metrics;
import me.skinnyjeans.gmd.hooks.PlaceholderAPIExpansion;
import org.bukkit.Bukkit;

public class MainManager {

    private final TabCompleterManager TAB_COMPLETER_MANAGER;
    private final DifficultyManager DIFFICULTY_MANAGER;
    private final InventoryManager INVENTORY_MANAGER;
    private final AffinityManager AFFINITY_MANAGER;
    private final CommandManager COMMAND_MANAGER;
    private final EntityManager ENTITY_MANAGER;
    private final PlayerManager PLAYER_MANAGER;
    private final EventManager EVENT_MANAGER;
    private final DataManager DATA_MANAGER;
    private final Main PLUGIN;

    public MainManager(Main main) {
        PLUGIN = main;

        DATA_MANAGER = new DataManager(this);
        PLAYER_MANAGER = new PlayerManager(this);
        DIFFICULTY_MANAGER = new DifficultyManager(this);

        TAB_COMPLETER_MANAGER = new TabCompleterManager(this);
        INVENTORY_MANAGER = new InventoryManager(this);
        AFFINITY_MANAGER = new AffinityManager(this);
        COMMAND_MANAGER = new CommandManager(this);
        ENTITY_MANAGER = new EntityManager(this);
        EVENT_MANAGER = new EventManager(this);

        main.getCommand("affinity").setExecutor(COMMAND_MANAGER);
        main.getCommand("affinity").setTabCompleter(TAB_COMPLETER_MANAGER);

        reloadConfig();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            new PlaceholderAPIExpansion(this).register();
    }

    public void reloadConfig() {
        DATA_MANAGER.reloadConfig();
        PLAYER_MANAGER.reloadConfig();
        DIFFICULTY_MANAGER.reloadConfig();

        TAB_COMPLETER_MANAGER.reloadConfig();
        INVENTORY_MANAGER.reloadConfig();
        AFFINITY_MANAGER.reloadConfig();
        COMMAND_MANAGER.reloadConfig();
        ENTITY_MANAGER.reloadConfig();
        EVENT_MANAGER.reloadConfig();

        checkMetrics();
    }

    public void checkMetrics() {
        Metrics m = new Metrics(PLUGIN, 11417);
        m.addCustomChart(new Metrics.SimplePie("difficulty_type", () ->
            DATA_MANAGER.getConfig().getString("toggle-settings.difficulty-type", "player").toLowerCase()
        ));
        m.addCustomChart(new Metrics.SimplePie("save_type", () ->
            DATA_MANAGER.getConfig().getString("saving-data.type", "file").toLowerCase()
        ));
        m.addCustomChart(new Metrics.SimplePie("amount_of_difficulties", () ->
            String.valueOf(DATA_MANAGER.getConfig().getConfigurationSection("difficulty").getKeys(false).size())
        ));
        m.addCustomChart(new Metrics.SimplePie("custom_armor_and_item_spawn_chance", () ->
            DATA_MANAGER.getConfig().getString("toggle-settings.advanced.custom-enchants-on-mobs", "false").toLowerCase()
        ));
    }

    public DifficultyManager getDifficultyManager() { return DIFFICULTY_MANAGER; }
    public InventoryManager getInventoryManager() { return INVENTORY_MANAGER; }
    public AffinityManager getAffinityManager() { return AFFINITY_MANAGER; }
    public CommandManager getCommandManager() { return COMMAND_MANAGER; }
    public PlayerManager getPlayerManager() { return PLAYER_MANAGER; }
    public EntityManager getEntityManager() { return ENTITY_MANAGER; }
    public EventManager getEventManager() { return EVENT_MANAGER; }
    public DataManager getDataManager() { return DATA_MANAGER; }
    public Main getPlugin() { return PLUGIN; }
}
