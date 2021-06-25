package me.skinnyjeans.gmd;

import me.skinnyjeans.gmd.hooks.databases.File;
import me.skinnyjeans.gmd.hooks.databases.SQL;
import me.skinnyjeans.gmd.hooks.SaveManager;
import me.skinnyjeans.gmd.hooks.databases.MongoDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class Affinity implements Listener {
    protected Main m;
    protected SaveManager SQL;
    protected DataManager data;
    protected int minAffinity,maxAffinity,onDeath,onPVPKill,onPVEKill,onMined,startAffinity,onInterval,onPlayerHit,worldAffinity;
    protected HashMap<UUID, Integer> playerAffinity = new HashMap<>();
    protected HashMap<UUID, Integer> playerMaxAffinity = new HashMap<>();
    protected HashMap<UUID, Integer> playerMinAffinity = new HashMap<>();
    protected boolean silkTouchAllowed,calcExactPercentage,randomizer;
    protected String difficultyType, saveType;
    protected List<String> disabledWorlds,disabledMobs;
    protected ArrayList<Integer> mobsOverrideIgnore = new ArrayList<>();
    protected HashMap<String, Integer> damageDoneByMobs = new HashMap<>();
    protected HashMap<String, Integer> experienceMultiplier = new HashMap<>();
    protected HashMap<String, Integer> damageDoneOnMobs = new HashMap<>();
    protected HashMap<String, Integer> difficultyAffinity = new HashMap<>();
    protected HashMap<String, Integer> doubleLootChance = new HashMap<>();
    protected HashMap<String, Integer> mobsPVE = new HashMap<>();
    protected HashMap<String, Integer> blocks = new HashMap<>();
    protected HashMap<String, Boolean> effectsWhenAttacked = new HashMap<>();
    protected HashMap<String, String> prefixes = new HashMap<>();
    protected HashMap<String, List<String>> mobsIgnorePlayers = new HashMap<>();
    protected ArrayList<String> difficulties = new ArrayList<>();
    protected ArrayList<PotionEffectType> effects = new ArrayList<>(Arrays.asList(PotionEffectType.WITHER, PotionEffectType.POISON,
            PotionEffectType.BLINDNESS, PotionEffectType.WEAKNESS, PotionEffectType.SLOW, PotionEffectType.CONFUSION, PotionEffectType.HUNGER));
    protected ArrayList<EntityPotionEffectEvent.Cause> effectCauses = new ArrayList<>(Arrays.asList(EntityPotionEffectEvent.Cause.ATTACK,
            EntityPotionEffectEvent.Cause.ARROW, EntityPotionEffectEvent.Cause.POTION_SPLASH));

    public Affinity(Main ma) {
        m = ma;
        emptyHitMobsList();
        loadConfig();
        if(Bukkit.getOnlinePlayers().size() > 0) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[DynamicDifficulty] Reloading or loading DynamicDifficulty with a plugin manager may break it");
            Bukkit.getOnlinePlayers().forEach(name -> {
                UUID uuid = name.getUniqueId();
                SQL.getAffinityValues(uuid.toString(), new findIntegerCallback() {
                    @Override
                    public void onQueryDone(List<Integer> r) {
                        if (r.get(0) == -1) {
                            playerAffinity.put(uuid, startAffinity);
                            playerMaxAffinity.put(uuid, -1);
                            playerMinAffinity.put(uuid, -1);
                            SQL.updatePlayer(uuid.toString(), startAffinity, -1, -1);
                        } else {
                            playerAffinity.put(uuid, r.get(0));
                            playerMaxAffinity.put(uuid, r.get(1));
                            playerMaxAffinity.put(uuid, r.get(2));
                        }
                    }
                });
            });
        }
    }

    public void reloadConfig() {
        saveData();
        difficultyAffinity.clear(); damageDoneByMobs.clear(); experienceMultiplier.clear(); damageDoneOnMobs.clear();
        doubleLootChance.clear(); mobsPVE.clear(); effectsWhenAttacked.clear(); prefixes.clear(); blocks.clear();
        difficulties.clear(); disabledWorlds.clear(); mobsOverrideIgnore.clear();
        loadConfig();
    }

    /** Load's everything in from the config file and sorts or calculates different data from it */
    public void loadConfig(){
        data = new DataManager(m);
        saveType = data.getConfig().getString("saving-data.type");
        randomizer = data.getConfig().getBoolean("difficulty-modifiers.randomize");
        silkTouchAllowed = data.getConfig().getBoolean("silk-touch-allowed");
        minAffinity = data.getConfig().getInt("min-affinity");
        maxAffinity = data.getConfig().getInt("max-affinity");
        onDeath = data.getConfig().getInt("death");
        onPVEKill = data.getConfig().getInt("pve-kill");
        onPVPKill = data.getConfig().getInt("pvp-kill");
        onMined = data.getConfig().getInt("block-mined");
        startAffinity = data.getConfig().getInt("starting-affinity");
        onInterval = data.getConfig().getInt("points-per-minute");
        onPlayerHit = data.getConfig().getInt("player-hit");
        difficultyType = data.getConfig().getString("difficulty-modifiers.type");
        calcExactPercentage = data.getConfig().getBoolean("difficulty-modifiers.exact-percentage");
        disabledWorlds = data.getConfig().getStringList("disabled-worlds");
        disabledMobs = data.getConfig().getStringList("disabled-mobs");
        HashMap<Integer, String> tmpMap = new HashMap<>();
        ArrayList<String> tmpList = new ArrayList<>();
        ConfigurationSection section = data.getConfig().getConfigurationSection("difficulty");

        if(SQL == null) {
            try{
                if(saveType.equalsIgnoreCase("mysql") || saveType.equalsIgnoreCase("sqlite") || saveType.equalsIgnoreCase("postgresql")){
                    SQL = new SQL(m, data, saveType);
                } else if(saveType.equalsIgnoreCase("mongodb")) {
                    SQL = new MongoDB(m, data);
                } else {
                    SQL = new File(m, data);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[DynamicDifficulty] Can't connect to the database, switching to 'file' mode");
                SQL = new File(m, data);
            }
        }

        try {
            SQL.getAffinityValues("world", new findIntegerCallback() {
                @Override
                public void onQueryDone(List<Integer> r) {
                    if(r.get(0) == -1){
                        SQL.updatePlayer("world", startAffinity, -1, -1);
                        worldAffinity = startAffinity;
                    } else {
                        worldAffinity = r.get(0);
                    }
                }
            });
        } catch(Exception e){
            e.printStackTrace();
        }

        Object[] tmpMobs = data.getConfig().getList("mobs-count-as-pve").toArray();
        for(Object s : tmpMobs){
            String[] sep = s.toString().replaceAll("[{|}]","").split("=");
            try{
                mobsPVE.put(sep[0], Integer.parseInt(sep[1]));
            } catch(Exception e){
                mobsPVE.put(sep[0], onPVEKill);
            }
        }

        Object[] tmpBlocks = data.getConfig().getList("blocks").toArray();
        for(Object s : tmpBlocks){
            String[] sep = s.toString().replaceAll("[{|}]","").split("=");
            try{
                blocks.put(sep[0], Integer.parseInt(sep[1]));
            } catch(Exception e){
                blocks.put(sep[0], onMined);
            }
        }

        if(minAffinity > maxAffinity){
            int tmp = maxAffinity;
            maxAffinity = minAffinity;
            minAffinity = tmp;
            Bukkit.getLogger().log(Level.WARNING, "[DynamicDifficulty] MinAffinity is larger than MaxAffinity, so their values have been switched.");
        } else if (minAffinity == maxAffinity){
            maxAffinity = 0;
            minAffinity = 1200;
            Bukkit.getLogger().log(Level.WARNING, "[DynamicDifficulty] MinAffinity has the same value as MaxAffinity, so their values have been set to default.");
        }

        for (String key : section.getKeys(false)) {
            tmpList.add(key);
            difficultyAffinity.put(key, section.getInt(key + ".affinity-required"));
            experienceMultiplier.put(key, (int) (section.getDouble(key + ".experience-multiplier", 100) * data.getConfig().getDouble("difficulty-modifiers.experience-multiplier", 1)));
            doubleLootChance.put(key, (int) (section.getDouble(key + ".double-loot-chance", 100) * data.getConfig().getDouble("difficulty-modifiers.double-loot-chance-multiplier", 1)));
            damageDoneByMobs.put(key, (int) (section.getDouble(key + ".damage-done-by-mobs", 100) * data.getConfig().getDouble("difficulty-modifiers.damage-done-by-mobs-multiplier", 1)));
            damageDoneOnMobs.put(key, (int) (section.getDouble(key + ".damage-done-on-mobs", 100) * data.getConfig().getDouble("difficulty-modifiers.damage-done-on-mobs-multiplier", 1)));
            effectsWhenAttacked.put(key, section.getBoolean(key + ".effects-when-attacked", true));
            prefixes.put(key, section.getString(key + ".prefix"));
            mobsIgnorePlayers.put(key, section.getStringList(key + ".mobs-ignore-player"));
        }

        // Everything beneath this comment is to sort the difficulties by their affinity requirement
        for (String s : tmpList) tmpMap.put(difficultyAffinity.get(s), s);
        TreeMap<Integer, String> tm = new TreeMap<>(tmpMap);
        for (int key : tm.keySet()) difficulties.add(tmpMap.get(key).replace(" ", "_"));
        tm.clear(); tmpList.clear(); tmpMap.clear();
    }

    public int getAffinity(UUID uuid) {
        if(uuid == null)
            return worldAffinity;
        return playerAffinity.get(uuid);
    }

    public void setAffinity(UUID uuid, int x) {
        if (uuid == null) { worldAffinity = calcAffinity(null, x); }
        else { playerAffinity.replace(uuid, calcAffinity(uuid, x)); }
    }

    private void emptyHitMobsList(){
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(m, () -> {
            if(mobsOverrideIgnore.size() > 0)
                mobsOverrideIgnore.clear();
        }, 0L, 1200L);
    }

    public int getMaxAffinity(UUID uuid) { return playerMaxAffinity.get(uuid); }
    public void setMaxAffinity(UUID uuid, int x) { playerMaxAffinity.replace(uuid, calcAffinity(null, x)); }
    public int getMinAffinity(UUID uuid) { return playerMinAffinity.get(uuid); }
    public void setMinAffinity(UUID uuid, int x) { playerMinAffinity.replace(uuid, calcAffinity(null, x)); }
    public int getVariableMaxAffinity(){ return maxAffinity; }
    public int getVariableMinAffinity(){ return minAffinity; }
    public boolean hasDifficulty(String x) { return difficulties.contains(x); }
    public int getDifficultyAffinity(String x) { return difficultyAffinity.get(x); }
    public ArrayList<String> getDifficulties() { return difficulties; }
    public String getPrefix(UUID uuid){ return prefixes.get(calcDifficulty(uuid)); }
    public interface findIntegerCallback { void onQueryDone(List<Integer> r); }

    /** Saves all player and world data every few minutes. */
    public void saveData(){
        SQL.updatePlayer("world", worldAffinity, -1, -1);
        for (Map.Entry<UUID, Integer> e : playerAffinity.entrySet()){
            SQL.updatePlayer(e.getKey().toString(), e.getValue(), playerMaxAffinity.get(e.getKey()), playerMinAffinity.get(e.getKey()));
        }
    }

    /**
     * Calculates if the amount exceeds the users Maximum or the servers Minimum/Maximum
     *
     * @param uuid of the user
     * @param x is the affinity given to calculate
     * @return INT the affinity after it has been checked
     */
    public int calcAffinity(UUID uuid, int x) {
        if(x == -1) { return -1; }
        if(uuid != null){
            int userMax = playerMaxAffinity.get(uuid);
            int userMin = playerMinAffinity.get(uuid);
            if (userMax != -1 && x > userMax){
                x = userMax;
            } else if (userMin != -1 && x < userMin){
                x = userMin;
            }
        }

        if (x > maxAffinity) {
            x = maxAffinity;
        } else if (x < minAffinity) {
            x = minAffinity;
        }
        return x;
    }

    /** Closes all databases */
    public void exitProgram() {
        try{
            SQL.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the difficulty of an user or the world
     *
     * @param uuid of the user or the world if null
     * @return String of the difficulty the world/user is on
     */
    public String calcDifficulty(UUID uuid) {
        if(randomizer) { return difficulties.get(new Random().nextInt(difficulties.size() - 1)); }
        String last = "";
        int af = worldAffinity;
        if(uuid != null) { af = playerAffinity.get(uuid); }
        try {
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
        } catch(IndexOutOfBoundsException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[DynamicDifficulty] "+e.toString()+": Looks like the difficulties didn't load in properly, will try to load them in again. Unless difficulties is empty...");
            reloadConfig();
            if(difficulties.size() == 0) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[DynamicDifficulty] Difficulties still haven't loaded in after reloading, Make sure you have atleast 1 difficulty in the config!");
                difficulties.add("Normal"); difficultyAffinity.put("Normal", 0);
                experienceMultiplier.put("Normal",90); doubleLootChance.put("Normal", 0);
                damageDoneByMobs.put("Normal",75); damageDoneOnMobs.put("Normal", 100);
                effectsWhenAttacked.put("Normal", true); prefixes.put("Normal", "&7&l[&9&lNormal&7&l]&r");
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW+"[DynamicDifficulty] Added the Normal difficulty from the default config");
            }
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
        if(randomizer) { return getHashData(mode, calcDifficulty(uuid)); }
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

    private void addAmountOfAffinity(UUID uuid, int x){
        if (difficultyType.equalsIgnoreCase("world")) { worldAffinity = calcAffinity(null, worldAffinity + x);
        } else { playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + x)); }
    }

    /* To increase/decrease players score every few minutes */
    public void onInterval() {
        if(difficultyType.equalsIgnoreCase("world")){
            worldAffinity = calcAffinity(null, worldAffinity + onInterval);
        } else {
            if(Bukkit.getOnlinePlayers().size() > 0){
                Bukkit.getOnlinePlayers().forEach(name -> {
                    UUID uuid = name.getUniqueId();
                    playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + onInterval));
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerRespawnEvent e) {
        if(!disabledWorlds.contains(e.getPlayer().getWorld().getName()) && onDeath != 0)
            addAmountOfAffinity(e.getPlayer().getUniqueId(), onDeath);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKill(EntityDeathEvent e) {
        if(!disabledWorlds.contains(e.getEntity().getWorld().getName())) {
            try {
                if(e.getEntity().getKiller() instanceof Player) {
                    UUID uuid = e.getEntity().getKiller().getUniqueId();
                    if (onPVEKill != 0 && e.getEntity() instanceof Player) {
                        addAmountOfAffinity(uuid, onPVPKill);
                    } else if (onPVPKill != 0 && mobsPVE.get(e.getEntityType().toString()) != null) {
                        addAmountOfAffinity(uuid, mobsPVE.get(e.getEntityType().toString()));
                    }

                    if (!(e.getEntity() instanceof Player) && !disabledMobs.contains(e.getEntityType().toString())) {
                        e.setDroppedExp((int) (e.getDroppedExp() * calcPercentage(uuid, "experience-multiplier") / 100.0));
                        double DoubleLoot = calcPercentage(uuid, "double-loot-chance");
                        if (DoubleLoot != 0.0 && new Random().nextDouble() < DoubleLoot / 100.0 && !e.getEntity().getCanPickupItems()) {
                            for (int i = 0; i < e.getDrops().size(); i++) {
                                Bukkit.getWorld(e.getEntity().getWorld().getUID()).dropItemNaturally(e.getEntity().getLocation(), e.getDrops().get(i));
                            }
                        }
                    }
                }
            } catch (NullPointerException er) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[Dynamic Difficulty] NullPointerException. A plugin might be causing issues");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMined(BlockBreakEvent e) {
        if(!disabledWorlds.contains(e.getPlayer().getWorld().getName()))
            if (onMined != 0 && blocks.get(e.getBlock().getBlockData().getMaterial().name()) != null)
                if(!e.getPlayer().getItemOnCursor().containsEnchantment(Enchantment.SILK_TOUCH) || silkTouchAllowed)
                    if(e.getPlayer().getGameMode() != GameMode.CREATIVE)
                        addAmountOfAffinity(e.getPlayer().getUniqueId(), onMined);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHit(EntityDamageByEntityEvent e) {
        if(!disabledWorlds.contains(e.getEntity().getWorld().getName())) {
            Entity prey = e.getEntity();
            Entity hunter = e.getDamager();
            try {
                if (prey instanceof Player) {
                    if (!(hunter instanceof Player) && !disabledMobs.contains(hunter.getType().toString())) {
                        UUID uuid = prey.getUniqueId();
                        playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + onPlayerHit));
                        double dam = e.getFinalDamage() * calcPercentage(uuid, "damage-done-by-mobs") / 100.0;
                        e.setDamage(dam);
                    }
                } else if (hunter instanceof Player && !disabledMobs.contains(prey.getType().toString())) {
                    double dam = e.getFinalDamage() * calcPercentage(hunter.getUniqueId(), "damage-done-on-mobs") / 100.0;
                    e.setDamage(dam);
                    if(!mobsOverrideIgnore.contains(prey.getEntityId()))
                        mobsOverrideIgnore.add(prey.getEntityId());
                }
            } catch (NullPointerException er) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[Dynamic Difficulty] NullPointerException. A plugin might be causing issues");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPotionEffect(EntityPotionEffectEvent e) {
        if(e.getEntity() instanceof Player)
            if(!effectsWhenAttacked.get(calcDifficulty(e.getEntity().getUniqueId())))
                if(effectCauses.contains(e.getCause()))
                    if(effects.contains(e.getModifiedType()))
                        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerSpot(EntityTargetLivingEntityEvent e) {
        if(e.getTarget() instanceof Player)
            if(mobsIgnorePlayers.get(calcDifficulty(e.getTarget().getUniqueId())).contains(e.getEntity().getType().toString()))
                if(!mobsOverrideIgnore.contains(e.getEntity().getEntityId()))
                    e.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        SQL.getAffinityValues(uuid.toString(), new findIntegerCallback() {
            @Override
            public void onQueryDone(List<Integer> r) {
                if (r.get(0) == -1){
                    playerAffinity.put(uuid, startAffinity);
                    playerMaxAffinity.put(uuid, -1);
                    playerMinAffinity.put(uuid, -1);
                    SQL.updatePlayer(uuid.toString(), startAffinity, -1, -1);
                } else {
                    playerAffinity.put(uuid, r.get(0));
                    playerMaxAffinity.put(uuid, r.get(1));
                    playerMinAffinity.put(uuid, r.get(2));
                }
            }
        });
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        SQL.updatePlayer(uuid.toString(), playerAffinity.get(uuid), playerMaxAffinity.get(uuid), playerMinAffinity.get(uuid));
        playerAffinity.remove(uuid);
        playerMaxAffinity.remove(uuid);
        playerMinAffinity.remove(uuid);
    }
}
