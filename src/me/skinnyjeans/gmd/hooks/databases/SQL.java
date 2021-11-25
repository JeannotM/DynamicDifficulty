package me.skinnyjeans.gmd.hooks.databases;

import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.DataManager;
import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.hooks.SaveManager;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;

public class SQL implements SaveManager {
    private final String tbName = "dynamicdifficulty";
    private final String difficultyType;
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
        difficultyType = data.getConfig().getString("difficulty-modifiers.type", "player").toLowerCase();
        connect();
        if(sT.equals("mysql"))
            addColumnsNotExists();
        createTable();
    }

    public boolean isConnected() { return connection != null; }
    public Connection getConnection() { return connection; }

    public void connect() throws SQLException, ClassNotFoundException {
        if(!isConnected())
            if(saveType.equals("mysql")) {
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&autoReconnect=true&useUnicode=yes&cachePrepStmts=true&useServerPrepStmts=true", user, pwd);
                Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Successfully connected to MySQL!");
            } else if (saveType.equals("sqlite")){
                connection = DriverManager.getConnection("jdbc:sqlite:plugins/DynamicDifficulty/data.db");
                Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Successfully connected to SQLite!");
            } else if (saveType.equals("postgresql")) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+dbName+"?autoReconnect=true&useUnicode=yes&cachePrepStmts=true&useServerPrepStmts=true", user, pwd);
                Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Successfully connected to PostGreSQL!");
            }
    }

    public void addColumnsNotExists() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("ALTER TABLE "+tbName+" "+
                        "ADD COLUMN MinAffinity INT DEFAULT -1");
                ps.executeUpdate();
            } catch(Exception e) { e.printStackTrace(); }
        });
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
    public void updatePlayer(String uuid, int af, int maxAf, int minAf) {
        playerExists(uuid, r -> {
            try {
                if(isConnected()) {
                    PreparedStatement ps;
                    if(r) {
                        ps = getConnection().prepareStatement("UPDATE "+tbName+" SET Affinity=?, MaxAffinity=?, MinAffinity=? WHERE UUID=?");
                    } else {
                        ps = getConnection().prepareStatement("INSERT INTO "+tbName+" (Affinity, MaxAffinity, MinAffinity, UUID, Name) VALUES (?, ?, ?, ?, ?)");
                        String name;
                        if(uuid.equalsIgnoreCase("world")) {
                            name = "world";
                        } else if(difficultyType.equals("biome")) {
                            name = uuid;
                        } else {
                            name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                        }
                        ps.setString(5, name);
                    }
                    ps.setInt(1, af);
                    ps.setInt(2, maxAf);
                    ps.setInt(3, minAf);
                    ps.setString(4, uuid);
                    ps.executeUpdate();
                }
            } catch(SQLException e) { e.printStackTrace(); }
        });
    }

    @Override
    public void getAffinityValues(String uuid, Affinity.findIntegerCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Integer> tmpArray = new ArrayList<>(Arrays.asList(-1));
            try {
                if(isConnected()) {
                    PreparedStatement ps = getConnection().prepareStatement("SELECT Affinity, MaxAffinity, MinAffinity FROM "+tbName+" WHERE UUID=?");
                    ps.setString(1, uuid);
                    ResultSet result = ps.executeQuery();
                    if(result.next()){
                        tmpArray.set(0, result.getInt("Affinity"));
                        tmpArray.add(result.getInt("MaxAffinity"));
                        tmpArray.add(result.getInt("MinAffinity"));
                        callback.onQueryDone(tmpArray);
                        return;
                    }
                }
            } catch(SQLException e) { e.printStackTrace(); }
            callback.onQueryDone(tmpArray);
        });
    }

    @Override
    public void playerExists(String uuid, final findBooleanCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if(isConnected()) {
                    PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM "+tbName+" WHERE UUID=?");
                    ps.setString(1, uuid);
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
