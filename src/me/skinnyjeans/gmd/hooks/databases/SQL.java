package me.skinnyjeans.gmd.hooks.databases;

import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.DataManager;
import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.hooks.SaveManager;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;

public class SQL implements SaveManager {
    private Main plugin;
    private String host = "localhost";
    private String port = "3306";
    private String dbName = "dynamicdifficulty";
    private String user = "root";
    private String pwd = "";
    private String saveType = "";
    private String tbName = "dynamicdifficulty";
    private Connection connection = null;

    public SQL(Main m, DataManager data, String sT) throws SQLException, ClassNotFoundException {
        plugin = m;
        host = data.getConfig().getString("saving-data.host");
        port = data.getConfig().getString("saving-data.port");
        dbName = data.getConfig().getString("saving-data.database");
        user = data.getConfig().getString("saving-data.username");
        pwd = data.getConfig().getString("saving-data.password");
        saveType = sT;
        connect();
        if(sT.equalsIgnoreCase("mysql"))
            addColumnsNotExists();
        createTable();
    }

    public boolean isConnected() { return connection != null; }
    public Connection getConnection() { return connection; }

    public void connect() throws SQLException, ClassNotFoundException {
        if(!isConnected()){
            if(saveType.equalsIgnoreCase("mysql")) {
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false", user, pwd);
                Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Succesfully connected to MySQL!");
            } else if (saveType.equalsIgnoreCase("sqlite")){
                connection = DriverManager.getConnection("jdbc:sqlite:plugins/DynamicDifficulty/data.db");
                Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Succesfully connected to SQLite!");
            } else if (saveType.equalsIgnoreCase("postgresql")) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+dbName, user, pwd);
                Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Succesfully connected to PostGreSQL!");
            }
        }
    }

    public void addColumnsNotExists() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PreparedStatement ps = getConnection().prepareStatement("ALTER TABLE "+tbName+" "+
                                    "ADD COLUMN MinAffinity INT");
                            ps.executeUpdate();
                        } catch(Exception e) {}
                    }
                });
            }
        });
    }

    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(isConnected()) {
                                PreparedStatement ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS "+tbName+" "+
                                        "(UUID VARCHAR(60)," +
                                        "Name VARCHAR(20), " +
                                        "Affinity INT, " +
                                        "MaxAffinity INT, " +
                                        "MinAffinity INT, " +
                                        "PRIMARY KEY(UUID))");
                                ps.execute();
                            }
                        } catch(SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void updatePlayer(String uuid, int af, int maxAf, int minAf) {
        playerExists(uuid, new findBooleanCallback() {
            @Override
            public void onQueryDone(boolean r) {
                try {
                    if(isConnected()) {
                        PreparedStatement ps;
                        if(r) {
                            ps = getConnection().prepareStatement("UPDATE "+tbName+" SET Affinity=?, MaxAffinity=?, MinAffinity=? WHERE UUID=?");
                        } else {
                            ps = getConnection().prepareStatement("INSERT INTO "+tbName+" (Affinity, MaxAffinity, MinAffinity, UUID, Name) VALUES (?, ?, ?, ?, ?)");
                            ps.setString(5, (uuid.equals("world") ? "world" : Bukkit.getPlayer(UUID.fromString(uuid)).getName()));
                        }
                        ps.setInt(1, af);
                        ps.setInt(2, maxAf);
                        ps.setInt(3, minAf);
                        ps.setString(4, (uuid.equals("world")  ? "world" : uuid));
                        ps.executeUpdate();
                    }
                } catch(SQLException e) {
                    e.printStackTrace();
                }
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
                        try {
                            if(isConnected()) {
                                PreparedStatement ps = getConnection().prepareStatement("SELECT Affinity, MaxAffinity, MinAffinity FROM "+tbName+" WHERE UUID=?");
                                ps.setString(1, uuid);
                                ResultSet result = ps.executeQuery();
                                if(result.next()){
                                    tmpArray.add(result.getInt("Affinity"));
                                    tmpArray.add(result.getInt("MaxAffinity"));
                                    tmpArray.add(result.getInt("MinAffinity"));
                                    callback.onQueryDone(tmpArray);
                                    return;
                                }
                            }
                        } catch(SQLException e) {
                            e.printStackTrace();
                        }
                        tmpArray.add(0, -1);
                        callback.onQueryDone(tmpArray);
                        return;
                    }
                });
            }
        });
    }

    @Override
    public void playerExists(String uuid, final findBooleanCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
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
                        } catch(SQLException e) {
                            e.printStackTrace();
                        }
                        callback.onQueryDone(false);
                    }
                });
            }
        });
    }

    @Override
    public void disconnect() throws SQLException {
        if(isConnected())
            connection.close();
    }
}
