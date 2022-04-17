package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.Main;
import org.bukkit.inventory.Inventory;

public class MainManager {

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
        INVENTORY_MANAGER = new InventoryManager(this);
        AFFINITY_MANAGER = new AffinityManager(this);
        COMMAND_MANAGER = new CommandManager(this);
        ENTITY_MANAGER = new EntityManager(this);
        EVENT_MANAGER = new EventManager(this);

        reloadConfig();
    }

    public void reloadConfig() {
        DIFFICULTY_MANAGER.reloadConfig();
        INVENTORY_MANAGER.reloadConfig();
        AFFINITY_MANAGER.reloadConfig();
        COMMAND_MANAGER.reloadConfig();
        ENTITY_MANAGER.reloadConfig();
        PLAYER_MANAGER.reloadConfig();
        EVENT_MANAGER.reloadConfig();
        DATA_MANAGER.reloadConfig();
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
