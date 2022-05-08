package me.skinnyjeans.gmd.databases;

import com.mongodb.*;
import me.skinnyjeans.gmd.Main;
import me.skinnyjeans.gmd.managers.DataManager;
import me.skinnyjeans.gmd.models.ISaveManager;
import me.skinnyjeans.gmd.models.Minecrafter;
import org.bukkit.Bukkit;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MongoDB implements ISaveManager {
    private final Main plugin;
    private final String host;
    private final String port;
    private final String dbName;
    private final String user;
    private final String pwd;
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

    public DBCollection getConnection() { return connection.getDB(dbName).getCollection("dynamicdifficulty") ;}
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
    public void updatePlayer(Minecrafter playerData) {
        playerExists(playerData.getUUID(), r -> {
            try {
                if(isConnected()) {
                    BasicDBObject obj = new BasicDBObject("_id", playerData.getUUID().toString()).append("Affinity", playerData.getAffinity())
                            .append("MinAffinity", playerData.getMinAffinity()).append("MaxAffinity", playerData.getMaxAffinity())
                            .append("Name", playerData.getName());

                    if(r) {
                        getConnection().update(new BasicDBObject("_id", playerData.getUUID().toString()), obj);
                    } else {
                        getConnection().insert(obj);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void getAffinityValues(UUID uuid, findCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Minecrafter data = new Minecrafter();
            try {
                if(isConnected()) {
                    DBCursor find = getConnection().find(new BasicDBObject("_id", uuid));
                    if(find.hasNext()){
                        DBObject object = find.next();
                        data.setName(object.get("Name").toString());
                        data.setUUID(uuid);
                        data.setAffinity(Integer.parseInt(object.get("Affinity").toString()));
                        data.setMaxAffinity(Integer.parseInt(object.get("MaxAffinity").toString()));
                        data.setMinAffinity(Integer.parseInt(object.get("MinAffinity").toString()));
                    }
                }
            } catch(Exception e) { e.printStackTrace(); }
            callback.onQueryDone(data);
        });
    }

    @Override
    public void playerExists(UUID uuid, final findBooleanCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if(isConnected()) {
                    DBCursor find = getConnection().find(new BasicDBObject("_id", uuid.toString()));
                    if(find.hasNext()) callback.onQueryDone(true);
                }
            } catch(Exception e) { e.printStackTrace(); }
            callback.onQueryDone(false);
        });
    }

    @Override
    public void disconnect() {
        if(isConnected())
            connection.close();
    }
}
