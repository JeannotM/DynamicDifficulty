package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerManager {

    private final MainManager MAIN_MANAGER;
    private final HashMap<UUID, Minecrafter> PLAYER_LIST = new HashMap<UUID, Minecrafter>();
    private final HashMap<String, UUID> NAME_TO_UUID = new HashMap<String, UUID>();
    private final static EnumSet<DifficultyTypes> DONT_ADD_PLAYER = EnumSet.of(DifficultyTypes.biome, DifficultyTypes.world);
    private DifficultySettings difficultySettings;

    private int maxAffinityGainPerMinute, maxAffinityLossPerMinute, intervalAffinity, serverMaxAffinity, serverMinAffinity;
    private DifficultyTypes difficultyType;
    private Minecrafter defaultData;

    public PlayerManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;

        Bukkit.getScheduler().runTaskTimerAsynchronously(MAIN_MANAGER.getPlugin(), () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) { return; }

            for (Minecrafter data : PLAYER_LIST.values()) {
                addAffinity(data.uuid, intervalAffinity);
                data.gainedThisMinute = 0;
            }
        }, 20 * 5, 20 * 60);

        Bukkit.getScheduler().runTaskTimerAsynchronously(MAIN_MANAGER.getPlugin(), () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) { return; }

            MAIN_MANAGER.getDataManager().saveData();
        }, 20 * 5, 20 * 60 * 5);
    }

    public int withinServerLimits(int value) {
        return Math.max(serverMinAffinity, Math.min(value, serverMaxAffinity)); }
    public int withinPlayerLimits(UUID uuid, int value) {
        Minecrafter data = getPlayerAffinity(uuid);
        value = withinServerLimits(value);

        if(data.minAffinity != -1) value = Math.max(data.minAffinity, value);
        if(data.maxAffinity != -1) value = Math.min(data.maxAffinity, value);

        return value;
    }

    public void resetAffinity(UUID uuid) {
        setMinAffinity(uuid, defaultData.minAffinity);
        setMaxAffinity(uuid, defaultData.maxAffinity);
        setAffinity(uuid, defaultData.affinity);
    }

    public void addPlayer(Entity player) {
        if(DifficultyTypes.player == difficultyType) {
            MAIN_MANAGER.getDataManager().getAffinityValues(player.getUniqueId(), playerData -> {
                NAME_TO_UUID.put(player.getName(), player.getUniqueId());
                if (playerData != null) {
                    PLAYER_LIST.put(player.getUniqueId(), playerData);
                    MAIN_MANAGER.getDifficultyManager().calculateDifficulty(player.getUniqueId());
                } else {
                    playerData = defaultData.clone();
                    playerData.name = player.getName();
                    playerData.uuid = player.getUniqueId();
                    PLAYER_LIST.put(player.getUniqueId(), playerData);
                    MAIN_MANAGER.getDataManager().updatePlayer(player.getUniqueId());
                    MAIN_MANAGER.getDifficultyManager().calculateDifficulty(player.getUniqueId());
                }
            });
        } else {
            Minecrafter playerData = defaultData.clone();
            playerData.name = player.getName();
            playerData.uuid = player.getUniqueId();
            if (DifficultyTypes.region == difficultyType || DifficultyTypes.time == difficultyType) {
                playerData.affinity = difficultySettings.calculateAffinity((Player) player, -1); }
            PLAYER_LIST.put(player.getUniqueId(), playerData);
            MAIN_MANAGER.getDifficultyManager().calculateDifficulty(player.getUniqueId());
        }
    }

    public boolean isPlayerValid(Entity player) {
        if(!(player instanceof Player)) return false;
        if(player.hasMetadata("NPC")) return false;
        if(MAIN_MANAGER.getDataManager().isWorldDisabled(player.getWorld().getName())) return false;
        if(!DONT_ADD_PLAYER.contains(difficultyType) && !PLAYER_LIST.containsKey(player.getUniqueId())) addPlayer(player);
        return true;
    }

    public boolean isPlayerValidNoWorld(Entity player) {
        if(!(player instanceof Player)) return false;
        if(player.hasMetadata("NPC")) return false;
        if(!DONT_ADD_PLAYER.contains(difficultyType) && !PLAYER_LIST.containsKey(player.getUniqueId())) addPlayer(player);
        return true;
    }

    public void unloadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (PLAYER_LIST.containsKey(uuid)) {
            NAME_TO_UUID.remove(player.getName().toLowerCase());
            MAIN_MANAGER.getDataManager().updatePlayer(uuid);
            PLAYER_LIST.remove(uuid);
        }
    }
    public HashMap<UUID, Minecrafter> getPlayerList() { return PLAYER_LIST; }
    public boolean hasPlayer(String name) { return NAME_TO_UUID.containsKey(name.toLowerCase()); }

    public Minecrafter getPlayerAffinity(String name) { return PLAYER_LIST.get( NAME_TO_UUID.get(name.toLowerCase()) ); }
    public Minecrafter getPlayerAffinity(Player player) { return PLAYER_LIST.get( determineUuid(player) ); }
    public Minecrafter getPlayerAffinity(UUID uuid) { return PLAYER_LIST.get( uuid ); }

    public String determineName(Player player) {
        if (DifficultyTypes.biome == difficultyType) {
            return player.getWorld().getBiome(player.getLocation()).toString();
        }

        if (DifficultyTypes.world == difficultyType) {
            return player.getWorld().getName();
        }

        return player.getName();
    }

    public UUID determineUuid(Player player) {
        if (DifficultyTypes.world == difficultyType) {
            return player.getWorld().getUID();
        }

        if (DifficultyTypes.biome == difficultyType) {
            String biome = NamespacedKey.minecraft(String.valueOf(player.getLocation().getBlock().getBiome()).toLowerCase()).getKey();
            return UUID.nameUUIDFromBytes(biome.getBytes());
        }

        if (player != null) {
            return player.getUniqueId();
        }

        return null;
    }

    public void addAffinity(Player player, int value) {
        if (value == 0) { return; }

        if(DifficultyTypes.region == difficultyType || DifficultyTypes.time == difficultyType) {
            setAffinity(determineUuid(player), difficultySettings.calculateAffinity(player, -1));
            return;
        }

        addAffinity(determineUuid(player), value);
    }

    public int addAffinity(UUID uuid, int value) {
        if (value == 0) { return value; }

        Minecrafter data = PLAYER_LIST.get(uuid);
        boolean ignoreTheCap = value > maxAffinityGainPerMinute || value < maxAffinityLossPerMinute;

        if (!ignoreTheCap) {
            if (value > 0) {
                if (data.gainedThisMinute + value > maxAffinityGainPerMinute) {
                    value = maxAffinityGainPerMinute - data.gainedThisMinute;
                }
            } else if (data.gainedThisMinute + value < maxAffinityLossPerMinute) {
                value = maxAffinityLossPerMinute - data.gainedThisMinute;
            }

            data.gainedThisMinute += value;
        }

        return setAffinity(uuid, data.affinity + value);
    }

    public void addMinAffinity(UUID uuid, int value) {
        if (value == 0) { return; }
        Minecrafter data = getPlayerAffinity(uuid);
        setMinAffinity(uuid, (data.minAffinity == -1 ? 1 : 0) + data.minAffinity + value);
    }

    public void addMaxAffinity(UUID uuid, int value) {
        if (value == 0) { return; }
        Minecrafter data = getPlayerAffinity(uuid);
        setMaxAffinity(uuid, (data.maxAffinity == -1 ? 1 : 0) + data.maxAffinity + value);
    }

    public int setAffinity(UUID uuid, int value) {
        value = withinPlayerLimits(uuid, value);
        getPlayerAffinity(uuid).affinity = value;
        return value;
    }

    public int setMinAffinity(UUID uuid, int value) {
        Minecrafter player = getPlayerAffinity(uuid);
        if(value != -1) {
            value = withinServerLimits(value);
            if(player.maxAffinity != -1)
                value = Math.min(player.maxAffinity, value);
        }
        player.minAffinity = value;
        return value;
    }

    public int setMaxAffinity(UUID uuid, int value) {
        Minecrafter player = getPlayerAffinity(uuid);
        if(value != -1) {
            value = withinServerLimits(value);
            if(player.minAffinity != -1)
                value = Math.max(player.minAffinity, value);
        }
        player.maxAffinity = value;
        return value;
    }

    public void reloadConfig() {
        FileConfiguration config = MAIN_MANAGER.getDataManager().getConfig();
        maxAffinityGainPerMinute = config.getInt("max-affinity-gain-per-minute", 0);
        maxAffinityLossPerMinute = config.getInt("max-affinity-loss-per-minute", 0);
        String type = (config.getString("toggle-settings.difficulty-type", "player")).toLowerCase();

        serverMinAffinity = config.getInt("min-affinity", 0);
        serverMaxAffinity = config.getInt("max-affinity", 1500);
        intervalAffinity = config.getInt("points-per-minute", 3);

        defaultData = new Minecrafter();
        defaultData.affinity = config.getInt("starting-affinity", 600);
        defaultData.minAffinity = config.getInt("starting-min-affinity", -1);
        defaultData.maxAffinity = config.getInt("starting-max-affinity", -1);

        try {
            difficultyType = DifficultyTypes.valueOf(type);
        } catch (IllegalArgumentException ignored) {
            difficultyType = DifficultyTypes.player;
        }

        if(difficultyType == DifficultyTypes.region) {
            difficultySettings = new RegionSettings(MAIN_MANAGER);
        } else if (difficultyType == DifficultyTypes.world) {
            List<World> worlds = Bukkit.getWorlds();
            for (World world : worlds) {
                UUID uuid = world.getUID();
                MAIN_MANAGER.getDataManager().getAffinityValues(uuid, (value) -> {
                    NAME_TO_UUID.put(world.getName(), uuid);
                    if (value != null) {
                        PLAYER_LIST.put(uuid, value);
                    } else {
                        Minecrafter playerData = defaultData.clone();
                        playerData.name = world.getName();
                        playerData.uuid = uuid;
                        PLAYER_LIST.put(uuid, playerData);
                        MAIN_MANAGER.getDifficultyManager().calculateDifficulty(uuid);
                    }
                });
            }
        } else if (difficultyType == DifficultyTypes.time) {
            difficultySettings = new TimeSettings(MAIN_MANAGER);
        } else if (difficultyType == DifficultyTypes.biome) {
            Field[] fields = Biome.class.getFields();

            for (Field field : fields) {
                if (field.getType() != Biome.class) { continue; }

                String biomeName = field.getName();
                String key = NamespacedKey.minecraft(biomeName.toLowerCase()).getKey();
                UUID uuid = UUID.nameUUIDFromBytes(key.getBytes());
                MAIN_MANAGER.getDataManager().getAffinityValues(uuid, (value) -> {
                    NAME_TO_UUID.put(key, uuid);
                    if (value != null) {
                        PLAYER_LIST.put(uuid, value);
                    } else {
                        Minecrafter playerData = defaultData.clone();
                        playerData.name = key;
                        playerData.uuid = uuid;
                        PLAYER_LIST.put(uuid, playerData);
                        MAIN_MANAGER.getDifficultyManager().calculateDifficulty(playerData);
                    }
                });
            }
        }
    }
}
