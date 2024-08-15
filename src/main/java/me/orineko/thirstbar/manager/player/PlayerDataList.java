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
import java.util.HashMap;
import java.util.List;

public class PlayerDataList extends DataList<PlayerData> {

    public PlayerData addData(@Nonnull String name) {
        PlayerData playerData = getData(name);
        if (playerData != null) return playerData;
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

    public void removeDataPlayers() {
        Bukkit.getScheduler().cancelTasks(ThirstBar.getInstance());
        getDataList().forEach(playerData -> {
            if (ThirstBar.getInstance().getSqlManager().getConnection() == null) {
                ThirstBar.getInstance().getPlayersFile().set(playerData.getName() + ".Thirst", playerData.getThirst());
            } else {
                ThirstBar.getInstance().getSqlManager().runSetThirstPlayer(playerData.getName(), playerData.getThirst());
            }
            if(playerData.getArmorStandFrontPlayer() != null) {
                playerData.getArmorStandFrontPlayer().remove();
                playerData.setArmorStandFrontPlayer(null);
            }
            playerData.setThirst(playerData.getThirstMax());
            Player player = playerData.getPlayer();
            if (player != null){
                playerData.disableStage(player, null);
                player.getActivePotionEffects().forEach(v -> {
                    player.removePotionEffect(v.getType());
                });
            }
            playerData.getBossBar().removeAll();
        });
        if (ThirstBar.getInstance().getSqlManager().getConnection() == null) {
            ThirstBar.getInstance().getPlayersFile().save();
        }
        getDataList().clear();
    }

    public void loadData() {
        FileManager file = ThirstBar.getInstance().getPlayersFile();
        if (ThirstBar.getInstance().getSqlManager().getConnection() == null) {
            ConfigurationSection section = file.getConfigurationSection("");
            if (section != null) section.getKeys(false).forEach(name -> {
                PlayerData playerData = addData(name);
                BigDecimal max = new BigDecimal(file.getString(name + ".Max", "0"));
                if (max.compareTo(BigDecimal.valueOf(1)) > 0) {
                    max = max.min(BigDecimal.valueOf(Double.MAX_VALUE));
                    playerData.setThirstMax(max.doubleValue());
                }
                playerData.setThirst(file.getDouble(name + ".Thirst", playerData.getThirstMax()));
                boolean disable = file.getBoolean(name + ".Disable", false);
                if (disable) playerData.setDisable(true);
                Player player = Bukkit.getPlayer(playerData.getName());
                if (player != null) {
                    playerData.updateAll(player);
                    boolean check1 = false;
                    try {
                        check1 = ConfigData.DISABLED_GAMEMODE.stream().anyMatch(g ->
                                player.getGameMode().equals(GameMode.valueOf(g.toUpperCase())));
                    } catch (IllegalArgumentException ignore) {
                    }
                    boolean check2 = ConfigData.DISABLED_WORLDS.stream().anyMatch(w ->
                            player.getWorld().getName().trim().equalsIgnoreCase(w.trim()));
                    playerData.setDisableAll(check1 || check2);
                }
            });
        } else {
            List<HashMap<String, Object>> list = ThirstBar.getInstance().getSqlManager().runGetPlayer();
            list.forEach(row -> {
                String name = (String) row.getOrDefault("name", null);
                boolean disable = (int) row.getOrDefault("disable", 0) == 1;
                double thirst = (double) row.getOrDefault("thirst", -1);
                double max = (double) row.getOrDefault("max", 0);
                if (name == null) return;
                PlayerData playerData = addData(name);
                if (disable) playerData.setDisable(true);
                if (thirst > 0) playerData.setThirst(thirst);
                if (max > 0) {
                    playerData.setThirstMax(max);
                }
                Player player = Bukkit.getPlayer(playerData.getName());
                if (player != null) {
                    playerData.updateAll(player);
                    boolean check1 = false;
                    try {
                        check1 = ConfigData.DISABLED_GAMEMODE.stream().anyMatch(g ->
                                player.getGameMode().equals(GameMode.valueOf(g.toUpperCase())));
                    } catch (IllegalArgumentException ignore) {
                    }
                    boolean check2 = ConfigData.DISABLED_WORLDS.stream().anyMatch(w ->
                            player.getWorld().getName().trim().equalsIgnoreCase(w.trim()));
                    playerData.setDisableAll(check1 || check2);
                }
            });
        }

    }
}
