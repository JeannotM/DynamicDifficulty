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
    private final String host, port;
    private final String user, pwd;
    private final String dbName, saveType;
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

    public void connect(DataManager d) throws SQLException, ClassNotFoundException {
        String database = "";
        if(!isConnected()) {
            Class.forName("com.mysql.jdbc.Driver");
            if(saveType.equals("mysql")) {
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&autoReconnect=true" +
                        "&useUnicode=yes&cachePrepStmts=true&useServerPrepStmts=true&maxReconnects=5&initialTimeout=2", user, pwd);
                database = "MySQL";
            } else if (saveType.equals("sqlite")){
                connection = DriverManager.getConnection("jdbc:sqlite:plugins/DynamicDifficulty/data.db");
                database = "SQLite";
            } else if (saveType.equals("postgresql")) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+dbName+"?autoReconnect=true&useUnicode=yes" +
                        "&cachePrepStmts=true&useServerPrepStmts=true&maxReconnects=5&initialTimeout=2", user, pwd);
                database = "PostGreSQL";
            } else if (saveType.equals("mariadb")) {
                Class.forName("org.mariadb.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mariadb://"+host+":"+port+"/"+dbName+"?autoReconnect=true&useUnicode=yes" +
                        "&cachePrepStmts=true&useServerPrepStmts=true&maxReconnects=5&initialTimeout=2", user, pwd);
                database = "MariaDB";
            }
        }
        Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] " + d.getLanguageString("other.database-chosen").replace("%database%", database));
    }

    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if(isConnected()) {
                    PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+tbName+" "+
                            "(UUID VARCHAR(60)," +
                            "Name VARCHAR(20), " +
                            "Affinity INT DEFAULT 500, " +
                            "MaxAffinity INT DEFAULT -1, " +
                            "MinAffinity INT DEFAULT -1, " +
                            "PRIMARY KEY(UUID))");
                    ps.execute();
                    ps.close();
                }
            } catch(SQLException e) { e.printStackTrace(); }
        });
    }

    @Override
    public void updatePlayer(Minecrafter playerData) {
        playerExists(playerData.uuid, r -> {
            try {
                if(isConnected()) {
                    PreparedStatement ps;
                    if(r) {
                        ps = connection.prepareStatement("UPDATE "+tbName+" SET Affinity=?, MaxAffinity=?, MinAffinity=? WHERE UUID=?");
                    } else {
                        ps = connection.prepareStatement("INSERT INTO "+tbName+" (Affinity, MaxAffinity, MinAffinity, UUID, Name) VALUES (?, ?, ?, ?, ?)");
                        ps.setString(5, playerData.name);
                    }
                    ps.setInt(1, playerData.affinity);
                    ps.setInt(2, playerData.maxAffinity);
                    ps.setInt(3, playerData.minAffinity);
                    ps.setString(4, playerData.uuid.toString());
                    ps.executeUpdate();
                    ps.close();
                }
            } catch(SQLException e) { e.printStackTrace(); }
        });
    }

    @Override
    public void getAffinityValues(UUID uuid, findCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if(isConnected()) {
                    PreparedStatement ps = connection.prepareStatement("SELECT Affinity, MaxAffinity, MinAffinity, Name FROM "+tbName+" WHERE UUID=?");
                    ps.setString(1, uuid.toString());
                    ResultSet result = ps.executeQuery();
                    if(result.next()) {
                        Minecrafter data = new Minecrafter();

                        data.uuid = uuid;
                        data.name = result.getString("Name");
                        data.affinity = result.getInt("Affinity");
                        data.maxAffinity = result.getInt("MaxAffinity");
                        data.minAffinity = result.getInt("MinAffinity");
                        callback.onQueryDone(data);
                        ps.close();
                        return;
                    }
                    ps.close();
                }
            } catch(SQLException e) { e.printStackTrace(); }
            callback.onQueryDone(null);
        });
    }

    @Override
    public void playerExists(UUID uuid, final findBooleanCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if(isConnected()) {
                    PreparedStatement ps = connection.prepareStatement("SELECT Name FROM "+tbName+" WHERE UUID=?");
                    ps.setString(1, uuid.toString());
                    ResultSet result = ps.executeQuery();
                    if(result.next()){
                        callback.onQueryDone(true);
                        return;
                    }
                    ps.close();
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
