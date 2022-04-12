//package me.skinnyjeans.gmd;
//
//import me.skinnyjeans.gmd.models.ISaveManager;
//import me.skinnyjeans.gmd.databases.*;
//import me.skinnyjeans.gmd.models.Difficulty;
//import me.skinnyjeans.gmd.models.EquipmentItems;
//import me.skinnyjeans.gmd.models.Minecrafter;
//import org.bukkit.*;
//import org.bukkit.block.Biome;
//import org.bukkit.configuration.ConfigurationSection;
//import org.bukkit.configuration.file.FileConfiguration;
//import org.bukkit.enchantments.Enchantment;
//import org.bukkit.entity.EntityType;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.Inventory;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.inventory.meta.SkullMeta;
//
//import java.util.*;
//import java.util.logging.Level;
//
//public class Affinity {
//    protected Main m;
//    protected ISaveManager SQL;
//    protected DataManager data;
//    protected FileConfiguration config;
//    protected int minAffinity,maxAffinity,onDeath,onPVPKill,startAffinity,onInterval,onPlayerHit,worldAffinity,affinityPerHeart;
//    protected boolean randomizer,customArmorSpawnChance,calculateExtraArmorDamage,preventAffinityLossOnSuicide;
//    protected String difficultyType,saveType;
//    protected List<String> disabledMobs = new ArrayList<>();
//    protected List<String> disabledWorlds = new ArrayList<>();
//    protected List<String> maxAffinityListItems = new ArrayList<>();
//    protected List<String> minAffinityListItems = new ArrayList<>();
//    protected ArrayList<Integer> mobsOverrideIgnore = new ArrayList<>();
//    protected ArrayList<Integer> ignoreMobs = new ArrayList<>();
//    protected HashMap<String, Difficulty> difficultyList = new HashMap<>();
//    protected HashMap<UUID, Minecrafter> playerList = new HashMap<>();
//    protected HashMap<String, Minecrafter> biomeList = new HashMap<>();
//    protected HashMap<String, Integer> chancePerArmor = new HashMap<>();
//    protected HashMap<String, Integer> chancePerWeapon = new HashMap<>();
//    protected HashMap<String, ArrayList<String>> enchantmentList = new HashMap<>();
//    protected HashMap<String, Integer> mobsPVE = new HashMap<>();
//    protected HashMap<String, Integer> blocks = new HashMap<>();
//    protected HashMap<String, Inventory> inventorySettings = new HashMap<>();
//    protected HashMap<String, UUID> playersUUID = new HashMap<>();
//    protected ArrayList<String> difficulties = new ArrayList<>();
//    protected ArrayList<String> customSpawnWeapons = new ArrayList<>();
//    protected ArrayList<Material> ranged = new ArrayList<>(Arrays.asList(Material.BOW, Material.CROSSBOW));
//    protected ArrayList<List<Enchantment>> enchantmentConflict = new ArrayList<>(Arrays.asList(
//            Arrays.asList(Enchantment.MENDING, Enchantment.ARROW_INFINITE),
//            Arrays.asList(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_PROJECTILE),
//            Arrays.asList(Enchantment.SILK_TOUCH, Enchantment.LOOT_BONUS_BLOCKS),
//            Arrays.asList(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ARTHROPODS, Enchantment.DAMAGE_UNDEAD),
//            Arrays.asList(Enchantment.DEPTH_STRIDER, Enchantment.FROST_WALKER)));
//
//    public Affinity(Main ma) {
//        m = ma;
//        loadConfig();
//        if(Bukkit.getOnlinePlayers().size() > 0) {
//            Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[DynamicDifficulty] Reloading or loading DynamicDifficulty with a plugin manager may break it!");
//            Bukkit.getOnlinePlayers().forEach(usr -> {
//                UUID uuid = usr.getUniqueId();
//                SQL.getAffinityValues(uuid.toString(), r -> {
//                    Minecrafter mc = new Minecrafter(usr.getName());
//                    if (r.get(0) == -1) {
//                        mc.setAffinity(startAffinity);
//                        mc.setMaxAffinity(-1);
//                        mc.setMinAffinity(-1);
//                        playerList.put(uuid, mc);
//                        SQL.updatePlayer(uuid.toString(), startAffinity, -1, -1);
//                    } else {
//                        mc.setAffinity(r.get(0));
//                        mc.setMaxAffinity(r.get(1));
//                        mc.setMinAffinity(r.get(2));
//                        playerList.put(uuid, mc);
//                    }
//                    playersUUID.put(usr.getName(), uuid);
//                });
//            });
//        }
//    }
//
//    public void reloadConfig() {
//        saveData();
//        disabledMobs.clear(); disabledWorlds.clear(); maxAffinityListItems.clear();
//        minAffinityListItems.clear(); mobsOverrideIgnore.clear(); ignoreMobs.clear();
//        difficultyList.clear(); chancePerArmor.clear(); customSpawnWeapons.clear();
//        chancePerWeapon.clear(); enchantmentList.clear(); mobsPVE.clear();
//        blocks.clear(); inventorySettings.clear(); difficulties.clear();
//        loadConfig();
//    }
//
//    /** Load's everything in from the config file and sorts or calculates different data from it */
//    private void loadConfig(){
//        data = new DataManager(m);
//        config = data.getConfig();
//        saveType = config.getString("saving-data.type", "file").toLowerCase();
//        randomizer = config.getBoolean("difficulty-modifiers.randomize", false);
//        minAffinity = config.getInt("min-affinity", 0);
//        maxAffinity = config.getInt("max-affinity", 1500);
//        onDeath = config.getInt("death", -100);
//        onPVPKill = config.getInt("pvp-kill", 20);
//        startAffinity = config.getInt("starting-affinity", 600);
//        onInterval = config.getInt("points-per-minute", 3);
//        onPlayerHit = config.getInt("player-hit", -1);
//        difficultyType = config.getString("difficulty-modifiers.type", "player").toLowerCase();
//        disabledWorlds = config.getStringList("disabled-worlds");
//        disabledMobs = config.getStringList("disabled-mobs");
//        affinityPerHeart = data.getConfig().getInt("affinity-per-heart-loss", -1);
//        customArmorSpawnChance = config.getBoolean("advanced-features.custom-mob-items-spawn-chance", false);
//        preventAffinityLossOnSuicide = config.getBoolean("prevent-affinity-loss-on-suicide", false);
//        calculateExtraArmorDamage = false;
//        HashMap<Integer, String> tmpMap = new HashMap<>();
//        ArrayList<String> tmpList = new ArrayList<>();
//        ConfigurationSection section = config.getConfigurationSection("difficulty");
//
//        if(section.getKeys(false).size() == 0) {
//            Bukkit.getLogger().log(Level.WARNING, "[DynamicDifficulty] You don't have any custom difficulties!!! Disabling Dynamic Difficulty.");
//            Bukkit.getPluginManager().disablePlugin(m);
//        }
//
//        if(minAffinity > maxAffinity) {
//            int tmp = maxAffinity;
//            maxAffinity = minAffinity;
//            minAffinity = tmp;
//            Bukkit.getLogger().log(Level.WARNING, "[DynamicDifficulty] MinAffinity is larger than MaxAffinity, so their values have been switched.");
//        }
//
//        for (String key : section.getKeys(false)) {
//            Difficulty tmp = new Difficulty(key);
//
//            String d = "difficulty-modifiers.";
//            int xpMult = (int) Math.ceil(section.getDouble(key + ".experience-multiplier", 100.0) * config.getDouble(d+"experience-multiplier", 1.0));
//            int dblLoot = (int) Math.ceil(section.getDouble(key + ".double-loot-chance", 100.0) * config.getDouble(d+"double-loot-chance-multiplier", 1.0));
//            int dmgByMobs = (int) Math.ceil(section.getDouble(key + ".damage-done-by-mobs", 100.0) * config.getDouble(d+"damage-done-by-mobs-multiplier", 1.0));
//            int dmgOnMobs = (int) Math.ceil(section.getDouble(key + ".damage-done-on-mobs", 100.0) * config.getDouble(d+"damage-done-on-mobs-multiplier", 1.0));
//
//            tmp.setAffinity(section.getInt(key + ".affinity-required"));
//
//            if(section.isSet(key + ".mobs-ignore-player"))
//                tmp.setIgnoredMobs(section.getStringList(key + ".mobs-ignore-player"));
//
//            if(section.isSet(key + ".commands-not-allowed-on-difficulty"))
//                tmp.setDisabledCommands(section.getStringList(key + ".commands-not-allowed-on-difficulty"));
//
//            if(section.isSet(key + ".extra-damage-for-certain-armor-types")){
//                ConfigurationSection damageTypes = section.getConfigurationSection(key + ".extra-damage-for-certain-armor-types");
//                HashMap<String, Integer> damage = new HashMap<>();
//                for(String value : damageTypes.getKeys(false))
//                    damage.put(value, damageTypes.getInt(value));
//                tmp.setArmorDamageMultiplier(damage);
//                calculateExtraArmorDamage = true;
//            }
//
//            tmp.setPrefix(section.getString(key + ".prefix", key));
//            tmp.setHungerDrain(section.getInt(key + ".hunger-drain-chance", 100));
//            tmp.setKeepInventory(section.getBoolean(key + ".keep-inventory", false));
//            tmp.setEffectsOnAttack(section.getBoolean(key + ".effects-when-attacked", true));
//            tmp.setDamageByRangedMobs(section.getInt(key + ".damage-done-by-ranged-mobs", 100));
//            tmp.setAllowPVP(section.getBoolean(key + ".allow-pvp", true));
//            tmp.setDoubleDurabilityDamageChance(section.getInt(key + ".double-durability-damage-chance", 0));
//            tmp.setDamageByMobs(dmgByMobs);
//            tmp.setDamageOnMobs(dmgOnMobs);
//            tmp.setDoubleLoot(dblLoot);
//            tmp.setExperienceMultiplier(xpMult);
//
//            tmpList.add(key);
//            difficultyList.put(key, tmp);
//        }
//        loadMobs();
//        loadBlocks();
//        createIndividualPlayerInventories();
//
//        // Everything beneath this comment is to sort the difficulties by their affinity requirement
//        for (String s : tmpList)
//            tmpMap.put(difficultyList.get(s).getAffinity(), s);
//        TreeMap<Integer, String> tm = new TreeMap<>(tmpMap);
//        String lastKey = null;
//        for (int key : tm.keySet()) {
//            String thisKey = tmpMap.get(key).replace(" ", "_");
//            difficulties.add(thisKey);
//            if(tmpMap.size() == difficulties.size())
//                difficultyList.get(thisKey).setUntil(maxAffinity);
//            if(lastKey != null)
//                difficultyList.get(lastKey).setUntil((key - 1));
//            lastKey = thisKey;
//        }
//        tm.clear(); tmpList.clear(); tmpMap.clear();
//
//        if(customArmorSpawnChance) {
//            section = config.getConfigurationSection("custom-mob-items-spawn-chance.difficulties");
//            if(section.getKeys(false).size() != 0) {
//                StringBuilder weirdDifficulty = new StringBuilder();
//                for (String key : section.getKeys(false))
//                    if(difficultyList.containsKey(key)) {
//                        Difficulty d = difficultyList.get(key);
//                        d.setChanceToEnchant(section.getDouble(key + ".chance-to-enchant-a-piece"));
//                        d.setChanceToHaveArmor(section.getDouble(key + ".chance-to-have-armor"));
//                        d.setMaxEnchants(section.getInt(key + ".max-enchants"));
//                        d.setMaxEnchantLevel(section.getInt(key + ".max-level"));
//                        d.setArmorDropChance(section.getDouble(key + ".armor-drop-chance"));
//                        d.setWeaponDropChance(section.getDouble(key + ".weapon-drop-chance"));
//
//                        HashMap<EquipmentItems, Double> chances = new HashMap<>();
//                        chances.put(EquipmentItems.WEAPON, section.getDouble(key + ".helmet-chance"));
//                        chances.put(EquipmentItems.HELMET, section.getDouble(key + ".weapon-chance"));
//                        chances.put(EquipmentItems.CHEST, section.getDouble(key + ".chest-chance"));
//                        chances.put(EquipmentItems.LEGGINGS, section.getDouble(key + ".leggings-chance"));
//                        chances.put(EquipmentItems.BOOTS, section.getDouble(key + ".boots-chance"));
//                        d.setEnchantChances(chances);
//                    } else {
//                        weirdDifficulty.append(key).append(", ");
//                    }
//
//                if(weirdDifficulty.length() != 0)
//                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[DynamicDifficulty] These difficulties don't exist and can't have custom enchants: " + weirdDifficulty.substring(0, weirdDifficulty.length() - 2));
//
//                ArrayList<String> invalidDifficulties = new ArrayList<>();
//                for(String diff : difficulties)
//                    if(Double.isNaN(difficultyList.get(diff).getChanceToEnchant()))
//                        invalidDifficulties.add(diff);
//
//                if(invalidDifficulties.size() != 0) {
//                    Difficulty d = difficultyList.get(difficulties.get(0));
//                    StringBuilder forgottenDifficulties = new StringBuilder();
//                    for(String diff : difficulties)
//                        if(!Double.isNaN(difficultyList.get(diff).getChanceToEnchant()))
//                            d = difficultyList.get(diff);
//
//                    for(String s : invalidDifficulties) {
//                        Difficulty forgotten = difficultyList.get(s);
//                        forgotten.setChanceToEnchant(d.getChanceToEnchant());
//                        forgotten.setChanceToHaveArmor(d.getChanceToHaveArmor());
//                        forgotten.setMaxEnchants(d.getMaxEnchants());
//                        forgotten.setMaxEnchantLevel(d.getMaxEnchantLevel());
//                        forgotten.setArmorDropChance(d.getArmorDropChance());
//                        forgotten.setWeaponDropChance(d.getWeaponDropChance());
//
//                        HashMap<EquipmentItems, Double> chances = new HashMap<>();
//                        chances.put(EquipmentItems.WEAPON, d.getEnchantChance(EquipmentItems.WEAPON));
//                        chances.put(EquipmentItems.HELMET, d.getEnchantChance(EquipmentItems.HELMET));
//                        chances.put(EquipmentItems.CHEST, d.getEnchantChance(EquipmentItems.CHEST));
//                        chances.put(EquipmentItems.LEGGINGS, d.getEnchantChance(EquipmentItems.LEGGINGS));
//                        chances.put(EquipmentItems.BOOTS, d.getEnchantChance(EquipmentItems.BOOTS));
//                        forgotten.setEnchantChances(chances);
//                        forgottenDifficulties.append(s).append(", ");
//                    }
//                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[DynamicDifficulty] These difficulties don't have any custom enchant settings: "+ forgottenDifficulties.substring(0, forgottenDifficulties.length() - 2));
//                }
//
//                ArrayList<String> array = new ArrayList<>(Arrays.asList("leather", "gold", "chainmail", "iron", "diamond", "netherite"));
//                int total = 0;
//                for(String key : array) {
//                    int count = config.getInt("custom-mob-items-spawn-chance.armor-set-weight." + key);
//                    total += count;
//                    chancePerArmor.put(key.equals("gold") ? "golden" : key, config.getInt("custom-mob-items-spawn-chance.armor-set-weight." + key));
//                }
//                chancePerArmor.put("total", total);
//                loadWeapons();
//                loadEnchants();
//            } else {
//                Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[DynamicDifficulty] You don't have any custom difficulties for custom-mob-items-spawn-chance. Disabled it");
//                customArmorSpawnChance = false;
//            }
//        }
//
//        if(difficultyType.equals("biome"))
//            Bukkit.getScheduler().runTaskAsynchronously(m, this::addBiomes);
//    }
//
//    private void addBiomes() {
//        for (Biome b : Biome.values()) {
//            String uuid = b.toString();
//            SQL.getAffinityValues(uuid, r -> {
//                Minecrafter mc = new Minecrafter(uuid);
//                if (r.get(0) == -1) {
//                    mc.setAffinity(startAffinity);
//                    mc.setMaxAffinity(calcAffinity((UUID)null, data.getConfig().getInt("starting-max-affinity", -1)));
//                    mc.setMinAffinity(calcAffinity((UUID)null, data.getConfig().getInt("starting-min-affinity", -1)));
//                    SQL.updatePlayer(uuid, startAffinity, mc.getMaxAffinity(), mc.getMinAffinity());
//                } else {
//                    mc.setAffinity(r.get(0));
//                    mc.setMaxAffinity(r.get(1));
//                    mc.setMinAffinity(r.get(2));
//                }
//                biomeList.put(uuid, mc);
//            });
//        }
//    }
//
//    private void loadWeapons() {
//        StringBuilder weaponNames = new StringBuilder();
//        int total = 0;
//        Object[] weaponArray = config.getList("custom-mob-items-spawn-chance.weapons-include").toArray();
//        for(Object s : weaponArray) {
//            String[] sep = s.toString().replaceAll("[{|}]","").split("=");
//            int count = 1;
//            try {
//                if(Material.valueOf(sep[0].toUpperCase()) == null) {
//                    weaponNames.append(sep[0]).append(", ");
//                    continue;
//                } else if(sep.length >= 2) {
//                    count = Integer.parseInt(sep[1]);
//                }
//            } catch(IllegalArgumentException e) {
//                weaponNames.append(sep[0]).append(", ");
//                continue;
//            }
//            total += count;
//            customSpawnWeapons.add(sep[0]);
//            chancePerWeapon.put(sep[0], count);
//        }
//        chancePerWeapon.put("total", total);
//        if(weaponNames.length() != 0)
//            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW+"[DynamicDifficulty] Invalid weapons in weapons-include: "+weaponNames.substring(0, weaponNames.length() - 2));
//    }
//
//    private void loadMobs() {
//        StringBuilder mobNames = new StringBuilder();
//        Object[] tmpMobs = config.getList("mobs-count-as-pve").toArray();
//        for(Object s : tmpMobs){
//            String[] sep = s.toString().replaceAll("[{|}]","").split("=");
//            try{
//                if(NamespacedKey.fromString(EntityType.valueOf(sep[0]).getKey().toString()) == null) {
//                    mobNames.append(sep[0]).append(", ");
//                    continue;
//                }
//
//                if(sep.length >= 2) {
//                    mobsPVE.put(sep[0], Integer.parseInt(sep[1]));
//                } else {
//                    mobsPVE.put(sep[0], config.getInt("pve-kill", 2));
//                }
//            } catch(Exception e) {
//                mobNames.append(sep[0]).append(", ");
//            }
//        }
//        if(mobNames.length() != 0)
//            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW+"[DynamicDifficulty] Invalid mobs in mobs-count-as-pve: "+mobNames.substring(0, mobNames.length() - 2));
//    }
//
//    private void loadBlocks() {
//        StringBuilder blockNames = new StringBuilder();
//        Object[] tmpBlocks = config.getList("blocks").toArray();
//        for(Object s : tmpBlocks) {
//            String[] sep = s.toString().replaceAll("[{|}]","").split("=");
//            try {
//                if(NamespacedKey.fromString(Material.valueOf(sep[0]).getKey().toString()) == null) {
//                    blockNames.append(sep[0]).append(", ");
//                    continue;
//                }
//
//                if(sep.length >= 2) {
//                    blocks.put(sep[0], Integer.parseInt(sep[1]));
//                } else {
//                    blocks.put(sep[0], config.getInt("block-mined", 2));
//                }
//            } catch(Exception e) {
//                blockNames.append(sep[0]).append(", ");
//            }
//        }
//        if(blockNames.length() != 0)
//            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW+"[DynamicDifficulty] Invalid blocks in blocks: "+blockNames.substring(0, blockNames.length() - 2));
//    }
//
//    private void loadEnchants() {
//        ArrayList<String> array = new ArrayList<>(Arrays.asList("helmet","chestplate","leggings","boots","weapon","bow"));
//        StringBuilder weirdEnchants = new StringBuilder();
//        for(String piece : array) {
//            int total = 0;
//            ArrayList<String> enchants = new ArrayList<>();
//            Object[] enchantArray = config.getList("custom-mob-items-spawn-chance."+piece+"-enchants-include").toArray();
//            for(Object s : enchantArray) {
//                String[] sep = s.toString().replaceAll("[{|}]", "").split("=");
//                int curr = 1;
//                try {
//                    if(Enchantment.getByKey(NamespacedKey.minecraft(sep[0].toLowerCase())) == null) {
//                        if(!weirdEnchants.toString().contains(sep[0]))
//                            weirdEnchants.append(sep[0]).append(", ");
//                        continue;
//                    } else if(sep.length >= 2) {
//                        curr = Integer.parseInt(sep[1]);
//                    }
//                } catch (Exception e) {
//                    if(!weirdEnchants.toString().contains(sep[0]))
//                        weirdEnchants.append(sep[0]).append(", ");
//                    continue;
//                }
//                total += curr;
//                enchants.add(sep[0].toLowerCase() + ":" + curr);
//            }
//            enchants.add(0, "total:"+total);
//            enchantmentList.put(piece, enchants);
//        }
//        if(weirdEnchants.length() != 0)
//            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW+"[DynamicDifficulty] Invalid enchants: " + weirdEnchants.substring(0, weirdEnchants.length() - 2));
//    }
//
//    public UUID getPlayerUUID(String name) {
//        return playersUUID.getOrDefault(name, null);
//    }
//    public boolean hasBiome(String name) { return biomeList.containsKey(name); }
//
//    public int getBiomeAffinity(String name) { return (biomeList.containsKey(name)) ? biomeList.get(name).getAffinity() : -1; }
//
//    public int getAffinity(UUID uuid) {
//        if(uuid == null) return worldAffinity;
//        if(playerList.containsKey(uuid)) return playerList.get(uuid).getAffinity();
//        return -1;
//    }
//
//    public void setAffinity(UUID uuid, int x) {
//        if (uuid == null) { worldAffinity = calcAffinity((UUID)null, x); }
//        else { playerList.get(uuid).setAffinity(calcAffinity(uuid, x)); }
//    }
//    public void setAffinity(String uuid, int x) {
//        if (biomeList.containsKey(uuid))
//            biomeList.get(uuid).setAffinity(x);
//    }
//
//    protected void emptyHitMobsList() {
//        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(m, () -> {
//            if(mobsOverrideIgnore.size() > 0)
//                mobsOverrideIgnore.clear();
//        }, 0L, 1200L);
//    }
//
//    public int getMaxAffinity(UUID uuid) { return playerList.get(uuid).getMaxAffinity(); }
//    public int getMaxAffinity(String name) { return biomeList.get(name).getMaxAffinity(); }
//    public int getMinAffinity(UUID uuid) { return playerList.get(uuid).getMinAffinity(); }
//    public int getMinAffinity(String name) { return biomeList.get(name).getMinAffinity(); }
//    public void setMaxAffinity(UUID uuid, int x) { playerList.get(uuid).setMaxAffinity(calcAffinity((UUID)null, x)); }
//    public void setMaxAffinity(String name, int x) { biomeList.get(name).setMaxAffinity(calcAffinity((UUID)null, x)); }
//    public void setMinAffinity(UUID uuid, int x) { playerList.get(uuid).setMinAffinity(calcAffinity((UUID)null, x)); }
//    public void setMinAffinity(String name, int x) { biomeList.get(name).setMinAffinity(calcAffinity((UUID)null, x)); }
//    public boolean hasDifficulty(String x) { return difficulties.contains(x); }
//    public int getDifficultyAffinity(String x) { return difficultyList.get(x).getAffinity(); }
//    public ArrayList<String> getDifficulties() { return difficulties; }
//    public String getPrefix(UUID uuid){ return difficultyList.get(calcDifficulty(uuid)).getPrefix(); }
//    public interface findIntegerCallback { void onQueryDone(List<Integer> r); }
//
//    public int getVariable(String x) {
//        if(x.equals("min-affinity")) { return minAffinity; }
//        else if(x.equals("max-affinity")) { return maxAffinity; }
//        return -1;
//    }
//
//    /** Saves all player and world data every few minutes. */
//    public void saveData() {
//        if(difficultyType.equals("world")){
//            SQL.updatePlayer("world", worldAffinity, -1, -1);
//        } else if(difficultyType.equals("biome")) {
//            biomeList.forEach((name, mc) ->
//                SQL.updatePlayer(name, mc.getAffinity(), mc.getMaxAffinity(), mc.getMinAffinity())
//            );
//        } else {
//            playersUUID.forEach((name, uuid) -> {
//                Minecrafter pl = playerList.get(uuid);
//                SQL.updatePlayer(uuid.toString(), pl.getAffinity(), pl.getMaxAffinity(), pl.getMinAffinity());
//            });
//        }
//        if(saveType.equals("file"))
//            data.saveData();
//    }
//
//    /**
//     * Calculates if the amount exceeds the users Maximum or the servers Minimum/Maximum
//     *
//     * @param uuid is the player whose affinity will be changed
//     * @param x is the affinity given to calculate
//     * @return INT the affinity after it has been checked
//     */
//    public int calcAffinity(UUID uuid, int x) {
//        if(x == -1) return -1;
//
//        if (x > maxAffinity) {
//            x = maxAffinity;
//        } else if (x < minAffinity) {
//            x = minAffinity;
//        }
//
//        if(uuid != null && playerList.containsKey(uuid)) {
//            Player pl = Bukkit.getOfflinePlayer(uuid).getPlayer();
//            Minecrafter p = difficultyType.equals("biome") ? biomeList.get(pl.getWorld().getBiome(pl.getLocation()).toString()) : playerList.get(uuid);
//
//            if(p.getMaxAffinity() != -1 && x > p.getMaxAffinity()) {
//                x = p.getMaxAffinity();
//            } else if(p.getMinAffinity() != -1 && x < p.getMinAffinity()) {
//                x = p.getMinAffinity();
//            }
//        }
//
//        return x;
//    }
//
//    /**
//     * Calculates if the amount exceeds the users Maximum or the servers Minimum/Maximum
//     *
//     * @param name is the player whose affinity will be changed
//     * @param x is the affinity given to calculate
//     * @return INT the affinity after it has been checked
//     */
//    public int calcAffinity(String name, int x) {
//        if(x == -1) return -1;
//
//        if (x > maxAffinity) {
//            x = maxAffinity;
//        } else if (x < minAffinity) {
//            x = minAffinity;
//        }
//
//        if(biomeList.containsKey(name)) {
//            Minecrafter p = biomeList.get(name);
//
//            if(p.getMaxAffinity() != -1 && x > p.getMaxAffinity()) {
//                x = p.getMaxAffinity();
//            } else if(p.getMinAffinity() != -1 && x < p.getMinAffinity()) {
//                x = p.getMinAffinity();
//            }
//        }
//
//        return x;
//    }
//
//    protected void addPlayerData(UUID uuid) {
//        Player usr = Bukkit.getOfflinePlayer(uuid).getPlayer();
//        if(!playerList.containsKey(uuid) && usr != null) {
//            SQL.getAffinityValues(uuid.toString(), r -> {
//                Minecrafter mc = new Minecrafter(usr.getName());
//                if (r.get(0) == -1) {
//                    mc.setAffinity(startAffinity);
//                    mc.setMaxAffinity(calcAffinity((UUID)null, data.getConfig().getInt("starting-max-affinity", -1)));
//                    mc.setMinAffinity(calcAffinity((UUID)null, data.getConfig().getInt("starting-min-affinity", -1)));
//                    SQL.updatePlayer(uuid.toString(), startAffinity, mc.getMaxAffinity(), mc.getMinAffinity());
//                } else {
//                    mc.setAffinity(r.get(0));
//                    mc.setMaxAffinity(r.get(1));
//                    mc.setMinAffinity(r.get(2));
//                }
//                playerList.put(uuid, mc);
//                playersUUID.put(usr.getName(), uuid);
//            });
//        }
//    }
//
//    public ItemStack calcEnchant(ItemStack item, String piece, String diff, double chanceToEnchant) {
//        for(int j=0;j<new Random().nextInt(difficultyList.get(diff).getMaxEnchants());j++) {
//            Enchantment chosenEnchant = null;
//            int currentAmount = 0;
//            int chosenAmount = 0;
//            List<String> enchants = enchantmentList.get(piece);
//            for(String s : enchants) {
//                String[] spl = s.split(":");
//                if(s.startsWith("total")) {
//                    chosenAmount = new Random().nextInt(Integer.parseInt(spl[1])) + 1;
//                    continue;
//                }
//
//                if(chosenEnchant == null)
//                    chosenEnchant = Enchantment.getByKey(NamespacedKey.minecraft(spl[0]));
//
//                if(currentAmount >= chosenAmount) {
//                    chosenEnchant = Enchantment.getByKey(NamespacedKey.minecraft(spl[0]));
//                    break;
//                }
//                currentAmount += Integer.parseInt(spl[1]);
//            }
//
//            if(!config.getBoolean("custom-mob-items-spawn-chance.override-enchant-conflicts", false)) {
//                boolean allowed = true;
//                for(List<Enchantment> enchantList : enchantmentConflict)
//                    if(allowed) {
//                        if(enchantList.contains(chosenEnchant))
//                            for(Enchantment currentEnchant : enchantList)
//                                if(item.containsEnchantment(currentEnchant)) {
//                                    allowed = false;
//                                    break;
//                                }
//                    } else {
//                        break;
//                    }
//                if (!allowed)
//                    continue;
//            }
//
//            int chosenLevel;
//            int maxlvl = Math.round(difficultyList.get(diff).getMaxEnchantLevel());
//            if(chosenEnchant.getMaxLevel() == 1) {
//                chosenLevel = chosenEnchant.getMaxLevel();
//            } else if (config.getBoolean("custom-mob-items-spawn-chance.override-default-limits", false)) {
//                chosenLevel = new Random().nextInt(Math.round(difficultyList.get(diff).getMaxEnchantLevel())) + 1;
//            } else if (maxlvl > chosenEnchant.getMaxLevel()) {
//                chosenLevel = new Random().nextInt(chosenEnchant.getMaxLevel()) + 1;
//            } else {
//                chosenLevel = new Random().nextInt(maxlvl);
//            }
//            item.addUnsafeEnchantment(chosenEnchant, chosenLevel);
//            if(new Random().nextDouble() > chanceToEnchant)
//                break;
//        }
//        return item;
//    }
//
//    /** Closes all databases */
//    public void exitProgram() {
//        try {
//            SQL.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Gets the difficulty of a user or the world
//     *
//     * @param name of the user or the world if null
//     * @return String of the difficulty the world/user is on
//     */
//    public String calcDifficulty(String name) {
//        if(randomizer) return difficulties.get(new Random().nextInt(difficulties.size() - 1));
//        if(biomeList.containsKey(name)) {
//            int af = biomeList.get(name).getAffinity();
//            int size = difficulties.size();
//            for (int i = 0; i < size; i++)
//                if(af <= difficultyList.get(difficulties.get(i)).getUntil())
//                    return difficulties.get(i);
//        }
//        return difficulties.get(0);
//    }
//
//    /**
//     * Gets the difficulty of a user or the world
//     *
//     * @param uuid of the user or the world if null
//     * @return String of the difficulty the world/user is on
//     */
//    public String calcDifficulty(UUID uuid) {
//        if(randomizer) return difficulties.get(new Random().nextInt(difficulties.size() - 1));
//        int af;
//
//        if(difficultyType.equals("world") || uuid == null){
//            af = worldAffinity;
//        } else if(difficultyType.equals("biome")) {
//            Player player = Bukkit.getOfflinePlayer(uuid).getPlayer();
//            af = biomeList.get(player.getWorld().getBiome(player.getLocation()).toString()).getAffinity();
//        } else if(difficultyType.equals("radius")) {
//            Location loc = Bukkit.getOfflinePlayer(uuid).getPlayer().getLocation();
//            Location spawn = new Location(loc.getWorld(), config.getInt("difficulty-modifiers.radius-x", 0), loc.getY(), config.getInt("difficulty-modifiers.radius-z", 0));
//            int difference = Math.max(config.getInt("difficulty-modifiers.max-radius-size", 10000) - (int) Math.floor(loc.distance(spawn)), 0);
//            af = maxAffinity - Math.min(difference / (config.getInt("difficulty-modifiers.max-radius-size", 10000) / maxAffinity), maxAffinity);
//        } else {
//            af = playerList.get(uuid).getAffinity();
//        }
//
//        int size = difficulties.size();
//        for (int i = 0; i < size; i++)
//            if(af <= difficultyList.get(difficulties.get(i)).getUntil())
//                return difficulties.get(i);
//        return difficulties.get(0);
//    }
//
//    /**
//     * Calculates the exact percentage between 2 difficulties
//     *
//     * @param uuid of the user
//     * @param mode which is used to select the correct variable
//     * @return Double of the exact or the difficulty based percentage
//     */
//    protected double calcPercentage(UUID uuid, String mode) {
//        if(randomizer) return getHashData(mode, calcDifficulty(uuid));
//        int thisDiff = difficulties.indexOf(calcDifficulty(uuid));
//        int affinity;
//
//        if(uuid == null || difficultyType.equals("world")) {
//            affinity = worldAffinity;
//        }else if(difficultyType.equals("biome")) {
//            Player player = Bukkit.getOfflinePlayer(uuid).getPlayer();
//            affinity = biomeList.get(player.getWorld().getBiome(player.getLocation()).toString()).getAffinity();
//        }else if(difficultyType.equals("radius")) {
//            Location loc = Bukkit.getOfflinePlayer(uuid).getPlayer().getLocation();
//            Location spawn = new Location(loc.getWorld(), config.getInt("difficulty-modifiers.radius-x", 0), loc.getY(), config.getInt("difficulty-modifiers.radius-z", 0));
//            int difference = Math.max(config.getInt("difficulty-modifiers.max-radius-size", 10000) - (int) Math.floor(loc.distance(spawn)), 0);
//            affinity = maxAffinity - Math.min(difference / (config.getInt("difficulty-modifiers.max-radius-size", 10000) / maxAffinity), maxAffinity);
//            int size = difficulties.size();
//            for (int i = 0; i < size; i++)
//                if(affinity <= difficultyList.get(difficulties.get(i)).getUntil()) {
//                    thisDiff = i;
//                    break;
//                }
//        } else {
//            affinity = playerList.get(uuid).getAffinity();
//        }
//
//        if (thisDiff + 1 != difficulties.size() && config.getBoolean("difficulty-modifiers.exact-percentage", true)) {
//            int differencePercentage = getHashData(mode, difficulties.get(thisDiff+1)) - getHashData(mode, difficulties.get(thisDiff));
//
//            if(differencePercentage == 0)
//                return getHashData(mode, difficulties.get(thisDiff));
//
//            if(differencePercentage < 0)
//                differencePercentage*=-1;
//
//            int a = difficultyList.get(difficulties.get(thisDiff+1)).getAffinity();
//            int b = difficultyList.get(difficulties.get(thisDiff)).getAffinity();
//            double c = (100.0 / (a - b) * (affinity - b));
//            double extraPercentage = (differencePercentage / 100.0) * c;
//
//            return (getHashData(mode, difficulties.get(thisDiff)) + extraPercentage);
//        }
//        return getHashData(mode, difficulties.get(thisDiff));
//    }
//
//    /**
//     * Returns data from the made HashMaps
//     *
//     * @param mode which is used to select the correct variable
//     * @param diff which is the difficulty setting
//     * @return INT from the selected variable and difficulty
//     */
//    protected int getHashData(String mode, String diff) {
//        if(difficultyList.containsKey(diff)) {
//            Difficulty d = difficultyList.get(diff);
//            if(mode.equals("damage-done-by-mobs")) { return d.getDamageByMobs(); }
//            if(mode.equals("damage-done-on-mobs")) { return d.getDamageOnMobs(); }
//            if(mode.equals("experience-multiplier")) { return d.getExperienceMultiplier(); }
//            if(mode.equals("double-loot-chance")) { return d.getDoubleLoot(); }
//            if(mode.equals("hunger-drain-chance")) { return d.getHungerDrain(); }
//            if(mode.equals("double-durability-damage")) { return d.getDoubleDurabilityDamageChance(); }
//            if(mode.equals("damage-done-by-ranged-mobs")) { return d.getDamageByRangedMobs(); }
//            if(mode.equals("chance-to-have-armor")) { return (int) Math.round(d.getChanceToHaveArmor()*100); }
//            if(mode.equals("chance-to-enchant-a-piece")) { return (int) Math.round(d.getChanceToEnchant()*100); }
//            if(mode.equals("weapon-chance")) { return (int) Math.round(d.getEnchantChance("weapon")*100); }
//            if(mode.equals("helmet-chance")) { return (int) Math.round(d.getEnchantChance("helmet")*100); }
//            if(mode.equals("chest-chance")) { return (int) Math.round(d.getEnchantChance("chest")*100); }
//            if(mode.equals("leggings-chance")) { return (int) Math.round(d.getEnchantChance("leggings")*100); }
//            if(mode.equals("boots-chance")) { return (int) Math.round(d.getEnchantChance("boots")*100); }
//            if(mode.equals("armor-drop-chance")) { return (int) Math.round(d.getArmorDropChance()*100); }
//            if(mode.equals("weapon-drop-chance")) { return (int) Math.round(d.getWeaponDropChance()*100); }
//        }
//        return -1;
//    }
//
//    protected void addAmountOfAffinity(UUID uuid, int x) {
//        if(x != 0) {
//            if (difficultyType.equals("world") || uuid == null) { worldAffinity = calcAffinity((UUID)null, worldAffinity + x); }
//            else if (difficultyType.equals("biome")) {
//                Player p = Bukkit.getOfflinePlayer(uuid).getPlayer();
//                Minecrafter biome = biomeList.get(p.getWorld().getBiome(p.getLocation()).toString());
//                biome.setAffinity(calcAffinity(p.getWorld().getBiome(p.getLocation()).toString(), biome.getAffinity() + x));
//            }
//            else { playerList.get(uuid).setAffinity(calcAffinity(uuid, playerList.get(uuid).getAffinity() + x)); }
//        }
//    }
//
//    protected void addAmountOfMinAffinity(UUID uuid, int x) {
//        if(x != 0) {
//            Minecrafter p = playerList.get(uuid);
//            p.setMinAffinity(calcAffinity((UUID)null, p.getMinAffinity() + x));
//        }
//    }
//    protected void addAmountOfMaxAffinity(UUID uuid, int x) {
//        if(x != 0) {
//            Minecrafter p = playerList.get(uuid);
//            p.setMaxAffinity(calcAffinity((UUID)null, p.getMaxAffinity() + (x * -1)));
//        }
//    }
//
//    /** To increase/decrease players Affinity every minute */
//    public void onInterval() {
//        if(onInterval != 0) {
//            if(difficultyType.equals("world")) {
//                worldAffinity = calcAffinity((UUID)null, worldAffinity + onInterval);
//            } else if (difficultyType.equals("biome")) {
//                Set<String> biomes = biomeList.keySet();
//                for(String biome : biomes) {
//                    Minecrafter b = biomeList.get(biome);
//                    b.setAffinity(calcAffinity(biome, b.getAffinity() + onInterval));
//                }
//            } else {
//                Bukkit.getOnlinePlayers().forEach(pl -> {
//                    Minecrafter p = playerList.get(pl.getUniqueId());
//                    p.setAffinity(calcAffinity(pl.getUniqueId(), p.getAffinity() + onInterval));
//                });
//            }
//        }
//    }
//
//    public void openPlayersInventory(Player user, int page) {
//        Inventory tmp = inventorySettings.get("player");
//        if(Bukkit.getOnlinePlayers().size() < 45) {
//            int i = 0;
//            for(Player player : Bukkit.getOnlinePlayers()) {
//                UUID uuid = player.getUniqueId();
//                ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
//                setPlayerHead(skull, player);
//                setItemStackName(skull, player.getName());
//                String c1 = ChatColor.BOLD+""+ChatColor.DARK_GREEN;
//                String c2 = ChatColor.BOLD+""+ChatColor.GREEN;
//                Minecrafter pl = playerList.get(uuid);
//                setLore(skull, new ArrayList<>(Arrays.asList(c1+"Affinity: "+c2+pl.getAffinity(),c1+"Min Affinity: "+c2+pl.getMinAffinity(),c1+"Max Affinity: "+c2+pl.getMaxAffinity())));
//                tmp.setItem(i++, skull);
//            }
//        } else {
//            int curr = page * 45;
//            Bukkit.getConsoleSender().sendMessage("page: "+page + " curr: "+curr);
//            Player[] pl = Bukkit.getOnlinePlayers().toArray(new Player[0]);
//            ItemStack goldIngot = new ItemStack(Material.GOLD_INGOT);
//            setLore(goldIngot, new ArrayList<>(Collections.singletonList(String.valueOf(page > 0 ? page - 1 : 0))));
//            setItemStackName(goldIngot, ChatColor.AQUA+""+ChatColor.BOLD+"Previous page");
//            ItemStack chestPlate = new ItemStack(Material.IRON_CHESTPLATE);
//            setLore(chestPlate, new ArrayList<>(Collections.singletonList(String.valueOf(page))));
//            setItemStackName(chestPlate, ChatColor.AQUA+""+ChatColor.BOLD+"Current page");
//            ItemStack ironIngot = new ItemStack(Material.IRON_INGOT);
//            setLore(ironIngot, new ArrayList<>(Collections.singletonList(String.valueOf(page + 1))));
//            setItemStackName(ironIngot, ChatColor.AQUA+""+ChatColor.BOLD+"Next page");
//            tmp.setItem(3, goldIngot);
//            tmp.setItem(4, chestPlate);
//            tmp.setItem(5, ironIngot);
//            for(int i=0;i<45;i++) {
//                if(i + curr < pl.length) {
//                    String name = pl[i + curr].getName();
//                    UUID uuid = pl[i + curr].getUniqueId();
//                    ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
//                    setItemStackName(skull, name);
//                    setPlayerHead(skull, Bukkit.getServer().getPlayer(name));
//                    String c1 = ChatColor.BOLD+""+ChatColor.DARK_GREEN;
//                    String c2 = ChatColor.BOLD+""+ChatColor.GREEN;
//                    Minecrafter p = playerList.get(uuid);
//                    setLore(skull, new ArrayList<>(Arrays.asList(c1+"Affinity: "+c2+p.getAffinity(),c1+"Min Affinity: "+c2+p.getMinAffinity(),c1+"Max Affinity: "+c2+p.getMaxAffinity())));
//                    tmp.setItem(i+9, skull);
//                } else {
//                    break;
//                }
//            }
//        }
//        user.openInventory(tmp);
//    }
//
//    public void createIndividualPlayerInventories() {
//        Inventory inv = Bukkit.createInventory(null, 54, "DynamicDifficulty - Players");
//        inventorySettings.put("player", inv);
//
//        inv = Bukkit.createInventory(null, 27, "DynamicDifficulty - Individual Player");
//        List<String> settings = new ArrayList<>(Arrays.asList("Affinity","Min Affinity","Max Affinity"));
//        List<String> changeSettings = new ArrayList<>(Arrays.asList("","-100","-10","-1", "", "+1", "+10", "+100", "Default"));
//        List<String> woolColors = new ArrayList<>(Arrays.asList("LIME", "PINK", "MAGENTA", "PURPLE", "", "BLUE", "CYAN", "LIGHT_BLUE", "RED"));
//        for(int i=0;i<27;i++) {
//            if(i % 9 != 4) {
//                ItemStack wool;
//                if (i % 9 == 0) {
//                    wool = new ItemStack(Material.LIME_WOOL, 1);
//                    int tmp = 0;
//                    if(i != 0) { tmp = i / 9; }
//                    setItemStackName(wool, settings.get(tmp));
//                } else {
//                    if(woolColors.get((i % 9)).equals("")) continue;
//                    wool = new ItemStack(Material.getMaterial(woolColors.get((i % 9)) + "_WOOL"), 1);
//                    setItemStackName(wool, changeSettings.get(i % 9));
//                }
//                inv.setItem(i, wool);
//            }
//        }
//        inventorySettings.put("iplayer", inv);
//    }
//
//    public void setItemStackName(ItemStack renamed, String customName) {
//        ItemMeta renamedMeta = renamed.getItemMeta();
//        renamedMeta.setDisplayName(customName);
//        renamed.setItemMeta(renamedMeta);
//    }
//
//    public void setPlayerHead(ItemStack skull, Player name) {
//        SkullMeta meta = (SkullMeta) skull.getItemMeta();
//        meta.setOwningPlayer(name);
//    }
//
//    public void setLore(ItemStack item, List<String> loreSet) {
//        ItemMeta meta = item.getItemMeta();
//        meta.setLore(loreSet);
//        item.setItemMeta(meta);
//    }
//}
