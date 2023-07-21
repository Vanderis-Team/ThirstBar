# THIRST BAR
- Author: Vanderis Developer Team

- Current Version: 2.0

![Thirst_Bar_3](https://github.com/Vanderis-Team/ThirstBar/assets/135495959/09962c6c-0845-4f55-bb13-78c458050c2e)
# INTRODUCE
Basically, the plugin allows the player to add a new unit called thirst.

The other features are all complemented around this thirst unit

# FEATURES
![Features](https://github.com/Vanderis-Team/ThirstBar/assets/135495959/d5cc823c-c751-4093-88d1-f4cab36ac135)
1. Display the current and the maximum thirst value of players through various forms such as BossBar, ActionBar, and PlaceholderAPI. (This value can be an integer or a real number)
2. The player's current thirst value will always be decremented over time, the default value and duration of each drop can be customized by the admin.
3. The player can restore thirst value by eating or drinking (consuming items), these items can be vanilla items, items with custom names, and lore, items with custom model data, or custom texture.
4. When the thirst bar reaches a certain value, you can change the color, change the bossbar title or change the title action bar. 
    - Also apply certain effects (Potion Effect) on the player, such as at 100%, the player's thirst bar is green, and has a speed 1 effect, while at 25%, the player's thirst bar is green. the player's thirst is red and has a slow effect 1. 
    - In addition to the potion effects, and changing the display status of the action bar/bossbar, we can also add actions like [title], [message], [sound], or [player]/[console]
5. Disable the thirst bar in some special worlds (DisabledWorld) or some special zones (Region - WorldGuard flag). You can also customize different thirst rates in different regions (WorldGuard)
6. Another special display method is to replace the hunger bar with the thirst bar. (Hunger bar will almost be removed and useless when you enable this feature)
7. Players can also drink rainwater or raw water if this feature is enabled. You can also customize the actions, effects, and thirst values ​​of each of these method types.

# COMMANDS
![Commands](https://github.com/Vanderis-Team/ThirstBar/assets/135495959/18261555-be45-4035-8fd3-8da1e54f0bc0)
- /tb [page] - /tb help [page]: Show information and a list of Thirst Bar commands.
- /tb reload: Reload plugin.
- /refresh [playerName]: Refresh for yourself or for [playerName].
- /refresh all: Refresh for all players in the server.
- /tb set <value> [playerName]: Set the current thirst value of yourself or [playerName].
