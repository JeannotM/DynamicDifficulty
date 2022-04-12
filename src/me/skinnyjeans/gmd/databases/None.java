package me.skinnyjeans.gmd.databases;

import me.skinnyjeans.gmd.models.ISaveManager;
import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.UUID;

public class None implements ISaveManager {

    public None(){ Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] 'None' mode selected, no data will be saved or read"); }

    @Override
    public boolean isConnected() { return false; }

    @Override
    public void updatePlayer(Minecrafter playerData) { return; }

    @Override
    public void getAffinityValues(UUID uuid, findCallback callback) { callback.onQueryDone(null); }

    @Override
    public void playerExists(UUID uuid, findBooleanCallback callback) { return; }

    @Override
    public void disconnect() throws SQLException { return; }
}
