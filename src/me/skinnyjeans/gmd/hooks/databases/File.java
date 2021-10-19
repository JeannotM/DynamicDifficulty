package me.skinnyjeans.gmd.hooks.databases;

import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.DataManager;
import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.hooks.SaveManager;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class File implements SaveManager {

    private Main plugin;
    private DataManager data;

    public File(Main m, DataManager d) {
        Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] using default 'file' mode to save and read data");
        data = d;
        plugin = m;
    }

    public boolean isConnected() { return data != null; }

    @Override
    public void updatePlayer(String uuid, int af, int maxAf, int minAf) {
        playerExists(uuid, r -> {
            if (r) {
                data.getDataFile().set(uuid + ".affinity", af);
                if(!uuid.equals("world")){
                    data.getDataFile().set(uuid + ".max-affinity", maxAf);
                    data.getDataFile().set(uuid + ".min-affinity", minAf);
                }
            } else {
                String name = (uuid.equals("world") ? "world" : Bukkit.getPlayer(UUID.fromString(uuid)).getName());
                data.getDataFile().set(uuid + ".affinity", af);
                if(!uuid.equals("world")){
                    data.getDataFile().set(uuid + ".max-affinity", maxAf);
                    data.getDataFile().set(uuid + ".min-affinity", minAf);
                    data.getDataFile().set(uuid + ".name", name);
                }
            }
        });
    }

    @Override
    public void getAffinityValues(String uuid, Affinity.findIntegerCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Bukkit.getScheduler().runTask(plugin, () -> {
            List<Integer> tmpArray = new ArrayList<>();
            if(data.getDataFile().getInt(uuid + ".affinity") != -1) {
                tmpArray.add(data.getDataFile().getInt(uuid + ".affinity"));
                if(!uuid.equalsIgnoreCase("world")){
                    tmpArray.add(data.getDataFile().getInt(uuid + ".max-affinity"));
                    tmpArray.add(data.getDataFile().getInt(uuid + ".min-affinity"));
                }
            } else {
                tmpArray.add(0, -1);
            }
            callback.onQueryDone(tmpArray);
        }));
    }

    @Override
    public void playerExists(String uuid, findBooleanCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Bukkit.getScheduler().runTask(plugin, () -> {
            callback.onQueryDone(data.getDataFile().getString(uuid + ".affinity") != null);
        }));
    }

    @Override
    public void disconnect() {
        if(isConnected())
            data.saveData();
    }
}
