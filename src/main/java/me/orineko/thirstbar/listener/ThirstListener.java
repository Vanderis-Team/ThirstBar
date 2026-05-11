package me.orineko.thirstbar.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketAdapter;
import me.orineko.pluginspigottools.nbtapi.NBTItem;
import me.orineko.pluginspigottools.utils.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.ThirstBarMethod;
import me.orineko.thirstbar.api.worldguardapi.WorldGuardApi;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.item.ItemData;
import me.orineko.thirstbar.manager.player.PlayerData;
import me.orineko.thirstbar.manager.stage.StageConfig;
import me.orineko.thirstbar.manager.stage.StageList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.*;

public class ThirstListener implements Listener {

    //    public static final HashMap<UUID, ArmorStand> armorStandMap = new HashMap<>();
    private final List<UUID> delayClickMap = new ArrayList<>();
    private final List<UUID> delayMoveMap = new ArrayList<>();
    private final List<UUID> consumeCooldownMap = new ArrayList<>();
    private final String keyPotionRaw = "RawWater";
    private final NamespacedKey usageKey = new NamespacedKey(ThirstBar.getInstance(), "custom_item_usage");
    private final Map<UUID, DrinkingAnimationState> drinkingAnimationStateMap = new HashMap<>();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    public ThirstListener() {
        registerProtocolItemPackets();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);

        Bukkit.getScheduler().runTaskAsynchronously(ThirstBar.getInstance(), () -> {
            double tempThirstFile = -1;
            boolean isSql = ThirstBar.getInstance().getSqlManager().getConnection() != null;
            if (!isSql) {
                tempThirstFile = ThirstBar.getInstance().getPlayersFile()
                        .getDouble(playerData.getName() + ".Thirst", -1);
                if (tempThirstFile >= 0) {
                    ThirstBar.getInstance().getPlayersFile().setAndSave(playerData.getName() + ".Thirst", null);
                }
            } else {
                tempThirstFile = ThirstBar.getInstance().getSqlManager().runGetThirstCurrentPlayer(playerData.getName());
                if (tempThirstFile >= 0) {
                    ThirstBar.getInstance().getSqlManager().runSetThirstPlayer(playerData.getName(), -1);
                }
            }

            final double finalThirst = tempThirstFile;
            Bukkit.getScheduler().runTask(ThirstBar.getInstance(), () -> {
                if (player == null || !player.isOnline()) return;

                if (finalThirst >= 0) {
                    playerData.setThirst(finalThirst);
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
                playerData.createArmorStand(player);
            });
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        drinkingAnimationMap.remove(uuid);
        drinkingAnimationStateMap.remove(uuid);
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        playerData.setDisplayBossBar(false, player);
        playerData.getBossBar().removePlayer(player);
        playerData.disableStage(player, null);

        ArmorStand armorStand = playerData.getArmorStandFrontPlayer();
        if (armorStand != null) {
            armorStand.remove();
            playerData.setArmorStandFrontPlayer(null);
        }
    }

    @EventHandler
    public void onChangeGameMode(PlayerGameModeChangeEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().getData(player.getName());
        if (playerData == null) return;
        GameMode gameMode = e.getNewGameMode();
        List<String> gamemodeList = ConfigData.DISABLED_GAMEMODE;
        try {
            playerData.setDisableAll(gamemodeList.stream()
                    .anyMatch(g -> gameMode.equals(GameMode.valueOf(g.toUpperCase()))));
        } catch (IllegalArgumentException ignore) {

        }
        playerData.updateAll(player);
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

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        // Real consume finished for our temporary drink item
        if (drinkingAnimationMap.contains(uuid)) {
            e.setCancelled(true);
            finishDrinkingAnimation(player, true);
            return;
        }

        // Block consume events that fire while our drink animation is running
        ItemStack itemHand = player.getItemInHand();
        if (itemHand.getType().equals(Material.AIR)) return;
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        if (playerData.isDisableAll() || playerData.isDisable()) return;

        String tagRawWater = null;
        try {
            NBTItem nbtItem = new NBTItem(itemHand);
            tagRawWater = nbtItem.getString(keyPotionRaw);
        } catch (Throwable ignore) {}

        if (tagRawWater != null && tagRawWater.equals("true")) {
            StageConfig stageWater = ThirstBar.getInstance().getStageList().getStageConfig(StageList.KeyConfig.WATER);
            if (stageWater != null) {

                if (ConfigData.STOP_DRINKING && playerData.getThirst() >= playerData.getThirstMax()) {
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
        if (itemData.getConsumeType() != null && itemData.getConsumeType().equalsIgnoreCase("on_player")) {
            e.setCancelled(true);
            if (consumeCooldownMap.contains(player.getUniqueId())) return;
            ItemStack consumed = e.getItem();
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0f, 1.0f);
            startDrinkingAnimation(player, itemData, consumed.clone(), playerData);
            return;
        }
        double value = itemData.getValue();
        if ((ConfigData.STOP_DRINKING && !checkFoodHaveEffect(itemHand)) &&
                playerData.getThirst() >= playerData.getThirstMax()) {
            e.setCancelled(true);
            return;
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

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockY() == e.getTo().getBlockY() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
            return;
        }

        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player.getName());

        if (!delayMoveMap.contains(player.getUniqueId())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(), () -> {
                delayMoveMap.remove(player.getUniqueId());
            }, 20);
            delayMoveMap.add(player.getUniqueId());

            if (ThirstBar.getInstance().isWorldGuardApiEnable()) {
                boolean check = ConfigData.DISABLED_WORLDS.stream().anyMatch(w ->
                        player.getWorld().getName().trim().equalsIgnoreCase(w.trim()));
                boolean check1 = false;
                try {
                    check1 = ConfigData.DISABLED_GAMEMODE.stream().anyMatch(g ->
                            player.getGameMode().equals(GameMode.valueOf(g.toUpperCase())));
                } catch (IllegalArgumentException ignore) {
                }
                boolean check2 = WorldGuardApi.isPlayerInFlag(player);
                playerData.setDisableAll(check || check1 || check2);
                playerData.updateAll(player);
            }
            if (!playerData.isDisableAll() && !playerData.isDisable()) {
                if (ThirstBar.getInstance().isWorldGuardApiEnable()) {
                    double reduce = WorldGuardApi.getReduceValueLocationPlayer(player);
                    if (reduce > 0) {
                        playerData.setThirstReduce(reduce);
                    } else {
                        playerData.setThirstReduce(ConfigData.THIRSTY_REDUCE);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAttackEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player player = (Player) e.getDamager();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player.getName());
        ArmorStand armorStand = playerData.getArmorStandFrontPlayer();
        if (armorStand == null) return;
        if (e.getEntity().equals(armorStand)) e.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onClickEntity(PlayerInteractAtEntityEvent e) {
        Player player = e.getPlayer();
        if (!player.isSneaking()) return;
        ItemStack itemStack = player.getItemInHand();
        if (!itemStack.getType().equals(Material.AIR)) return;
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        Entity entity = e.getRightClicked();
        ArmorStand armorStand = playerData.getArmorStandFrontPlayer();
        if (armorStand == null) return;
        if (!entity.equals(armorStand)) return;
        if (playerData.isDisable()) return;
        StageConfig stageWater = ThirstBar.getInstance().getStageList().getStageConfig(StageList.KeyConfig.WATER);
        if (stageWater != null) {
            if (!delayClickMap.contains(player.getUniqueId())) {
                if (stageWater.isEnable()) {
                    if (ThirstBarMethod.checkSightIsWater(player)) {
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
                                    playerData.updateAll(player);
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
                                        playerData.updateAll(player);
                                    }, stageRain.getDuration());
                            Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                    () -> delayClickMap.remove(player.getUniqueId()), stageRain.getDelay());
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(PlayerInteractEvent e) {
        if(e.isCancelled()) return;
        if (e.getHand() != null && e.getHand() != EquipmentSlot.HAND) return;
        if (!(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) return;
        Player player = e.getPlayer();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        if (playerData.isDisableAll() || playerData.isDisable()) return;
        ItemStack itemStack = player.getItemInHand();
        if (itemStack.getType().equals(Material.AIR) && player.isSneaking()) {
            if (!ConfigData.STOP_DRINKING || playerData.getThirst() >= playerData.getThirstMax()) {
                StageConfig stageWater = ThirstBar.getInstance().getStageList().getStageConfig(StageList.KeyConfig.WATER);
                if (stageWater != null && !delayClickMap.contains(player.getUniqueId()) &&
                        stageWater.isEnable() && ThirstBarMethod.checkSightIsWater(player)) {
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
                                playerData.updateAll(player);
                            }, stageWater.getDuration());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                            () -> delayClickMap.remove(player.getUniqueId()), stageWater.getDelay());
                }
            }
        }

        if (player.getInventory().getItemInOffHand().getType().equals(Material.GLASS_BOTTLE)) {
            e.setCancelled(true);
        } else if (itemStack.getType().equals(Material.GLASS_BOTTLE)) {
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                if (ThirstBarMethod.checkSightIsWater(player)) {
                    ItemStack itemBottle = MethodDefault.getItemAllVersion("POTION");
                    NBTItem nbtItem = new NBTItem(itemBottle);
                    nbtItem.setString(keyPotionRaw, "true");
                    itemBottle = nbtItem.getItem();
                    ItemMeta meta = itemBottle.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(ConfigData.NAME_RAW_POTION);
                        meta.setLore(ConfigData.LORE_RAW_POTION);
                        try {
                            ItemFlag flag = ItemFlag.valueOf("HIDE_POTION_EFFECTS");
                            meta.addItemFlags(flag);
                        } catch (Exception ignore) {
                            ItemFlag fallback = ItemFlag.valueOf("HIDE_ADDITIONAL_TOOLTIP");
                            meta.addItemFlags(fallback);
                        }
                        itemBottle.setItemMeta(meta);
                    }

                    if (ThirstBar.getInstance().getVersionBukkit() > 16) {
                        PotionMeta potionMeta = (PotionMeta) itemBottle.getItemMeta();
                        if (potionMeta != null) {
                            potionMeta.setColor(Color.fromRGB(ConfigData.RED_COLOR_RAW_POTION, ConfigData.GREEN_COLOR_RAW_POTION, ConfigData.BLUE_COLOR_RAW_POTION));
                            itemBottle.setItemMeta(potionMeta);
                        }
                    }
                    e.setCancelled(true);
                    if (itemStack.getAmount() == 1) {
                        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), itemBottle);
                    } else {
                        ItemStack item = itemStack.clone();
                        item.setAmount(itemStack.getAmount() - 1);
                        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), item);
                        player.getInventory().addItem(itemBottle);
                    }
                } else {
                    e.setCancelled(true);
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
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent e) {
        ItemStack source = e.getSource();
        ItemData itemData = ThirstBar.getInstance().getItemDataList().getData(source);
        if (itemData == null) {
            if (isAnyCustomCookingMaterial(source.getType())) e.setTotalCookTime(Integer.MAX_VALUE);
            return;
        }
        if (itemData.getCookType() == null || !itemData.getCookType().equalsIgnoreCase("cooking")) {
            if (isAnyCustomCookingMaterial(source.getType())) e.setTotalCookTime(Integer.MAX_VALUE);
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent e) {
        ItemStack source = e.getSource();
        ItemData itemData = ThirstBar.getInstance().getItemDataList().getData(source);
        if (itemData == null) {
            if (isAnyCustomCookingMaterial(source.getType())) e.setCancelled(true);
            return;
        }
        if (itemData.getCookType() == null || !itemData.getCookType().equalsIgnoreCase("cooking")) {
            if (isAnyCustomCookingMaterial(source.getType())) e.setCancelled(true);
            return;
        }
        if (itemData.getCookReplace() == null || itemData.getCookReplace().trim().isEmpty()) return;
        ItemData replaceData = ThirstBar.getInstance().getItemDataList().getData(itemData.getCookReplace());
        if (replaceData == null || replaceData.getItemStack() == null) return;
        ItemStack result = replaceData.getItemStack().clone();
        transferUsage(source, result);
        e.setResult(result);
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e) {
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

    private boolean checkFoodHaveEffect(@Nonnull ItemStack itemStack) {
        List<String> list = Arrays.asList("GOLDEN_APPLE", "ENCHANTED_GOLDEN_APPLE",
                "PUFFERFISH", "MILK_BUCKET", "CHORUS_FRUIT", "POISONOUS_POTATO");
        for (String value : list) {
            if (itemStack.getType().equals(MethodDefault.getItemAllVersion(value).getType())) return true;
        }
        if (itemStack.getType().equals(Material.POTION)) {
            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            return potionMeta != null && !potionMeta.getCustomEffects().isEmpty();
        }
        return false;
    }

    private boolean isLookingAtWater(@Nonnull Player player) {
        try {
            Class<?> fluidCollisionModeClass = Class.forName("org.bukkit.FluidCollisionMode");
            Object always = Enum.valueOf((Class<Enum>) fluidCollisionModeClass.asSubclass(Enum.class), "ALWAYS");
            Object hit = Player.class.getMethod("rayTraceBlocks", double.class, fluidCollisionModeClass)
                    .invoke(player, 4.5, always);
            if (hit != null) {
                Object block = hit.getClass().getMethod("getHitBlock").invoke(hit);
                if (block instanceof Block) {
                    return ((Block) block).getType().name().contains("WATER");
                }
            }
        } catch (Throwable ignore) {
        }
        return ThirstBarMethod.checkSightIsWater(player);
    }

    private void replaceHeldItem(@Nonnull Player player, @Nonnull String replaceName) {
        replaceItemInSlot(player, player.getInventory().getHeldItemSlot(), replaceName);
    }

    private void replaceItemInSlot(@Nonnull Player player, int slot, @Nonnull String replaceName) {
        ItemData replaceData = ThirstBar.getInstance().getItemDataList().getData(replaceName);
        if (replaceData == null || replaceData.getItemStack() == null) return;
        ItemStack out = replaceData.getItemStack().clone();
        applyLocalVisualMeta(out, replaceData);
        player.getInventory().setItem(slot, out);
    }

    private void consumeCustomItem(@Nonnull Player player, @Nonnull PlayerData playerData,
                                   @Nonnull ItemStack itemHand, @Nonnull ItemData itemData, int slot) {
        if (ConfigData.STOP_DRINKING && playerData.getThirst() >= playerData.getThirstMax()) return;
        if (itemData.getConsumeRestore() != 0) {
            playerData.addThirst(itemData.getConsumeRestore());
        } else {
            double value = itemData.getValue();
            if (value > 0) playerData.addThirst(value);
            else if (itemData.getValuePercent() > 0) {
                playerData.addThirst(playerData.getThirstMax() * (itemData.getValuePercent() / 100));
            }
        }
        applyWaterType(player, playerData, itemData.getConsumeWaterType());
        if (playerData.getThirst() > playerData.getThirstMax()) playerData.setThirst(playerData.getThirstMax());
        if (playerData.getThirst() < 0) playerData.setThirst(0);
        if (itemData.getConsumeMaxUsage() > 0) {
            int usage = increaseUsageAndUpdateBar(itemHand, itemData.getConsumeMaxUsage(), itemData.isShowDurability());
            applyLocalVisualMeta(itemHand, itemData);
            player.getInventory().setItem(slot, itemHand);
            if (usage >= itemData.getConsumeMaxUsage() && itemData.getConsumeReplace() != null) {
                replaceItemInSlot(player, slot, itemData.getConsumeReplace());
            }
        } else if (itemData.getConsumeReplace() != null) {
            replaceItemInSlot(player, slot, itemData.getConsumeReplace());
        }
        playerData.updateAll(player);
    }

    private int increaseUsageAndUpdateBar(@Nonnull ItemStack itemStack, int maxUsage, boolean showDurability) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return 0;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int current = pdc.has(usageKey, PersistentDataType.INTEGER)
                ? pdc.get(usageKey, PersistentDataType.INTEGER) : 0;
        current++;
        pdc.set(usageKey, PersistentDataType.INTEGER, current);
        if (showDurability && meta instanceof Damageable && itemStack.getType().getMaxDurability() > 0) {
            Damageable damageable = (Damageable) meta;
            int maxDurability = itemStack.getType().getMaxDurability();
            int damage = (int) Math.round((Math.min(current, maxUsage) / (double) maxUsage) * (maxDurability - 1));
            damageable.setDamage(Math.max(0, damage));
        }
        itemStack.setItemMeta(meta);
        return current;
    }

    private void applyWaterType(@Nonnull Player player, @Nonnull PlayerData playerData, String waterType) {
        if (waterType == null) return;
        if (!waterType.equalsIgnoreCase("RAW_WATER")) return;
        StageConfig stageWater = ThirstBar.getInstance().getStageList().getStageConfig(StageList.KeyConfig.WATER);
        if (stageWater == null) return;
        if (playerData.idDelayDisable != 0) {
            Bukkit.getScheduler().cancelTask(playerData.idDelayDisable);
            playerData.idDelayDisable = 0;
        }
        playerData.disableStage(player, StageList.KeyConfig.WATER);
        playerData.setStage(player, stageWater);
        playerData.idDelayDisable = Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                () -> playerData.disableStage(player, StageList.KeyConfig.WATER), stageWater.getDuration());
    }

    // Tracks players currently in drink animation to prevent consume re-entry
    private final java.util.Set<UUID> drinkingAnimationMap = new java.util.HashSet<>();
    private static final long DRINK_ANIMATION_TICKS = 32L;
    private static class DrinkingAnimationState {
        private final ItemData itemData;
        private final ItemStack originalHand;
        private final PlayerData playerData;
        private final int originHotbarSlot;
        private int monitorTaskId = 0;

        private DrinkingAnimationState(@Nonnull ItemData itemData, @Nonnull ItemStack originalHand,
                                      @Nonnull PlayerData playerData, int originHotbarSlot) {
            this.itemData = itemData;
            this.originalHand = originalHand;
            this.playerData = playerData;
            this.originHotbarSlot = originHotbarSlot;
        }
    }

    /**
     * Plays a real drink animation by temporarily swapping the held item server-side to a
     * consumable POTION (with drinking-model custom-model-data if configured), letting Minecraft
     * handle the native hold-and-drink animation, then restoring the original item after
     * the animation completes (~32 ticks).
     *
     * The consume logic is delayed to fire AFTER the animation finishes so the player
     * actually sees and hears the full drinking sequence before the item is replaced.
     */
    private void startDrinkingAnimation(@Nonnull Player player, @Nonnull ItemData itemData,
                                        @Nonnull ItemStack originalHand, @Nonnull PlayerData playerData) {
        UUID uuid = player.getUniqueId();
        if (drinkingAnimationMap.contains(uuid)) return;
        drinkingAnimationMap.add(uuid);
        int heldSlot = player.getInventory().getHeldItemSlot();
        DrinkingAnimationState state = new DrinkingAnimationState(itemData, originalHand.clone(), playerData, heldSlot);
        drinkingAnimationStateMap.put(uuid, state);

        // Build the visual drinking item — must be a consumable so Minecraft plays animation
        String drinkMat = itemData.getDrinkingModelMaterial();
        Material mat = null;
        if (drinkMat != null && !drinkMat.isEmpty()) {
            mat = Material.matchMaterial(drinkMat.toUpperCase());
        }
        if (mat == null) mat = Material.POTION;

        ItemStack drinkItem = new ItemStack(mat, 1);
        ItemMeta drinkMeta = drinkItem.getItemMeta();
        if (drinkMeta != null) {
            int cmd = itemData.getDrinkingModelData();
            if (cmd > 0) drinkMeta.setCustomModelData(cmd);
            Integer usage = null;
            ItemMeta originalHandMeta = originalHand.getItemMeta();
            if (originalHandMeta != null) {
                usage = originalHandMeta.getPersistentDataContainer().get(usageKey, PersistentDataType.INTEGER);
            }
            int currentUsage = usage == null ? 0 : usage;
            applyUsageBarText(drinkMeta, itemData, currentUsage);
            // Fallback if no template-based display/lore is configured
            ItemStack original = itemData.getItemStack();
            if (original != null) {
                ItemMeta origMeta = original.getItemMeta();
                if (origMeta != null) {
                    if (drinkMeta.displayName() == null && origMeta.hasDisplayName()) {
                        drinkMeta.displayName(LEGACY_SERIALIZER.deserialize(origMeta.getDisplayName())
                                .decoration(TextDecoration.ITALIC, false));
                    }
                    if (drinkMeta.lore() == null && origMeta.hasLore()) {
                        List<Component> lore = new ArrayList<>();
                        for (String line : origMeta.getLore()) {
                            lore.add(LEGACY_SERIALIZER.deserialize(line).decoration(TextDecoration.ITALIC, false));
                        }
                        drinkMeta.lore(lore);
                    }
                }
            }
            // Make it drinkable on 1.20.5+ so native animation triggers
            try {
                Object food = drinkMeta.getClass().getMethod("getFood").invoke(drinkMeta);
                food.getClass().getMethod("setCanAlwaysEat", boolean.class).invoke(food, true);
                food.getClass().getMethod("setNutrition", int.class).invoke(food, 0);
                food.getClass().getMethod("setSaturation", float.class).invoke(food, 0.0f);
                try { food.getClass().getMethod("setEatSeconds", float.class).invoke(food, 1.6f); } catch (NoSuchMethodException ignore) {}
                drinkMeta.getClass().getMethod("setFood", food.getClass()).invoke(drinkMeta, food);
            } catch (Throwable ignore) {}
            drinkItem.setItemMeta(drinkMeta);
        }

        // Swap real item server-side so client sees and animates properly
        player.getInventory().setItemInMainHand(drinkItem);
        player.updateInventory();

        // Monitor every tick: restore immediately when player cancels, hard-timeout as fallback
        final int[] ticks = {0};
        state.monitorTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ThirstBar.getInstance(), () -> {
            if (!drinkingAnimationMap.contains(uuid)) {
                cancelAnimationMonitor(state);
                return;
            }
            if (!player.isOnline()) {
                finishDrinkingAnimation(player, false);
                return;
            }
            ticks[0]++;
            if (ticks[0] >= (DRINK_ANIMATION_TICKS + 8L)) {
                finishDrinkingAnimation(player, false);
            }
        }, 1L, 1L);
    }

    private void finishDrinkingAnimation(@Nonnull Player player, boolean consumed) {
        UUID uuid = player.getUniqueId();
        DrinkingAnimationState state = drinkingAnimationStateMap.remove(uuid);
        if (state == null) {
            drinkingAnimationMap.remove(uuid);
            return;
        }
        drinkingAnimationMap.remove(uuid);
        cancelAnimationMonitor(state);
        if (!player.isOnline()) return;

        // Always restore original item to its original slot (not current selected slot)
        player.getInventory().setItem(state.originHotbarSlot, state.originalHand.clone());
        player.updateInventory();

        if (!consumed) return;
        consumeCustomItem(player, state.playerData, state.originalHand.clone(), state.itemData, state.originHotbarSlot);
    }

    private void cancelAnimationMonitor(@Nonnull DrinkingAnimationState state) {
        if (state.monitorTaskId == 0) return;
        Bukkit.getScheduler().cancelTask(state.monitorTaskId);
        state.monitorTaskId = 0;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChangeHeldSlot(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        if (!drinkingAnimationMap.contains(player.getUniqueId())) return;
        finishDrinkingAnimation(player, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDropWhileDrinking(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!drinkingAnimationMap.contains(uuid)) return;

        DrinkingAnimationState state = drinkingAnimationStateMap.get(uuid);
        if (state == null) {
            drinkingAnimationMap.remove(uuid);
            return;
        }

        // Ensure dropped item is the original custom item, not temporary potion swap item
        e.getItemDrop().setItemStack(state.originalHand.clone());

        // Item has been dropped, so clear original slot and end animation state without consume
        player.getInventory().setItem(state.originHotbarSlot, new ItemStack(Material.AIR));
        drinkingAnimationStateMap.remove(uuid);
        drinkingAnimationMap.remove(uuid);
        player.updateInventory();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClickWhileDrinking(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (!drinkingAnimationMap.contains(player.getUniqueId())) return;
        e.setCancelled(true);
        finishDrinkingAnimation(player, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDragWhileDrinking(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (!drinkingAnimationMap.contains(player.getUniqueId())) return;
        e.setCancelled(true);
        finishDrinkingAnimation(player, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHandWhileDrinking(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        if (!drinkingAnimationMap.contains(player.getUniqueId())) return;
        e.setCancelled(true);
        finishDrinkingAnimation(player, false);
    }

    private void playDrinkAnimation(@Nonnull Player player) {
        // Prefer ProtocolLib packet animation when available
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            try {
                PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_STATUS);
                packet.getIntegers().write(0, player.getEntityId());
                packet.getBytes().write(0, (byte) 9);
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                return;
            } catch (Exception ignore) {
            }
        }

        String[] candidates = {"DRINK_MILK", "DRINK"};
        for (String candidate : candidates) {
            try {
                EntityEffect effect = EntityEffect.valueOf(candidate);
                player.playEffect(effect);
                return;
            } catch (IllegalArgumentException ignore) {
            }
        }
    }

    private void registerProtocolItemPackets() {
        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) return;
        try {
            ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                    ThirstBar.getInstance(),
                    ListenerPriority.NORMAL,
                    PacketType.Play.Client.USE_ITEM,
                    PacketType.Play.Client.USE_ITEM_ON
            ) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    Player player = event.getPlayer();
                    if (player == null || !player.isOnline()) return;
                    EquipmentSlot hand = readHand(event.getPacket());
                    if (hand != EquipmentSlot.HAND) return;
                    Bukkit.getScheduler().runTask(ThirstBar.getInstance(), () -> handleUseItemPacket(player));
                }
            });

            ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                    ThirstBar.getInstance(),
                    ListenerPriority.NORMAL,
                    PacketType.Play.Server.SET_SLOT,
                    PacketType.Play.Server.WINDOW_ITEMS,
                    PacketType.Play.Server.SET_CURSOR_ITEM
            ) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    try {
                        PacketContainer packet = event.getPacket();
                        if (packet.getType() == PacketType.Play.Server.SET_SLOT) {
                            ItemStack item = packet.getItemModifier().read(0);
                            packet.getItemModifier().write(0, applyPacketVisuals(item));
                            return;
                        }
                        if (packet.getType() == PacketType.Play.Server.WINDOW_ITEMS) {
                            List<ItemStack> items = packet.getItemListModifier().read(0);
                            if (items == null) return;
                            List<ItemStack> mapped = new ArrayList<>(items.size());
                            for (ItemStack i : items) mapped.add(applyPacketVisuals(i));
                            packet.getItemListModifier().write(0, mapped);
                            return;
                        }
                        if (packet.getType() == PacketType.Play.Server.SET_CURSOR_ITEM) {
                            ItemStack cursor = packet.getItemModifier().read(0);
                            packet.getItemModifier().write(0, applyPacketVisuals(cursor));
                        }
                    } catch (Exception ex) {
                        ThirstBar.getInstance().getLogger().warning("Protocol item packet rewrite failed: " + ex.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            ThirstBar.getInstance().getLogger().warning("Failed to register ProtocolLib listeners: " + e.getMessage());
        }
    }

    private EquipmentSlot readHand(PacketContainer packet) {
        try {
            Object handObj = packet.getEnumModifier(EquipmentSlot.class, 0).read(0);
            if (handObj instanceof EquipmentSlot) return (EquipmentSlot) handObj;
        } catch (Exception ignore) {
        }
        return EquipmentSlot.HAND;
    }

    private void handleUseItemPacket(@Nonnull Player player) {
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        if (playerData.isDisableAll() || playerData.isDisable()) return;
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) return;
        ItemData itemData = ThirstBar.getInstance().getItemDataList().getData(hand);
        if (itemData == null) return;

        if ("on_water".equalsIgnoreCase(itemData.getRightClickType())) {
            if (tryFillFromPacketTrace(player, itemData.getRightClickReplace())) return;
        }

        if ("on_player".equalsIgnoreCase(itemData.getConsumeType())) {
            if (consumeCooldownMap.contains(player.getUniqueId())) return;
            if (ConfigData.STOP_DRINKING && playerData.getThirst() >= playerData.getThirstMax()) return;
            consumeCooldownMap.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(ThirstBar.getInstance(), () -> consumeCooldownMap.remove(player.getUniqueId()), 40L);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0f, 1.0f);
            startDrinkingAnimation(player, itemData, hand.clone(), playerData);
        }
    }

    private boolean tryFillFromPacketTrace(@Nonnull Player player, String replaceName) {
        Block traced = getTargetWaterLikeBottle(player);
        if (traced == null) return false;
        if (traced.getType() == Material.WATER_CAULDRON) decreaseCauldronWaterLevel(traced);
        replaceHeldItem(player, replaceName);
        playFillWaterSound(player);
        return true;
    }

    private Block getTargetWaterLikeBottle(@Nonnull Player player) {
        try {
            Block target = player.getTargetBlockExact(5, FluidCollisionMode.ALWAYS);
            return target != null && isFillableWaterSource(target) ? target : null;
        } catch (Throwable ignored) {
            Block traced = getRayTraceBlock(player, 5.0);
            if (traced != null && isFillableWaterSource(traced)) return traced;
            return null;
        }
    }

    private ItemStack applyPacketVisuals(ItemStack source) {
        if (source == null || source.getType() == Material.AIR) return source;
        ItemData itemData = ThirstBar.getInstance().getItemDataList().getData(source);
        if (itemData == null) return source;
        ItemStack item = source.clone();
        applyLocalVisualMeta(item, itemData);
        return item;
    }

    private void applyLocalVisualMeta(@Nonnull ItemStack item, @Nonnull ItemData itemData) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        Integer usage = meta.getPersistentDataContainer().get(usageKey, PersistentDataType.INTEGER);
        int currentUsage = usage == null ? 0 : usage;

        if (itemData.isHideAttribute()) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            try { meta.addItemFlags(ItemFlag.valueOf("HIDE_ATTRIBUTE_MODIFIERS")); } catch (Exception ignore) {}
            try { meta.addItemFlags(ItemFlag.valueOf("HIDE_ADDITIONAL_TOOLTIP")); } catch (Exception ignore) {}
            try { meta.getClass().getMethod("setHideTooltip", boolean.class).invoke(meta, true); } catch (Exception ignore) {}
            try { meta.getClass().getMethod("setAttributeModifiers", com.google.common.collect.Multimap.class).invoke(meta, new Object[] { null }); } catch (Exception ignore) {}
        }
        applyUsageBarText(meta, itemData, currentUsage);
        if (itemData.isShowDurability() && itemData.getConsumeMaxUsage() > 0 && meta instanceof Damageable
                && item.getType().getMaxDurability() > 0) {
            Damageable damageable = (Damageable) meta;
            int maxDurability = item.getType().getMaxDurability();
            int damage = (int) Math.round((Math.min(currentUsage, itemData.getConsumeMaxUsage()) /
                    (double) itemData.getConsumeMaxUsage()) * (maxDurability - 1));
            damageable.setDamage(Math.max(0, damage));
        }
        item.setItemMeta(meta);
    }

    private void applyUsageBarText(@Nonnull ItemMeta meta, @Nonnull ItemData itemData, int usage) {
        String usageBar = buildUsageBar(itemData, usage);
        String usageBarFont = itemData.getUsageBarFont();
        String displayTemplate = itemData.getDisplayNameTemplate();
        if (displayTemplate != null) {
            meta.displayName(renderTemplateWithUsageBar(displayTemplate, usageBar, usageBarFont));
        }
        List<String> loreTemplate = itemData.getLoreTemplate();
        if (loreTemplate != null && !loreTemplate.isEmpty()) {
            List<Component> lore = new ArrayList<>(loreTemplate.size());
            for (String line : loreTemplate) {
                lore.add(renderTemplateWithUsageBar(line, usageBar, usageBarFont));
            }
            meta.lore(lore);
        }
    }

    private Component renderTemplateWithUsageBar(@Nonnull String template, @Nonnull String usageBar, String usageBarFont) {
        String normalizedTemplate = template.replace("\\n", "\n");
        String[] parts = normalizedTemplate.split(java.util.regex.Pattern.quote("<usage-bar>"), -1);
        Component out = Component.empty();
        Key fontKey = parseFontKey(usageBarFont);
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                out = out.append(LEGACY_SERIALIZER.deserialize(parts[i]).decoration(TextDecoration.ITALIC, false));
            }
            if (i < parts.length - 1) {
                Component usageComponent = LEGACY_SERIALIZER.deserialize(usageBar);
                if (fontKey != null) usageComponent = usageComponent.font(fontKey);
                usageComponent = usageComponent
                        .decoration(TextDecoration.ITALIC, false)
                        .colorIfAbsent(NamedTextColor.WHITE);
                out = out.append(usageComponent);
            }
        }
        return out.decoration(TextDecoration.ITALIC, false);
    }

    private Key parseFontKey(String font) {
        if (font == null || font.trim().isEmpty()) return null;
        try {
            return Key.key(font.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String buildUsageBar(@Nonnull ItemData itemData, int usage) {
        if (itemData.getConsumeMaxUsage() <= 0) return "";
        int maxUsage = itemData.getConsumeMaxUsage();
        int consumed = Math.max(0, Math.min(usage, maxUsage));
        int filled = Math.max(0, maxUsage - consumed);
        String left = itemData.getUsageBarLeft() == null ? "" : itemData.getUsageBarLeft();
        String fill = itemData.getUsageBarFill() == null ? "" : itemData.getUsageBarFill();
        String empty = itemData.getUsageBarEmpty() == null ? "" : itemData.getUsageBarEmpty();
        String right = itemData.getUsageBarRight() == null ? "" : itemData.getUsageBarRight();
        StringBuilder out = new StringBuilder(left.length() + right.length() + ((fill.length() + empty.length()) * maxUsage));
        out.append(left);
        for (int i = 0; i < filled; i++) out.append(fill);
        for (int i = 0; i < consumed; i++) out.append(empty);
        out.append(right);
        return out.toString();
    }

    private boolean tryFillFromWaterSource(@Nonnull PlayerInteractEvent e, @Nonnull Player player, String replaceName, @Nonnull ItemStack currentItem) {
        if (replaceName == null || replaceName.trim().isEmpty()) return false;
        Block clicked = e.getClickedBlock();
        if (clicked != null && isFillableWaterSource(clicked)) {
            if (clicked.getType() == Material.WATER_CAULDRON) decreaseCauldronWaterLevel(clicked);
            replaceHeldItem(player, replaceName);
            playFillWaterSound(player);
            return true;
        }
        Block traced = getRayTraceBlock(player, 4.5);
        if (traced != null && isFillableWaterSource(traced)) {
            if (traced.getType() == Material.WATER_CAULDRON) decreaseCauldronWaterLevel(traced);
            replaceHeldItem(player, replaceName);
            playFillWaterSound(player);
            return true;
        }
        return false;
    }

    private Block getRayTraceBlock(@Nonnull Player player, double distance) {
        try {
            Class<?> fluidCollisionModeClass = Class.forName("org.bukkit.FluidCollisionMode");
            Object always = Enum.valueOf((Class<Enum>) fluidCollisionModeClass.asSubclass(Enum.class), "ALWAYS");
            Object hit = Player.class.getMethod("rayTraceBlocks", double.class, fluidCollisionModeClass)
                    .invoke(player, distance, always);
            if (hit == null) return null;
            Object block = hit.getClass().getMethod("getHitBlock").invoke(hit);
            return (block instanceof Block) ? (Block) block : null;
        } catch (Throwable ignore) {
            return null;
        }
    }

    private boolean isFillableWaterSource(@Nonnull Block block) {
        Material type = block.getType();
        if (type == Material.WATER) {
            if (block.getBlockData() instanceof Levelled) {
                Levelled levelled = (Levelled) block.getBlockData();
                return levelled.getLevel() == 0;
            }
            return true;
        }
        if (type == Material.WATER_CAULDRON) {
            if (block.getBlockData() instanceof Levelled) {
                return ((Levelled) block.getBlockData()).getLevel() > 0;
            }
        }
        return false;
    }

    private void decreaseCauldronWaterLevel(@Nonnull Block block) {
        if (!(block.getBlockData() instanceof Levelled)) return;
        Levelled data = (Levelled) block.getBlockData();
        int next = data.getLevel() - 1;
        if (next <= 0) {
            block.setType(Material.CAULDRON);
            return;
        }
        data.setLevel(next);
        block.setBlockData(data);
    }

    private void transferUsage(@Nonnull ItemStack source, @Nonnull ItemStack target) {
        ItemMeta sourceMeta = source.getItemMeta();
        ItemMeta targetMeta = target.getItemMeta();
        if (sourceMeta == null || targetMeta == null) return;
        PersistentDataContainer sourcePdc = sourceMeta.getPersistentDataContainer();
        if (sourcePdc.has(usageKey, PersistentDataType.INTEGER)) {
            Integer usage = sourcePdc.get(usageKey, PersistentDataType.INTEGER);
            if (usage != null) targetMeta.getPersistentDataContainer().set(usageKey, PersistentDataType.INTEGER, usage);
        }
        if (sourceMeta instanceof Damageable && targetMeta instanceof Damageable) {
            ((Damageable) targetMeta).setDamage(((Damageable) sourceMeta).getDamage());
        }
        target.setItemMeta(targetMeta);
    }

    private void playFillWaterSound(@Nonnull Player player) {
        try {
            player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0f, 1.0f);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 0.7f, 1.0f);
        } catch (Throwable ignored) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 0.9f, 1.0f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 0.7f, 1.0f);
        }
    }

    private boolean isAnyCustomCookingMaterial(@Nonnull Material material) {
        for (ItemData data : ThirstBar.getInstance().getItemDataList().getDataList()) {
            if (data.getItemStack() == null) continue;
            if (data.getCookType() == null || !data.getCookType().equalsIgnoreCase("cooking")) continue;
            if (data.getItemStack().getType() == material) return true;
        }
        return false;
    }
}
