package me.skinnyjeans.gmd.databases;

import me.skinnyjeans.gmd.managers.DataManager;
import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.models.ISaveManager;
import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;

import java.util.UUID;

public class File implements ISaveManager {

    private final Main plugin;
    private final DataManager data;
    private final String difficultyType;

    public File(Main m, DataManager d) {
        Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] using default 'file' mode to save and read data");
        data = d;
        plugin = m;
        difficultyType = d.getConfig().getString("difficulty-modifiers.type", "player").toLowerCase();
    }

    public boolean isConnected() { return data != null; }

    @Override
    public void updatePlayer(Minecrafter playerData) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            data.getConfig().set(playerData.getUUID() + ".affinity", playerData.getAffinity());
            data.getConfig().set(playerData.getUUID() + ".max-affinity", playerData.getMaxAffinity());
            data.getConfig().set(playerData.getUUID() + ".min-affinity", playerData.getMinAffinity());
            data.getConfig().set(playerData.getUUID() + ".name", playerData.getName());
        });
    }

    @Override
    public void getAffinityValues(UUID uuid, findCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Minecrafter playerData = new Minecrafter(uuid);
            if(data.getConfig().isSet(uuid + ".affinity")) {
                playerData.setAffinity(data.getConfig().getInt(uuid + ".affinity"));
                playerData.setMinAffinity(data.getConfig().getInt(uuid + ".min-affinity"));
                playerData.setMaxAffinity(data.getConfig().getInt(uuid + ".max-affinity"));
                playerData.setName(data.getConfig().getString(uuid + ".name"));
            }
            callback.onQueryDone(playerData);
        });
    }

    @Override
    public void playerExists(UUID uuid, findBooleanCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
            callback.onQueryDone(data.getConfig().isSet(uuid + ".affinity"))
        );
    }

    @Override
    public void disconnect() { return; }
}
