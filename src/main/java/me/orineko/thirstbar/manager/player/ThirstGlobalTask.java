package me.orineko.thirstbar.manager.player;

import me.orineko.thirstbar.ThirstBar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ThirstGlobalTask extends BukkitRunnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline() || player.isDead()) {
                continue;
            }
            
            PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().getData(player.getName());
            if (playerData != null) {
                playerData.tick(player);

                ArmorStand armorStand = playerData.getArmorStandFrontPlayer();
                if (armorStand != null) {
                    if (player.isSneaking() && player.getItemInHand().getType().equals(Material.AIR)) {
                        Location location = player.getEyeLocation().clone();
                        Vector vector = location.getDirection();
                        location = location.add(vector.getX() * 3, vector.getY() * 3, vector.getZ() * 3);
                        location = location.subtract(vector.getX() * 0.5, 1, vector.getZ() * 0.5);
                        if (!location.getChunk().isLoaded()) location.getChunk().load();
                        try {
                            armorStand.teleport(location);
                        } catch (Exception ignored) {
                        }
                        playerData.setArmorStandBehindPlayer(true);
                    } else if (playerData.isArmorStandBehindPlayer()) {
                        Location playerLocation = player.getLocation();
                        Location location = new Location(player.getWorld(), playerLocation.getX(), 255, playerLocation.getZ());
                        if (!location.getChunk().isLoaded()) location.getChunk().load();
                        try {
                            armorStand.teleport(location);
                        } catch (Exception ignored) {
                        }
                        playerData.setArmorStandBehindPlayer(false);
                    }
                }
            }
        }
    }
}
