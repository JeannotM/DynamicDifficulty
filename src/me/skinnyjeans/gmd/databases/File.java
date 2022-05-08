package me.skinnyjeans.gmd.databases;

import me.skinnyjeans.gmd.managers.DataManager;
import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.models.ISaveManager;
import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.UUID;

public class File implements ISaveManager {

    private final Main plugin;
    private final FileConfiguration data;
    private final java.io.File dataFile;

    public File(Main m, DataManager d) {
        Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] using default 'file' mode to save and read data");
        plugin = m;
        dataFile = new java.io.File(plugin.getDataFolder(), "data.yml");

        if(!dataFile.exists()) plugin.saveResource("data.yml",false);

        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public boolean isConnected() { return data != null; }

    @Override
    public void updatePlayer(Minecrafter playerData) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            data.set(playerData.getUUID() + ".affinity", playerData.getAffinity());
            data.set(playerData.getUUID() + ".max-affinity", playerData.getMaxAffinity());
            data.set(playerData.getUUID() + ".min-affinity", playerData.getMinAffinity());
            data.set(playerData.getUUID() + ".name", playerData.getName());
            try {
                data.save(dataFile);
            } catch (Exception ignored) { }
        });
    }

    @Override
    public void getAffinityValues(UUID uuid, findCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Minecrafter playerData = new Minecrafter(uuid);
            if(data.isSet(String.valueOf(uuid))) {
                playerData.setAffinity(data.getInt(uuid + ".affinity"));
                playerData.setMinAffinity(data.getInt(uuid + ".min-affinity"));
                playerData.setMaxAffinity(data.getInt(uuid + ".max-affinity"));
                playerData.setName(data.getString(uuid + ".name"));
            }
            callback.onQueryDone(playerData);
        });
    }

    @Override
    public void playerExists(UUID uuid, findBooleanCallback callback) {
        callback.onQueryDone(data.isSet(String.valueOf(uuid)));
    }

    @Override
    public void disconnect() { return; }
}
