package me.skinnyjeans.gmd;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class WorldAffinity implements Listener{
	private DataManager data;
	private int minAffinity,maxAffinity,onDeath,onPVPKill,onPVEKill,onMined,startAffinity,onInterval,onPlayerHit,worldAffinity;
	private HashMap<UUID, Integer> playerAffinity = new HashMap<>();
	private HashMap<UUID, Integer> playerMaxAffinity = new HashMap<>();
	private boolean silkTouchAllowed, calcExactPercentage;
	private List<String> mobsPVE, blocks;
	private HashMap<String, Integer> damageDoneByMobs = new HashMap<>();
	private HashMap<String, Integer> experienceMultiplier = new HashMap<>();
	private HashMap<String, Integer> damageDoneOnMobs = new HashMap<>();
	private HashMap<String, Integer> difficultyAffinity = new HashMap<>();
	private HashMap<String, Integer> doubleLootChance = new HashMap<>();
	private HashMap<String, Boolean> effectsWhenAttacked = new HashMap<>();
	private ArrayList<String> difficulties = new ArrayList<>();
	private PotionEffectType[] effects = new PotionEffectType[] { PotionEffectType.WITHER, PotionEffectType.POISON,
			PotionEffectType.BLINDNESS, PotionEffectType.WEAKNESS, PotionEffectType.SLOW };
	
	public WorldAffinity(Main m) {
		data = new DataManager(m);
		startAffinity = data.getConfig().getInt("starting-affinity");
		
		if(data.getDataFile().getString("world.affinity") == null)
			data.getDataFile().set("world.affinity", startAffinity);
		
		silkTouchAllowed = data.getConfig().getBoolean("silk-touch-allowed");
		minAffinity = data.getConfig().getInt("min-affinity");
		maxAffinity = data.getConfig().getInt("max-affinity");
		onDeath = data.getConfig().getInt("death");
		onPVEKill = data.getConfig().getInt("pve-kill");
		onPVPKill = data.getConfig().getInt("pvp-kill");
		onMined = data.getConfig().getInt("block-mined");
		mobsPVE = data.getConfig().getStringList("mobs-count-as-pve");
		blocks = data.getConfig().getStringList("blocks");
		onInterval = data.getConfig().getInt("points-on-interval");
		onPlayerHit = data.getConfig().getInt("player-hit");
		worldAffinity = data.getDataFile().getInt("world.affinity");
		calcExactPercentage = data.getConfig().getBoolean("calculate-exact-percentage");
		HashMap<Integer, String> tmpMap = new HashMap<>();
		ArrayList<String> tmpList = new ArrayList<>();
		ConfigurationSection section = data.getConfig().getConfigurationSection("difficulty");

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
		playerAffinity.remove(uuid);
		playerMaxAffinity.remove(uuid);
	}

	@EventHandler
	public void onDeath(PlayerRespawnEvent e) {
		if (onDeath != 0) {
			worldAffinity = calcAffinity(worldAffinity + onDeath);
		}
	}

	@EventHandler
	public void onKill(EntityDeathEvent e) {
		if ((onPVPKill != 0 || onPVEKill != 0) && e.getEntity().getKiller() instanceof Player) {
			if (e.getEntity() instanceof Player) {
				worldAffinity = calcAffinity(worldAffinity + onPVPKill);
			} else if (mobsPVE.contains(e.getEntityType().toString())) {
				worldAffinity = calcAffinity(worldAffinity + onPVEKill);
			}
		}
		
		if (!(e.getEntity() instanceof Player) && !(e.getEntity() instanceof EnderDragon) && !(e.getEntity() instanceof Wither) && e.getEntity().getKiller() instanceof Player) {
			e.setDroppedExp( (int) (e.getDroppedExp() * calcPercentage("experience-multiplier") / 100.0));
			double DoubleLoot = calcPercentage("double-loot-chance");
			if (DoubleLoot != 0.0 && new Random().nextDouble() < DoubleLoot / 100.0 && !e.getEntity().getCanPickupItems()) {
				for (int i = 0; i < e.getDrops().size(); i++)
					Bukkit.getWorld(e.getEntity().getWorld().getUID()).dropItemNaturally(e.getEntity().getLocation(), e.getDrops().get(i));
			}
		}
	}

	@EventHandler
	public void onMined(BlockBreakEvent e) {
		if (onMined != 0 && blocks.contains(e.getBlock().getBlockData().getMaterial().name()) && (!e.getPlayer().getItemOnCursor().containsEnchantment(Enchantment.SILK_TOUCH) || silkTouchAllowed ) && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
			worldAffinity = calcAffinity(worldAffinity + onMined);
		}
	}

	@EventHandler
	public void onHit(EntityDamageByEntityEvent e) {
		Entity prey = e.getEntity();
		Entity hunter = e.getDamager();
		if (prey instanceof Player) {
			if (!(hunter instanceof Player) && !(hunter instanceof EnderDragon) && !(hunter instanceof Wither)) {
				worldAffinity = calcAffinity(worldAffinity + onPlayerHit);
				double dam = e.getFinalDamage() * calcPercentage("damage-done-by-mobs") / 100.0;
				e.setDamage(dam);
			}
			if (!(hunter instanceof Player) && (hunter instanceof LivingEntity || hunter instanceof Arrow) && effectsWhenAttacked.get(calcDifficulty()))
				for (PotionEffectType effect : effects) {
					if (((LivingEntity) prey).hasPotionEffect(effect))
						((LivingEntity) prey).removePotionEffect(effect);
				}
		} else if (hunter instanceof Player && !(prey instanceof Player) && !(prey instanceof EnderDragon) && !(prey instanceof Wither)) {
			double dam = e.getFinalDamage() * calcPercentage("damage-done-on-mobs")  / 100.0;
			e.setDamage(dam);
		}
	}
	
	// Save world affinity every few minutes to insure less data loss if the server crashes
	public void saveAllData() {
		data.getDataFile().set("world.affinity", worldAffinity);
	}
	
	// To increase/decrease score every few minutes
	public void onInterval() {
		worldAffinity = calcAffinity(worldAffinity + onInterval);
	}
	
	public int getAffinityUser(UUID uuid) {
		return playerAffinity.get(uuid);
	}
	
	public int getAffinityWorld() {
		return worldAffinity;
	}

	public int getMaxAffinityUser(UUID uuid) {
		return playerMaxAffinity.get(uuid);
	}

	public void setAffinityWorld(int x) {
		worldAffinity = calcAffinity(x);
	}
	
	/**
	 * Calculates if the amount exceeds the servers Minimum/Maximum
	 * 
	 * @param x the affinity given to calculate
	 * @return INT the affinity after it has been checked
	 */
	public int calcAffinity(int x) {
		if (x > maxAffinity) {
			x = maxAffinity;
		} else if (x < minAffinity) {
			x = minAffinity;
		}
		return x;
	}

	/**
	 * Gets the difficulty of the world
	 * 
	 * @return String of the difficulty the world is on
	 */
	public String calcDifficulty() {
		String last = "";
		for (int i = 0; i < difficulties.size(); i++) {
			if (!last.equals("") && difficultyAffinity.get(difficulties.get(i)) >= worldAffinity && difficultyAffinity.get(last) <= worldAffinity)
				return last;
			if (i + 1 == difficulties.size())
				return difficulties.get(i);
			last = difficulties.get(i);
		}
		return difficulties.get(0);
	}
	
	/**
	 * Gets the difficulty of an user
	 * 
	 * @param uuid of the user
	 * @return String of the difficulty the user is on
	 */
	public String calcDifficultyUser(UUID uuid) {
		String last = "";
		for (int i = 0; i < difficulties.size(); i++) {
			if (!last.equals("") && difficultyAffinity.get(difficulties.get(i)) >= playerAffinity.get(uuid) && difficultyAffinity.get(last) <= playerAffinity.get(uuid))
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
	 * @param mode which is used to select the correct variable
	 * @return Double of the exact or the difficulty based percentage
	 */
	private double calcPercentage(String mode) {
		int thisDiff = difficulties.indexOf(calcDifficulty());
		
		if (thisDiff + 1 != difficulties.size() && calcExactPercentage) {
			int differencePercentage = getHashData(mode, difficulties.get(thisDiff+1)) - getHashData(mode, difficulties.get(thisDiff));
			
			if(differencePercentage == 0)
				return getHashData(mode, difficulties.get(thisDiff));
						
			if(differencePercentage < 0) 
				differencePercentage*=-1;
			
			int a = difficultyAffinity.get(difficulties.get(thisDiff+1));
			int b = difficultyAffinity.get(difficulties.get(thisDiff));
			double c = (100.0 / (a - b) * (worldAffinity - b));
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
		return switch (mode) {
			case "damage-done-by-mobs" -> damageDoneByMobs.get(diff);
			case "damage-done-on-mobs" -> damageDoneOnMobs.get(diff);
			case "experience-multiplier" -> experienceMultiplier.get(diff);
			case "double-loot-chance" -> doubleLootChance.get(diff);
			default -> -1;
		};
	}
}
