package me.skinnyjeans.gmd.hooks.databases;

import com.mongodb.*;
import me.skinnyjeans.gmd.Affinity;
import me.skinnyjeans.gmd.DataManager;
import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.hooks.SaveManager;
import org.bukkit.Bukkit;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MongoDB implements SaveManager {
    private Main plugin;
    private String host = "localhost";
    private String port = "3306";
    private String dbName = "dynamicdifficulty";
    private String user = "root";
    private String pwd = "";
    private String tbName = "dynamicdifficulty";
    private MongoClient connection;

    public MongoDB(Main m, DataManager data) throws UnknownHostException {
        plugin = m;
        host = data.getConfig().getString("saving-data.host");
        port = data.getConfig().getString("saving-data.port");
        dbName = data.getConfig().getString("saving-data.database");
        user = data.getConfig().getString("saving-data.username");
        pwd = data.getConfig().getString("saving-data.password");
        connect();
    }

    public DBCollection getConnection() { return connection.getDB(dbName).getCollection(tbName) ;}
    public boolean isConnected() { return connection != null; }

    public void connect() throws UnknownHostException {
        if(!isConnected()){
            ServerAddress address = new ServerAddress(host, Integer.parseInt(port));
            if(user != null && pwd != null) {
                MongoCredential credential = MongoCredential.createCredential(user, dbName, pwd.toCharArray());
                connection = new MongoClient(address, Arrays.asList(credential));
            } else {
                connection = new MongoClient(address);
            }
            Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Succesfully connected to MongoDB!");
        }
    }

    @Override
    public void updatePlayer(String uuid, int af, int maxAf, int minAf) {
        playerExists(uuid, new findBooleanCallback() {
            @Override
            public void onQueryDone(boolean r) {
                try {
                    if(isConnected()) {
                        DBObject obj = new BasicDBObject("_id", uuid).append("Affinity", af)
                                .append("MinAffinity", minAf).append("MaxAffinity", maxAf);
                        try {
                            ((BasicDBObject) obj).append("Name", (uuid.equals("world") ? "world" : Bukkit.getPlayer(UUID.fromString(uuid)).getName()));
                        } catch(Exception e) {
                            ((BasicDBObject) obj).append("Name", getConnection().find(new BasicDBObject("_id", uuid)).next().get("Name"));
                        }

                        if(r) {
                            getConnection().update(new BasicDBObject("_id", uuid), obj);
                        } else {
                            getConnection().insert(obj);
                        }
                    }
                } catch(Exception e) {
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
                                DBCursor find = getConnection().find(new BasicDBObject("_id", uuid));
                                if(find.hasNext() && find != null){
                                    DBObject tmp = find.next();
                                    tmpArray.add(Integer.parseInt(tmp.get("Affinity").toString()));
                                    tmpArray.add(Integer.parseInt(tmp.get("MaxAffinity").toString()));
                                    tmpArray.add(Integer.parseInt(tmp.get("MinAffinity").toString()));
                                    callback.onQueryDone(tmpArray);
                                    return;
                                }
                            }
                        } catch(Exception e) {
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
                                DBCursor find = getConnection().find(new BasicDBObject("_id", uuid));
                                if(find.hasNext() && find != null){
                                    callback.onQueryDone(true);
                                    return;
                                }
                            }
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        callback.onQueryDone(false);
                    }
                });
            }
        });
    }

    @Override
    public void disconnect() {
        if(isConnected())
            connection.close();
    }
}
