# DynamicDifficulty
## Table of Contents:
#### [About DynamicDifficulty](https://github.com/JeannotM/DynamicDifficulty#about-dynamicdifficulty-1)
#### [Config](https://github.com/JeannotM/DynamicDifficulty#config-1)
#### [calculate-exact-percentage](https://github.com/JeannotM/DynamicDifficulty#calculate-exact-percentage-1)
#### [Commands, Permissions and explanations](https://github.com/JeannotM/DynamicDifficulty#commands-permissions-and-explanations-1)
#### [Plugin Support](https://github.com/JeannotM/DynamicDifficulty#plugin-support-1)
#### [Other Small Things](https://github.com/JeannotM/DynamicDifficulty#other-small-things-1)
#### [Possible Future Updates](https://github.com/JeannotM/DynamicDifficulty#possible-future-updates-1)

## About DynamicDifficulty
I made the DynamicDifficulty for 3 reasons. I couldn't find any other DynamicDifficulty plugin for 1.16, the ones I saw from earlier versions weren't that customizable and I wanted experienced and new Minecraft players to be able to play on the same server without having to worry about it being too hard or too easy for them.

## Config
You can read the everything about the config here
[Read the config here](https://github.com/JeannotM/DynamicDifficulty/wiki/Config)

## calculate-exact-percentage
So if calculate-exact-percentage is disabled the damage chart will look something like this:
![calculate-exact-percentage-disabled.jpg](docs/calculate-exact-percentage-disabled.jpeg?raw=true "Disabled")

And if it's enabled it will look like this:
![calculate-exact-percentage-enabled.jpg](docs/calculate-exact-percentage-enabled.jpeg?raw=true "Enabled")

I made this a separate option so you can decide yourself if you would like to keep the steep difficulty spikes similar to the world difficulties of Minecraft or if you want the players to feel more immersed in the difficulty changes over time. You won't notice it as much this way and it will keep the players more into the flow as they get better.
If you'd like to read more about the psychology behind it: https://en.wikipedia.org/wiki/Flow_(psychology)

## Commands, Permissions and explanations
The Commands aren't case-sensitive, meaning both remove and ReMoVe will result in the same function being executed. Also applies to /aFFinIty. This may not work on player names though
If you want to change the settings of the world you'll need to replace the <user> part with world
```
You can also add .other or .self after the permissions to only allow commands for oneself.
affinity.*.self / affinity.*.other is also allowed.
If you don't provide a user in the commands, the player executing the command will be selected

/Affinity set <user?> <number>
perm: affinity.set
The amount of affinity an user will be set to.

/Affinity get <user?>
perm: affinity.get
Get the maximum and current affinity of an user.

/Affinity add <user?> <number>
perm: affinity.add
Adds an amount of affinity to an user.

/Affinity remove <user?> <number>
perm: affinity.remove
Removes an amount of affinity to an user.

/Affinity delMax <user?> <number>
perm: affinity.delmax
Removes the maximum Affinity limit for an user.

/Affinity setMax <user?> <number>
perm: affinity.setmax
Sets a maximum Affinity limit for an user.

/Affinity delMin <user?> <number>
perm: affinity.delmin
Removes the minimum Affinity limit for an user.

/Affinity setMin <user?> <number>
perm: affinity.setmin
Sets a minimum Affinity limit for an user.

/Affinity author
perm: affinity.author
Mentions DynamicDifficulty, my name and the person who translated the language you selected

/Affinity reload
perm: affinity.reload
Reloads the config.

/Affinity forceSave
perm: affinity.forcesave
Force saves the current data to the yml or database.

/Affinity playergui
perm: affinity.playergui
Allows you to change all player settings in a chestGUI.

/Affinity help
perm: affinity.help
Sends all the commands in DD to the player.

/Affinity info
perm: affinity.info
Returns some info about stuff that was saved or calculated in the app
```
## Plugin Support
#### PlaceholderAPI [[Link](https://www.spigotmc.org/resources/placeholderapi.6245/)]
```
%dd_user_difficulty%
 - Returns the difficulty of a player.

%dd_user_progress%
 - The progress between this and the next difficulty.

%dd_user_next_difficulty%
 - The next difficulty the user will get.

%dd_user_affinity%
 - Returns the affinity points of a player.

%dd_user_min_affinity%
 - Returns the minimum affinity a user can acquire.

%dd_user_max_affinity%
 - Returns the maximum affinity a user can acquire.
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
- [x] Block commands per difficulty
- [x] radial difficulty (gets harder the further you get away from spawn)
- [x] per biome difficulty

## Possible Future Updates
- [ ] per biome difficulty adjustments (extra)
- [ ] promoted/demoted message

## Extra thanks to:
Noiverre - For testing the Papi functions (Because there were a few issues sometimes)

Mithran - For submitting several idea's and informing me of a lot of issues

CodedRed - For all the Minecraft plugin videos

Len76 - For testing Dynamic Difficulty, giving a few idea's and giving some advice regarding performance
