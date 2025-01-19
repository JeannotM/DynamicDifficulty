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
        plugin = m;
        dataFile = new java.io.File(plugin.getDataFolder(), "data.yml");

        if (! dataFile.exists())
            try {
                dataFile.createNewFile();
            } catch(Exception ignored) { }

        data = YamlConfiguration.loadConfiguration(dataFile);
//        Bukkit.getLogger().warning();
        Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] " + d.getLanguageString("other.database-chosen").replace("%database%", "File"));
    }

    public boolean isConnected() { return data != null; }

    @Override
    public void updatePlayer(Minecrafter playerData) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            data.set(playerData.uuid + ".affinity", playerData.affinity);
            data.set(playerData.uuid + ".max-affinity", playerData.maxAffinity);
            data.set(playerData.uuid + ".min-affinity", playerData.minAffinity);
            data.set(playerData.uuid + ".name", playerData.name);
            try {
                data.save(dataFile);
            } catch (Exception ignored) { }
        });
    }

    @Override
    public void getAffinityValues(UUID uuid, findCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if(data.isSet(String.valueOf(uuid)) && data.isSet(uuid + ".affinity")) {
                Minecrafter playerData = new Minecrafter(uuid);
                playerData.affinity = data.getInt(uuid + ".affinity");
                playerData.minAffinity = data.getInt(uuid + ".min-affinity");
                playerData.maxAffinity = data.getInt(uuid + ".max-affinity");
                playerData.name = data.getString(uuid + ".name");
                callback.onQueryDone(playerData);
                return;
            }
            callback.onQueryDone(null);
        });
    }

    @Override
    public void playerExists(UUID uuid, findBooleanCallback callback) {
        callback.onQueryDone(data.isSet(String.valueOf(uuid)));
    }

    @Override
    public void disconnect() { }
}
