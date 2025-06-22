package me.orineko.thirstbar;

import lombok.Getter;
import me.orineko.nbtapi.NBT;
import me.orineko.pluginspigottools.FileManager;
import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.command.CommandManager;
import me.orineko.thirstbar.command.MainCommand;
import me.orineko.thirstbar.listener.ThirstListener;
import me.orineko.thirstbar.manager.ThirstBarMethod;
import me.orineko.thirstbar.manager.action.ActionManager;
import me.orineko.thirstbar.api.PlaceholderAPI;
import me.orineko.thirstbar.api.ThirstBarExpansion;
import me.orineko.thirstbar.api.UpdateChecker;
import me.orineko.thirstbar.api.sql.SqlManager;
import me.orineko.thirstbar.api.worldguardapi.WorldGuardApi;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.file.MessageData;
import me.orineko.thirstbar.manager.item.ItemDataList;
import me.orineko.thirstbar.manager.player.PlayerData;
import me.orineko.thirstbar.manager.player.PlayerDataList;
import me.orineko.thirstbar.manager.stage.StageList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

@Getter
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
    @Nullable
    private PlaceholderAPI placeholderAPI;
    private int versionBukkit;

    @Override
    public void onLoad() {
        super.onLoad();
        registerFlag();
    }

    @Override
    public void onEnable() {
        if (!NBT.preloadApi()) {
            getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin");
            getPluginLoader().disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        plugin = this;
        versionBukkit = (int) MethodDefault.formatNumber(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1], 0);
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
        Bukkit.getOnlinePlayers().forEach(ThirstBarMethod::disableGameMode);


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
        if(getVersionBukkit() < 16) {
            furnaceRecipe = new FurnaceRecipe(bottle, potionRawItem.getType());
        } else {
            furnaceRecipe = new FurnaceRecipe(NamespacedKey.randomKey(), bottle, potionRawItem.getType(), ConfigData.CUSTOM_FURNACE_EXP, ConfigData.CUSTOM_FURNACE_COOKING_TIME);
        }
        Bukkit.addRecipe(furnaceRecipe);
    }

    @Override
    public void onDisable() {
        if(actionManager != null) actionManager.removeRegister();
        getPlayerDataList().removeDataPlayers();
    }

    public void renewData() {
        reloadConfig();
        if (sqlManager.getConnection() == null) {
            itemsFile.reloadWithoutCreateFile();
            playersFile.reloadWithoutCreateFile();
        }
        try {
            messageFile.reload();
            stageFile.reload();
            actionsFile.reload();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            playerData.createArmorStand(p);
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
            List<Player> playerList = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.isOp() || p.hasPermission("thirstbar.admin"))
                    .collect(Collectors.toList());
            if(getVersionBukkit() < 16) {
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
}
