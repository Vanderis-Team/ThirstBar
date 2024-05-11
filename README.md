# ThirstBar v2.1a - PlaceholderAPI and WorldGuard support!
  
![Thirst_Bar_3](https://raw.githubusercontent.com/Vanderis-Team/ThirstBar/main/Media%20FIles/Designs/Thirst_Bar.png)

Are you looking for something new in your Minecraft server? What are your thoughts on the idea of players having to balance something other than their own hunger bar?  
Thirst Bar brings to your server a thirst mechanism as well as additional mechanisms surrounding it, just like Minecraft's inherent hunger bar.
This can make the gameplay on your server more lively and realistic. 

### Support:
  - PlaceholderAPI
  - WorldGuard
___
![Features](https://raw.githubusercontent.com/Vanderis-Team/ThirstBar/main/Media%20FIles/Designs/Features.png)
  
- One of the most important features is that each player has a thirst point, a unit like a hunger bar, which can be displayed in two main ways: Bossbar and Action Bar.
    - Another unique method of displaying Thirst Bars is the bar from hunger to bar.
- The thirst value of each player can be expressed by an integer or a real number.
- The config allows for adjusting the player's default maximum thirst, and it can also be increased. For example, player A initially has a default maximum thirst of 100, but with permission or admin commands, their maximum thirst can be raised to 200.
- The player's thirst bar will gradually decrease over time, with the rate of decrease being adjustable as X/Y (where X represents the amount of thirst that will decrease per Y tick).
    
    Note: the higher y, the more lag.
  
- Players can restore thirst value by eating or drinking (consuming items), these items can be vanilla items, items with custom names, and lore, or items with custom model data, custom textures.
    - By shifting + right-clicking on the air or water source, players can now directly drink rainwater or raw water. However, these methods frequently have accompanying effects that the administrator can customize in the configuration.
  
![ezgif com-optimize (11)](https://github.com/L3via/JustTest/assets/98169091/69c6e39e-62b0-4309-8b1e-d8a58bab11da)
  
![ezgif com-optimize (15)](https://github.com/L3via/JustTest/assets/98169091/e3824181-6239-4b18-b2db-d92ddf9c8f76)

![ezgif com-optimize (13)](https://github.com/L3via/JustTest/assets/98169091/46b514cf-3d59-4e5e-ac4b-d90f9c7fb402)

- When the thirst bar reaches a certain threshold, it can change color, bossbar title, or action bar title. At the same time, it gives the player certain effects (Potion Effects).
    
    For example, at 100%, the player's thirst bar is green and has speed effect 1, whereas at 25%, it is red and has a slow effect 1. Change the display state of the action bar/boss bar, and we can also add actions like [title], [message], [sound], or [player]/[console] - these actions will be performed when the player has just reached that thirst threshold.
    
![ezgif com-optimize (10)](https://github.com/L3via/JustTest/assets/98169091/8706456b-ebec-4e5e-9b89-0614d59104da)
  
- Disable the thirst bar in specific worlds (DisabledWorld) or regions (Region - WorldGuard flag).
    - We can still adjust the thirst speed in each world/region in addition to completely disabling the thirst bar.

![ezgif com-optimize (16)](https://github.com/L3via/JustTest/assets/98169091/fd2c69c3-8f65-4b9a-82fa-0eb862420270)
  
- The thirst bar can also be disabled when in specific game modes that can be customized by the administrator, such as: creative, spectator,...
  
![ezgif com-optimize (14)](https://github.com/L3via/JustTest/assets/98169091/3b38c6c0-82f1-4985-9c1d-b2e28b66a398)
___
![Commands](https://github.com/L3via/JustTest/assets/98169091/c69574ea-7ead-4a0b-851b-5b3d13161200)
  
- /tb reload: Reload plugin.
- /refresh [player]: Refresh a player.
- /refreshall: Refresh all players.
- /tb set <value> [player]: Set current thirst value for a player.
- /tb restore <value> [player]: Restores current thirst value for a player.
- /tb reduce <value> [player]: Reduces current thirst value for a player.
- /tb max set <value> [player]: Set the maximum thirst value for a player.
- /tb reset: Reset all players' maximum thirst value to default.
- /tb disable [player]: Disables thirst bars for a player.
- /tb disableall: Disables thirst bar for all players.
- /tb stage <stage> [player]: Set the thirst stage for the player.
- /tb stageall <stage> : Set thirst stage for all players.
- /tb item save <name> <value>: Save custom items to restore thirst.
- /tb item give <name> [player]: Give custom items to a player
___
![permission](https://github.com/L3via/JustTest/assets/98169091/63bf50c1-210d-4610-a261-8fb61c713a9b) 
  
- thirstbar.help: permission to use command /tb [page] & /tb help [page]
- thirstbar.reload: permission to use /tb reload command
- thirstbar.refresh: permission to use /tb refresh command on yourself
- thirstbar.refresh.other: permission to use the /tb refresh command on both yourself and other players (with no cooldown).
- thirstbar.refreshall: permission to use /tb refreshall command
- thirstbar.set.current: permission to use the command /tb set…
- thirstbar.add: permission to use the command /tb add…
- thirstbar.reduce: permission to use /tb reduce… command
- thirstbar.set.max: permission to use the command /tb max set…
- thirstbar.reset: permission to use the command /tb reset…
- thirstbar.disable: permission to use the command /tb disable…
- thirstbar.disableall: permission to use the command /tb disableall…
- thirstbar.stage: permission to use the command /tb stage…
- thirstbar.stageall: permission to use the command /tb stageall…
- thirstbar.item.save: permission to use the command /tb item save…
- thirstbar.item.give: permission to use the command /tb item give…
___
![Placeholder](https://github.com/L3via/JustTest/assets/98169091/824c9c47-f3b1-45dc-9850-85b306788859)
  
- %thirstbar_current_int%: Current thirst value (integer).
- %thirstbar_current_float%: Current thirst value (float).
- %thirstbar_max_int%: Maximum thirst value (int).
- %thirstbar_max_float%: Maximum thirst value (float).
- %thirstbar_reduceValue_int%: The player’s thirst value is reduced (integer).
- %thirstbar_reduceTime_int%: The player’s thirst reduction time (integer).
- %thirstbar_reduceValue_float%: The player’s thirst value is reduced (float).
- %thirstbar_reduceTime_float%: The player’s thirst reduction time (float).
- %thirstbar_reducePerSec_int%: The player’s thirst value is reduced per time (integer).
- %thirstbar_reducePerSec_float%: The player’s thirst value is reduced per time (float).
- %thirstbar_isDisabled%: Player’s thirst status (whether disabled or not).
___
![configuration](https://github.com/L3via/JustTest/assets/98169091/beebe670-24c3-4362-b251-71d6691a9510)
## config.yml
```yaml
Thirsty:
  Max: 100
  Reduce: 1
  Time: 100 #Tick (20 ticks = 1 second)
  Damage: 1.5 #Lost 0.5 hp when thirsty to 0
CooldownRefresh: 5 #Second
DisabledGamemode:
  - "CREATIVE"
  - "SPECTATOR"
DisabledWorlds:
  - "world_nether"
DrinkingRawWater:
  Enable: true
  Delay: 10 #Tick
  Value: 5 #Value to increase thirst
  Reduce: 50
  #Your thirst speed will increase by 50%
  Duration: 100 #Tick
  TitleActionBar: "&a<value>&f/&b<max> &f- <reduce>/<time>s &f-&c RAW WATER"
  TitleBossBar: "&a<value>&f/&b<max> &f- <reduce>/<time>s &f-&c RAW WATER"
  Color: BLUE
  Style: SEGMENTED_10
  Effects:
    - "SLOW:1"
  Actions:
    - "[message] &cYou are drinking raw water!"
    - "[sound] ENTITY_GENERIC_DRINK"
    # You can uncomment if you want.
    # - "[title] &cBe careful"
    # - "[title-sub] &cYou are drinking raw water!"
    # - "[player] help"
    # - "[console] msg <player> You are drinking raw water!."
DrinkingRain:
  Enable: true
  Delay: 10
  Value: 5
  Reduce: 50
  Duration: 100
  TitleActionBar: "&a<value>&f/&b<max> &f- <reduce>/<time>s &f-&c RAIN WATER"
  TitleBossBar: "&a<value>&f/&b<max> &f- <reduce>/<time>s &f-&c RAIN WATER"
  Color: BLUE
  Style: SEGMENTED_10
  Effects:
    - "SLOW:1"
  Actions:
    - "[message] &cYou are drinking rain water!"
    - "[sound] ENTITY_GENERIC_DRINK"
    # You can uncomment if you want.
    # - "[title] &cBe careful"
    # - "[title-sub] &cYou are drinking rain water!"
    # - "[sound] BLOCK_ANVIL_BREAK"
    # - "[player] help"
    # - "[console] msg <player> You are drinking rain water!."
ReplaceHunger: false
# If enabled, the player's hunger bar will be the thirst bar.
BossBar:
  Enable: true
  Title: "&a<value>&f/&b<max> &f- <reduce>/<time>s"
  DisableTitle: "&a<value>&f/&b<max> &f- <reduce>/<time>s - &4DISABLE"
  Color: BLUE #BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW
  Style: SEGMENTED_10 #SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
ActionBar:
  Enable: true
  Title: "&a<value>&f/&b<max> &f- <reduce>/<time>s"
  DisableTitle: "&a<value>&f/&b<max> &f- <reduce>/<time>s - &4DISABLE"
Materials:
  - "APPLE:20"
  - "POTATO:20%"
  - "POTION:30"
  - "GOLDEN_APPLE:50"
  - "MILK_BUCKET:75%"
```

## message.yml
```yaml
SetItemSuccess: "&eSet item success."
Refresh: "&eYou have been successfully refreshed."
RefreshOther: "&e<player> has been successfully refreshed."
RefreshAll: "&eAll players have been successfully refreshed."
Set: "&eYour thirst value has been set to <value>."
SetOther: "&e<player>'s thirst value has been set to <value>."
Restore: "&eYou have been restored to <value> thirst values"
RestoreOther: "&e<player> has been restored to <value> thirst values"
Reduce: "&eYou have been reduced by <value> thirst values"
ReduceOther: "&e<player> has been reduced by <value> thirst values"
Load: "&eYou have received <item>."
LoadOther: "&e<player> has received <item>."
Disable: "&eYour thirst bar has been disabled."
DisableOther: "&e<player>'s thirst  bar has been disabled."
Enable: "&eYour thirst bar has been enabled."
EnableOther: "&e<player> has been enabled."
DisableAll: "&eAll players's thirst bars have been enabled."
SetMax: "&eYour maximum thirst value has been set to <value>."
SetMaxOther: "&e<player>'s max thirst value has been set to <value>."
SetStage: "&eYour stage has been set to <stage>."
SetStageOther: "&e<player>'s stage has been set to <value>."
SetStageAll: "&eAll players' stage have been set to <value>."
Reload: "&eReload successful."
Reset: "&eReset successful."
CommandNotExist: "&cThis command does not exist."
ItemNotFound: "&cItem is not found."
StageNotFound: "&cStage is not found."
PlayerNotFound: "&cPlayer is not online."
NeedItemInHand: "&cYou need to hold the item in your hand."
WaitingRefresh: "&cYou need to wait <time> seconds to to this again."
ErrorFormat: "&cYour format is not correct."
OnlyPlayerUseCommand: "&cOnly players can use this command."
DontHavePermission: "&cYou don't have permission to do this."
```

### stages.yml
```yaml
Stage1:
  Range: "40:20"
  Reduce: 50 #This means the thirst speed will be 50% faster than the default
  TitleActionBar: "&a<value>&f/&b<max> &f- <reduce>/<time>s &c- Stage 1"
  TitleBossBar: "&a<value>&f/&b<max> &f- <reduce>/<time>s &c- Stage 1"
  Color: YELLOW
  Style: SEGMENTED_10
  Effects:
    - "SLOW:1"
  Actions:
    - "[title] &6You start to feel thirst"
    - "[title-sub] &fLook for water sources"
    # You can also use other actions such as below:
    # - "[sound] BLOCK_ANVIL_BREAK"
    # - "[message] &eYou start to feel thirst."
    # - "[player] idk"
    # - "[console] msg <player> Admin reminds you to drink water every day"
    # - "[console] give <player> milk_bucket"
Stage2:
  Range: "19:0"
  Reduce: 100
  TitleActionBar: "&a<value>&f/&b<max> &f- <reduce>/<time>s &c- Stage2"
  TitleBossBar: "&a<value>&f/&b<max> &f- <reduce>/<time>s &c- Stage2"
  Color: RED
  Style: SEGMENTED_10
  Effects:
    - "SLOW:2"
    - "NAUSEA:1"
    - "WEAKNESS:1"
  Actions:
    - "[title] &cExhausted from thirst"
    - "[title-sub] &fYou need to drink water to survive"
```
