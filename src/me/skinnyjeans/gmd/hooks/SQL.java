package me.skinnyjeans.gmd.hooks;

import com.mongodb.*;
import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.DataManager;
import me.skinnyjeans.gmd.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.net.UnknownHostException;
import java.sql.*;
import java.util.*;

public class SQL {
    private Main plugin;
    private String host = "localhost";
    private String port = "3306";
    private String dbName = "dynamicdifficulty";
    private String user = "root";
    private String pwd = "";
    private String saveType = "";
    private String tbName = "dynamicdifficulty";
    private Connection conn = null;
    private MongoClient connNoSQL = null;

    public SQL(Main m, DataManager data) throws SQLException, UnknownHostException {
        plugin = m;
        host = data.getConfig().getString("saving-data.host");
        port = data.getConfig().getString("saving-data.port");
        dbName = data.getConfig().getString("saving-data.database");
        user = data.getConfig().getString("saving-data.username");
        pwd = data.getConfig().getString("saving-data.password");
        saveType = data.getConfig().getString("saving-data.type");
        connect();
        Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Succesfully connected to the database!");
        if(saveType.equalsIgnoreCase("mysql"))
            addColumnsNotExists();
        createTable();
    }

    public Connection getConnSQL(){ return conn; }
    public DBCollection getConnNoSQL(){ return connNoSQL.getDB(dbName).getCollection(tbName); }
    public boolean isConnected(){ return conn != null; }
    public interface findBooleanCallback { void onQueryDone(boolean r); }

    public void connect() throws SQLException, UnknownHostException {
        if(!isConnected()){
            if(saveType.equalsIgnoreCase("mysql")) {
                conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false", user, pwd);
            } else if (saveType.equalsIgnoreCase("mongodb")) {
                ServerAddress address = new ServerAddress(host, Integer.parseInt(port));
                if(user != null && pwd != null) {
                    MongoCredential credential = MongoCredential.createCredential(user, dbName, pwd.toCharArray());
                    connNoSQL = new MongoClient(address, Arrays.asList(credential));
                } else {
                    connNoSQL = new MongoClient(address);
                }
                Bukkit.broadcastMessage(connNoSQL.toString());
            } else {
                conn = DriverManager.getConnection("jdbc:sqlite:plugins/DynamicDifficulty/data.db");
                if(!saveType.equalsIgnoreCase("sqlite"))
                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW+"[DynamicDifficulty] Couldn't find the correct database you wanted to connect to, so SQLite is now being used");
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
                            PreparedStatement ps = getConnSQL().prepareStatement("ALTER TABLE "+tbName+" "+
                                    "ADD COLUMN MinAffinity int(6) DEFAULT -1");
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
                            if(isConnected()){
                                PreparedStatement ps = getConnSQL().prepareStatement("CREATE TABLE IF NOT EXISTS "+tbName+" "+
                                        "(UUID VARCHAR(60)," +
                                        "Name VARCHAR(20), " +
                                        "Affinity int(6), " +
                                        "MaxAffinity int(6) DEFAULT -1, " +
                                        "MinAffinity int(6) DEFAULT -1, " +
                                        "PRIMARY KEY(UUID))");
                                ps.executeUpdate();
                            }
                        } catch(SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void updatePlayer(String uuid, int af, int maxAf, int minAf) {
        playerExists(uuid, new findBooleanCallback() {
            @Override
            public void onQueryDone(boolean r) {
                try {
                    if(!isConnected()) {
                        DBObject obj = new BasicDBObject("_id", uuid).append("Affinity", af)
                                .append("MinAffinity", minAf).append("MaxAffinity", maxAf);
                        try{
                            ((BasicDBObject) obj).append("Name", (uuid.equals("world") ? "world" : Bukkit.getPlayer(UUID.fromString(uuid)).getName()));
                        } catch(Exception e) {
                            ((BasicDBObject) obj).append("Name", getConnNoSQL().find(new BasicDBObject("_id", uuid)).next().get("Name"));
                        }

                        if(r) {
                            getConnNoSQL().update(new BasicDBObject("_id", uuid), obj);
                        } else {
                            getConnNoSQL().insert(obj);
                        }
                    } else {
                        PreparedStatement ps;
                        if(r){
                            ps = getConnSQL().prepareStatement("UPDATE "+tbName+" SET Affinity=?, MaxAffinity=?, MinAffinity=? WHERE UUID=?");
                        } else {
                            ps = getConnSQL().prepareStatement("INSERT INTO "+tbName+" (Affinity, MaxAffinity, MinAffinity, UUID, Name) VALUES (?, ?, ?, ?, ?)");
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

    public void getAffinityValues(String uuid, final Affinity.findIntegerCallback callback){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        List<Integer> tmpArray = new ArrayList<>();
                        try {
                            if(!isConnected()) {
                                DBCursor find = getConnNoSQL().find(new BasicDBObject("_id", uuid));
                                if(find.hasNext() && find != null){
                                    DBObject tmp = find.next();
                                    tmpArray.add(Integer.parseInt(tmp.get("Affinity").toString()));
                                    tmpArray.add(Integer.parseInt(tmp.get("MaxAffinity").toString()));
                                    tmpArray.add(Integer.parseInt(tmp.get("MinAffinity").toString()));
                                    callback.onQueryDone(tmpArray);
                                    return;
                                }
                            } else {
                                PreparedStatement ps = getConnSQL().prepareStatement("SELECT Affinity, MaxAffinity, MinAffinity FROM "+tbName+" WHERE UUID=?");
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

    public void playerExists(String uuid, final findBooleanCallback callback){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(!isConnected()) {
                                DBCursor find = getConnNoSQL().find(new BasicDBObject("_id", uuid));
                                if(find.hasNext() && find != null){
                                    callback.onQueryDone(true);
                                    return;
                                }
                            } else {
                                PreparedStatement ps = getConnSQL().prepareStatement("SELECT * FROM "+tbName+" WHERE UUID=?");
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

    public void disconnect() {
        try{
            if(!isConnected()){
                connNoSQL.close();
            } else {
                conn.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
