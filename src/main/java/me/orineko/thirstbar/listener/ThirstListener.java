package me.orineko.thirstbar.listener;

import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.pluginspigottools.NBTTag;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.api.worldguardapi.WorldGuardApi;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.item.ItemData;
import me.orineko.thirstbar.manager.player.PlayerData;
import me.orineko.thirstbar.manager.stage.StageConfig;
import me.orineko.thirstbar.manager.stage.StageList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.*;

public class ThirstListener implements Listener {

    public static final HashMap<UUID, ArmorStand> armorStandMap = new HashMap<>();
    private final List<UUID> delayClickMap = new ArrayList<>();
    private final String keyPotionRaw = "RawWater";

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        if(ThirstBar.getInstance().getSqlManager().getConnection() == null) {
            double thirstFile = ThirstBar.getInstance().getPlayersFile()
                    .getDouble(playerData.getName()+".Thirst", -1);
            if(thirstFile >= 0) {
                playerData.setThirst(thirstFile);
                ThirstBar.getInstance().getPlayersFile().setAndSave(playerData.getName()+".Thirst", null);
            }
        } else {
            double thirstFile = ThirstBar.getInstance().getSqlManager().runGetThirstCurrentPlayer(playerData.getName());
            if(thirstFile >= 0){
                playerData.setThirst(thirstFile);
                ThirstBar.getInstance().getSqlManager().runSetThirstPlayer(playerData.getName(), -1);
            }
        }

        playerData.showBossBar(player);
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

        if(armorStandMap.containsKey(player.getUniqueId())) return;
        ArmorStand armorStand = player.getWorld().spawn(
                new Location(player.getWorld(), 0, 0, 0), ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStandMap.put(player.getUniqueId(), armorStand);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        ThirstBar.getInstance().getPlayerDataList().addData(player).setDisplayBossBar(false, player);

        ArmorStand armorStand = armorStandMap.getOrDefault(player.getUniqueId(), null);
        if (armorStand == null) return;
        armorStand.remove();
        armorStandMap.remove(player.getUniqueId());
    }

    @EventHandler
    public void onChangeGameMode(PlayerGameModeChangeEvent e){
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().getData(player.getName());
        if(playerData == null) return;
        GameMode gameMode = e.getNewGameMode();
        List<String> gamemodeList = ConfigData.DISABLED_GAMEMODE;
        try {
            playerData.setDisableAll(gamemodeList.stream()
                    .anyMatch(g -> gameMode.equals(GameMode.valueOf(g.toUpperCase()))));
        } catch (IllegalArgumentException ignore){

        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        playerData.setThirst(playerData.getThirstMax());
        boolean check = ConfigData.DISABLED_WORLDS.stream().anyMatch(w ->
                player.getWorld().getName().trim().equalsIgnoreCase(w.trim()));
        playerData.setDisableAll(check);
        playerData.updateAll(player);
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        ItemStack itemHand = player.getItemInHand();
        if (itemHand.getType().equals(Material.AIR)) return;
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        if (playerData.isDisableAll() || playerData.isDisable()) return;

        String tagRawWater = NBTTag.getKey(itemHand, keyPotionRaw);
        if(tagRawWater != null && tagRawWater.equals("true")){
            StageConfig stageWater = ThirstBar.getInstance().getStageList().getStageConfig(StageList.KeyConfig.WATER);
            if (stageWater != null) {

                double value = stageWater.getValue();
                if(playerData.getThirst() + value/2 >= playerData.getThirstMax()) {
                    e.setCancelled(true);
                    return;
                }

                playerData.disableStage(player, StageList.KeyConfig.WATER);
                playerData.setStage(player, stageWater);
                playerData.addThirst(stageWater.getValue());
                if (playerData.getThirst() > playerData.getThirstMax())
                    playerData.setThirst(playerData.getThirstMax());
                if (playerData.getThirst() < 0) playerData.setThirst(0);
                playerData.updateAll(player);
                playerData.idDelayDisable = Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                        () -> playerData.disableStage(player, StageList.KeyConfig.WATER), stageWater.getDuration());
            }
            return;
        }

        ItemData itemData = ThirstBar.getInstance().getItemDataList().getData(itemHand);
        if (itemData == null) return;
        double value = itemData.getValue();
        if(!(ConfigData.STOP_DRINKING && checkFoodHaveEffect(itemHand))){
            if(playerData.getThirst() + value/2 >= playerData.getThirstMax()
                    || playerData.getThirst() + (playerData.getThirst()*itemData.getValuePercent()/100)/2 >= playerData.getThirstMax()) {
                e.setCancelled(true);
                return;
            }
        }
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
/*

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        new Thread(() -> {
            PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player.getName());
            if(playerData.isDisableAll() || playerData.isDisable()) return;
            ItemStack itemHand = player.getItemInHand();
            Location location = player.getEyeLocation().clone();
            Vector vector = location.getDirection();
            final ArmorStand[] armorStand = {armorStandMap.getOrDefault(player.getUniqueId(), null)};
            if (armorStand[0] != null && (!e.isSneaking() || !itemHand.getType().equals(Material.AIR))) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(), () -> armorStand[0].remove());
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
            if (armorStand[0] == null && !hasBlock) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(), () -> {
                    armorStand[0] = player.getWorld().spawn(location, ArmorStand.class);
                    armorStand[0].setVisible(false);
                    armorStandMap.put(player.getUniqueId(), armorStand[0]);
                });
            }
            if (armorStand[0] != null && hasBlock) {
                armorStand[0].remove();
                armorStandMap.remove(player.getUniqueId());
                armorStand[0] = null;
            }
            if (armorStand[0] == null) return;
            armorStand[0].setVisible(false);
            armorStand[0].setGravity(false);
        }).start();
    }
*/

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player.getName());

        if(ThirstBar.getInstance().isWorldGuardApiEnable()){
            boolean check = ConfigData.DISABLED_WORLDS.stream().anyMatch(w ->
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

        ArmorStand armorStand = armorStandMap.getOrDefault(player.getUniqueId(), null);
        if (armorStand == null) return;
        if(player.isSneaking() && player.getItemInHand().getType().equals(Material.AIR)) {
            /*if (!armorStand.getLocation().equals(new Location(player.getWorld(), 0, 0, 0))){
                armorStand.teleport(new Location(player.getWorld(), 0, 0, 0));
            }*/
            Location location = player.getEyeLocation().clone();
            Vector vector = location.getDirection();
            location = location.add(vector.getX() * 3, vector.getY() * 3, vector.getZ() * 3);
            location = location.subtract(vector.getX() * 0.5, 1, vector.getZ() * 0.5);
            if(!location.getChunk().isLoaded()) location.getChunk().load();
            armorStand.teleport(location);
        } else {
            Location playerLocation = player.getLocation();
            Location location = new Location(player.getWorld(), playerLocation.getX(), 1, playerLocation.getZ());
            if(!location.getChunk().isLoaded()) location.getChunk().load();
            armorStand.teleport(location);
        }
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
                        for (int i = player.getLocation().getBlockY() + 1; i < 320; i++) {
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
                if(ConfigData.STOP_DRINKING && playerData.getThirst() >= playerData.getThirstMax()) {
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
        }

        if (itemStack.getType().equals(Material.GLASS_BOTTLE)){
            if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)){
                Block block = e.getPlayer().getTargetBlock(null, 4);
                if (block.getType().equals(Material.WATER) || block.getType().name().equals("STATIONARY_WATER")) {
                    ItemStack itemBottle = MethodDefault.getItemAllVersion("POTION");
                    itemBottle = NBTTag.setKey(itemBottle, keyPotionRaw, "true");
                    ItemMeta meta = itemBottle.getItemMeta();
                    if(meta != null){
                        meta.setDisplayName(ConfigData.NAME_RAW_POTION);
                        meta.setLore(ConfigData.LORE_RAW_POTION);
                        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                        itemBottle.setItemMeta(meta);
                    }
                    e.setCancelled(true);
                    if (itemStack.getAmount() == 1) {
                        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), itemBottle);
                    } else {
                        ItemStack item = itemStack.clone();
                        item.setAmount(itemStack.getAmount()-1);
                        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), item);
                        player.getInventory().addItem(itemBottle);
                    }
                }
            }
        }

        ItemStack itemHand = player.getItemInHand();
        if(ConfigData.STOP_DRINKING && checkFoodHaveEffect(itemHand)){
            if (itemHand.getType().equals(Material.AIR)) return;
            ItemData itemData = ThirstBar.getInstance().getItemDataList().getData(itemHand);
            if (itemData == null) return;
            if (player.getFoodLevel() != 20) return;
            player.setFoodLevel(19);
            Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(), () -> player.setFoodLevel(20));
        }
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e){
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player.getName());
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

    /*public void onFurnace(FurnaceSmeltEvent e){
        Bukkit.add
        ItemStack source = e.getSource();
        if()
    }*/

    private boolean checkFoodHaveEffect(@Nonnull ItemStack itemStack){
        List<String> list = Arrays.asList("GOLDEN_APPLE", "ENCHANTED_GOLDEN_APPLE",
                "PUFFERFISH", "MILK_BUCKET", "CHORUS_FRUIT", "POISONOUS_POTATO");
        for (String value : list) {
            if(itemStack.getType().equals(MethodDefault.getItemAllVersion(value).getType())) return true;
        }
        if(itemStack.getType().equals(Material.POTION)){
            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            return potionMeta != null && !potionMeta.getCustomEffects().isEmpty();
        }
        return false;
    }
}
