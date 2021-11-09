# DynamicDifficulty
## Table of Contents:
#### [About DynamicDifficulty](https://github.com/JeannotM/DynamicDifficulty#about-dynamicdifficulty-1)
#### [Settings with explanations](https://github.com/JeannotM/DynamicDifficulty#settings-with-explanations-1)
#### [calculate-exact-percentage](https://github.com/JeannotM/DynamicDifficulty#calculate-exact-percentage-1)
#### [Commands, Permissions and explanations](https://github.com/JeannotM/DynamicDifficulty#commands-permissions-and-explanations-1)
#### [Plugin Support](https://github.com/JeannotM/DynamicDifficulty#plugin-support-1)
#### [Other Small Things](https://github.com/JeannotM/DynamicDifficulty#other-small-things-1)
#### [Possible Future Updates](https://github.com/JeannotM/DynamicDifficulty#possible-future-updates-1)

## About DynamicDifficulty
I made the DynamicDifficulty for 3 reasons. I couldn't find any other DynamicDifficulty plugin for 1.16, the ones I saw from earlier versions weren't that customizable and I wanted experienced and new Minecraft players to be able to play on the same server without having to worry about it being too hard or too easy for them.

## Settings with explanations
```
# The amount of affinity a user or the world increases (or decreases):
# points-on-interval is made to give the user points every minute.
points-per-minute: 5

# when a block from the blocks list is mined.
block-mined: 2

# When player is hit by a mob.
player-hit: -1

# When a player kills another player.
pvp-kill: 20

# When a mob from the mobs-count-as-pve list is killed.
pve-kill: 2

# When a player dies.
death: -80

# The amount of affinity a user has when joining the server for the first time (or the first time after installing DynamicDifficulty)
starting-affinity: 600

# What The Minimum and Maximum Affinity Are, no one can get more or less than this (even with commands)
# Any value is acceptable, but it's not recommended to go lower than 0 as it may break a few commands.
min-affinity: 0
max-affinity: 1500

# Mobs That Will Give Points From "pve-kill" when killed: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html
# Format:
# - MOBTYPE: <Affinity points>
# Will use pve-kill if only MOBTYPE is given
mobs-count-as-pve:
- BLAZE: 4
- CAVE_SPIDER: 3
- CREEPER: 3
- DROWNED
- ENDER_DRAGON: 100
- ELDER_GUARDIAN: 20
- ENDERMAN: 5
- GUARDIAN: 3
- HUSK
- IRON_GOLEM: 50
- MAGMA_CUBE
- PHANTOM
- PIGLIN
- PIGLIN_BRUTE: 5
- SKELETON
- SPIDER
- VILLAGER: 20
- VINDICATOR: 5
- WITCH
- WITHER: 100
- WITHER_SKELETON: 5
- ZOMBIE

# Blocks That Will Give Points From "block-mined" when mined: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
# Format:
# - BLOCKTYPE: <Affinity points>
# Will use on-mined if only MOBTYPE is given
blocks:
- ANCIENT_DEBRIS: 4
- DIAMOND_ORE
- EMERALD_ORE
  
# Whether the Player Will Receive Affinity When Mining Blocks With Silk Touch
silk-touch-allowed: false

# whether to hook into some other plugins or not. (List may be expanded in the future)
plugin-support:
  allow-papi: false # Check the Github or Spigot page for the commands
  allow-bstats: true # To Only Disable it on DynamicDifficulty (enabling bstats here when it's disabled won't work)
  use-prefix: true
  no-changes-to-spawned-mobs: false # Mobs that spawn from eggs or spawners, won't have damage, loot or other changes. Players will also NOT receive any Affinity.
  unload-leaving-player: false # Removes a few issues with not being able to find certain players. Enabling it will improve ram a few kb's, but may cause some problems that were stated earlier.

saving-data:
  type: file # Supported: file, mysql, sqlite, mongodb
#  port: "3306"
#  host: "localhost"
#  username: "root"
#  password: ""
#  database: "dynamicdifficulty"

# You can disable DynamicDifficulty in certain worlds
disabled-worlds:
- example_name

# These mobs ignore everything except the "effects-when-attacked" and "mobs-ignore-player" settings from difficulty
disabled-mobs:
- ENDER_DRAGON
- ELDER_GUARDIAN
- WITHER

difficulty-modifiers:
  type: player # Supported difficulty types: player, world
  randomize: false # randomizes all difficulty settings for everyone (uses the settings at the end of the page)
  exact-percentage: true # Calculates the percentage between 2 difficulties so the progression will feel more natural
  # Multiplies all the difficulty values by x amount (100 * 2.5 = 250)
  damage-done-by-mobs-multiplier: 1.0
  damage-done-on-mobs-multiplier: 1.0
  double-loot-chance-multiplier: 1.0
  experience-multiplier: 1.0
  
# This has been calculated from the hard difficulty. So it is recommended to change these if you're not playing on hard world difficulty
# To see mob damage: https://minecraft.gamepedia.com/Mob#Damage_dealt_by_hostile_and_neutral_mobs
# Format if you want to create your own difficulty:
# <custom_name>:
#   affinity-required: <number, At What affinity this difficulty starts working>
#   damage-done-by-mobs: <percentage, How much damage mobs do to you>
#   damage-done-on-mobs: <percentage, How much damage you do on mobs>
#   hunger-drain-chance: <percentage, Chance that the hunger of a player will drain>
#   damage-done-by-ranged-mobs: <percentage, How much damage projectiles do>
#   double-durability-damage-chance: <percentage, Chance to take double damage on item durability>
#   experience-multiplier: <percentage, Experience Multiplier>
#   double-loot-chance: <percentage, Chance to double the loot dropped when a mob is killed>
#   allow-pvp: <bool, Whether the player can attack or be attacked by other players>
#   keep-inventory: <bool, Whether the player keeps everything on death or not>
#   effects-when-attacked: <bool, Whether you get poison/wither etc from mob attacks (not including splash potions), works only on normal/hard world difficulty>
#   prefix: <text, prefix to return if PlaceholderAPI is enabled>
#   mobs-ignore-player: <list, these mobs will ignore the players unless they're provoked>
difficulty:
  Easy:
    affinity-required: 0
    damage-done-by-mobs: 50
    damage-done-on-mobs: 100
    hunger-drain-chance: 60
    damage-done-by-ranged-mobs: 100
    double-durability-damage-chance: 0
    experience-multiplier: 70
    double-loot-chance: 0
    allow-pvp: true
    keep-inventory: true
    effects-when-attacked: false
    prefix: '&7&l[&b&lEasy&7&l]&r'
    mobs-ignore-player:
    - CREEPER
  Normal:
    affinity-required: 400
    damage-done-by-mobs: 75
    damage-done-on-mobs: 100
    hunger-drain-chance: 80
    damage-done-by-ranged-mobs: 100
    double-durability-damage-chance: 1
    experience-multiplier: 90
    double-loot-chance: 0
    allow-pvp: true
    keep-inventory: false
    effects-when-attacked: true
    prefix: '&7&l[&9&lNormal&7&l]&r'
  Hard:
    affinity-required: 1100
    damage-done-by-mobs: 100
    damage-done-on-mobs: 100
    hunger-drain-chance: 100
    damage-done-by-ranged-mobs: 125
    double-durability-damage-chance: 5
    experience-multiplier: 125
    double-loot-chance: 5
    allow-pvp: true
    keep-inventory: false
    effects-when-attacked: true
    prefix: '&7&l[&4&lHard&7&l]&r'

# Messages that are sent to the player when the attacker or attackee has allow-pvp on false
messages:
  attacker-no-pvp: "You cannot attack this player because you're still having a hard time!"
  attackee-no-pvp: "You cannot attack this player because he's already having a hard time!"

# Some of these features are experimental, change existing features or have a chance to reduce performance
advanced-features:
  # The 'auto-calculate' features will, add and remove min/max affinity from players depending on configured settings
  # These WILL override existing min/max affinity settings.
  auto-calculate-min-affinity: false
  auto-calculate-max-affinity: false
  custom-mob-items-spawn-chance: false

# Difficulty name NEEDS to be EXACTLY the same as the ones in 'difficulty'
custom-mob-items-spawn-chance:
  override-default-limits: false # will allow higher levels than Minecraft's defaults. (E.g. Unbreaking 7 can happen if max-level is higher or equals 7)
  override-enchant-conflicts: false # Allows conflicting enchants to occur on a single armor (E.g. Protection & Fire Protection)
  # These are the only mobs that will spawn with these custom settings
  # You can add any mob you want, but some will not show any armor. So it's not recommended going too wild
  includes-mobs:
  - ZOMBIE
  - SKELETON
  weapons-include:
  - WOODEN_SHOVEL: 200
  - GOLDEN_SHOVEL: 50
  - IRON_SHOVEL: 5
  - WOODEN_AXE: 200
  - GOLDEN_AXE: 50
  - IRON_AXE: 5
  - WOODEN_PICKAXE: 200
  - GOLDEN_PICKAXE: 50
  - IRON_PICKAXE: 5
  - WOODEN_SWORD: 200
  - GOLDEN_SWORD: 50
  - IRON_SWORD: 5
  armor-set-weight:
    leather: 3706
    gold: 4872
    chain: 1290
    iron: 127
    diamond: 4
    netherite: 1
  # https://www.digminecraft.com/lists/enchantment_list_pc.php
  # You'll have to use the names that are beneath the names with those __snakes__
  helmet-enchants-include:
  #- <ENCHANT_NAME>: WEIGHT, higher means more likely to occur, no value = weight of 1
  - protection: 10
  - blast_protection: 5
  - fire_protection: 5
  - projectile_protection: 5
  - vanishing_curse
  - binding_curse
  - aqua_affinity: 2
  - thorns
  - respiration: 2
  - unbreaking: 5
  chestplate-enchants-include:
  - protection: 10
  - blast_protection: 5
  - fire_protection: 5
  - projectile_protection: 5
  - vanishing_curse
  - binding_curse
  - thorns
  - unbreaking: 5
  leggings-enchants-include:
  - protection: 10
  - blast_protection: 5
  - fire_protection: 5
  - projectile_protection: 5
  - vanishing_curse
  - binding_curse
  - thorns
  - unbreaking: 5
  boots-enchants-include:
  - protection: 10
  - blast_protection: 5
  - fire_protection: 5
  - projectile_protection: 5
  - vanishing_curse
  - binding_curse
  - feather_falling: 5
  - thorns
  - frost_walker: 2
  - soul_speed
  - depth_strider: 2
  - unbreaking: 5
  weapon-enchants-include:
  - sharpness: 10
  - bane_of_arthropods: 5
  - smite: 5
  - fire_aspect: 2
  - looting: 2
  - knockback: 2
  - sweeping: 2
  - vanishing_curse
  - binding_curse
  - unbreaking: 5
  bow-enchants-include:
  - power: 10
  - flame: 2
  - infinity
  - punch: 2
  - vanishing_curse
  - binding_curse
  - unbreaking: 5
  # You'll need to use the EXACT same name as in difficulties.
  # (E.g. if Extreme difficulty exists, but not here; will use the last difficulty that had)
  difficulties:
    Easy:
      max-enchants: 1
      max-level: 1
      chance-to-have-armor: 5.0
      chance-to-enchant-a-piece: 15
      armor-drop-chance: 10.0
      weapon-chance: 1.0
      helmet-chance: 100.0
      chest-chance: 75.0
      leggings-chance: 56.25
      boots-chance: 42.19
    Normal:
      max-enchants: 2
      max-level: 1
      chance-to-have-armor: 15.0
      chance-to-enchant-a-piece: 30
      armor-drop-chance: 15.0
      weapon-chance: 2.0
      helmet-chance: 100.0
      chest-chance: 75.0
      leggings-chance: 56.25
      boots-chance: 42.19
    Hard:
      max-enchants: 3
      max-level: 2
      chance-to-have-armor: 20.0
      chance-to-enchant-a-piece: 45
      armor-drop-chance: 20.0
      weapon-chance: 5.0
      helmet-chance: 100.0
      chest-chance: 90.0
      leggings-chance: 81.0
      boots-chance: 72.9

# This section is to calculate the min & max affinity of players automatically, WILL OVERRIDE existing values.
calculating-affinity:
  # this setting whether to check every minute or if a player equips/holds an item.
  check-equipment: every-5-minutes # Supported: every-minute, every-5-minutes, on-equip
  min-affinity-changes:
    # Affinity limit is counted from the min-affinity set at the top (0 + 600 = 600)
    affinity-limit: 600
    points-per-minute: 1
    block-mined: 1
    player-hit: 0
    pvp-kill: 5
    pve-kill: 1
    death: -100
    # Will remove or add/remove affinity to the minAffinity of players if they're wearing/holding these items
    items-held-or-worn:
    - IRON_HELMET: 5
    - IRON_CHESTPLATE: 10
    - IRON_LEGGINGS: 5
    - IRON_BOOTS: 5
    - DIAMOND_HELMET: 10
    - DIAMOND_CHESTPLATE: 15
    - DIAMOND_LEGGINGS: 10
    - DIAMOND_BOOTS: 10
    - NETHERITE_HELMET: 10
    - NETHERITE_CHESTPLATE: 15
    - NETHERITE_LEGGINGS: 10
    - NETHERITE_BOOTS: 10
    - ELYTRA: 20
    - IRON_SWORD: 1
    - IRON_PICKAXE: 1
    - IRON_AXE: 1
    - DIAMOND_SWORD: 3
    - DIAMOND_PICKAXE: 3
    - DIAMOND_AXE: 3
    - NETHERITE_SWORD: 10
    - NETHERITE_PICKAXE: 10
    - NETHERITE_AXE: 10
    - GOLDEN_APPLE: 10
    - ENCHANTED_GOLDEN_APPLE: 20
  max-affinity-changes:
    # Affinity limit is counted from the max-affinity set at the top (1200 - 550 = 650)
    affinity-limit: 550
    points-per-minute: -2
    block-mined: -1
    player-hit: 1
    pvp-kill: -4
    pve-kill: -1
    death: 100
    # Will remove or add/remove affinity to the maxAffinity of players if they're wearing/holding these items
    items-held-or-worn:
    - IRON_HELMET: -1
    - IRON_CHESTPLATE: -2
    - IRON_LEGGINGS: -1
    - IRON_BOOTS: -1
    - DIAMOND_HELMET: -3
    - DIAMOND_CHESTPLATE: -4
    - DIAMOND_LEGGINGS: -3
    - DIAMOND_BOOTS: -3
    - NETHERITE_HELMET: -5
    - NETHERITE_CHESTPLATE: -8
    - NETHERITE_LEGGINGS: -5
    - NETHERITE_BOOTS: -5
    - ELYTRA: -10
    - IRON_SWORD: -1
    - IRON_PICKAXE: -1
    - IRON_AXE: -1
    - DIAMOND_SWORD: -3
    - DIAMOND_PICKAXE: -3
    - DIAMOND_AXE: -3
    - NETHERITE_SWORD: -5
    - NETHERITE_PICKAXE: -5
    - NETHERITE_AXE: -5
    - GOLDEN_APPLE: -5
    - ENCHANTED_GOLDEN_APPLE: -20
```
## calculate-exact-percentage
So if calculate-exact-percentage is disabled the damage chart will look something like this:
![calculate-exact-percentage-disabled.jpg](docs/calculate-exact-percentage-disabled.jpeg?raw=true "Disabled")

And if it's enabled it will look like this:
![calculate-exact-percentage-enabled.jpg](docs/calculate-exact-percentage-enabled.jpeg?raw=true "Enabled")

I made this a separate option so you can decide yourself if you would like to keep the steep difficulty spikes similar to the world difficulties of Minecraft or if you want the players to feel more immersed in the difficulty changes over time. You won't notice it as much this way and it will keep the players more into the flow as they get better.
If you'd like to read more about the psychology behind it: https://en.wikipedia.org/wiki/Flow_(psychology)

## Commands, Permissions and explanations
The Commands aren't case sensitive, meaning both remove and ReMoVe will result in the same function being executed. Also applies to /aFFinIty. This may not work on player names though
If you want to change the settings of the world you'll need to replace the <user> part with world
```
You can also add .other or .self after the permissions to only support 1 kind of change

/Affinity set <user> <number>
perm: affinity.set
The amount of affinity an user will be set to.

/Affinity get <user>
perm: affinity.get
Get the maximum and current affinity of an user.

/Affinity add <user> <number>
perm: affinity.add
Adds an amount of affinity to an user.

/Affinity remove <user> <number>
perm: affinity.remove
Removes an amount of affinity to an user.

/Affinity delMax <user> <number>
perm: affinity.delmax
Removes the maximum Affinity limit for an user.

/Affinity setMax <user> <number>
perm: affinity.setmax
Sets a maximum Affinity limit for an user.

/Affinity delMin <user> <number>
perm: affinity.delmin
Removes the minimum Affinity limit for an user.

/Affinity setMin <user> <number>
perm: affinity.setmin
Sets a minimum Affinity limit for an user.

/Affinity author
perm: affinity.author
Mentions DynamicDifficulty, my name and the Github page.

/Affinity reload
perm: affinity.reload
Reloads the config.

/Affinity force-save
perm: affinity.force-save
Force saves the current data to the yml or database.

/Affinity playergui
perm: affinity.playergui
Allows you to change all player settings in a chestGUI
```
## Plugin Support
#### PlaceholderAPI [[Link](https://www.spigotmc.org/resources/placeholderapi.6245/)]
```
%dd_text_difficulty%
 - Returns the difficulty of a player

%dd_affinity_points%
 - Returns the affinity points of a player
 
%dd_world_text_difficulty%
 - Returns the difficulty of the world

%dd_world_affinity_points%
 - Returns the affinity points of the world

%dd_max_affinity%
 - Returns the maximum affinity you can acquire
 
%dd_min_affinity%
 - Returns the minimum affinity you can acquire
```

## Other Small Things
Feel free to contact me if you have any idea's that could expand/improve the DynamicDifficulty plugin or have any trouble getting it to work on your server due to errors
- [x] Per player and World difficulties
- [x] Permissions on Luckperms and other management tools (now with .other & .self)
- [x] Custom Affinity points for each mobs and blocks
- [x] Implemented BStats & Placeholder API.
- [x] Disable DD for certain worlds and Mobs
- [x] /reload & /force-save command
- [x] noSaveType, MySQL, SQlite, MongoDB, PostGreSQL support
- [x] Randomize Difficulty mode
- [x] Change player settings with chest GUI
- [x] Stop certain mobs from following players on chosen difficulties
- [x] selector support (@a, @p etc)
- [x] Hunger Drain
- [x] Auto calculate Min Affinity setting
- [x] Mob armor spawn chance
- [x] 

## Possible Future Updates
- [ ] per biome difficulty
- [ ] promoted/demoted message

## Extra thanks to:
Noiverre - For testing the Papi functions (Because there were a few issues sometimes)
Mithran - For submitting several idea's and informing me of a lot of issues
CodedRed - For all the Minecraft plugin videos
Len76 - For testing Dynamic Difficulty, giving a few idea's and giving some advice regarding performance
