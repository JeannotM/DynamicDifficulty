package me.skinnyjeans.gmd.models;

import java.sql.SQLException;
import java.util.UUID;

public interface ISaveManager {
    interface findBooleanCallback { void onQueryDone(boolean r); }
    interface findCallback { void onQueryDone(Minecrafter playerData); }
    boolean isConnected();
    void updatePlayer(Minecrafter playerData);
    void getAffinityValues(UUID uuid, final findCallback callback);
    void playerExists(UUID uuid, final findBooleanCallback callback);
    void disconnect() throws SQLException;
}
