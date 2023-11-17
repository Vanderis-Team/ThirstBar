package me.orineko.thirstbar.manager.player;

import me.orineko.pluginspigottools.DataList;
import me.orineko.pluginspigottools.FileManager;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.file.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

public class PlayerDataList extends DataList<PlayerData> {

    public PlayerData addData(@Nonnull String name) {
        PlayerData playerData = getData(name);
        if(playerData != null) return playerData;
        playerData = new PlayerData(name);
        getDataList().add(playerData);
        return playerData;
    }

    public PlayerData addData(@Nonnull Player player) {
        return addData(player.getName());
    }

    @Nullable
    public PlayerData getData(@Nonnull String name) {
        return super.getData(d -> d.getName().equals(name));
    }

    public void removeDataPlayersOnline(){
        getDataList().stream().filter(p -> p.getPlayer() != null)
                .forEach(playerData -> {
                    ThirstBar.getInstance().getPlayersFile().set(playerData.getName()+".Thirst", playerData.getThirst());
                    playerData.getBossBar().removePlayer(playerData.getPlayer());
                    playerData.disableExecuteReduce();
                    playerData.disableExecuteRefresh();
                    Player player = playerData.getPlayer();
                    if(player != null) playerData.disableStage(player, null);
                });
        ThirstBar.getInstance().getPlayersFile().save();
    }

    public void loadData(){
        FileManager file = ThirstBar.getInstance().getPlayersFile();
        ConfigurationSection section = file.getConfigurationSection("");
        if(section != null) section.getKeys(false).forEach(name -> {
            PlayerData playerData = addData(name);
            BigDecimal max = new BigDecimal(file.getString(name+".Max", "0"));
            if(max.compareTo(BigDecimal.valueOf(1)) > 0) {
                max = max.min(BigDecimal.valueOf(Double.MAX_VALUE));
                playerData.setThirstMax(max.doubleValue());
                playerData.refresh();
            }
            Player player = Bukkit.getPlayer(playerData.getName());
            if(player != null) {
                boolean disable = file.getBoolean(name + ".Disable", false);
                if (disable) playerData.setDisable(true);
                boolean check1 = false;
                try {
                    check1 = ConfigData.DISABLED_GAMEMODE.stream().anyMatch(g ->
                            player.getGameMode().equals(GameMode.valueOf(g.toUpperCase())));
                } catch (IllegalArgumentException ignore) {
                }
                boolean check2 = ConfigData.DISABLED_WORLDS.stream().anyMatch(w ->
                        player.getWorld().getName().trim().equalsIgnoreCase(w.trim()));
                playerData.setDisableAll(check1 || check2);
                playerData.updateAll(player);
            }
        });
    }
}
