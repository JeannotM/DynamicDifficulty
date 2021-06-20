package me.skinnyjeans.gmd.hooks;

import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.DataManager;
import me.skinnyjeans.gmd.Main;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySQL {
    private Main plugin;
    private String host = "localhost";
    private String port = "3306";
    private String database = "dynamicdifficulty";
    private String username = "root";
    private String password = "";
    private final String tbName = "dynamicdifficulty";
    private Connection connection;

    public MySQL(Main m, DataManager data){
        plugin = m;
        host = data.getConfig().getString("saving-data.host");
        port = data.getConfig().getString("saving-data.port");
        database = data.getConfig().getString("saving-data.database");
        username = data.getConfig().getString("saving-data.username");
        password = data.getConfig().getString("saving-data.password");
    }

    public boolean isConnected(){ return connection != null; }
    public interface findBooleanCallback { void onQueryDone(boolean r); }

    public void connect() throws SQLException {
        if(!isConnected())
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
    }

    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PreparedStatement ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS "+tbName+" "+
                                    "(UUID VARCHAR(60)," +
                                    "Name VARCHAR(20), " +
                                    "Affinity int(6), " +
                                    "MaxAffinity int(6), " +
                                    "PRIMARY KEY(UUID))");
                            ps.executeUpdate();
                        } catch(SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void updatePlayer(String uuid, int af, int maxAf) {
        playerExists(uuid, new findBooleanCallback() {
            @Override
            public void onQueryDone(boolean r) {
                PreparedStatement ps;
                try {
                    if(r){
                        ps = getConnection().prepareStatement("UPDATE "+tbName+" SET Affinity=?, MaxAffinity=? WHERE UUID=?");
                    } else {
                        ps = getConnection().prepareStatement("INSERT INTO "+tbName+" (Affinity, MaxAffinity, UUID, Name) VALUES (?, ?, ?, ?)");
                        ps.setString(4, (uuid.equals("world") ? "world" : Bukkit.getPlayer(UUID.fromString(uuid)).getName()));
                    }
                    ps.setInt(1, af);
                    ps.setInt(2, maxAf);
                    ps.setString(3, (uuid.equals("world")  ? "world" : uuid));
                    Bukkit.getConsoleSender().sendMessage(uuid+": "+af+" - "+maxAf);
                    ps.executeUpdate();
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getAffinityValues(String uuid, final Affinity.findIntegerCallback callback){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        List<Integer> tmpArray = new ArrayList<>();
                        try {
                            PreparedStatement ps = getConnection().prepareStatement("SELECT Affinity, MaxAffinity FROM "+tbName+" WHERE UUID=?");
                            ps.setString(1, uuid);
                            ResultSet result = ps.executeQuery();
                            if(result.next()){
                                tmpArray.add(result.getInt("Affinity"));
                                tmpArray.add(result.getInt("maxAffinity"));
                                callback.onQueryDone(tmpArray);
                                return;
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

    public void playerExists(String uuid, final findBooleanCallback callback){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM "+tbName+" WHERE UUID=?");
                            ps.setString(1, uuid);
                            ResultSet result = ps.executeQuery();
                            if(result.next()){
                                callback.onQueryDone(true);
                                return;
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

    public void disconnect() {
        if(isConnected()){
            try{
                connection.close();
            } catch(SQLException e){
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection(){ return connection; }
}
