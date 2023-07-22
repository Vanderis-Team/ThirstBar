# THIRST BAR
- Author: Vanderis Developer Team

- Current Version: 2.0

![Thirst_Bar_3](https://github.com/Vanderis-Team/ThirstBar/assets/135495959/09962c6c-0845-4f55-bb13-78c458050c2e)
# INTRODUCE
This plugin adds thirst to Minecraft. The other features are all designed to complement this unit.

# FEATURES
![Features](https://github.com/Vanderis-Team/ThirstBar/assets/135495959/d5cc823c-c751-4093-88d1-f4cab36ac135)
1. The plugin can display the current and maximum thirst values of players in various ways, such as through the BossBar, ActionBar, and PlaceholderAPI. These values can be integers or real numbers.
2. The player's current thirst value will always decrease over time. The default value and duration of each decrease can be customized by the administrator.
3. Players can restore their thirst values by eating or drinking (consuming items). These items can be vanilla items, items with custom names and lore, items with custom model data, or items with custom textures.
4. When the thirst bar reaches a certain value, you can change the color, the title of the BossBar, or the title of the ActionBar.
    - You can also apply certain effects (Potion Effects) to the player. For example, at 100%, the player's thirst bar is green and they have the speed 1 effect. At 25%, the player's thirst bar is red and they have the slow 1 effect.
    - In addition to the potion effects and changing the display status of the ActionBar/BossBar, you can also add actions such as [title], [message], [sound], or [player]/[console]. 
6. You can disable the thirst bar in certain worlds (DisabledWorld) or certain zones (Region - WorldGuard flag). You can also customize the thirst rates in different regions (WorldGuard).
7. Another special display method is to replace the hunger bar with the thirst bar. When this feature is enabled, the hunger bar will almost be removed and useless.
8. Players can also drink rainwater or raw water if this feature is enabled. You can also customize the actions, effects, and thirst values of each of these method types.

# COMMANDS
![Commands](https://github.com/Vanderis-Team/ThirstBar/assets/135495959/18261555-be45-4035-8fd3-8da1e54f0bc0)
- /tb [page] - /tb help [page]: Show information and a list of Thirst Bar commands.
- /tb reload: Reload plugin.
- /refresh [playerName]: Refresh for yourself or for [playerName].
- /refresh all: Refresh for all players in the server.
- /tb set <value> [playerName]: Set current thirst value of yourself or [playerName].
- /tb add <value> [playerName]: Add current thirst value of yourself or [playerName].
- /tb reduce <value> [playerName]: Reduce current thirst value of yourself or [playerName].

