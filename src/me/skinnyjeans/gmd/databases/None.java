package me.skinnyjeans.gmd.databases;

import me.skinnyjeans.gmd.managers.DataManager;
import me.skinnyjeans.gmd.models.ISaveManager;
import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

public class None implements ISaveManager {

    public None(DataManager d){ Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] " + d.getLanguageString("other.database-chosen").replace("%database%", "None")); }

    @Override
    public boolean isConnected() { return false; }

    @Override
    public void updatePlayer(Minecrafter playerData) { return; }

    @Override
    public void batchSavePlayers(Collection<Minecrafter> players) { return; }

    @Override
    public void getAffinityValues(UUID uuid, findCallback callback) { callback.onQueryDone(null); }

    @Override
    public void playerExists(UUID uuid, findBooleanCallback callback) { return; }

    @Override
    public void disconnect() throws SQLException { return; }
}
