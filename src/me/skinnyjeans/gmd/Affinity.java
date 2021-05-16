package me.skinnyjeans.gmd;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Level;

public class Affinity implements Listener {
    protected DataManager data;
    protected int minAffinity,maxAffinity,onDeath,onPVPKill,onPVEKill,onMined,startAffinity,onInterval,onPlayerHit,worldAffinity;
    protected HashMap<UUID, Integer> playerAffinity = new HashMap<>();
    protected HashMap<UUID, Integer> playerMaxAffinity = new HashMap<>();
    protected boolean silkTouchAllowed, calcExactPercentage, loadAllUserData;
    protected List<String> mobsPVE, blocks;
    protected HashMap<String, Integer> damageDoneByMobs = new HashMap<>();
    protected HashMap<String, Integer> experienceMultiplier = new HashMap<>();
    protected HashMap<String, Integer> damageDoneOnMobs = new HashMap<>();
    protected HashMap<String, Integer> difficultyAffinity = new HashMap<>();
    protected HashMap<String, Integer> doubleLootChance = new HashMap<>();
    protected HashMap<String, Boolean> effectsWhenAttacked = new HashMap<>();
    protected ArrayList<String> difficulties = new ArrayList<>();
    protected PotionEffectType[] effects = new PotionEffectType[] { PotionEffectType.WITHER, PotionEffectType.POISON,
            PotionEffectType.BLINDNESS, PotionEffectType.WEAKNESS, PotionEffectType.SLOW };

    public Affinity(Main m) {
        data = new DataManager(m);
        silkTouchAllowed = data.getConfig().getBoolean("silk-touch-allowed");
        minAffinity = data.getConfig().getInt("min-affinity");
        maxAffinity = data.getConfig().getInt("max-affinity");
        onDeath = data.getConfig().getInt("death");
        onPVEKill = data.getConfig().getInt("pve-kill");
        onPVPKill = data.getConfig().getInt("pvp-kill");
        onMined = data.getConfig().getInt("block-mined");
        startAffinity = data.getConfig().getInt("starting-affinity");
        mobsPVE = data.getConfig().getStringList("mobs-count-as-pve");
        blocks = data.getConfig().getStringList("blocks");
        onInterval = data.getConfig().getInt("points-on-interval");
        onPlayerHit = data.getConfig().getInt("player-hit");
        calcExactPercentage = data.getConfig().getBoolean("calculate-exact-percentage");
        HashMap<Integer, String> tmpMap = new HashMap<>();
        ArrayList<String> tmpList = new ArrayList<>();
        ConfigurationSection section = data.getConfig().getConfigurationSection("difficulty");

        if(data.getDataFile().getInt("world.affinity") == -1)
            data.getDataFile().set("world.affinity", startAffinity);

        worldAffinity = data.getDataFile().getInt("world.affinity");

        if(minAffinity > maxAffinity){
            int tmp = maxAffinity;
            maxAffinity = minAffinity;
            minAffinity = tmp;
            Bukkit.getLogger().log(Level.WARNING, "[DynamicDifficulty] MinAffinity is larger than MaxAffinity, so their values have been switched.");
        }
        else if (minAffinity == maxAffinity){
            maxAffinity = 0;
            minAffinity = 1200;
            Bukkit.getLogger().log(Level.WARNING, "[DynamicDifficulty] MinAffinity has the same value as MaxAffinity, so their values have been set to default.");
        }

        for (String key : section.getKeys(false)) {
            tmpList.add(key);
            difficultyAffinity.put(key, section.getInt(key + ".affinity-required"));
            experienceMultiplier.put(key, section.getInt(key + ".experience-multiplier"));
            doubleLootChance.put(key, section.getInt(key + ".double-loot-chance"));
            damageDoneByMobs.put(key, section.getInt(key + ".damage-done-by-mobs"));
            damageDoneOnMobs.put(key, section.getInt(key + ".damage-done-on-mobs"));
            effectsWhenAttacked.put(key, section.getBoolean(key + ".effects-when-attacked"));
        }

        // Everything beneath this comment is to sort the difficulties by their affinity requirement
        for (String s : tmpList) tmpMap.put(difficultyAffinity.get(s), s);

        TreeMap<Integer, String> tm = new TreeMap<>(tmpMap);

        for (int key : tm.keySet()) {
            difficulties.add(tmpMap.get(key));
        }
        tm.clear(); tmpList.clear(); tmpMap.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (data.getDataFile().getString("players." + uuid + ".affinity") == null) {
            data.getDataFile().set("players." + uuid + ".name", e.getPlayer().getName());
            data.getDataFile().set("players." + uuid + ".affinity", startAffinity);
            data.getDataFile().set("players." + uuid + ".max-affinity", -1);
            data.saveData();
        }
        playerAffinity.put(uuid, data.getDataFile().getInt("players." + uuid + ".affinity"));
        playerMaxAffinity.put(uuid, data.getDataFile().getInt("players." + uuid + ".max-affinity"));
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        data.getDataFile().set("players." + uuid + ".affinity", playerAffinity.get(uuid));
        data.getDataFile().set("players." + uuid + ".max-affinity", playerMaxAffinity.get(uuid));
        data.saveData();
        playerAffinity.remove(uuid);
        playerMaxAffinity.remove(uuid);
    }

    public int getAffinity(UUID uuid) {
        if(uuid == null)
            return worldAffinity;
        return playerAffinity.get(uuid);
    }

    public void setAffinity(UUID uuid, int x) {
        if (uuid == null) {
            worldAffinity = calcAffinity(uuid, x);
        } else {
            playerAffinity.replace(uuid, calcAffinity(uuid, x));
        }
    }

    public int getMaxAffinity(UUID uuid) { return playerMaxAffinity.get(uuid); }
    public void setMaxAffinity(UUID uuid, int x) { playerMaxAffinity.replace(uuid, calcAffinity(uuid, x)); }

    public int getVariableMaxAffinity(){ return maxAffinity; }
    public int getVariableMinAffinity(){ return minAffinity; }

    public boolean hasDifficulty(String x) { return difficulties.contains(x); }
    public int getDifficultyAffinity(String x) { return difficultyAffinity.get(x); }
    public void onInterval(){};
    public ArrayList<String> getDifficulties() { return difficulties; }

    // Saves all player and world data every few minutes.
    public void saveData(){
        for (Map.Entry<UUID, Integer> e : playerAffinity.entrySet())
            data.getDataFile().set("players." + e.getKey() + ".affinity", e.getValue());

        data.getDataFile().set("world.affinity", worldAffinity);
    }

    /**
     * Calculates if the amount exceeds the users Maximum or the servers Minimum/Maximum
     *
     * @param uuid of the user
     * @param x is the affinity given to calculate
     * @return INT the affinity after it has been checked
     */
    public int calcAffinity(UUID uuid, int x) {
        if(uuid != null){
            int userMax = playerMaxAffinity.get(uuid);
            if (userMax != -1 && x > userMax) {
                x = userMax;
            }
        }

        if (x > maxAffinity) {
            x = maxAffinity;
        } else if (x < minAffinity) {
            x = minAffinity;
        }
        return x;
    }

    /**
     * Gets the difficulty of an user or the world
     *
     * @param uuid of the user or the world if null
     * @return String of the difficulty the world/user is on
     */
    public String calcDifficulty(UUID uuid) {
        String last = "";
        int af = worldAffinity;
        if(uuid != null) { af = playerAffinity.get(uuid); }

        for (int i = 0; i < difficulties.size(); i++) {
            if (af == difficultyAffinity.get(difficulties.get(i)))
                return difficulties.get(i);
            if (!last.equals("") && difficultyAffinity.get(difficulties.get(i)) >= af && difficultyAffinity.get(last) <= af)
                return last;
            if (i + 1 == difficulties.size())
                return difficulties.get(i);
            last = difficulties.get(i);
        }
        return difficulties.get(0);
    }

    /**
     * Calculates the exact percentage between 2 difficulties
     *
     * @param uuid of the user
     * @param mode which is used to select the correct variable
     * @return Double of the exact or the difficulty based percentage
     */
    public double calcPercentage(UUID uuid, String mode) {
        int thisDiff = difficulties.indexOf(calcDifficulty(null));
        int affinity = worldAffinity;
        if(uuid != null){
            thisDiff = difficulties.indexOf(calcDifficulty(uuid));
            affinity = playerAffinity.get(uuid);
        }

        if (thisDiff + 1 != difficulties.size() && calcExactPercentage) {
            int differencePercentage = getHashData(mode, difficulties.get(thisDiff+1)) - getHashData(mode, difficulties.get(thisDiff));

            if(differencePercentage == 0)
                return getHashData(mode, difficulties.get(thisDiff));

            if(differencePercentage < 0)
                differencePercentage*=-1;

            int a = difficultyAffinity.get(difficulties.get(thisDiff+1));
            int b = difficultyAffinity.get(difficulties.get(thisDiff));
            double c = (100.0 / (a - b) * (affinity - b));
            double extraPercentage = (differencePercentage / 100.0) * c;

            return (getHashData(mode, difficulties.get(thisDiff)) + extraPercentage);
        }
        return getHashData(mode, difficulties.get(thisDiff));
    }

    /**
     * Returns data from the made HashMaps
     *
     * @param mode which is used to select the correct variable
     * @param diff which is the difficulty setting
     * @return INT from the selected variable and difficulty
     */
    private int getHashData(String mode, String diff) {
        if(mode.equalsIgnoreCase("damage-done-by-mobs")){ return damageDoneByMobs.get(diff); }
        if(mode.equalsIgnoreCase("damage-done-on-mobs")) { return damageDoneOnMobs.get(diff); }
        if(mode.equalsIgnoreCase("experience-multiplier")) { return experienceMultiplier.get(diff); }
        if(mode.equalsIgnoreCase("double-loot-chance")) { return doubleLootChance.get(diff); }
        return -1;
    }
}
