package me.skinnyjeans.gmd.hooks.databases;

import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.hooks.SaveManager;

import java.sql.SQLException;

public class PostGreSQL implements SaveManager {
    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void updatePlayer(String uuid, int af, int maxAf, int minAf) {

    }

    @Override
    public void getAffinityValues(String uuid, Affinity.findIntegerCallback callback) {

    }

    @Override
    public void playerExists(String uuid, findBooleanCallback callback) {

    }

    @Override
    public void disconnect() throws SQLException {

    }
}
