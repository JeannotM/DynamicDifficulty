package me.skinnyjeans.gmd.hooks;

import me.skinnyjeans.gmd.Affinity;

import java.sql.*;

public interface SaveManager {
    interface findBooleanCallback { void onQueryDone(boolean r); }
    boolean isConnected();
    void updatePlayer(String uuid, int af, int maxAf, int minAf);
    void getAffinityValues(String uuid, final Affinity.findIntegerCallback callback);
    void playerExists(String uuid, final findBooleanCallback callback);
    void disconnect() throws SQLException;
}
