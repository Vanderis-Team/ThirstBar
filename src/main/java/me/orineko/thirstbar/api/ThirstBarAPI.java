package me.orineko.thirstbar.api;

import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.player.PlayerData;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * ThirstBarAPI provides a unified interface for other developers to interact with the ThirstBar plugin.
 */
public class ThirstBarAPI {

    /**
     * Gets the current thirst value of a player.
     * @param player The player
     * @return The thirst value, or -1 if the player data is not found.
     */
    public static double getThirst(Player player) {
        PlayerData data = ThirstBar.getInstance().getPlayerDataList().getData(player.getName());
        return data != null ? data.getThirst() : -1;
    }

    /**
     * Sets the thirst value of a player.
     * @param player The player
     * @param value The new thirst value
     */
    public static void setThirst(Player player, double value) {
        PlayerData data = ThirstBar.getInstance().getPlayerDataList().getData(player.getName());
        if (data != null) {
            data.setThirst(value);
            data.updateAll(player);
        }
    }

    /**
     * Adds thirst to a player.
     * @param player The player
     * @param value The amount of thirst to add
     */
    public static void addThirst(Player player, double value) {
        PlayerData data = ThirstBar.getInstance().getPlayerDataList().getData(player.getName());
        if (data != null) {
            data.addThirst(value);
            data.updateAll(player);
        }
    }

    /**
     * Subtracts thirst from a player.
     * @param player The player
     * @param value The amount of thirst to subtract
     */
    public static void removeThirst(Player player, double value) {
        PlayerData data = ThirstBar.getInstance().getPlayerDataList().getData(player.getName());
        if (data != null) {
            data.addThirst(-value);
            data.updateAll(player);
        }
    }

    /**
     * Gets the maximum thirst value for a player.
     * @param player The player
     * @return The maximum thirst value
     */
    public static double getMaxThirst(Player player) {
        PlayerData data = ThirstBar.getInstance().getPlayerDataList().getData(player.getName());
        return data != null ? data.getThirstMax() : 20.0;
    }

    /**
     * Triggers the ItemsAdder deployment process manually.
     * This will update the ItemsAdder resource pack files if needed.
     */
    public static void deployItemsAdderResources() {
        me.orineko.thirstbar.api.itemsadderapi.ItemsAdderDeployer.checkAndDeploy();
    }
}
