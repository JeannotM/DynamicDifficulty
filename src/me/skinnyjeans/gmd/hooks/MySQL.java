package me.skinnyjeans.gmd.hooks;

import me.skinnyjeans.gmd.DataManager;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.UUID;

public class MySQL {
    private String host = "localhost";
    private String port = "3306";
    private String database = "dynamicdifficulty";
    private String username = "root";
    private String password = "";
    private String tbName = "dynamicdifficulty";
    private Connection connection;

    public MySQL(DataManager data){
        host = data.getConfig().getString("saving-data.host");
        port = data.getConfig().getString("saving-data.port");
        database = data.getConfig().getString("saving-data.database");
        username = data.getConfig().getString("saving-data.username");
        password = data.getConfig().getString("saving-data.password");
    }

    public boolean isConnected(){ return connection != null; }

    public void connect() throws ClassNotFoundException, SQLException {
        if(!isConnected())
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
    }

    public void createTable() {
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

    public void updatePlayer(String uuid, int af, int maxAf) {
        PreparedStatement ps;
        try {
            if(playerExists(uuid)){
                ps = getConnection().prepareStatement("UPDATE "+tbName+" SET Affinity=? and MaxAffinity=? WHERE UUID=?");
            } else {
                ps = getConnection().prepareStatement("INSERT INTO "+tbName+" (Affinity, MaxAffinity, UUID, Name) VALUES (?, ?, ?, ?)");
                ps.setString(4, (uuid == "world" ? "world" : Bukkit.getPlayer(UUID.fromString(uuid)).getName()));
            }
            ps.setInt(1, af);
            ps.setInt(2, maxAf);
            ps.setString(3, (uuid == "world" ? "world" : uuid));
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public int getAffinity(String uuid){
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT Affinity FROM "+tbName+" WHERE UUID=?");
            ps.setString(1, uuid);
            ResultSet result = ps.executeQuery();
            if(result.next())
                return result.getInt("Affinity");
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getMaxAffinity(String uuid){
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT MaxAffinity FROM "+tbName+" WHERE UUID=?");
            ps.setString(1, uuid);
            ResultSet result = ps.executeQuery();
            if(result.next())
                return result.getInt("MaxAffinity");
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean playerExists(String uuid){
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM "+tbName+" WHERE UUID=?");
            ps.setString(1, uuid);
            ResultSet result = ps.executeQuery();
            if(result.next())
                return true;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
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
