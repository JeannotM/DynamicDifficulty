package me.skinnyjeans.gmd.databases;

import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.managers.DataManager;
import me.skinnyjeans.gmd.models.ISaveManager;
import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;

public class SQL implements ISaveManager {
    private final String tbName = "dynamicdifficulty";
    private final Main plugin;
    private final String host;
    private final String port;
    private final String dbName;
    private final String user;
    private final String pwd;
    private final String saveType;
    private Connection connection = null;

    public SQL(Main m, DataManager data, String sT) throws SQLException, ClassNotFoundException {
        plugin = m;
        host = data.getConfig().getString("saving-data.host");
        port = data.getConfig().getString("saving-data.port");
        dbName = data.getConfig().getString("saving-data.database");
        user = data.getConfig().getString("saving-data.username", "root");
        pwd = data.getConfig().getString("saving-data.password", "");
        saveType = sT.toLowerCase();
        connect(data);
        createTable();
    }

    public boolean isConnected() { return connection != null; }
    public Connection getConnection() { return connection; }

    public void connect(DataManager d) throws SQLException, ClassNotFoundException {
        String database = "";
        if(!isConnected())
            if(saveType.equals("mysql")) {
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&autoReconnect=true&useUnicode=yes&cachePrepStmts=true&useServerPrepStmts=true", user, pwd);
                database = "MySQL";
            } else if (saveType.equals("sqlite")){
                connection = DriverManager.getConnection("jdbc:sqlite:plugins/DynamicDifficulty/data.db");
                database = "SQLite";
            } else if (saveType.equals("postgresql")) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+dbName+"?autoReconnect=true&useUnicode=yes&cachePrepStmts=true&useServerPrepStmts=true", user, pwd);
                database = "PostGreSQL";
            }
        Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] " + d.getLanguageString("other.database-chosen").replace("%database%", database));
    }

    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if(isConnected()) {
                    PreparedStatement ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS "+tbName+" "+
                            "(UUID VARCHAR(60)," +
                            "Name VARCHAR(20), " +
                            "Affinity INT DEFAULT 500, " +
                            "MaxAffinity INT DEFAULT -1, " +
                            "MinAffinity INT DEFAULT -1, " +
                            "PRIMARY KEY(UUID))");
                    ps.execute();
                }
            } catch(SQLException e) { e.printStackTrace(); }
        });
    }

    @Override
    public void updatePlayer(Minecrafter playerData) {
        playerExists(playerData.getUUID(), r -> {
            try {
                if(isConnected()) {
                    PreparedStatement ps;
                    if(r) {
                        ps = getConnection().prepareStatement("UPDATE "+tbName+" SET Affinity=?, MaxAffinity=?, MinAffinity=? WHERE UUID=?");
                    } else {
                        ps = getConnection().prepareStatement("INSERT INTO "+tbName+" (Affinity, MaxAffinity, MinAffinity, UUID, Name) VALUES (?, ?, ?, ?, ?)");
                        ps.setString(5, playerData.getName());
                    }
                    ps.setInt(1, playerData.getAffinity());
                    ps.setInt(2, playerData.getMaxAffinity());
                    ps.setInt(3, playerData.getMinAffinity());
                    ps.setString(4, playerData.getUUID().toString());
                    ps.executeUpdate();
                }
            } catch(SQLException e) { e.printStackTrace(); }
        });
    }

    @Override
    public void getAffinityValues(UUID uuid, findCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Minecrafter data = new Minecrafter();
            try {
                if(isConnected()) {
                    PreparedStatement ps = getConnection().prepareStatement("SELECT Affinity, MaxAffinity, MinAffinity, Name FROM "+tbName+" WHERE UUID=?");
                    ps.setString(1, uuid.toString());
                    ResultSet result = ps.executeQuery();
                    if(result.next()) {
                        data.setUUID(uuid);
                        data.setName(result.getString("Name"));
                        data.setAffinity(result.getInt("Affinity"));
                        data.setMaxAffinity(result.getInt("MaxAffinity"));
                        data.setMinAffinity(result.getInt("MinAffinity"));
                    }
                }
            } catch(SQLException e) { e.printStackTrace(); }
            callback.onQueryDone(data);
        });
    }

    @Override
    public void playerExists(UUID uuid, final findBooleanCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if(isConnected()) {
                    PreparedStatement ps = getConnection().prepareStatement("SELECT id FROM "+tbName+" WHERE UUID=?");
                    ps.setString(1, uuid.toString());
                    ResultSet result = ps.executeQuery();
                    if(result.next()){
                        callback.onQueryDone(true);
                        return;
                    }
                }
            } catch(SQLException e) { e.printStackTrace(); }
            callback.onQueryDone(false);
        });
    }

    @Override
    public void disconnect() throws SQLException {
        if(isConnected())
            connection.close();
    }
}
