package me.skinnyjeans.gmd;

import me.skinnyjeans.gmd.hooks.MySQL;
import org.bukkit.Bukkit;
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
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.logging.Level;

public class Affinity implements Listener {
    protected Main m;
    protected MySQL SQL;
    protected DataManager data;
    protected int minAffinity,maxAffinity,onDeath,onPVPKill,onPVEKill,onMined,startAffinity,onInterval,onPlayerHit,worldAffinity;
    protected HashMap<UUID, Integer> playerAffinity = new HashMap<>();
    protected HashMap<UUID, Integer> playerMaxAffinity = new HashMap<>();
    protected boolean silkTouchAllowed, calcExactPercentage, randomizer;
    protected String difficultyType;
    protected List<String> blocks, disabledWorlds;
    protected ArrayList<Integer> mobsOverrideIgnore = new ArrayList<>();
    protected HashMap<String, Integer> damageDoneByMobs = new HashMap<>();
    protected HashMap<String, Integer> experienceMultiplier = new HashMap<>();
    protected HashMap<String, Integer> damageDoneOnMobs = new HashMap<>();
    protected HashMap<String, Integer> difficultyAffinity = new HashMap<>();
    protected HashMap<String, Integer> doubleLootChance = new HashMap<>();
    protected HashMap<String, Integer> mobsPVE = new HashMap<>();
    protected HashMap<String, Boolean> effectsWhenAttacked = new HashMap<>();
    protected HashMap<String, String> prefixes = new HashMap<>();
    protected HashMap<String, List<String>> mobsIgnorePlayers = new HashMap<>();
    protected ArrayList<String> difficulties = new ArrayList<>();
    protected PotionEffectType[] effects = new PotionEffectType[] { PotionEffectType.WITHER, PotionEffectType.POISON,
            PotionEffectType.BLINDNESS, PotionEffectType.WEAKNESS, PotionEffectType.SLOW };

    public Affinity(Main ma, MySQL s) {
        m = ma;
        SQL = s;
        try {
            if(SQL != null){
                SQL.connect();
                Bukkit.getConsoleSender().sendMessage("[DynamicDifficulty] Succesfully connected to the database!");
                SQL.createTable();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        emptyHitMobsList();
        loadConfig();
    }

    public void reloadConfig() {
        saveData();
        difficultyAffinity.clear(); damageDoneByMobs.clear(); experienceMultiplier.clear(); damageDoneOnMobs.clear();
        doubleLootChance.clear(); mobsPVE.clear(); effectsWhenAttacked.clear(); prefixes.clear();
        difficulties.clear(); blocks.clear(); disabledWorlds.clear(); mobsOverrideIgnore.clear();
        loadConfig();
    }

    public void loadConfig(){
        data = new DataManager(m);
        randomizer = data.getConfig().getBoolean("difficulty-modifiers.randomize");
        silkTouchAllowed = data.getConfig().getBoolean("silk-touch-allowed");
        minAffinity = data.getConfig().getInt("min-affinity");
        maxAffinity = data.getConfig().getInt("max-affinity");
        onDeath = data.getConfig().getInt("death");
        onPVEKill = data.getConfig().getInt("pve-kill");
        onPVPKill = data.getConfig().getInt("pvp-kill");
        onMined = data.getConfig().getInt("block-mined");
        startAffinity = data.getConfig().getInt("starting-affinity");
        blocks = data.getConfig().getStringList("blocks");
        onInterval = data.getConfig().getInt("points-on-interval");
        onPlayerHit = data.getConfig().getInt("player-hit");
        difficultyType = data.getConfig().getString("difficulty-modifiers.type");
        calcExactPercentage = data.getConfig().getBoolean("calculate-exact-percentage");
        disabledWorlds = data.getConfig().getStringList("disabled-worlds");
        HashMap<Integer, String> tmpMap = new HashMap<>();
        ArrayList<String> tmpList = new ArrayList<>();
        ConfigurationSection section = data.getConfig().getConfigurationSection("difficulty");

        if(SQL != null && SQL.isConnected()){
            if(SQL.getAffinity("world") == -1)
                SQL.updatePlayer("world", startAffinity, -1);
            worldAffinity = SQL.getAffinity("world");
        } else {
            if(data.getDataFile().getInt("world.affinity") == -1)
                data.getDataFile().set("world.affinity", startAffinity);
            worldAffinity = data.getDataFile().getInt("world.affinity");
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
            experienceMultiplier.put(key, (int) (section.getDouble(key + ".experience-multiplier") * data.getConfig().getDouble("difficulty-modifiers.experience-multiplier")));
            doubleLootChance.put(key, (int) (section.getDouble(key + ".double-loot-chance") * data.getConfig().getDouble("difficulty-modifiers.double-loot-chance-multiplier")));
            damageDoneByMobs.put(key, (int) (section.getDouble(key + ".damage-done-by-mobs") * data.getConfig().getDouble("difficulty-modifiers.damage-done-by-mobs-multiplier")));
            damageDoneOnMobs.put(key, (int) (section.getDouble(key + ".damage-done-on-mobs") * data.getConfig().getDouble("difficulty-modifiers.damage-done-on-mobs-multiplier")));
            effectsWhenAttacked.put(key, section.getBoolean(key + ".effects-when-attacked"));
            prefixes.put(key, section.getString(key + ".prefix"));
            mobsIgnorePlayers.put(key, section.getStringList(key + ".mobs-ignore-player"));
        }

        // Everything beneath this comment is to sort the difficulties by their affinity requirement
        for (String s : tmpList) tmpMap.put(difficultyAffinity.get(s), s);

        TreeMap<Integer, String> tm = new TreeMap<>(tmpMap);

        for (int key : tm.keySet()) {
            difficulties.add(tmpMap.get(key).replace(" ", "_"));
        }
        tm.clear(); tmpList.clear(); tmpMap.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if(SQL != null && SQL.isConnected()){
            if (SQL.getAffinity(uuid.toString()) == -1)
                SQL.updatePlayer(uuid.toString(), startAffinity, -1);
            playerAffinity.put(uuid, SQL.getAffinity(uuid.toString()));
            playerMaxAffinity.put(uuid, SQL.getMaxAffinity(uuid.toString()));
        } else {
            if (data.getDataFile().getString("players." + uuid + ".affinity") == null) {
                data.getDataFile().set("players." + uuid + ".name", e.getPlayer().getName());
                data.getDataFile().set("players." + uuid + ".affinity", startAffinity);
                data.getDataFile().set("players." + uuid + ".max-affinity", -1);
                data.saveData();
            }
            playerAffinity.put(uuid, data.getDataFile().getInt("players." + uuid + ".affinity"));
            playerMaxAffinity.put(uuid, data.getDataFile().getInt("players." + uuid + ".max-affinity"));
        }

    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if(SQL != null && SQL.isConnected()) {
            SQL.updatePlayer(uuid.toString(), playerAffinity.get(uuid), playerMaxAffinity.get(uuid));
        } else {
            data.getDataFile().set("players." + uuid + ".affinity", playerAffinity.get(uuid));
            data.getDataFile().set("players." + uuid + ".max-affinity", playerMaxAffinity.get(uuid));
        }
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
            worldAffinity = calcAffinity(null, x);
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
    public ArrayList<String> getDifficulties() { return difficulties; }

    public String getPrefix(UUID uuid){ return prefixes.get(calcDifficulty(uuid)); }

    /* Saves all player and world data every few minutes. */
    public void saveData(){
        for (Map.Entry<UUID, Integer> e : playerAffinity.entrySet()){
            if(SQL != null && SQL.isConnected()){
                SQL.updatePlayer(e.getKey().toString(), e.getValue(), playerMaxAffinity.get(e.getKey()));
            } else {
                data.getDataFile().set("players." + e.getKey() + ".affinity", e.getValue());
                data.getDataFile().set("world.affinity", worldAffinity);
            }
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
        if(uuid != null){
            int userMax = playerMaxAffinity.get(uuid);
            if (userMax != -1 && x > userMax)
                x = userMax;
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
        if(randomizer) { return difficulties.get(new Random().nextInt(difficulties.size() - 1)); }
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
        if (difficultyType.equalsIgnoreCase("world")) {
            worldAffinity = calcAffinity(null, worldAffinity + x);
        } else {
            playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + x));
        }
    }

    /* To increase/decrease players score every few minutes */
    public void onInterval() {
        if(difficultyType.equalsIgnoreCase("world")){
            worldAffinity = calcAffinity(null, worldAffinity + onInterval);
        } else {
            Bukkit.getOnlinePlayers().forEach(name -> {
                UUID uuid = name.getUniqueId();
                playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + onInterval));
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerRespawnEvent e) {
        if(!(disabledWorlds.contains(e.getPlayer().getWorld().getName()))){
            if (onDeath != 0)
                addAmountOfAffinity(e.getPlayer().getUniqueId(), onDeath);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKill(EntityDeathEvent e) {
        if(!(disabledWorlds.contains(e.getEntity().getWorld().getName()))) {
            try {
                if ((onPVPKill != 0 || onPVEKill != 0) && e.getEntity().getKiller() instanceof Player) {
                    UUID uuid = e.getEntity().getKiller().getUniqueId();
                    if (e.getEntity() instanceof Player) {
                        addAmountOfAffinity(uuid, onPVPKill);
                    } else if (mobsPVE.get(e.getEntityType().toString()) != null) {
                        addAmountOfAffinity(uuid, mobsPVE.get(e.getEntityType().toString()));
                    }
                }

                if (!(e.getEntity() instanceof Player) && !(e.getEntity() instanceof EnderDragon) && !(e.getEntity() instanceof Wither) && e.getEntity().getKiller() instanceof Player) {
                    UUID uuid = e.getEntity().getKiller().getUniqueId();
                    e.setDroppedExp((int) (e.getDroppedExp() * calcPercentage(uuid, "experience-multiplier") / 100.0));
                    double DoubleLoot = calcPercentage(uuid, "double-loot-chance");
                    if (DoubleLoot != 0.0 && new Random().nextDouble() < DoubleLoot / 100.0 && !e.getEntity().getCanPickupItems()) {
                        for (int i = 0; i < e.getDrops().size(); i++) {
                            Bukkit.getWorld(e.getEntity().getWorld().getUID()).dropItemNaturally(e.getEntity().getLocation(), e.getDrops().get(i));
                        }
                    }
                }
            } catch (NullPointerException error) {
                Bukkit.getConsoleSender().sendMessage("NullPointerException. Enemytype Attack: " + e.getEntity().getKiller().getType() + " Enemytype Hit: " + e.getEntity().getType());
                // Ugly tmp fix
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMined(BlockBreakEvent e) {
        if(!(disabledWorlds.contains(e.getPlayer().getWorld().getName()))) {
            if (onMined != 0 && blocks.contains(e.getBlock().getBlockData().getMaterial().name()) && (!e.getPlayer().getItemOnCursor().containsEnchantment(Enchantment.SILK_TOUCH) || silkTouchAllowed) && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                UUID uuid = e.getPlayer().getUniqueId();
                addAmountOfAffinity(uuid, onMined);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHit(EntityDamageByEntityEvent e) {
        if(!(disabledWorlds.contains(e.getEntity().getWorld().getName()))) {
            Entity prey = e.getEntity();
            Entity hunter = e.getDamager();
            try {
                if (prey instanceof Player) {
                    if (!(hunter instanceof Player) && !(hunter instanceof EnderDragon) && !(hunter instanceof Wither)) {
                        UUID uuid = prey.getUniqueId();
                        playerAffinity.replace(uuid, calcAffinity(uuid, playerAffinity.get(uuid) + onPlayerHit));
                        double dam = e.getFinalDamage() * calcPercentage(uuid, "damage-done-by-mobs") / 100.0;
                        e.setDamage(dam);
                    }
                    if (!(hunter instanceof Player) && (hunter instanceof LivingEntity || hunter instanceof Arrow) && effectsWhenAttacked.get(calcDifficulty(prey.getUniqueId())))
                        for (PotionEffectType effect : effects) {
                            if (((LivingEntity) prey).hasPotionEffect(effect))
                                ((LivingEntity) prey).removePotionEffect(effect);
                        }
                } else if (hunter instanceof Player && !(prey instanceof Player) && !(prey instanceof EnderDragon) && !(prey instanceof Wither)) {
                    double dam = e.getFinalDamage() * calcPercentage(hunter.getUniqueId(), "damage-done-on-mobs") / 100.0;
                    e.setDamage(dam);
                    if(!mobsOverrideIgnore.contains(prey.getEntityId()))
                        mobsOverrideIgnore.add(prey.getEntityId());
                }
            } catch (NullPointerException error) {
                Bukkit.getConsoleSender().sendMessage("NullPointerException. Enemytype Attack: " + hunter + " Enemytype Hit: " + prey);
                // Ugly tmp fix
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerSpot(EntityTargetLivingEntityEvent e) {
        if(e.getTarget() instanceof Player) {
            if(mobsIgnorePlayers.get(calcDifficulty(e.getTarget().getUniqueId())).contains(e.getEntity().getType().toString())){
                if(!mobsOverrideIgnore.contains(e.getEntity().getEntityId()))
                    e.setCancelled(true);
            }
        }
    }

    private void emptyHitMobsList(){
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(m, () -> {
            if(mobsOverrideIgnore.size() > 0)
                mobsOverrideIgnore.clear();
        }, 0L, 20L*60);
    }
}
