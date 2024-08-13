package me.orineko.thirstbar;

import de.tr7zw.nbtapi.NBT;
import me.orineko.pluginspigottools.FileManager;
import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.command.CommandManager;
import me.orineko.thirstbar.command.MainCommand;
import me.orineko.thirstbar.listener.ThirstListener;
import me.orineko.thirstbar.manager.Method;
import me.orineko.thirstbar.manager.action.ActionManager;
import me.orineko.thirstbar.manager.api.PlaceholderAPI;
import me.orineko.thirstbar.manager.api.ThirstBarExpansion;
import me.orineko.thirstbar.manager.api.UpdateChecker;
import me.orineko.thirstbar.manager.api.sql.SqlManager;
import me.orineko.thirstbar.manager.api.worldguardapi.WorldGuardApi;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.file.MessageData;
import me.orineko.thirstbar.manager.item.ItemDataList;
import me.orineko.thirstbar.manager.player.PlayerData;
import me.orineko.thirstbar.manager.player.PlayerDataList;
import me.orineko.thirstbar.manager.stage.StageList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ThirstBar extends JavaPlugin {

    private static ThirstBar plugin;
    private PlayerDataList playerDataList;
    private StageList stageList;
    private ItemDataList itemDataList;
    private ConfigData configData;
    private MessageData messageData;
    private FileManager itemsFile;
    private FileManager messageFile;
    private FileManager playersFile;
    private FileManager stageFile;
    private FileManager actionsFile;
    private boolean worldGuardApiEnable = false;
    private SqlManager sqlManager;
    private ActionManager actionManager;
    private PlaceholderAPI placeholderAPI;

    @Override
    public void onEnable() {
        if (!NBT.preloadApi()) {
            getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin");
            getPluginLoader().disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        plugin = this;
        sqlManager = new SqlManager();

        messageFile = new FileManager("message.yml", this);
        stageFile = new FileManager("stages.yml", this);
        actionsFile = new FileManager("actions.yml", this);
        messageFile.copyDefault();
        stageFile.copyDefault();
        actionsFile.copyDefault();
        loadResourcePackFile();

        if (sqlManager.getConnection() == null) {
            itemsFile = new FileManager("customitems.db", this);
            playersFile = new FileManager("players.db", this);
            itemsFile.createFile();
            playersFile.createFile();
        }

        renewData();
        Bukkit.getOnlinePlayers().forEach(Method::disableGameMode);


        CommandManager.CommandRegistry.register(true, this, new MainCommand(this));
        getServer().getPluginManager().registerEvents(new ThirstListener(), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPI = new PlaceholderAPI();
            new ThirstBarExpansion().register();
        }
        checkForUpdate();

        ItemStack bottle = new ItemStack(Material.POTION, 1);
        ItemMeta meta = bottle.getItemMeta();
        PotionMeta pmeta = (PotionMeta) meta;
        PotionData pdata = new PotionData(PotionType.WATER);
        if(pmeta != null) pmeta.setBasePotionData(pdata);
        bottle.setItemMeta(meta);
        ItemStack potionRawItem = MethodDefault.getItemAllVersion("POTION");
        FurnaceRecipe furnaceRecipe;
        int versionNumber = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);
        if(versionNumber < 16) {
            furnaceRecipe = new FurnaceRecipe(bottle, potionRawItem.getType());
        } else {
            furnaceRecipe = new FurnaceRecipe(NamespacedKey.randomKey(), bottle, potionRawItem.getType(), ConfigData.CUSTOM_FURNACE_EXP, ConfigData.CUSTOM_FURNACE_COOKING_TIME);
        }
        Bukkit.addRecipe(furnaceRecipe);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        registerFlag();
    }

    @Override
    public void onDisable() {
        ThirstListener.armorStandMap.forEach((k, v) -> v.remove());
        if(actionManager != null) actionManager.removeRegister();
        getPlayerDataList().removeDataPlayersOnline();
    }

    public void renewData() {
        ThirstListener.armorStandMap.forEach((k, v) -> v.remove());
        Bukkit.getOnlinePlayers().forEach(player -> {
            ArmorStand armorStand = player.getWorld().spawn(
                    new Location(player.getWorld(), 0, 0, 0), ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            ThirstListener.armorStandMap.put(player.getUniqueId(), armorStand);
        });

        reloadConfig();
        if (sqlManager.getConnection() == null) {
            itemsFile.reloadWithoutCreateFile();
            playersFile.reloadWithoutCreateFile();
        }
        messageFile.reload();
        stageFile.reload();
        actionsFile.reload();
        configData = new ConfigData();
        messageData = new MessageData();
        itemDataList = new ItemDataList();
        itemDataList.loadData();
        stageList = new StageList();
        playerDataList = new PlayerDataList();
        Bukkit.getOnlinePlayers().forEach(p -> {
            PlayerData playerData = getPlayerDataList().addData(p);
            boolean check = ConfigData.DISABLED_WORLDS.stream().anyMatch(w ->
                    p.getWorld().getName().trim().equalsIgnoreCase(w.trim()));
            playerData.setDisableAll(check);
        });
        if(actionManager != null) actionManager.removeRegister();
        actionManager = new ActionManager();
        playerDataList.loadData();
    }

    private void loadResourcePackFile(){
        try {
            FileManager tutorialFile = new FileManager("tutorial.txt", this);
            tutorialFile.copyDefault();
        } catch (Exception ignore) {}
    }

    private void registerFlag() {
        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            worldGuardApiEnable = true;
        } catch (ClassNotFoundException e) {
            worldGuardApiEnable = false;
            return;
        }
        WorldGuardApi.addFlagThirstBar();
        WorldGuardApi.registerFlag();
    }


    private void checkForUpdate() {
        List<String> textList = new ArrayList<>();
        new UpdateChecker(113587).getVersion(version -> {
//            String versions = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            int versionNumber = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);
            List<Player> playerList = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.isOp() || p.hasPermission("thirstbar.admin"))
                    .collect(Collectors.toList());
            if(versionNumber < 16) {
                if (this.getDescription().getVersion().equals(version)) {
                    textList.add("§b[ThirstBar] §aThere is not a new update available.");
                } else {
                    textList.add("§b[ThirstBar] §7The plugin version you are using is §4out of date§7!");
                    textList.add("§b[ThirstBar] §7There is a new update available.");
                    textList.add("§b[ThirstBar] §7Download it here: §6https://www.spigotmc.org/resources/1-9-1-20-1-%E2%9A%A1-thirst-bar-%E2%9A%A1-add-thirst-unit-for-player-%E2%AD%90-placeholderapi-and-worldguard-support.113587/");
                }
                textList.forEach(t -> Bukkit.getConsoleSender().sendMessage(t));
                playerList.forEach(p -> textList.forEach(p::sendMessage));
            } else {
                String textTitle = MethodDefault.formatColor("§7§l[§r#007fff§lT#008afe§lH#0095fe§lI#009ffd§lR#00aafd§lS#00b5fc§lT#17bdf8 #2ec5f3§lB#44ccef§lA#5bd4ea§lR§7§l]");
                if (this.getDescription().getVersion().equals(version)) {
                    textList.add(textTitle + " §aThere is not a new update available.");
                } else {
                    textList.add(textTitle + " §7The plugin version you are using is §4out of date§7!");
                    textList.add(textTitle + " §7There is a new update available.");
                    textList.add(textTitle + " §7Download it here: §6https://www.spigotmc.org/resources/1-9-1-20-1-%E2%9A%A1-thirst-bar-%E2%9A%A1-add-thirst-unit-for-player-%E2%AD%90-placeholderapi-and-worldguard-support.113587/");
                }
                textList.forEach(t -> Bukkit.getConsoleSender().sendMessage(t));
                playerList.forEach(p -> textList.forEach(p::sendMessage));
            }
        });
    }

    public static ThirstBar getInstance() {
        return plugin;
    }

    public PlayerDataList getPlayerDataList() {
        return playerDataList;
    }

    public StageList getStageList() {
        return stageList;
    }

    public ItemDataList getItemDataList() {
        return itemDataList;
    }

    public ConfigData getConfigData() {
        return configData;
    }

    public MessageData getMessageData() {
        return messageData;
    }

    public FileManager getItemsFile() {
        return itemsFile;
    }

    public FileManager getMessageFile() {
        return messageFile;
    }

    public FileManager getPlayersFile() {
        return playersFile;
    }

    public FileManager getStageFile() {
        return stageFile;
    }

    public FileManager getActionsFile() {
        return actionsFile;
    }

    public boolean isWorldGuardApiEnable() {
        return worldGuardApiEnable;
    }

    public SqlManager getSqlManager() {
        return sqlManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    @Nullable
    public PlaceholderAPI getPlaceholderAPI() {
        return placeholderAPI;
    }
}
