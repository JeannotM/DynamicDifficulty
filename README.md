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
# points-on-interval is made to give the user points every few minutes. Pairs up with interval-timer
points-on-interval: 4

# The amount of affinity a user or the world increases (or decreases):
# when a block from the blocks list is mined.
block-mined: 1

# When player is hit by a mob.
player-hit: -1

# When a player kills another player.
pvp-kill: 10

# When a mob from the mobs-count-as-pve list is killed.
pve-kill: 1

# When a player dies.
death: -100

# The amount of affinity a user has when joining the server for the first time (or the first time after installing DynamicDifficulty)
starting-affinity: 500

# What The Minimum and Maximum Affinity Are, no one can get more or less than this (even with commands)
min-affinity: 0
max-affinity: 1200

# 0 Equals Disabled, timer is in minutes
interval-timer: 2

# Mobs That Will Give Points From "pve-kill" when killed: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html
mobs-count-as-pve:
- ZOMBIE
- BLAZE
- CREEPER
- SKELETON
- ENDERMAN
- WITHER_SKELETON
- SPIDER
- CAVESPIDER
- VINDICATOR
- WITCH

# Blocks That Will Give Points From "block-mined" when mined: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
blocks:
- DIAMOND_ORE
- EMERALD_ORE

# Whether the Player Will Receive Affinity When Mining Blocks With Silk Touch
silk-touch-allowed: false

# Calculates the percentage between 2 difficulties so the progression will feel more natural.
# A better explanation is given underneath the settings explanation tab
calculate-exact-percentage: true

# Will calculate difficulty per player if enabled (equity)
# And will calculate the difficulty for all players the same way if disabled (equality)
per-player-difficulty: true

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
difficulty:
  easy:
    affinity-required: 0
    damage-done-by-mobs: 50
    damage-done-on-mobs: 100
    experience-multiplier: 70
    double-loot-chance: 0
    effects-when-attacked: false
  normal:
    affinity-required: 400
    damage-done-by-mobs: 75
    damage-done-on-mobs: 100
    experience-multiplier: 90
    double-loot-chance: 0
    effects-when-attacked: true
  hard:
    affinity-required: 1000
    damage-done-by-mobs: 100
    damage-done-on-mobs: 100
    experience-multiplier: 125
    double-loot-chance: 5
    effects-when-attacked: true
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

/Affinity removeMax <user> <number>
perm: affinity.removemax
Removes the maximum Affinity limit for an user.

/Affinity setMax <user> <number>
perm: affinity.setmax
Sets a maximum Affinity limit for an user.

/Affinity author
perm: affinity.author
Mentions DynamicDifficulty, my name and the Github page.
```
## Plugin Support
#### PlaceholderAPI [[Link](https://www.spigotmc.org/resources/placeholderapi.6245/)]
```
%dynamicdifficulty_text_difficulty%
 - Returns the difficulty of a player

%dynamicdifficulty_affinity_points%
 - Returns the affinity points of a player
 
%dynamicdifficulty_world_text_difficulty%
 - Returns the difficulty of the world

%dynamicdifficulty_world_affinity_points%
 - Returns the affinity points of the world

%dynamicdifficulty_max_affinity%
 - Returns the maximum affinity you can acquire
 
%dynamicdifficulty_min_affinity%
 - Returns the minimum affinity you can acquire
```

## Other Small Things
Feel free to contact me if you have any idea's that could expand/improve the DynamicDifficulty plugin or have any trouble getting it to work on your server due to errors
- [x] Automatically sorts difficulties by affinity requirement.
- [x] Double Loot does not work on mobs that can pickup items.
- [x] Per player and World difficulties
- [x] Permissions on Luckperms and other management tools
- [x] Placeholder API.

## Possible Future Updates
- [ ] Prefixes with Vault.
- [ ] Multiverse Worlds Implementation
- [ ] Implement BStats
- [ ] Custom Affinity points for each mobs