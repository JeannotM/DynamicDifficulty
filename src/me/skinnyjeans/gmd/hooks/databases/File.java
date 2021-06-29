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
    private DataManager data = null;

    public File(Main m, DataManager d) {
        Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] using default 'file' mode to save and read data");
        data = d;
        plugin = m;
    }

    public boolean isConnected() { return data != null; }

    @Override
    public void updatePlayer(String uuid, int af, int maxAf, int minAf) {
        playerExists(uuid, new findBooleanCallback() {
            @Override
            public void onQueryDone(boolean r) {
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
                data.saveData();
            }
        });
    }

    @Override
    public void getAffinityValues(String uuid, Affinity.findIntegerCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        List<Integer> tmpArray = new ArrayList<>();
                        if(data.getDataFile().getString(uuid + ".affinity") != null) {
                            tmpArray.add(data.getDataFile().getInt(uuid + ".affinity"));
                            if(!uuid.equals("world")){
                                tmpArray.add(data.getDataFile().getInt(uuid + ".max-affinity"));
                                tmpArray.add(data.getDataFile().getInt(uuid + ".min-affinity"));
                            }
                        } else {
                            tmpArray.add(0, -1);
                        }
                        callback.onQueryDone(tmpArray);
                    }
                });
            }
        });
    }

    @Override
    public void playerExists(String uuid, findBooleanCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        callback.onQueryDone(data.getDataFile().getString(uuid + ".affinity") != null);
                    }
                });
            }
        });
    }

    @Override
    public void disconnect() {
        if(isConnected())
            data.saveData();
    }
}
