package me.orineko.thirstbar.listener;

import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.api.worldguardapi.WorldGuardApi;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.item.ItemData;
import me.orineko.thirstbar.manager.player.PlayerData;
import me.orineko.thirstbar.manager.stage.StageConfig;
import me.orineko.thirstbar.manager.stage.StageList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ThirstListener implements Listener {

    public static final HashMap<UUID, ArmorStand> armorStandMap = new HashMap<>();
    private final List<UUID> delayClickMap = new ArrayList<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        playerData.showBossBar(player);
        boolean check = ConfigData.DISABLE_WORLDS.stream().anyMatch(w ->
                player.getWorld().getName().trim().equalsIgnoreCase(w.trim()));
        playerData.setDisableAll(check);
        playerData.updateAll(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        ThirstBar.getInstance().getPlayerDataList().addData(player).setDisplayBossBar(false, player);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        playerData.setThirst(playerData.getThirstMax());
        boolean check = ConfigData.DISABLE_WORLDS.stream().anyMatch(w ->
                player.getWorld().getName().trim().equalsIgnoreCase(w.trim()));
        playerData.setDisableAll(check);
        playerData.updateAll(player);
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        ItemStack itemHand = player.getItemInHand();
        if (itemHand.getType().equals(Material.AIR)) return;
        ItemData itemData = ThirstBar.getInstance().getItemDataList().getData(itemHand);
        if (itemData == null) return;
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        if (playerData.isDisableAll() || playerData.isDisable()) return;
        double value = itemData.getValue();
        if (value <= 0) {
            value = itemData.getValuePercent();
            if (value > 0) {
                playerData.addThirst(playerData.getThirstMax() * (value / 100));
            }
        } else {
            playerData.addThirst(itemData.getValue());
        }
        if (playerData.getThirst() > playerData.getThirstMax()) playerData.setThirst(playerData.getThirstMax());
        if (playerData.getThirst() < 0) playerData.setThirst(0);
        Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(), () -> playerData.updateFood(player));
        playerData.updateAll(player);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player.getName());
        if(playerData.isDisableAll() || playerData.isDisable()) return;
        ItemStack itemHand = player.getItemInHand();
        Location location = player.getEyeLocation().clone();
        Vector vector = location.getDirection();
        ArmorStand armorStand = armorStandMap.getOrDefault(player.getUniqueId(), null);
        if (armorStand != null && (!e.isSneaking() || !itemHand.getType().equals(Material.AIR))) {
            armorStand.remove();
            armorStandMap.remove(player.getUniqueId());
            return;
        }
        if (!e.isSneaking() || !itemHand.getType().equals(Material.AIR)) return;
        Location locationCal = player.getEyeLocation().clone();
        boolean hasBlock = false;
        for (int i = 1; i <= 4; i++) {
            locationCal.add(vector.getX(), vector.getY(), vector.getZ());
            if (locationCal.getBlock().getType().equals(Material.AIR) ||
                    locationCal.getBlock().getType().equals(Material.WATER) ||
                    locationCal.getBlock().getType().name().equals("STATIONARY_WATER")) continue;
            hasBlock = true;
            break;
        }
        if (armorStand == null && !hasBlock) {
            armorStand = player.getWorld().spawn(location, ArmorStand.class);
            armorStandMap.put(player.getUniqueId(), armorStand);
        }
        if (armorStand != null && hasBlock) {
            armorStand.remove();
            armorStandMap.remove(player.getUniqueId());
            armorStand = null;
        }
        if (armorStand == null) return;
        armorStand.setVisible(false);
        armorStand.setGravity(false);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player.getName());

        if(ThirstBar.getInstance().isWorldGuardApiEnable()){
            boolean check = ConfigData.DISABLE_WORLDS.stream().anyMatch(w ->
                    player.getWorld().getName().trim().equalsIgnoreCase(w.trim()));
            boolean check2 = WorldGuardApi.isPlayerInFlag(player);
            playerData.setDisableAll(check || check2);
            playerData.updateAll(player);
        }
        if(playerData.isDisableAll() || playerData.isDisable()) return;
        if(ThirstBar.getInstance().isWorldGuardApiEnable()){
            double reduce = WorldGuardApi.getReduceValueLocationPlayer(player);
            if(reduce > 0){
                playerData.setThirstReduce(reduce);
            } else {
                playerData.setThirstReduce(ConfigData.THIRSTY_REDUCE);
            }
        }
        Location location = player.getEyeLocation().clone();
        Vector vector = location.getDirection();
        location = location.add(vector.getX() * 3, vector.getY() * 3, vector.getZ() * 3);
        ArmorStand armorStand = armorStandMap.getOrDefault(player.getUniqueId(), null);
        if (armorStand == null) return;
        armorStand.teleport(location.subtract(vector.getX() * 0.5, 0, vector.getZ() * 0.5));
    }

    @EventHandler
    public void onAttackEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player player = (Player) e.getDamager();
        ArmorStand armorStand = armorStandMap.getOrDefault(player.getUniqueId(), null);
        if (armorStand == null) return;
        if (e.getEntity().equals(armorStand)) e.setCancelled(true);
    }

    @EventHandler
    public void onClickEntity(PlayerInteractAtEntityEvent e) {
        Player player = e.getPlayer();
        if (!player.isSneaking()) return;
        ItemStack itemStack = player.getItemInHand();
        if (!itemStack.getType().equals(Material.AIR)) return;
        Entity entity = e.getRightClicked();
        ArmorStand armorStand = armorStandMap.getOrDefault(player.getUniqueId(), null);
        if (armorStand == null) return;
        if (!entity.equals(armorStand)) return;
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        if (playerData.isDisable()) return;
        StageConfig stageWater = ThirstBar.getInstance().getStageList().getStageConfig(StageList.KeyConfig.WATER);
        if (stageWater != null) {
            if (!delayClickMap.contains(player.getUniqueId())) {
                if (stageWater.isEnable()) {
                    Block block = e.getPlayer().getTargetBlock(null, 4);
                    if (block.getType().equals(Material.WATER) || block.getType().name().equals("STATIONARY_WATER")) {
                        delayClickMap.add(player.getUniqueId());
                        if (playerData.idDelayDisable != 0) {
                            Bukkit.getScheduler().cancelTask(playerData.idDelayDisable);
                            playerData.idDelayDisable = 0;
                        }
                        playerData.disableStage(player, StageList.KeyConfig.WATER);
                        playerData.setStage(player, stageWater);
                        playerData.addThirst(stageWater.getValue());
                        if (playerData.getThirst() > playerData.getThirstMax())
                            playerData.setThirst(playerData.getThirstMax());
                        if (playerData.getThirst() < 0) playerData.setThirst(0);
                        playerData.updateAll(player);
                        playerData.idDelayDisable = Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                () -> {
                                    playerData.disableStage(player, StageList.KeyConfig.WATER);
                                    playerData.idDelayDisable = 0;
                                }, stageWater.getDuration());
                        Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                () -> delayClickMap.remove(player.getUniqueId()), stageWater.getDelay());
                    }
                }
            }
        }
        StageConfig stageRain = ThirstBar.getInstance().getStageList().getStageConfig(StageList.KeyConfig.RAIN);
        if (stageRain != null) {
            if (stageRain.isEnable()) {
                if (!delayClickMap.contains(player.getUniqueId())) {
                    if (player.getWorld().hasStorm()) {
                        boolean check = false;
                        for (int i = player.getLocation().getBlockY() + 1; i <= 320; i++) {
                            Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), i, player.getLocation().getBlockZ());
                            if (!block.getType().equals(Material.AIR)) {
                                check = true;
                                break;
                            }
                        }
                        if (!check && player.getEyeLocation().getPitch() <= -45) {
                            delayClickMap.add(player.getUniqueId());
                            if (playerData.idDelayDisable != 0) {
                                Bukkit.getScheduler().cancelTask(playerData.idDelayDisable);
                                playerData.idDelayDisable = 0;
                            }
                            playerData.disableStage(player, StageList.KeyConfig.RAIN);
                            playerData.setStage(player, stageRain);
                            playerData.addThirst(stageRain.getValue());
                            if (playerData.getThirst() > playerData.getThirstMax())
                                playerData.setThirst(playerData.getThirstMax());
                            if (playerData.getThirst() < 0) playerData.setThirst(0);
                            playerData.updateAll(player);
                            playerData.idDelayDisable = Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                    () -> {
                                        playerData.disableStage(player, StageList.KeyConfig.RAIN);
                                        playerData.idDelayDisable = 0;
                                    }, stageRain.getDuration());
                            Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                    () -> delayClickMap.remove(player.getUniqueId()), stageRain.getDelay());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (!(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) return;
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        if (playerData.isDisableAll() || playerData.isDisable()) return;
        ItemStack itemStack = player.getItemInHand();
        if (itemStack.getType().equals(Material.AIR)) {
            if (player.isSneaking()) {
                StageConfig stageWater = ThirstBar.getInstance().getStageList().getStageConfig(StageList.KeyConfig.WATER);
                if (stageWater != null) {
                    if (!delayClickMap.contains(player.getUniqueId())) {
                        if (stageWater.isEnable()) {
                            Block block = e.getPlayer().getTargetBlock(null, 4);
                            if (block.getType().equals(Material.WATER) || block.getType().name().equals("STATIONARY_WATER")) {
                                delayClickMap.add(player.getUniqueId());
                                if (playerData.idDelayDisable != 0) {
                                    Bukkit.getScheduler().cancelTask(playerData.idDelayDisable);
                                    playerData.idDelayDisable = 0;
                                }
                                playerData.disableStage(player, StageList.KeyConfig.WATER);
                                playerData.setStage(player, stageWater);
                                playerData.addThirst(stageWater.getValue());
                                if (playerData.getThirst() > playerData.getThirstMax())
                                    playerData.setThirst(playerData.getThirstMax());
                                if (playerData.getThirst() < 0) playerData.setThirst(0);
                                playerData.updateAll(player);
                                playerData.idDelayDisable = Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                        () -> {
                                            playerData.disableStage(player, StageList.KeyConfig.WATER);
                                            playerData.idDelayDisable = 0;
                                        }, stageWater.getDuration());
                                Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                        () -> delayClickMap.remove(player.getUniqueId()), stageWater.getDelay());
                            }
                        }
                    }
                }
            }
        }

        ItemStack itemHand = player.getItemInHand();
        if (itemHand.getType().equals(Material.AIR)) return;
        ItemData itemData = ThirstBar.getInstance().getItemDataList().getData(itemHand);
        if (itemData == null) return;
        if (player.getFoodLevel() != 20) return;
        player.setFoodLevel(19);
        Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(), () -> player.setFoodLevel(20));

    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e){
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player.getName());
        boolean check = ConfigData.DISABLE_WORLDS.stream().anyMatch(w ->
                player.getWorld().getName().trim().equalsIgnoreCase(w.trim()));
        playerData.setDisableAll(check);
        playerData.updateAll(player);
    }
}
