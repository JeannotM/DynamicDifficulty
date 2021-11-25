package me.skinnyjeans.gmd.hooks.databases;

import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.DataManager;
import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.hooks.SaveManager;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class File implements SaveManager {

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
    public void updatePlayer(String uuid, int af, int maxAf, int minAf) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean isWorld = (uuid.equalsIgnoreCase("world"));
            data.getDataFile().set(uuid + ".affinity", af);
            if (!isWorld) {
                data.getDataFile().set(uuid + ".max-affinity", maxAf);
                data.getDataFile().set(uuid + ".min-affinity", minAf);
                data.getDataFile().set(uuid + ".name", difficultyType.equals("biome") ? uuid : Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
            }
        });
    }

    @Override
    public void getAffinityValues(String uuid, Affinity.findIntegerCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean isWorld = (uuid.equalsIgnoreCase("world"));
            List<Integer> tmpArray = new ArrayList<>(Arrays.asList(-1));
            if(data.getDataFile().isSet(uuid + ".affinity")) {
                tmpArray.set(0, data.getDataFile().getInt(uuid + ".affinity"));
                if(!isWorld){
                    tmpArray.add(data.getDataFile().getInt(uuid + ".max-affinity"));
                    tmpArray.add(data.getDataFile().getInt(uuid + ".min-affinity"));
                }
            }
            callback.onQueryDone(tmpArray);
        });
    }

    @Override
    public void playerExists(String uuid, findBooleanCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
            callback.onQueryDone(data.getDataFile().isSet(uuid + ".affinity"))
        );
    }

    @Override
    public void disconnect() {
        if(isConnected())
            data.saveData();
    }
}
