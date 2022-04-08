package me.skinnyjeans.gmd.databases;

import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.models.ISaveManager;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class None implements ISaveManager {

    public None(){ Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] 'None' mode selected, no data will be saved or read"); }

    @Override
    public boolean isConnected() { return false; }

    @Override
    public void updatePlayer(String uuid, int af, int maxAf, int minAf) { return; }

    @Override
    public void getAffinityValues(String uuid, Affinity.findIntegerCallback callback) { callback.onQueryDone(new ArrayList<>(Arrays.asList(-1))); }

    @Override
    public void playerExists(String uuid, findBooleanCallback callback) { return; }

    @Override
    public void disconnect() throws SQLException { return; }
}
