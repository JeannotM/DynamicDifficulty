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
block-mined: 1

# When player is hit by a mob.
player-hit: -1

# When a player kills another player.
pvp-kill: 10

# When a mob from the mobs-count-as-pve list is killed.
pve-kill: 1

# When a player dies.
death: -80

# The amount of affinity a user has when joining the server for the first time (or the first time after installing DynamicDifficulty)
starting-affinity: 500

# What The Minimum and Maximum Affinity Are, no one can get more or less than this (even with commands)
min-affinity: 0
max-affinity: 1200

# Mobs That Will Give Points From "pve-kill" when killed: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html
# Format:
# - MOBTYPE: <Affinity points>
# Will use pve-kill if only MOBTYPE is given
mobs-count-as-pve:
- BLAZE: 4
- CAVESPIDER: 3
- CREEPER: 3
- DROWNED
- ELDER_GUARDIAN: 20
- ENDERMAN: 5
- ENDER_DRAGON: 100
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

saving-data:
  type: file # Supported: file, mysql, sqlite, mongodb
#  port: "3306"
#  host: "localhost"
#  username: "root"
#  password: ""
#  database: "dynamicdifficulty"

# You can disable DynamicDifficulty in certain worlds
# disabled-worlds:
# - example_name

# These mobs ignore everything except the "effects-when-attacked" and "mobs-ignore-player" settings from difficulty
disabled-mobs:
- WITHER
- ENDER_DRAGON

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
# Only "effects-when-attacked" affect the Wither and EnderDragon
# To see mob damage: https://minecraft.gamepedia.com/Mob#Damage_dealt_by_hostile_and_neutral_mobs
# Format if you want to create your own difficulty:
# <custom_name>:
#   affinity-required: <number, At What affinity this difficulty starts working>
#   damage-done-by-mobs: <percentage, How much damage mobs do to you>
#   damage-done-on-mobs: <percentage, How much damage you do on mobs>
#   experience-multiplier: <percentage, Experience Multiplier>
#   double-loot-chance: <percentage, Chance to double the loot dropped when a mob is killed>
#   effects-when-attacked: <bool, Whether you get poison/wither etc from mob attacks (not including splash potions), works only on normal/hard world difficulty>
#   prefix: <text, prefix to return if PlaceholderAPI is enabled>
#   mobs-ignore-player: <list, these mobs will ignore the players unless they're provoked>
difficulty:
  Easy:
    affinity-required: 0
    damage-done-by-mobs: 50
    damage-done-on-mobs: 100
    experience-multiplier: 70
    double-loot-chance: 0
    effects-when-attacked: false
    prefix: '&7&l[&b&lEasy&7&l]&r'
    mobs-ignore-player:
    - CREEPER
  Normal:
    affinity-required: 400
    damage-done-by-mobs: 75
    damage-done-on-mobs: 100
    experience-multiplier: 90
    double-loot-chance: 0
    effects-when-attacked: true
    prefix: '&7&l[&9&lNormal&7&l]&r'
  Hard:
    affinity-required: 1000
    damage-done-by-mobs: 100
    damage-done-on-mobs: 100
    experience-multiplier: 125
    double-loot-chance: 5
    effects-when-attacked: true
    prefix: '&7&l[&4&lHard&7&l]&r'
```
## calculate-exact-percentage
So if calculate-exact-percentage is disabled the damage chart will look something like this:
![calculate-exact-percentage-disabled.jpg](docs/calculate-exact-percentage-disabled.jpeg?raw=true "Disabled")

And if it's enabled it will look like this:
![calculate-exact-percentage-enabled.jpg](docs/calculate-exact-percentage-enabled.jpeg?raw=true "Enabled")

I made this a separate option so you can decide yourself if you would like to keep the steep difficulty spikes similar to the world difficulties of Minecraft or if you want the players to feel more immersed in the difficulty changes over time. You won't notice it as much this way and it will keep the players more into the flow as they get better.
If you'd like to read more about the psychology behind it: https://en.wikipedia.org/wiki/Flow_(psychology)

## Commands, Permissions and explanations
The Commands aren't case sensitive, meaning both remove and ReMoVe will result in the same function being executed. Also applies to /aFFinIty. This does not work on player names though
If you want to change the settings of the world you'll need to replace the <user> part with world
```
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
- [x] Permissions on Luckperms and other management tools
- [x] Custom Affinity points for each mobs and blocks
- [x] Implemented BStats & Placeholder API.
- [x] Disable DD for certain worlds and Mobs
- [x] /reload & /force-save command
- [x] MySQL, SQlite, MongoDB support
- [x] Randomize Difficulty mode
- [x] Stop certain mobs from following players on chosen difficulties

## Possible Future Updates
- [ ] change settings ingame
- [ ] per biome difficulty
- [ ] promoted/demoted message
- [ ] No save type
- [ ] Export to DB / File
- [ ] PostGreSQL